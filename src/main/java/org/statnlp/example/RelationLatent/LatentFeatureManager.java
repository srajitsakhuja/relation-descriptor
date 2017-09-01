package org.statnlp.example.RelationLatent;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;

import org.statnlp.commons.types.WordToken;
import org.statnlp.hypergraph.FeatureArray;
import org.statnlp.hypergraph.FeatureManager;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.Network;
import org.statnlp.hypergraph.NetworkConfig;

public class LatentFeatureManager extends FeatureManager {
	
	private static final long serialVersionUID = -9053492051283606458L;
	
	private boolean zero_digit = false;
	
	public LatentFeatureManager(GlobalNetworkParam param_g, boolean zero_digit) {
        super(param_g);
        this.zero_digit = zero_digit;
    }

    private enum FeatType{
      unigram, bigram, transition;
    };
    @Override
    protected FeatureArray extract_helper(Network network, int parent_k, int[] children_k, int children_k_index) {
        RelationInstance inst=(RelationInstance)network.getInstance();
        int[] paArr=network.getNodeArray(parent_k);
        if(LatentNetworkCompiler.NodeType.values()[paArr[2]]== LatentNetworkCompiler.NodeType.leaf || LatentNetworkCompiler.NodeType.values()[paArr[2]]== LatentNetworkCompiler.NodeType.root){
            return FeatureArray.EMPTY;
        }
        int pos=paArr[0];
        int currTag=paArr[1];
        int[] childArr=network.getNodeArray(children_k[0]);
        int prevTag=childArr[1];
        List<WordToken> wts=inst.input.wts;
        String word=wts.get(pos).getForm();

        List<Integer> fs1 = new ArrayList<>();
        List<Integer> fs2 = new ArrayList<>();
        List<Integer> fs3 = new ArrayList<>();

        //FIRST-ORDER FEATURES
        //UNIGRAM FEATURES
        if (NetworkConfig.USE_NEURAL_FEATURES) {
        	String sent = getSentence(wts);
        	SimpleImmutableEntry<String, Integer> sentAndPos = new SimpleImmutableEntry<String, Integer>(sent, pos);
        	this.addNeural(network, 0, parent_k, children_k_index, sentAndPos, currTag);
        } else {
        	fs1.add(_param_g.toFeature(network, FeatType.unigram.name()+"*w0",  prevTag+" "+currTag, word));
        }
        
        fs3.add(_param_g.toFeature(network, FeatType.transition.name(), currTag+"", prevTag+""));
        
        FeatureArray fa1  = this.createFeatureArray(network, fs1);
        FeatureArray fa2  = this.createFeatureArray(network, fs2);
        FeatureArray fa3  = this.createFeatureArray(network, fs3);
        fa1.addNext(fa2).addNext(fa3);
        
        return fa1;
    }
    
    private String getSentence(List<WordToken> wts) {
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < wts.size(); i++) {
    		String curr = i == 0 ? wts.get(i).getForm()  : " " + wts.get(i).getForm();
    		if (this.zero_digit) {
    			curr = curr.replaceAll("\\d", "0");
    		}
    		sb.append(curr);
    	}
    	return sb.toString();
    }
}
