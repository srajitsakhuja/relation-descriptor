package org.statnlp.example.TwitterProfileLogistic;

import org.statnlp.commons.types.Instance;
import org.statnlp.example.base.BaseNetwork;
import org.statnlp.hypergraph.LocalNetworkParam;
import org.statnlp.hypergraph.Network;
import org.statnlp.hypergraph.NetworkCompiler;
import org.statnlp.hypergraph.NetworkIDMapper;

import java.util.Arrays;

public class TwitterLogisticNetworkCompiler extends NetworkCompiler {
    public TwitterLogisticNetworkCompiler() {

    }
    protected enum nodeType{
        leaf,tag,root;
    };
    static{
        NetworkIDMapper.setCapacity(new int[]{3,2,3});
    }
    public long toRoot(){
        return NetworkIDMapper.toHybridNodeID(new int[]{2, 0,nodeType.root.ordinal()});
    }
    public long toLeaf(){
        return NetworkIDMapper.toHybridNodeID(new int[]{0, 0,nodeType.leaf.ordinal()});
    }
    public long toTagNode(int tagId){
        return NetworkIDMapper.toHybridNodeID(new int[]{1,tagId ,nodeType.tag.ordinal()});
    }
    @Override
    public Network compileLabeled(int networkId, Instance inst, LocalNetworkParam param) {
        BaseNetwork.NetworkBuilder<BaseNetwork> builder=BaseNetwork.NetworkBuilder.builder();
        TwitterInstance myInst=(TwitterInstance)inst;
        long leaf=toLeaf();
        long root=toRoot();
        builder.addNode(leaf);
        builder.addNode(root);
        int output=myInst.getOutput()>0?myInst.getOutput():0;
        long tagNode=toTagNode(output);
        builder.addNode(tagNode);
        builder.addEdge(tagNode, new long[]{leaf});
        builder.addEdge(root, new long[]{tagNode});
        return builder.build(networkId, myInst, param, this);
    }

    @Override
    public Network compileUnlabeled(int networkId, Instance inst, LocalNetworkParam param) {
        BaseNetwork.NetworkBuilder<BaseNetwork> builder=BaseNetwork.NetworkBuilder.builder();
        TwitterInstance myInst=(TwitterInstance)inst;
        long leaf=toLeaf();
        long root=toRoot();
        builder.addNode(leaf);
        builder.addNode(root);
        long tagNode1=toTagNode(0);
        long tagNode2=toTagNode(1);
        builder.addNode(tagNode1);builder.addNode(tagNode2);
        builder.addEdge(tagNode1, new long[]{leaf});
        builder.addEdge(tagNode2, new long[]{leaf});
        builder.addEdge(root, new long[]{tagNode1});
        builder.addEdge(root, new long[]{tagNode2});
        return builder.build(networkId, myInst, param, this);
    }

    @Override
    public Instance decompile(Network network) {
        BaseNetwork unlablledNetwork=(BaseNetwork)network;
        TwitterInstance inst=(TwitterInstance)network.getInstance();
        long root=toRoot();
        int rootIdx= Arrays.binarySearch(unlablledNetwork.getAllNodes(), root);
        int child=unlablledNetwork.getMaxPath(rootIdx)[0];
        int[] childArr=unlablledNetwork.getNodeArray(child);
        int prediction=childArr[1];
        inst.setPrediction(prediction);
        return inst;
    }
}
