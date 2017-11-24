package org.statnlp.example.TwitterProfileLogistic;

import org.statnlp.example.RelationLatent.LatentNetworkCompiler;
import org.statnlp.hypergraph.FeatureArray;
import org.statnlp.hypergraph.FeatureManager;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.Network;

import java.util.ArrayList;
import java.util.List;

public class TwitterLogisticFeatureManager extends FeatureManager {
    public TwitterLogisticFeatureManager(GlobalNetworkParam param_g) {
        super(param_g);
    }
    private enum featureType{
      entity, token, window, tweet;
    };
    @Override
    protected FeatureArray extract_helper(Network network, int parent_k, int[] children_k, int children_k_index) {
        TwitterInstance inst=(TwitterInstance)network.getInstance();
        List<Integer> fs=new ArrayList<Integer>();
        int[] paArr=network.getNodeArray(parent_k);
        int NodeType=paArr[2];
        if (NodeType != TwitterLogisticNetworkCompiler.nodeType.tag.ordinal())
            return FeatureArray.EMPTY;
//        if(inst.getInput()==null){System.out.println(inst.getInstanceId());System.out.printf("%d:%d:%s\n", paArr[0], paArr[1], TwitterLogisticNetworkCompiler.nodeType.values()[paArr[2]]);}
        int currTag=paArr[1];
        String entityString=inst.getInput().entity;
        String[] entityList=entityString.split(" ");



        List<Integer> eStart=inst.getInput().eStart;
        List<Integer> eEnd=inst.getInput().eEnd;
        int windowSize=2;

        String entityUpperCase="0";
        for(int i=0; i<entityList.length; i++){
            if(Character.isUpperCase( entityList[i].charAt(0))){
                entityUpperCase="1";
                break;
            }
        }
        String entityLength=entityList.length+"";
        fs.add(_param_g.toFeature(network, featureType.entity.name()+"length",  currTag+"", entityLength));
        fs.add(_param_g.toFeature(network, featureType.entity.name()+"upperCase",  currTag+"", entityUpperCase));
        for(int j=0; j<eStart.size(); j++) {
            for (int i = eStart.get(j); i <= eEnd.get(j); i++) {
                fs.add(_param_g.toFeature(network, featureType.token.name() + "WordIdentity", currTag + "", inst.getInput().wts.get(i).getForm()));
                fs.add(_param_g.toFeature(network, featureType.token.name() + "POS", currTag + "", inst.getInput().wts.get(i).getTag()));
                fs.add(_param_g.toFeature(network, featureType.token.name() + "NER", currTag + "", inst.getInput().wts.get(i).getPhraseTag()));
            }
            String lword = eStart.get(j) > 0 ? inst.getInput().wts.get(eStart.get(j) - 1).getForm() : "LEFTLIM-W";
            String rword = eEnd.get(j) < inst.size() - 1 ? inst.getInput().wts.get(eEnd.get(j) + 1).getForm() : "RIGHTLIM-W";
            String lPOS = eStart.get(j) > 0 ? inst.getInput().wts.get(eStart.get(j) - 1).getTag() : "LEFTLIM-P";
            String rPOS = eEnd.get(j) < inst.size() - 1 ? inst.getInput().wts.get(eEnd.get(j) + 1).getTag() : "RIGHTLIM-W";
            fs.add(_param_g.toFeature(network, featureType.window.name() + "WordIdentity-1", currTag + "", lword));
            fs.add(_param_g.toFeature(network, featureType.window.name() + "WordIdentity+1", currTag + "", rword));
            fs.add(_param_g.toFeature(network, featureType.window.name() + "POS-1", currTag + "", lPOS));
            fs.add(_param_g.toFeature(network, featureType.window.name() + "POS+1", currTag + "", rPOS));
        }
//
        for(int i=0; i<inst.size(); i++){
            fs.add(_param_g.toFeature(network, featureType.tweet.name(), currTag+"", inst.getInput().wts.get(i).getForm()));
        }
        return this.createFeatureArray(network, fs);
    }
}
