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
      unigram, bigram, transition, entity
    };
    @Override
    protected FeatureArray extract_helper(Network network, int parent_k, int[] children_k, int children_k_index) {
        RelationInstance inst=(RelationInstance)network.getInstance();
        List<Integer> fs = new ArrayList<>();
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

        String lword=(pos-1)>=0?wts.get(pos-1).getForm():"START-W"+(pos-1);
        String llword=(pos-2)>=0?wts.get(pos-2).getForm():"START-W"+(pos-2);
        String rword=(pos+1)<inst.size()?wts.get(pos+1).getForm():"END-W"+(pos+1);
        String rrword=(pos+2)<inst.size()?wts.get(pos+2).getForm():"END-W"+(pos+1);

        String POS=wts.get(pos).getTag();
        String lPOS=(pos-1)>=0?wts.get(pos-1).getTag():"START-PO"+(pos-1);
        String llPOS=(pos-2)>=0?wts.get(pos-2).getTag():"START-PO"+(pos-2);
        String rPOS=(pos+1)<inst.size()?wts.get(pos+1).getTag():"END-PO"+(pos+1);
        String rrPOS=(pos+2)<inst.size()?wts.get(pos+2).getTag():"END-PO"+(pos+1);

        fs.add(_param_g.toFeature(network, "relW-1",  currTag+"", word));
        
        
        //ZEROTH-ORDER FEATURES
//        //UNIGRAM FEATURES
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-w0",  currTag+"", word));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-w-1",  currTag+"", lword));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-w-2",  currTag+"", llword));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-w+1",  currTag+"", rword));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-w+2",  currTag+"", rrword));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-po0",  currTag+"", POS));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-po-1",  currTag+"", lPOS));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-po-2",  currTag+"", llPOS));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-po+1",  currTag+"", rPOS));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-po+2",  currTag+"", rrPOS));

        //BIGRAM FEATURES
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-w-2-1",  currTag+"", llword+" "+lword));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-w-10",  currTag+"", lword+" "+word));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-w0+1",  currTag+"", word+" "+rword));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-w+1+2",  currTag+"", rword+" "+rrword));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-po-2-1",  currTag+"", llPOS+" "+lPOS));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-po-10",  currTag+"", lPOS+" "+POS));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-po0+1",  currTag+"", POS+" "+rPOS));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-po+1+2",  currTag+"", rPOS+" "+rrPOS));


        List<Integer> fs1 = new ArrayList<>();
        List<Integer> fs2 = new ArrayList<>();
        List<Integer> fs3 = new ArrayList<>();
        List<Integer> fs4 = new ArrayList<>();



        fs1.add(_param_g.toFeature(network, FeatType.unigram.name()+"*w0",  prevTag+" "+currTag, word));
        fs1.add(_param_g.toFeature(network, FeatType.unigram.name()+"*w-1",  prevTag+" "+currTag, lword));
        fs1.add(_param_g.toFeature(network, FeatType.unigram.name()+"*w-2",  prevTag+" "+currTag, llword));
        fs1.add(_param_g.toFeature(network, FeatType.unigram.name()+"*w+1",  prevTag+" "+currTag, rword));
        fs1.add(_param_g.toFeature(network, FeatType.unigram.name()+"*w+2",  prevTag+" "+currTag, rrword));
        fs1.add(_param_g.toFeature(network, FeatType.unigram.name()+"*po0",  prevTag+" "+currTag, POS));
        fs1.add(_param_g.toFeature(network, FeatType.unigram.name()+"*po-1",  prevTag+" "+currTag, lPOS));
        fs1.add(_param_g.toFeature(network, FeatType.unigram.name()+"*po-2",  prevTag+" "+currTag, llPOS));
        fs1.add(_param_g.toFeature(network, FeatType.unigram.name()+"*po+1",  prevTag+" "+currTag, rPOS));
        fs1.add(_param_g.toFeature(network, FeatType.unigram.name()+"*po+2",  prevTag+" "+currTag, rrPOS));

        //BIGRAM FEATURES
        fs2.add(_param_g.toFeature(network, FeatType.bigram.name()+"*w-2-1",  prevTag+" "+currTag, llword+" "+lword));
        fs2.add(_param_g.toFeature(network, FeatType.bigram.name()+"*w-10",  prevTag+" "+currTag, lword+" "+word));
        fs2.add(_param_g.toFeature(network, FeatType.bigram.name()+"*w0+1",  prevTag+" "+currTag, word+" "+rword));
        fs2.add(_param_g.toFeature(network, FeatType.bigram.name()+"*w+1+2",  prevTag+" "+currTag, rword+" "+rrword));
        fs2.add(_param_g.toFeature(network, FeatType.bigram.name()+"*po-2-1",  prevTag+" "+currTag, llPOS+" "+lPOS));
        fs2.add(_param_g.toFeature(network, FeatType.bigram.name()+"*po-10",  prevTag+" "+currTag, lPOS+" "+POS));
        fs2.add(_param_g.toFeature(network, FeatType.bigram.name()+"*po0+1",  prevTag+" "+currTag, POS+" "+rPOS));
        fs2.add(_param_g.toFeature(network, FeatType.bigram.name()+"*po+1+2",  prevTag+" "+currTag, rPOS+" "+rrPOS));

        if (NetworkConfig.USE_NEURAL_FEATURES) {
        	String sent = getSentence(wts);
        	SimpleImmutableEntry<String, Integer> sentAndPos = new SimpleImmutableEntry<String, Integer>(sent, pos);
        	this.addNeural(network, 0, parent_k, children_k_index, sentAndPos, currTag);
        } else {
        	fs1.add(_param_g.toFeature(network, FeatType.unigram.name()+"*w0",  prevTag+" "+currTag, word));
        }
       
        fs3.add(_param_g.toFeature(network, FeatType.transition.name(), currTag+"", prevTag+""));

        //ENTITY FEATURES=>Implemented using bag of words approach
        if(pos==inst.size()-1){
            String relType=inst.output.relType;
            int e1Start=inst.input.e1Start;
            int e1End=inst.input.e1End;
            int e2Start=inst.input.e2Start;
            int e2End=inst.input.e2End;
            for(int i=e1Start; i<=e1End; i++){
                //int relPos=i-(e1End-e1Start);
                fs4.add(_param_g.toFeature(network, FeatType.entity.name()+"-1", relType, inst.input.wts.get(i).getForm()));
                fs4.add(_param_g.toFeature(network, FeatType.entity.name()+"-1", relType, inst.input.wts.get(i).getTag()));
            }
            for(int i=e2Start; i<=e2End; i++){
                //int relPos=i-(e2End-e2Start);
                fs4.add(_param_g.toFeature(network, FeatType.entity.name()+"-2", relType, inst.input.wts.get(i).getForm()));
                fs4.add(_param_g.toFeature(network, FeatType.entity.name()+"-2", relType, inst.input.wts.get(i).getTag()));
            }
        }

        FeatureArray fa1  = this.createFeatureArray(network, fs1);
        FeatureArray fa2  = this.createFeatureArray(network, fs2);
        FeatureArray fa3  = this.createFeatureArray(network, fs3);
        FeatureArray fa4  = this.createFeatureArray(network, fs4);
        FeatureArray fa5  = this.createFeatureArray(network, fs);

        fa1.addNext(fa2).addNext(fa3);
        fa1.addNext(fa4).addNext(fa5);
        
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
