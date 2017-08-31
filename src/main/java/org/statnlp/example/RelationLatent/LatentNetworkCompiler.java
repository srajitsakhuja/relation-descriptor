package org.statnlp.example.RelationLatent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.statnlp.commons.types.Instance;
import org.statnlp.example.base.BaseNetwork;
import org.statnlp.example.base.BaseNetwork.NetworkBuilder;
import org.statnlp.hypergraph.LocalNetworkParam;
import org.statnlp.hypergraph.Network;
import org.statnlp.hypergraph.NetworkCompiler;
import org.statnlp.hypergraph.NetworkIDMapper;

public class LatentNetworkCompiler extends NetworkCompiler {
	private static final long serialVersionUID = 2046107789709874963L;
	private List<String> relTypes=new ArrayList<String>();
    private List<String> relTags=new ArrayList<String>();
    private Map<String, Integer> relType2Id=new HashMap<String, Integer>();
    private Map<String, Integer> relTag2Id=new HashMap<String, Integer>();
    static{
        NetworkIDMapper.setCapacity(new int[]{150, 100, 3, 100000});
    }
    public LatentNetworkCompiler(List<String> relTypes) {
        this.relTypes=relTypes;
        for(int i=0; i<relTypes.size(); i++){
            relType2Id.put(relTypes.get(i), i);
            relTags.add("B-"+relTypes.get(i));
            relTags.add("I-"+relTypes.get(i));
            //System.out.print(relTypes.get(i));
        }
        relTags.add("O");
        for(int i=0; i<relTags.size(); i++){
            relTag2Id.put(relTags.get(i),i);
        }
    }
    protected enum NodeType{
        leaf, root, tag
    };


    public long toLeafNode(){
        return toNode(0,0, NodeType.leaf, 0);
    }
    public long toRootNode(int size){
        return toNode(size-1, relTags.size(), NodeType.root, size);
    }
    public long toTagNode(int pos, int tagId, int chainId){
        return toNode(pos, tagId, NodeType.tag, chainId);
    }
    public long toNode(int pos, int tagId, NodeType nodeType, int chainId){
        int nodeInfo[]={pos, tagId, nodeType.ordinal(), chainId};
        return NetworkIDMapper.toHybridNodeID(nodeInfo);
    }

    private boolean covers(int el, int left, int right){
        if(el>=left && el<=right){
            return true;
        }
        return false;
    }
    @Override
    public Network compileLabeled(int networkId, Instance inst, LocalNetworkParam param) {
        BaseNetwork.NetworkBuilder<BaseNetwork> builder=BaseNetwork.NetworkBuilder.builder();
        RelationInstance myInst=(RelationInstance) inst;
        int size=myInst.size();
        long leaf=toLeafNode();
        long root=toRootNode(size);
        builder.addNode(leaf);
        builder.addNode(root);
        int e1=myInst.input.e1Pos;
        int e2=myInst.input.e2Pos;
        long child=leaf;
        String relType=myInst.output.relType;
        //System.out.println(relType);
        int relId=this.relType2Id.get(relType);
        for(int left=0; left<size; left++){
            for(int right=left; right<inst.size(); right++){
                if(covers(e1, left, right)  || covers(e2, left, right)){
                    continue;
                }
                child=leaf;
                int chainId=relId*1000+left*100+right;
                for(int pos=0; pos<size; pos++){
                    String tag;
                    if(covers(pos, left, right)){
                        if(pos==left){
                            tag="B-"+relType;
                        }
                        else{
                            tag="I-"+relType;
                        }
                    }
                    else{
                        tag="O";
                    }
                    int tagId=relTag2Id.get(tag);
                    long tagNode=toTagNode(pos, tagId,chainId);
                    builder.addNode(tagNode);
                    builder.addEdge(tagNode, new long[]{child});
                    child=tagNode;
                }
                builder.addEdge(root, new long[]{child});
            }
        }

        return builder.build(networkId, inst, param, this);
    }

    @Override
    public Network compileUnlabeled(int networkId, Instance inst, LocalNetworkParam param) {
       NetworkBuilder<BaseNetwork> builder= NetworkBuilder.builder();
        RelationInstance myInst=(RelationInstance)inst;
        int size=myInst.size();

        int e1=myInst.input.e1Pos;
        int e2=myInst.input.e2Pos;
        long leaf=toLeafNode();
        long root=toRootNode(size);
        builder.addNode(leaf);
        builder.addNode(root);
        long child=leaf;

        for(int i=0; i<this.relTypes.size(); i++){
            String relType=this.relTypes.get(i);
            int relId=this.relType2Id.get(relType);
            for(int left=0; left<size; left++){
                for(int right=left; right<size; right++){
                    if(covers(e1, left, right)  || covers(e2, left, right)){
                        continue;
                    }
                    child=leaf;
                    int chainId=relId*1000+left*100+right;
                    for(int pos=0; pos<size; pos++){
                        String tag;
                        if(covers(pos, left, right)){
                            if(pos==left){
                                tag="B-"+relType;
                            }
                            else{
                                tag="I-"+relType;
                            }
                        }
                        else{
                            tag="O";
                        }
                        int tagId=relTag2Id.get(tag);
                        long tagNode=toTagNode(pos, tagId,chainId);
                        builder.addNode(tagNode);
                        builder.addEdge(tagNode, new long[]{child});
                        child=tagNode;
                    }
                    builder.addEdge(root, new long[]{child});
                }
            }
        }

        return builder.build(networkId, inst, param, this);
    }

    @Override
    public Instance decompile(Network network) {
        BaseNetwork unlablledNetwork=(BaseNetwork)network;
        RelationInstance inst=(RelationInstance)unlablledNetwork.getInstance();
        int size=inst.size();
        long root=toRootNode(size);
        int rootIdx= Arrays.binarySearch(unlablledNetwork.getAllNodes(), root);
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
