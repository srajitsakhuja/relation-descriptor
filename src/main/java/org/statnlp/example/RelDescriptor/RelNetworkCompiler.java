package org.statnlp.example.RelDescriptor;

import com.fasterxml.jackson.databind.ser.Serializers;
import jdk.internal.util.xml.impl.Input;
import org.statnlp.commons.types.Instance;
import org.statnlp.commons.types.WordToken;
import org.statnlp.example.base.BaseNetwork;
import org.statnlp.hypergraph.LocalNetworkParam;
import org.statnlp.hypergraph.Network;
import org.statnlp.hypergraph.NetworkCompiler;
import org.statnlp.hypergraph.NetworkIDMapper;

import javax.xml.soap.Node;
import java.util.*;

public class RelNetworkCompiler extends NetworkCompiler {
    private BaseNetwork generic;
    protected List<String> relTags;
    public Map<String, Integer> relTags2Id;
    private int maxLen=150;
    static{
        NetworkIDMapper.setCapacity(new int[]{150, 50, 3, 100000});
    }
    public Map<String, Integer> chainIdMap=new HashMap<String, Integer>();


    public RelNetworkCompiler(List<String> relTags) {
        this.relTags=relTags;
        this.relTags2Id=new HashMap<>(relTags.size());
        for(int i=0; i<this.relTags.size(); i++){
            this.relTags2Id.put(relTags.get(i), i);
        }
        //buildGenericNetwork();
    }
    protected enum NodeType{
      leaf, root, tag;
    };
    public long toNode_leaf(){
        return toNode(0,0, NodeType.leaf, 0);
    }
    public long toNode_root(int size){
        return toNode(size-1, this.relTags.size(), NodeType.root, size);
    }
    public long toNode_tag(int pos, int tagId, int id){
        return toNode(pos, tagId, NodeType.tag, id);
    }
    public long toNode(int pos, int tagId, NodeType nodeType, int id){
        int nodeinfo[]={pos, tagId, nodeType.ordinal(), id};
        return NetworkIDMapper.toHybridNodeID(nodeinfo);
    }
    @Override
    public Network compileLabeled(int networkId, Instance inst, LocalNetworkParam param) {
        BaseNetwork.NetworkBuilder<BaseNetwork> builder=BaseNetwork.NetworkBuilder.builder();
        RelInstance myInst=(RelInstance)inst;
        InputData input =myInst.getInput();
        int arg1Idx=input.arg1Idx;
        int arg2Idx=input.arg1Idx;
        List<String> output=myInst.getOutput();
        int left=myInst.size(), right=myInst.size();
        String prevTag="O";
        boolean startFound=false;
        for(int i=0; i<myInst.size(); i++){
            if(!startFound && output.get(i).equals("BR")){
                startFound=true;
                left=i;
            }
            if(!prevTag.equals("O") && output.get(i).equals("O")){
                right=i-1;
            }
            prevTag=output.get(i);
        }

        int id=left*100+right;
        long leaf=toNode_leaf();
        builder.addNode(leaf);
        long root=toNode_root(myInst.size());
        long child=leaf;
        builder.addNode(root);
        for(int i=0; i<myInst.size(); i++){
            int tagId=relTags2Id.get(output.get(i));
            long node=toNode_tag(i, tagId, id);
            builder.addNode(node);
            builder.addEdge(node,new long[]{child});
            child=node;
        }
        builder.addEdge(root, new long[]{child});
        BaseNetwork network=builder.build(networkId, inst, param, this);
        return network;
    }
    /*public Network compileLabeled(int networkId, Instance inst, LocalNetworkParam param) {
        BaseNetwork.NetworkBuilder<BaseNetwork> builder=BaseNetwork.NetworkBuilder.builder();
        RelInstance myinst=(RelInstance)inst;

        long leaf=toNode_leaf();
        builder.addNode(leaf);
        long child=leaf;
        int i;
        for(i=0; i<inst.size(); i++){
            List<String> output=myinst.getOutput();
            long node=toNode_tag(i, relTags2Id.get(output.get(i)));
            builder.addNode(node);
            builder.addEdge(node, new long[]{child});
            child=node;
        }
        long root=toNode_root(i+1);
        builder.addNode(root);
        builder.addEdge(root,new long[]{child});
        BaseNetwork network=builder.build(networkId, inst, param, this);
        return network;
    }*/
    private boolean covers(int argIdx, int left, int right ){
        if(argIdx>=left && argIdx<=right){
            return true;
        }
        return false;
    }
    @Override
    public Network compileUnlabeled(int networkId, Instance inst, LocalNetworkParam param) {

        BaseNetwork.NetworkBuilder<BaseNetwork> builder=BaseNetwork.NetworkBuilder.builder();
        RelInstance myInst=(RelInstance)inst;
        int size=myInst.size();
        //System.out.println(myInst.getInstanceId()+" "+size);
        long leaf=toNode_leaf();
        long root=toNode_root(size);
        builder.addNode(leaf);
        builder.addNode(root);
        long child=leaf;
        int arg1Idx=myInst.getInput().arg1Idx;
        int arg2Idx=myInst.getInput().arg2Idx;
        for(int left=0; left<size; left++){
            for(int right=left; right<size; right++){
                if(covers(arg1Idx, left, right) || covers(arg2Idx, left, right)){
                    continue;
                }
                int id=left*100+right;
                child=leaf;
                for(int i=0; i<size; i++){
                    int tagId;
                    if(covers(i,left,right)){
                        if(i==left){
                            tagId=this.relTags2Id.get("B-R");
                        }
                        else{
                            tagId=this.relTags2Id.get("I-R");
                        }
                    }
                    else{
                        tagId=this.relTags2Id.get("O");
                    }

                    long node=toNode_tag(i, tagId, id);
                    builder.addNode(node);
                    builder.addEdge(node, new long[]{child});
                    child=node;
                }
                builder.addEdge(root, new long[]{child});
            }
        }
        child=leaf;
        int left=size;
        int right=size;
        int id=left*1000+right;

        for(int i=0; i<size; i++){
            int tagId=this.relTags2Id.get("O");
            long node=toNode_tag(i, tagId, id);
            builder.addNode(node);
            builder.addEdge(node, new long[]{child});
            child=node;
        }
        builder.addEdge(root, new long[]{child});
        BaseNetwork unlabelledNetwork=builder.build(networkId, inst, param, this);
        return unlabelledNetwork;
    }
    /*public Network compileUnlabeled(int networkId, Instance inst, LocalNetworkParam param) {
        long[] nodes=this.generic.getAllNodes();
        int children[][][]=this.generic.getAllChildren();
        long root=toNode_root(inst.size());
        int rootIdx= Arrays.binarySearch(nodes, root);
        BaseNetwork unlabelledNetwork=BaseNetwork.NetworkBuilder.quickBuild(networkId, inst, nodes, children, rootIdx+1, param, this);
        return unlabelledNetwork;
    }

    private void buildGenericNetwork(){
        BaseNetwork.NetworkBuilder<BaseNetwork> builder=BaseNetwork.NetworkBuilder.builder();
        long leaf=toNode_leaf();
        builder.addNode(leaf);
        long[] children={leaf};
        for(int i=0; i<this.maxLen; i++){
            long[] currentNodes=new long[relTags.size()];
            for(int j=0; j<this.relTags.size(); j++){
                long node=toNode_tag(i,j);
                builder.addNode(node);
                currentNodes[j]=node;
                for(long child: children){
                    builder.addEdge(node, new long[]{child});
                }
            }
            children=currentNodes;
            long root=toNode_root(i+1);
            builder.addNode(root);
            for(long child:children){
                builder.addEdge(root,new long[]{child});
            }
        }
        this.generic=builder.buildRudimentaryNetwork();
    }*/
    @Override
    public Instance decompile(Network network) {
        BaseNetwork unlablledNetwork=(BaseNetwork)network;
        RelInstance inst=(RelInstance)unlablledNetwork.getInstance();
        int size=inst.size();
        long root=toNode_root(size);
        int rootIdx=Arrays.binarySearch(unlablledNetwork.getAllNodes(), root);
        List<String> predictions=new ArrayList<String>(size);
        for(int i=0; i<size; i++){
            int child=unlablledNetwork.getMaxPath(rootIdx)[0];
            int[] childArr=unlablledNetwork.getNodeArray(child);
            predictions.add(0, relTags.get(childArr[1]));
            rootIdx=child;
        }
        inst.setPrediction(predictions);
        return inst;
    }
}
