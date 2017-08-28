package org.statnlp.example.RelationDescriptor;

import org.statnlp.commons.types.Instance;
import org.statnlp.example.RelDescriptor.RelInstance;
import org.statnlp.example.base.BaseNetwork;
import org.statnlp.hypergraph.LocalNetworkParam;
import org.statnlp.hypergraph.Network;
import org.statnlp.hypergraph.NetworkCompiler;
import org.statnlp.hypergraph.NetworkIDMapper;

import java.util.*;

public class RelationNetworkCompiler extends NetworkCompiler {
    private List<String> relTags=new ArrayList<String>();
    private Map<String, Integer> relTags2Id=new HashMap<String, Integer>();
    static{
        NetworkIDMapper.setCapacity(new int[]{150, 50,3, 10000000});
    }
    public RelationNetworkCompiler(List<String> relTags) {
        for(int i=0;i<relTags.size(); i++){
            this.relTags.add(relTags.get(i));
            relTags2Id.put(relTags.get(i), i);
        }
    }
    protected enum NodeType{
        leaf, root, tag;
    }
    public long toLeafNode(){
        return toNode(0,0, NodeType.leaf, 0);
    }
    public long toRootNode(int size){
        return toNode(size-1, this.relTags.size(), NodeType.root,size);
    }
    public long toTagNode(int pos, int rel, int chainId){
        return toNode(pos, rel, NodeType.tag, chainId);
    }
    public long toNode(int pos, int rel, NodeType nodeType, int chainId ){
        return NetworkIDMapper.toHybridNodeID(new int[]{pos,rel,nodeType.ordinal(), chainId});
    }



    @Override
    public Network compileLabeled(int networkId, Instance inst, LocalNetworkParam param) {
        BaseNetwork.NetworkBuilder<BaseNetwork> builder= BaseNetwork.NetworkBuilder.builder();
        RelationInstance myInst=(RelationInstance)inst;
        CandidatePair input=myInst.getInput();
        List<String> output=myInst.getOutput();
        long leaf=toLeafNode();
        long root=toRootNode(myInst.size());
        builder.addNode(leaf);
        long child=leaf;
        builder.addNode(root);
        int left=myInst.size(); int right=myInst.size();
        String prevTag="O";
        for(int i=0; i<input.size(); i++){
            if(output.get(i).equals("B-R")){
                left=i;
            }
            if(output.get(i).equals("O")  && !prevTag.equals("O")){
                right=i-1;
            }
            prevTag=output.get(i);
        }
        int chainId=left*100+right;
        for(int i=0; i<input.size(); i++){
            long tagNode=toTagNode(i, this.relTags2Id.get(output.get(i)), chainId);
            builder.addNode(tagNode);
            builder.addEdge(tagNode, new long[]{child});
            child=tagNode;
        }
        builder.addEdge(root, new long[] {child});
        return builder.build(networkId, inst, param, this);
    }
    public boolean covers(int left, int right, int arg){
        if(arg>=left && arg<=right){
            return true;
        }
        return false;
    }

    @Override
    public Network compileUnlabeled(int networkId, Instance inst, LocalNetworkParam param) {
        BaseNetwork.NetworkBuilder<BaseNetwork> builder= BaseNetwork.NetworkBuilder.builder();
        RelationInstance myInst=(RelationInstance)inst;
        CandidatePair input=myInst.getInput();
        List<String> output=myInst.getOutput();
        int arg1Idx=input.arg1Idx;
        int arg2Idx=input.arg2Idx;
        long leaf=toLeafNode();
        builder.addNode(leaf);
        long root=toRootNode(myInst.size());
        builder.addNode(root);
        long child=leaf;

        for(int left=0; left<myInst.size();left++){
            for(int right=left; right<myInst.size(); right++){
                if(covers(left, right, arg1Idx) || covers(left, right,arg2Idx)){
                    continue;
                }
                    int chainId=left*100+right;
                child=leaf;
                    for(int i=0; i<myInst.size(); i++){
                        int relTag;
                        if(i>=left && i<=right){
                            if(i==left){
                                relTag=this.relTags2Id.get("B-R");
                            }
                            else{
                                relTag=this.relTags2Id.get("I-R");
                            }
                        }
                        else{
                            relTag=this.relTags2Id.get("O");
                        }
                        long tagNode=toTagNode(i, relTag, chainId);
                        builder.addNode(tagNode);
                        builder.addEdge(tagNode, new long[]{child});
                        child=tagNode;
                    }
                    builder.addEdge(root, new long[]{child});

            }
        }
        child=leaf;
        int left=myInst.size();
        int right=myInst.size();
        int chainId=left*100+right;
        for(int i=0; i<myInst.size(); i++){
            long tagNode=toTagNode(i, this.relTags2Id.get("O"), chainId);
            builder.addNode(tagNode);
            builder.addEdge(tagNode, new long[]{child});
            child=tagNode;
        }
        builder.addEdge(root, new long[]{child});
        BaseNetwork unlabelledNetwork=(BaseNetwork)builder.build(networkId, inst, param, this);
        return unlabelledNetwork;
    }

    @Override
    public Instance decompile(Network network) {
        BaseNetwork unlabelledNetwork=(BaseNetwork)network;
        RelationInstance myInst=(RelationInstance)unlabelledNetwork.getInstance();
        long root=toRootNode(myInst.size());
        int rootIdx= Arrays.binarySearch(unlabelledNetwork.getAllNodes(), root);
        List<String> predictions=new ArrayList<String>();
        for(int i=0; i<myInst.size(); i++){
            int child=unlabelledNetwork.getMaxPath(rootIdx)[0];
            int[] childArr=unlabelledNetwork.getNodeArray(child);
            predictions.add(0, relTags.get(childArr[1]));
            rootIdx=child;
        }
        myInst.setPrediction(predictions);
        return myInst;
    }

}
