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
        if(inst.getInput()==null){System.out.println(inst.getInstanceId());System.out.printf("%d:%d:%s\n", paArr[0], paArr[1], TwitterLogisticNetworkCompiler.nodeType.values()[paArr[2]]);}
        int currTag=paArr[1];
        String entityString=inst.getInput().entity;
        String[] entityList=entityString.split(" ");
        List<Integer> eStart=inst.in.eStart;
        List<Integer> eEnd=inst.in.eEnd;
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

        for(int i=0; i<entityList.length; i++){
            fs.add(_param_g.toFeature(network, featureType.token.name()+"WordIdentity", currTag+"", entityList[i] ));
        }

        String lword=eStart.get(0)>0?inst.in.wts.get(eStart.get(0)-1).getForm():"LEFTLIM";
        String rword=eEnd.get(0)<inst.size()-1?inst.in.wts.get(eEnd.get(0)+1).getForm():"RIGHTLIM";
        fs.add(_param_g.toFeature(network, featureType.window.name()+"-1", currTag+"", lword));
        fs.add(_param_g.toFeature(network, featureType.window.name()+"+1", currTag+"", rword));

        for(int i=0; i<inst.size(); i++){
            fs.add(_param_g.toFeature(network, featureType.tweet.name(), currTag+"", inst.in.wts.get(i).getForm()));
        }
        return this.createFeatureArray(network, fs);
    }
}
