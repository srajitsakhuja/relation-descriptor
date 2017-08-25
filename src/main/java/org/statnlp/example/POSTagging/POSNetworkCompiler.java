package org.statnlp.example.POSTagging;

import org.statnlp.commons.types.Instance;
import org.statnlp.example.base.BaseNetwork;
import org.statnlp.hypergraph.LocalNetworkParam;
import org.statnlp.hypergraph.Network;
import org.statnlp.hypergraph.NetworkCompiler;
import org.statnlp.hypergraph.NetworkIDMapper;

import java.util.*;

public class POSNetworkCompiler extends NetworkCompiler {
    protected List<String> posTags;
    protected Map<String, Integer> posTags2Id;
    private BaseNetwork generic;
    private int maxSize=130;
    protected enum NodeType{
        leaf, tag, root
    };
    static{
        int cap_arr[]={150, 50,3};
        NetworkIDMapper.setCapacity(cap_arr);
    }
    public long toNode_root(int size){
        return toNode(size-1, posTags.size(), NodeType.root);
    }
    public long toNode_leaf(){
        return toNode(0,0,NodeType.leaf);
    }
    public long toNode_tag(int pos, int posTagId){
        return toNode(pos, posTagId, NodeType.tag);
    }
    public long toNode(int pos, int posTagId, NodeType nodeType){
        int nodeInfo[]={pos,posTagId, nodeType.ordinal()};
        return NetworkIDMapper.toHybridNodeID(nodeInfo);
    }
    public POSNetworkCompiler(List<String> posTags) {
        this.posTags=posTags;
        this.posTags2Id= new HashMap<>(this.posTags.size());
        for(int i=0; i<posTags.size(); i++){
            this.posTags2Id.put(posTags.get(i), i);
        }
        this.buildGenericNetwork();
    }

    public Network compileLabeled(int networkId, Instance inst, LocalNetworkParam param) {
        BaseNetwork.NetworkBuilder<BaseNetwork> builder=BaseNetwork.NetworkBuilder.builder();
        POSInstance posInst=(POSInstance)inst;
        List<String> input=posInst.getInput();
        List<String> output=posInst.getOutput();
        long leaf=toNode_leaf();
        builder.addNode(leaf);
        long child=leaf;
        int i;
        for( i=0; i<input.size(); i++){
            String posTag=output.get(i);
            int posTagId=this.posTags2Id.get(posTag);
            long tagNode=toNode_tag(i, posTagId);
            builder.addNode(tagNode);
            long children[]={child};
            builder.addEdge(tagNode,children);
            child=tagNode;
        }
        long rootNode=toNode_root(i+1);
        builder.addNode(rootNode);
        long children[]={child};
        builder.addEdge(rootNode, children);
        BaseNetwork network=builder.build(networkId, inst, param, this);
        return network;
    }

    public Network compileUnlabeled(int networkId, Instance inst, LocalNetworkParam param) {
        int size=inst.size();
        long nodes[]=this.generic.getAllNodes();
        int children[][][]=this.generic.getAllChildren();
        long root=this.toNode_root(size);
        int rootIdx= Arrays.binarySearch(nodes, root);
        BaseNetwork unlabelledNetwork= BaseNetwork.NetworkBuilder.quickBuild(networkId, inst, nodes, children, rootIdx+1, param, this);
        return unlabelledNetwork;
    }

    private void buildGenericNetwork(){
        BaseNetwork.NetworkBuilder<BaseNetwork> builder=BaseNetwork.NetworkBuilder.builder();
        long leaf=toNode_leaf();
        builder.addNode(leaf);
        long children[]={leaf};
        for(int i=0; i<this.maxSize; i++){
            long[] currentNodes=new long[this.posTags.size()];
            for(int l=0; l<this.posTags.size(); l++){
                long tagNode=toNode_tag(i,l);
                builder.addNode(tagNode);
                currentNodes[l]=tagNode;
                for(long child: children){
                    builder.addEdge(tagNode, new long[]{child});
                }
            }
            children=currentNodes;
            long root=toNode_root(i+1);
            builder.addNode(root);
            for(long child: children) {
                builder.addEdge(root, new long[]{child});
            }
        }
        this.generic=builder.buildRudimentaryNetwork();

    }
    public Instance decompile(Network network) {
        BaseNetwork unlabelledNetwork=(BaseNetwork)network;
        POSInstance inst=(POSInstance)unlabelledNetwork.getInstance();
        int size=inst.size();
        long root=this.toNode_root(size);
        int currIdx=Arrays.binarySearch(unlabelledNetwork.getAllNodes(), root);
        List<String> prediction=new ArrayList<String>(size);
        for(int i=0; i<size; i++){
            int child=unlabelledNetwork.getMaxPath(currIdx)[0];
            int childArr[]=unlabelledNetwork.getNodeArray(child);
            int posTagId=childArr[1];
            prediction.add(0, posTags.get(posTagId));
            currIdx=child;
        }
        inst.setPrediction(prediction);
        return inst;
    }

}
