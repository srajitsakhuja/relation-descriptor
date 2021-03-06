include 'LampleLSTM.lua'
local SimpleBiLSTM, parent = torch.class('SimpleBiLSTM', 'AbstractNeuralNetwork')

function SimpleBiLSTM:__init(doOptimization, gpuid)
    parent.__init(self, doOptimization)
    self.data = {}
end

function SimpleBiLSTM:initialize(javadata, ...)
    self.data = {}
    local data = self.data
    data.sentences = listToTable(javadata:get("nnInputs"))
    data.hiddenSize = javadata:get("hiddenSize")
    data.optimizer = javadata:get("optimizer")
    self.numLabels = javadata:get("numLabels")
    data.embedding = javadata:get("embedding")
    local isTraining = javadata:get("isTraining")
    if isTraining then
        self.x = self:prepare_input()
    else 
        self.testInput = self:prepare_input()
    end
    self.numSent = #data.sentences
    self.output = torch.Tensor()
    

    if self.net == nil then
        -- means is initialized process and we don't have the input yet.
        self.x1Tab = {}
        self.x1 = torch.LongTensor()
        self.x2Tab = {}
        self.x2 = torch.LongTensor()
        self.gradOutput = {}
        local outputAndGradOutputPtr = {... }
        self.outputPtr = torch.pushudata(outputAndGradOutputPtr[1], "torch.DoubleTensor")
        self.gradOutputPtr = torch.pushudata(outputAndGradOutputPtr[2], "torch.DoubleTensor")
        self:createNetwork()
        print(self.net)
        return self:obtainParams()
    end
end

--The network is only created once is used.
function SimpleBiLSTM:createNetwork()
    local data = self.data
    local hiddenSize = data.hiddenSize
    local sharedLookupTable
    if data.embedding ~= nil then
        if data.embedding == 'glove' then
            sharedLookupTable = loadGlove(self.idx2word, hiddenSize, true)
        else -- unknown/no embedding, defaults to random init
            print ("unknown embedding type, use random embedding..")
            sharedLookupTable = nn.LookupTableMaskZero(self.vocabSize, hiddenSize)
            print("lookup table parameter: ".. sharedLookupTable:getParameters():nElement())
        end
    else
        print ("Not using any embedding, just use random embedding")
        sharedLookupTable = nn.LookupTableMaskZero(self.vocabSize, hiddenSize)
    end

    -- forward rnn
    local fwdLSTM = nn.FastLSTM(hiddenSize, hiddenSize):maskZero(1)
    print("number of lstm parameters:"..fwdLSTM:getParameters():nElement())
    local fwd = nn.Sequential()
       :add(sharedLookupTable)
       :add(fwdLSTM)

    -- internally, rnn will be wrapped into a Recursor to make it an AbstractRecurrent instance.
    local fwdSeq = nn.Sequencer(fwd)

    -- backward rnn (will be applied in reverse order of input sequence)
    local bwd, bwdSeq
    bwd = nn.Sequential()
           :add(sharedLookupTable:sharedClone())
           :add(nn.FastLSTM(hiddenSize, hiddenSize):maskZero(1))
           
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
    
    local brnn = nn.Sequential()
       :add(parallel)
       :add(nn.ZipTable())
       :add(mergeSeq)
    local mergeHiddenSize = 2 * hiddenSize
    local rnn = nn.Sequential()
        :add(brnn) 
        :add(nn.Sequencer(nn.MaskZero(nn.Linear(mergeHiddenSize, self.numLabels), 1))) 
    self.net = rnn
end

function SimpleBiLSTM:obtainParams()
    --make sure we will not replace this variable
    self.params, self.gradParams = self.net:getParameters()
    print("Number of parameters: " .. self.params:nElement())
    if self.doOptimization then
        self:createOptimizer()
        -- no return array if optim is done here
    else
        self.params:retain()
        self.paramsPtr = torch.pointer(self.params)
        self.gradParams:retain()
        self.gradParamsPtr = torch.pointer(self.gradParams)
        return self.paramsPtr, self.gradParamsPtr
    end
end

function SimpleBiLSTM:createOptimizer()
    local data = self.data
    -- set optimizer. If nil, optimization is done by caller.
    print(string.format("Optimizer: %s", data.optimizer))
    self.doOptimization = data.optimizer ~= nil and data.optimizer ~= 'none'
    if self.doOptimization == true then
        if data.optimizer == 'sgd' then
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
        end
    end
end

function SimpleBiLSTM:forward(isTraining, batchInputIds)
    local nnInput = self:getForwardInput(isTraining, batchInputIds)
    local output_table = self.net:forward(nnInput)
    --- this is to converting the table into tensor.
    self.output = torch.cat(self.output, output_table, 1)
    if not self.outputPtr:isSameSizeAs(self.output) then
        self.outputPtr:resizeAs(self.output)
    end
    self.outputPtr:copy(self.output)
end

function SimpleBiLSTM:getForwardInput(isTraining, batchInputIds)
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

function SimpleBiLSTM:getBackwardInput()
    if self.batchInputIds ~= nil then
        return self.batchInput
    else
        return self.x
    end
end

function SimpleBiLSTM:getBackwardSentNum()
    if self.batchInputIds ~= nil then
        return self.batchInputIds:size(1)
    else
        return self.numSent
    end
end

function SimpleBiLSTM:backward()
    self.gradParams:zero()
    local gradOutputTensor = self.gradOutputPtr
    local backwardInput = self:getBackwardInput()  --since backward only happen in training
    local backwardSentNum = self:getBackwardSentNum()
    torch.split(self.gradOutput, gradOutputTensor, backwardSentNum, 1)
    self.net:backward(backwardInput, self.gradOutput)
    if self.doOptimization then
        self.optimizer(self.feval, self.params, self.optimState)
    end
    
end

function SimpleBiLSTM:prepare_input()
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
    end
    print("max sentencen length:"..maxLen)
    for step=1,maxLen do
        inputs_rev[step] = torch.LongTensor(#sentences)
        for j=1,#sentences do
            local tokens = sentence_toks[j]
            inputs_rev[step][j] = inputs[maxLen-step+1][j]
        end
    end
    self.maxLen = maxLen
    return {inputs, inputs_rev}
end

function SimpleBiLSTM:buildVocab(sentences, sentence_toks)
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

function SimpleBiLSTM:save_model(path)
    --need to save the vocabulary as well.
    torch.save(path, self.net)
end

function SimpleBiLSTM:load_model(path)
    self.net = torch.load(path)
end
