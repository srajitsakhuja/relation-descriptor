package org.statnlp.example.RelDescriptor;

import org.statnlp.commons.types.WordToken;
import org.statnlp.hypergraph.FeatureArray;
import org.statnlp.hypergraph.FeatureManager;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.Network;
import scala.Int;

import java.util.ArrayList;
import java.util.List;

public class RelFeatureManager extends FeatureManager {
    public RelFeatureManager(GlobalNetworkParam param_g) {

        super(param_g);
    }
    private enum FeatType{
        unigram, bigram, transition, longContext;
    }
    @Override
    protected FeatureArray extract_helper(Network network, int parent_k, int[] children_k, int children_k_index) {
        RelInstance inst=(RelInstance)network.getInstance();
        int[] paArr=network.getNodeArray(parent_k);
        if(RelNetworkCompiler.NodeType.values()[paArr[2]]== RelNetworkCompiler.NodeType.leaf || RelNetworkCompiler.NodeType.values()[paArr[2]]== RelNetworkCompiler.NodeType.root){
            return FeatureArray.EMPTY;
        }
        int pos=paArr[0];
        String tag=paArr[1]+"";
        List<WordToken> wts=inst.getInput().sent;
        List<Integer> fs=new ArrayList<Integer>();
        String w=wts.get(pos).getForm();
        String POS=wts.get(pos).getTag();
        String phrase=wts.get(pos).getPhraseTag();
        int uni=0;
        int bi=0;
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-w0", tag,w));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-po0", tag, POS));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-ph0", tag, phrase));

        String lw=pos-1>=0 ? wts.get(pos-1).getForm():"START-W"+(pos-1);
        String lPOS=pos-1>=0 ? wts.get(pos-1).getTag():"START-PO"+(pos-1);
        String lPhrase=pos-1>=0 ? wts.get(pos-1).getPhraseTag():"START-PH"+(pos-1);


        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-w-1", tag, lw));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-po-1", tag, lPOS));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-ph-1", tag, lPhrase));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-w-10", tag, lw+" "+w));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-p-10", tag, lPOS+" "+POS));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-ph-10", tag, lPhrase+" "+phrase));

        String llw=pos-2>=0 ? wts.get(pos-2).getForm():"START-W"+(pos-2);
        String llPOS=pos-2>=0 ? wts.get(pos-2).getTag():"START-PO"+(pos-2);
        String llPhrase=pos-2>=0 ? wts.get(pos-2).getPhraseTag():"START-PH"+(pos-2);

        fs.add(_param_g.toFeature(network, FeatType.unigram+"-w-2", tag, llw));
        fs.add(_param_g.toFeature(network, FeatType.unigram+"-po-2", tag, llPOS));
        fs.add(_param_g.toFeature(network, FeatType.unigram+"-ph-2", tag, llPhrase));
        fs.add(_param_g.toFeature(network, FeatType.bigram+"-w-2-1", tag, llw+" "+lw));
        fs.add(_param_g.toFeature(network, FeatType.bigram+"-po-2-1", tag, llPOS+" "+lPOS));
        fs.add(_param_g.toFeature(network, FeatType.bigram+"-ph-2-1", tag, llPhrase+" "+lPhrase));


        String rw=(pos+1)<=inst.size()-1 ? wts.get(pos+1).getForm():"END-W"+(pos+1);
        String rPOS=(pos+1)<=inst.size()-1 ? wts.get(pos+1).getTag():"END-PO"+(pos+1);
        String rPhrase=(pos+1)<=inst.size()-1 ? wts.get(pos+1).getPhraseTag():"END-PH"+(pos+1);

        fs.add(_param_g.toFeature(network, FeatType.unigram+"-w+1", tag, rw));
        fs.add(_param_g.toFeature(network, FeatType.unigram+"-po+1", tag, rPOS));
        fs.add(_param_g.toFeature(network, FeatType.unigram+"-ph+1", tag, rPhrase));
        fs.add(_param_g.toFeature(network, FeatType.bigram+"-w0+1", tag, w+" "+rw));
        fs.add(_param_g.toFeature(network, FeatType.bigram+"-po0+1", tag, POS+" "+rPOS));
        fs.add(_param_g.toFeature(network, FeatType.bigram+"-ph0+1", tag, phrase+" "+rPhrase));

        String rrw=(pos+2)<=inst.size()-1 ? wts.get(pos+2).getForm():"END-W"+(pos+2);
        String rrPOS=(pos+2)<=inst.size()-1 ? wts.get(pos+2).getTag():"END-PO"+(pos+2);
        String rrPhrase=(pos+2)<=inst.size()-1 ? wts.get(pos+2).getPhraseTag():"END-PH"+(pos+2);

        fs.add(_param_g.toFeature(network, FeatType.unigram+"-w+2", tag, rrw));
        fs.add(_param_g.toFeature(network, FeatType.unigram+"-po+2", tag, rrPOS));
        fs.add(_param_g.toFeature(network, FeatType.unigram+"-ph+2", tag, rrPhrase));
        fs.add(_param_g.toFeature(network, FeatType.bigram+"-w+1+2", tag, rrw+" "+rw));
        fs.add(_param_g.toFeature(network, FeatType.bigram+"-po+1+2", tag, rPOS+" "+rrPOS));
        fs.add(_param_g.toFeature(network, FeatType.bigram+"-ph+1+2", tag, rPhrase+" "+rrPhrase));


        //Long Range Features

        if(pos==inst.size()-1){
            List<WordToken> wordTokens=new ArrayList<WordToken>();
            List<Integer> tags=new ArrayList<Integer>();

            int position=pos;
            int[] currArr=network.getNodeArray(parent_k);

            wordTokens.add(0,wts.get(position));
            tags.add(0, currArr[1]);


            int currIdx=children_k[0];
            while(true){
                currArr=network.getNodeArray(currIdx);
                if(currArr[2]!= RelNetworkCompiler.NodeType.tag.ordinal()){
                    break;
                }
                position=network.getNodeArray(currIdx)[0];
                tags.add(0, currArr[1]);
                wordTokens.add(0, wts.get(position));
                currIdx=network.getChildren(currIdx)[0][0];
            }
            String longWordl="";
            String longTagl="";
            String longWordr="";
            String longTagr="";
            boolean relFound=false;
            int relStart=0;
            int relEnd=wts.size()-1;
            for(int i=0; i<wordTokens.size(); i++){
                if(!relFound && tags.get(i)==1){
                    relStart=i;
                    if(i>0){
                        longWordl=wts.get(i-1).getForm();
                        longTagl=wts.get(i-1).getTag();
                    }
                    relFound=true;
                }
                if(relFound && tags.get(i)==0){
                    relEnd=i-1;
                    longWordr=wts.get(i).getForm();
                    longTagr=wts.get(i).getTag();

                }
            }
            //DEBUGGING
            /*if(inst.getInstanceId()==-81) {
                System.out.println("++++++++++++++++++++++");
                System.out.print(inst.getInstanceId() + ":::");
                for (int i = 0; i < wts.size(); i++) {
                    System.out.print(wts.get(i).getForm() + " ");
                }
                System.out.println();
                System.out.println(tags);
                System.out.println(longWordl);
                System.out.println(longWordr);
                System.out.println(relFound);
                System.out.println(relStart);
                System.out.println(relEnd);
                System.out.println();
            }*/

            if(relFound) {
                for(int i=relStart; i<=relEnd; i++) {
                    fs.add(_param_g.toFeature(network, FeatType.longContext.name(), tags.get(i) + "", longWordl));
                    fs.add(_param_g.toFeature(network, FeatType.longContext.name(), tags.get(i) + "", longWordr));
                    fs.add(_param_g.toFeature(network, FeatType.longContext.name(), tags.get(i) + "", longTagl));
                    fs.add(_param_g.toFeature(network, FeatType.longContext.name(), tags.get(i) + "", longTagr));
                }
            }
        }
        int childIdx=children_k[0];
        int[] childArr=network.getNodeArray(childIdx);
        String prevTag=childArr[1]+"";
        fs.add(_param_g.toFeature(network, FeatType.transition.name(), tag, prevTag));

        return this.createFeatureArray(network,fs);
    }
}
