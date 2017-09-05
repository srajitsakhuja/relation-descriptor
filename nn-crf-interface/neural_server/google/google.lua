torch.setdefaulttensortype('torch.DoubleTensor')

-- opt = {
--     binfilename = 'Google_torch/Google.6B.50d.txt',
--     outfilename = 'Google_torch/Google.6B.50d.t7'
-- }
-- second workstation
local embeddingFile = '/home/lihao/corpus/embedding/GoogleNews-vectors-negative300.bin.t7'
local Google = {}
-- if not paths.filep(opt.outfilename) then
-- 	Google = require('bintot7.lua')
-- else
-- 	Google = torch.load(opt.outfilename)
-- 	print('Done reading Google data.')
-- end

Google.load = function (self,dim)
	local GoogleFile = embeddingFile
    --local GoogleFile = 'nn-crf-interface/neural_server/goolge/GoogleNews-vectors-negative300.bin.t7'
    if not paths.filep(GoogleFile) then
        error('Please run bintot7.lua to preprocess Google data!')
    else
        Google.Google = torch.load(GoogleFile)
        print('Done reading Google data.')
    end
end

Google.distance = function (self,vec,k)
	local k = k or 1	
	--self.zeros = self.zeros or torch.zeros(self.M:size(1));
	local norm = vec:norm(2)
	vec:div(norm)
	local distances = torch.mv(self.Google.M ,vec)
	distances , oldindex = torch.sort(distances,1,true)
	local returnwords = {}
	local returndistances = {}
	for i = 1,k do
		table.insert(returnwords, self.Google.v2wvocab[oldindex[i]])
		table.insert(returndistances, distances[i])
	end
	return {returndistances, returnwords}
end

Google.word2vec = function (self,word,throwerror)
   local throwerror = throwerror or false
   local ind = self.Google.w2vvocab[word]
   if throwerror then
		assert(ind ~= nil, 'Word does not exist in the dictionary!')
   end   
	if ind == nil then
		ind = self.Google.w2vvocab['</s>']
        if ind == nil then
            ind = self.Google.w2vvocab['</s>']
        end
	end
   return self.Google.M[ind]
end

return Google
