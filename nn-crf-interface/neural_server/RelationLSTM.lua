local RelationLSTM, parent = torch.class('RelationLSTM', 'AbstractNeuralNetwork')

function RelationLSTM:__init(doOptimization, gpuid)
    parent.__init(self, doOptimization)
    self.data = {}
    self.gpuid = gpuid
end

function RelationLSTM:initialize(javadata, ...)
    self.data = {}
    local data = self.data
    data.sentences = listToTable(javadata:get("nnInputs"))
    data.hiddenSize = javadata:get("hiddenSize")
    data.optimizer = javadata:get("optimizer")
    data.learningRate = javadata:get("learningRate")
    data.clipping = javadata:get("clipping")
    self.numLabels = javadata:get("numLabels")
    data.embedding = javadata:get("embedding")
    self.fixEmbedding = javadata:get("fixEmbedding")
    local modelPath = javadata:get("nnModelFile")
    local isTraining = javadata:get("isTraining")
    data.isTraining = isTraining

    if isTraining then
        self.x = self:prepare_input()
    end

    if self.net == nil and isTraining then
        -- means is initialized process and we don't have the input yet.
        self:createNetwork()
        if self.fixEmbedding then
            print(self.parallelInput)
        end
        print(self.net)
    end
    if self.net == nil then 
        self:load_model(modelPath)
    end


    if not isTraining then 
        self.testInput = self:prepare_input()
    end
    self.numSent = #data.sentences
    self.output = torch.Tensor()
    self.x1Tab = {}
    self.x1 = torch.LongTensor()
    self.x2Tab = {}
    self.x2 = torch.LongTensor()
    if self.gpuid >= 0 then
        self.x1 = self.x1:cuda()
        self.x2 = self.x2:cuda()
    end
    self.gradOutput = {}
    local outputAndGradOutputPtr = {... }
    if #outputAndGradOutputPtr > 0 then
        self.outputPtr = torch.pushudata(outputAndGradOutputPtr[1], "torch.DoubleTensor")
        self.gradOutputPtr = torch.pushudata(outputAndGradOutputPtr[2], "torch.DoubleTensor")
        return self:obtainParams()
    end
end

--The network is only created once is used.
function RelationLSTM:createNetwork()
    local data = self.data
    local hiddenSize = data.hiddenSize
    local sharedLookupTable
    local forwardInputLayer
    local backwardInputLayer
    if data.embedding ~= nil then
        if data.embedding == 'glove' then
            sharedLookupTable = loadGlove(self.idx2word, hiddenSize, true)
        elseif data.embedding == 'google' then
            sharedLookupTable = loadGoogle(self.idx2word, hiddenSize, true)
        else -- unknown/no embedding, defaults to random init
            print ("unknown embedding type, use random embedding..")
            sharedLookupTable = nn.LookupTableMaskZero(self.vocabSize, hiddenSize)
            print("lookup table parameter: ".. sharedLookupTable:getParameters():nElement())
        end
    else
        print ("Not using any embedding, just use random embedding")
        sharedLookupTable = nn.LookupTableMaskZero(self.vocabSize, hiddenSize)
    end
    if self.fixEmbedding then 
        sharedLookupTable.accGradParameters = function() end
    end

    -- forward rnn
    local fwdLSTM = nn.FastLSTM(hiddenSize, hiddenSize):maskZero(1)
    print("number of lstm parameters:"..fwdLSTM:getParameters():nElement())
    local fwd = nn.Sequential()
    if self.fixEmbedding then
        forwardInputLayer = nn.Sequential():add(sharedLookupTable)
    else
        fwd:add(sharedLookupTable)
    end
    fwd:add(fwdLSTM)
    -- internally, rnn will be wrapped into a Recursor to make it an AbstractRecurrent instance.
    local fwdSeq = nn.Sequencer(fwd)

    -- backward rnn (will be applied in reverse order of input sequence)
    local bwd = nn.Sequential()
    local bwdSeq
    if self.fixEmbedding then
        backwardInputLayer = nn.Sequential()
           :add(sharedLookupTable:sharedClone())
    else
        bwd:add(sharedLookupTable:sharedClone())
    end
    bwd:add(nn.FastLSTM(hiddenSize, hiddenSize):maskZero(1))
           
    bwdSeq = nn.Sequential()
            :add(nn.Sequencer(bwd))
            :add(nn.ReverseTable())

    -- merges the output of one time-step of fwd and bwd rnns.
    -- You could also try nn.AddTable(), nn.Identity(), etc.
    local merge = nn.JoinTable(1, 1)
    local mergeSeq = nn.Sequencer(merge)

    -- Assume that two input sequences are given (original and reverse, both are right-padded).
    -- Instead of ConcatTable, we use ParallelTable here.
    local parallel = nn.ParallelTable()
    parallel:add(fwdSeq)
    parallel:add(bwdSeq)

    if self.fixEmbedding then
        self.parallelInput = nn.ParallelTable()
        self.parallelInput:add(nn.Sequencer(forwardInputLayer))
        self.parallelInput:add(nn.Sequencer(backwardInputLayer))
    end
    
    local brnn = nn.Sequential()
       :add(parallel)
       :add(nn.ZipTable())
       :add(mergeSeq)
    local mergeHiddenSize = 2 * hiddenSize
    local finalTanh = nn.Sequential()
                        :add(nn.MaskZero(nn.Linear(mergeHiddenSize, hiddenSize), 1))
                        :add(nn.Tanh())
                        :add(nn.MaskZero(nn.Linear(hiddenSize, self.numLabels), 1))
    local rnn = nn.Sequential()
        :add(brnn)
        :add(nn.Sequencer(finalTanh)) 
    if self.gpuid >= 0 then 
        if self.fixEmbedding then
            self.parallelInput:cuda()
        end
        rnn:cuda() 
    end
    self.net = rnn
end

function RelationLSTM:obtainParams()
    --make sure we will not replace this variable
    self.params, self.gradParams = self.net:getParameters()
    print("Number of parameters: " .. self.params:nElement())
    if self.doOptimization then
        self:createOptimizer()
        -- no return array if optim is done here
    else
        if self.gpuid >= 0 then
            -- since the the network is gpu network.
            self.paramsDouble = self.params:double()
            self.paramsDouble:retain()
            self.paramsPtr = torch.pointer(self.paramsDouble)
            self.gradParamsDouble = self.gradParams:double()
            self.gradParamsDouble:retain()
            self.gradParamsPtr = torch.pointer(self.gradParamsDouble)
            return self.paramsPtr, self.gradParamsPtr
        else
            self.params:retain()
            self.paramsPtr = torch.pointer(self.params)
            self.gradParams:retain()
            self.gradParamsPtr = torch.pointer(self.gradParams)
            return self.paramsPtr, self.gradParamsPtr
        end
    end
end

function RelationLSTM:createOptimizer()
    local data = self.data
    -- set optimizer. If nil, optimization is done by caller.
    print(string.format("Optimizer: %s", data.optimizer))
    self.doOptimization = data.optimizer ~= nil and data.optimizer ~= 'none'
    if self.doOptimization == true then
        if data.optimizer == 'sgd_normal' then
            self.optimizer = optim.sgd
            self.optimState = {learningRate=data.learningRate}
        elseif data.optimizer == 'adagrad' then
            self.optimizer = optim.adagrad
            self.optimState = {learningRate=data.learningRate}
        elseif data.optimizer == 'adam' then
            self.optimizer = optim.adam
            self.optimState = {learningRate=data.learningRate}
        elseif data.optimizer == 'adadelta' then
            self.optimizer = optim.adadelta
            self.optimState = {learningRate=data.learningRate}
        elseif data.optimizer == 'lbfgs' then
            self.optimizer = optim.lbfgs
            self.optimState = {tolFun=10e-10, tolX=10e-16}
        elseif data.optimizer == 'sgd' then
            --- with gradient clipping
            self.optimizer = sgdgc
            self.optimState = {learningRate=data.learningRate, clipping=data.clipping}
        end
    end
end

function RelationLSTM:forward(isTraining, batchInputIds)
    if self.gpuid >= 0 and not self.doOptimization and isTraining then
        self.params:copy(self.paramsDouble:cuda())
    end
    local nnInput = self:getForwardInput(isTraining, batchInputIds)
    local nnInput_x = nnInput
    if self.fixEmbedding then
        nnInput_x =  self.parallelInput:forward(nnInput)
    end
    local output_table = self.net:forward(nnInput_x)
    if self.gpuid >= 0 then
        nn.utils.recursiveType(output_table, 'torch.DoubleTensor')
    end 
    --- this is to converting the table into tensor.
    self.output = torch.cat(self.output, output_table, 1)
    if not self.outputPtr:isSameSizeAs(self.output) then
        self.outputPtr:resizeAs(self.output)
    end
    self.outputPtr:copy(self.output)
end

function RelationLSTM:getForwardInput(isTraining, batchInputIds)
    if isTraining then
        if batchInputIds ~= nil then
            batchInputIds:add(1) -- because the sentence is 0 indexed.
            self.batchInputIds = batchInputIds
            self.x1 = torch.cat(self.x1, self.x[1], 2):index(1, batchInputIds)
            self.x1:resize(self.x1:size(1)*self.x1:size(2))
            torch.split(self.x1Tab, self.x1, batchInputIds:size(1), 1)
            self.x2 = torch.cat(self.x2, self.x[2], 2):index(1, batchInputIds)
            self.x2:resize(self.x2:size(1)*self.x2:size(2))
            torch.split(self.x2Tab, self.x2, batchInputIds:size(1), 1)

            self.batchInput = {self.x1Tab, self.x2Tab}
            return self.batchInput
        else
            return self.x
        end
    else
        return self.testInput
    end
end

function RelationLSTM:getBackwardInput()
    if self.batchInputIds ~= nil then
        return self.batchInput
    else
        return self.x
    end
end

function RelationLSTM:getBackwardSentNum()
    if self.batchInputIds ~= nil then
        return self.batchInputIds:size(1)
    else
        return self.numSent
    end
end

function RelationLSTM:backward()
    self.gradParams:zero()
    local gradOutputTensor = self.gradOutputPtr
    local backwardInput = self:getBackwardInput()  --since backward only happen in training
    local backwardSentNum = self:getBackwardSentNum()
    torch.split(self.gradOutput, gradOutputTensor, backwardSentNum, 1)
    if self.gpuid >= 0 then
        nn.utils.recursiveType(self.gradOutput, 'torch.CudaTensor')
    end
    local nnInput_x = backwardInput
    if self.fixEmbedding then
        nnInput_x =  self.parallelInput:forward(backwardInput)
    end
    self.net:backward(nnInput_x, self.gradOutput)
    if self.doOptimization then
        self.optimizer(self.feval, self.params, self.optimState)
    else
        if self.gpuid >= 0 then
            self.gradParamsDouble:copy(self.gradParams:double())
        end
    end
    
end

function RelationLSTM:prepare_input()
    local data = self.data

    local sentences = data.sentences
    local sentence_toks = {}
    local maxLen = 0
    for i=1,#sentences do
        local tokens = stringx.split(sentences[i]," ")
        table.insert(sentence_toks, tokens)
        if #tokens > maxLen then
            maxLen = #tokens
        end
    end

    --note that inside if the vocab is already created
    --just directly return
    self:buildVocab(sentences, sentence_toks)    

    local inputs = {}
    local inputs_rev = {}
    for step=1,maxLen do
        inputs[step] = torch.LongTensor(#sentences)
        for j=1,#sentences do
            local tokens = sentence_toks[j]
            if step > #tokens then
                inputs[step][j] = 0 --padding token
            else
                local tok = sentence_toks[j][step]
                local tok_id = self.word2idx[tok]
                if tok_id == nil then
                    tok_id = self.word2idx['<UNK>']
                end
                inputs[step][j] = tok_id
            end
        end
        if self.gpuid >= 0 then inputs[step] = inputs[step]:cuda() end
    end
    print("max sentencen length:"..maxLen)
    for step=1,maxLen do
        inputs_rev[step] = torch.LongTensor(#sentences)
        for j=1,#sentences do
            local tokens = sentence_toks[j]
            inputs_rev[step][j] = inputs[maxLen-step+1][j]
        end
        if self.gpuid >= 0 then inputs_rev[step] = inputs_rev[step]:cuda() end
    end
    self.maxLen = maxLen
    return {inputs, inputs_rev}
end

function RelationLSTM:buildVocab(sentences, sentence_toks)
    if self.idx2word ~= nil then
        --means the vocabulary is already created
        return 
    end
    self.idx2word = {}
    self.word2idx = {}
    self.word2idx['<PAD>'] = 0
    self.idx2word[0] = '<PAD>'
    self.word2idx['<UNK>'] = 1
    self.idx2word[1] = '<UNK>'
    self.vocabSize = 2
    for i=1,#sentences do
        local tokens = sentence_toks[i]
        for j=1,#tokens do
            local tok = tokens[j]
            local tok_id = self.word2idx[tok]
            if tok_id == nil then
                self.vocabSize = self.vocabSize+1
                self.word2idx[tok] = self.vocabSize
                self.idx2word[self.vocabSize] = tok
            end
        end
    end
    print("number of unique words:" .. #self.idx2word)
end

function RelationLSTM:save_model(path)
    --need to save the vocabulary as well.
    torch.save(path, {self.net, self.idx2word, self.word2idx})
end

function RelationLSTM:load_model(path)
    local object = torch.load(path)
    self.net = object[1]
    self.idx2word = object[2]
    self.word2idx = object[3]
end
