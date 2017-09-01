package org.statnlp.example.RelationLatent;

import java.util.AbstractMap.SimpleImmutableEntry;

import org.statnlp.hypergraph.neural.NeuralNetworkCore;

public class RelationLSTM extends NeuralNetworkCore {
	
	
	public RelationLSTM(int hiddenSize, int numLabels, int gpuId, String embedding, boolean fixEmbedding) {
		super(numLabels);
		config.put("class", "RelationLSTM");
        config.put("hiddenSize", hiddenSize);
        config.put("numLabels", numLabels);
        config.put("embedding", embedding);
        config.put("fixEmbedding", fixEmbedding);
        config.put("gpuid", gpuId);
	}

	@Override
	public Object hyperEdgeInput2NNInput(Object edgeInput) {
		@SuppressWarnings("unchecked")
		SimpleImmutableEntry<String, Integer> sentAndPos = (SimpleImmutableEntry<String, Integer>) edgeInput;
		return sentAndPos.getKey();
	}
	
	@Override
	public int hyperEdgeInput2OutputRowIndex (Object edgeInput) {
		@SuppressWarnings("unchecked")
		SimpleImmutableEntry<String, Integer> sentAndPos = (SimpleImmutableEntry<String, Integer>) edgeInput;
		int sentID = this.getNNInputID(sentAndPos.getKey()); 
		int row = sentAndPos.getValue()*this.getNNInputSize()+sentID;
		return row;
	}

}
