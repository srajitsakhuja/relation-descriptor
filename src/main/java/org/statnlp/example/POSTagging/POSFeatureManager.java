package org.statnlp.example.POSTagging;

import org.statnlp.hypergraph.FeatureArray;
import org.statnlp.hypergraph.FeatureManager;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.Network;

import java.util.ArrayList;
import java.util.List;

public class POSFeatureManager extends FeatureManager {
    public POSFeatureManager(GlobalNetworkParam param_g) {
        super(param_g);
    }
    private enum FeatType{
        transition, emission;
    }
    @Override
    protected FeatureArray extract_helper(Network network, int parent_k, int[] children_k, int children_k_index) {
        int[] paArr=network.getNodeArray(parent_k);
        POSNetworkCompiler.NodeType paNodeType=POSNetworkCompiler.NodeType.values()[paArr[2]];
        if(paNodeType== POSNetworkCompiler.NodeType.leaf || paNodeType== POSNetworkCompiler.NodeType.root){
            return FeatureArray.EMPTY;
        }
        POSInstance inst=(POSInstance)network.getInstance();
        List<String> sentence=inst.getInput();
        int pos=paArr[0];
        int tagId=paArr[1];
        String tag=tagId+"";
        List<Integer> fs=new ArrayList<Integer>();
        String word=sentence.get(pos);
        fs.add(this._param_g.toFeature(network, FeatType.emission.name(), tag, word));
        //for(int i=0; i<children_k.length; i++){
        int childIdx=children_k[0];
        int childArr[]=network.getNodeArray(childIdx);
        int prevTagId=childArr[1];
        POSNetworkCompiler.NodeType prevNodeType= POSNetworkCompiler.NodeType.values()[childArr[2]];
        String prevTag=prevNodeType== POSNetworkCompiler.NodeType.leaf?"START":prevTagId+"";
        fs.add(this._param_g.toFeature(network, FeatType.transition.name(), tag, prevTag));
        //}


        return this.createFeatureArray(network,fs);
    }
}
