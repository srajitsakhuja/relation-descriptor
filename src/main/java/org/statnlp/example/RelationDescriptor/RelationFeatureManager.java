package org.statnlp.example.RelationDescriptor;

import org.statnlp.commons.types.WordToken;
import org.statnlp.example.base.BaseNetwork;
import org.statnlp.hypergraph.FeatureArray;
import org.statnlp.hypergraph.FeatureManager;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.Network;

import java.util.ArrayList;
import java.util.List;

public class RelationFeatureManager extends FeatureManager {
    public RelationFeatureManager(GlobalNetworkParam param_g) {
        super(param_g);
    }
    enum FeatType{
        unigram, bigram, transition, longContext, longPath
    };
    @Override
    protected FeatureArray extract_helper(Network network, int parent_k, int[] children_k, int children_k_index) {
        List<Integer> fs=new ArrayList<Integer>();
        RelationInstance inst=(RelationInstance)network.getInstance();
        int[] paArr=network.getNodeArray(parent_k);
        if(RelationNetworkCompiler.NodeType.values()[paArr[2]]== RelationNetworkCompiler.NodeType.leaf || RelationNetworkCompiler.NodeType.values()[paArr[2]]== RelationNetworkCompiler.NodeType.root){
            return FeatureArray.EMPTY;
        }
        String currTag=paArr[1]+"";
        int pos=paArr[0];
        List<WordToken> wt=inst.getInput().wts;


        //LC-Features
        String w=wt.get(pos).getForm();
        String lw=(pos-1)>=0?wt.get(pos-1).getForm():"START-W"+(pos-1);
        String llw=(pos-2)>=0?wt.get(pos-2).getForm():"START-W"+(pos-2);
        String rw=(pos+1)<inst.size()?wt.get(pos+1).getForm():"END-W"+(pos+1);
        String rrw=(pos+2)<inst.size()?wt.get(pos+2).getForm():"END-W"+(pos+2);

        String POS=wt.get(pos).getTag();
        String lPOS=(pos-1)>=0?wt.get(pos-1).getTag():"START-PO"+(pos-1);
        String llPOS=(pos-2)>=0?wt.get(pos-2).getTag():"START-PO"+(pos-2);
        String rPOS=(pos+1)<inst.size()?wt.get(pos+1).getTag():"END-PO"+(pos+1);
        String rrPOS=(pos+2)<inst.size()?wt.get(pos+2).getTag():"END-PO"+(pos+2);

        String phrase=wt.get(pos).getPhraseTag();
        String lPhrase=(pos-1)>=0?wt.get(pos-1).getPhraseTag():"START-PH"+(pos-1);
        String llPhrase=(pos-2)>=0?wt.get(pos-2).getPhraseTag():"START-PH"+(pos-2);
        String rPhrase=(pos+1)<inst.size()?wt.get(pos+1).getPhraseTag():"START-PH"+(pos+1);
        String rrPhrase=(pos+2)<inst.size()?wt.get(pos+2).getPhraseTag():"START-PH"+(pos+2);
        int childIdx=children_k[0];
        int[] childArr=network.getNodeArray(childIdx);
        String prevTag=childArr[1]+"";

        //zeroth-order features
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-w0", currTag, w));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-w-1", currTag, lw));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-w-2", currTag, llw));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-w+1", currTag, rw));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-w+2", currTag, rrw));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-po0", currTag, POS));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-po-1", currTag, lPOS));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-po-2", currTag, llPOS));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-po+1", currTag, rPOS));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-po+2", currTag, rrPOS));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-ph0", currTag, phrase));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-ph-1", currTag, lPhrase));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-ph-2", currTag, llPhrase));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-ph+1", currTag, rPhrase));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"-ph+2", currTag, rrPhrase));

        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-w-2-1", currTag, llw+" "+lw));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-w-10", currTag, lw+" "+w));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-w0+1", currTag, w+" "+rw));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-w+1+2", currTag, rw+" "+rrw));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-po-2-1", currTag, llPOS+" "+lPOS));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-po-10", currTag, lPOS+" "+POS));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-po0+1", currTag, POS+" "+rPOS));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-po+1+2", currTag, rPOS+" "+rrPOS));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-ph-2-1", currTag, llPhrase+" "+lPhrase));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-ph-10", currTag, lPhrase+" "+phrase));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-ph0+1", currTag, phrase+" "+rPhrase));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"-ph+1+2", currTag, rPhrase+" "+rrPhrase));

        //first-order features
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"*w0", currTag+" "+prevTag, w));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"*w-1", currTag+" "+prevTag, lw));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"*w-2", currTag+" "+prevTag, llw));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"*w+1", currTag+" "+prevTag, rw));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"*w+2", currTag+" "+prevTag, rrw));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"*po0", currTag+" "+prevTag, POS));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"*po-1", currTag+" "+prevTag, lPOS));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"*po-2", currTag+" "+prevTag, llPOS));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"*po+1", currTag+" "+prevTag, rPOS));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"*po+2", currTag+" "+prevTag, rrPOS));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"*ph0", currTag+" "+prevTag, phrase));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"*ph-1", currTag+" "+prevTag, lPhrase));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"*ph-2", currTag+" "+prevTag, llPhrase));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"*ph+1", currTag+" "+prevTag, rPhrase));
        fs.add(_param_g.toFeature(network, FeatType.unigram.name()+"*ph+2", currTag+" "+prevTag, rrPhrase));

        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"*w-2-1", currTag+" "+prevTag, llw+" "+lw));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"*w-10", currTag+" "+prevTag, lw+" "+w));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"*w0+1", currTag+" "+prevTag, w+" "+rw));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"*w+1+2", currTag+" "+prevTag, rw+" "+rrw));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"*po-2-1", currTag+" "+prevTag, llPOS+" "+lPOS));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"*po-10", currTag+" "+prevTag, lPOS+" "+POS));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"*po0+1", currTag+" "+prevTag, POS+" "+rPOS));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"*po+1+2", currTag+" "+prevTag, rPOS+" "+rrPOS));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"*ph-2-1", currTag+" "+prevTag, llPhrase+" "+lPhrase));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"*ph-10", currTag+" "+prevTag, lPhrase+" "+phrase));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"*ph0+1", currTag+" "+prevTag, phrase+" "+rPhrase));
        fs.add(_param_g.toFeature(network, FeatType.bigram.name()+"*ph+1+2", currTag+" "+prevTag, rPhrase+" "+rrPhrase));

        fs.add(_param_g.toFeature(network, FeatType.transition.name(), currTag, prevTag));



        //LONG-RANGE FEATURES
        if(pos==inst.size()-1){
            int arg1Idx=inst.getInput().arg1Idx;
            int arg2Idx=inst.getInput().arg2Idx;
            List<WordToken> wordTokens=new ArrayList<WordToken>();
            List<Integer> tags=new ArrayList<Integer>();

            int position=pos;
            int[] currArr=network.getNodeArray(parent_k);

            wordTokens.add(0,wt.get(position));
            tags.add(0, currArr[1]);


            int currIdx=children_k[0];
            while(true){
                currArr=network.getNodeArray(currIdx);
                if(currArr[2]!= RelationNetworkCompiler.NodeType.tag.ordinal()){
                    break;
                }
                position=network.getNodeArray(currIdx)[0];
                tags.add(0, currArr[1]);
                wordTokens.add(0, wt.get(position));
                currIdx=network.getChildren(currIdx)[0][0];
            }
            String longWordl="";
            String longTagl="";
            String longWordr="";
            String longTagr="";
            boolean relFound=false;
            int relStart=0;
            int relEnd= wt.size()-1;
            for(int i=0; i<wordTokens.size(); i++) {
                if (!relFound && tags.get(i) == 1) {
                    relStart = i;
                    relFound = true;
                }
                if (relFound && tags.get(i) == 0) {
                    relEnd = i - 1;
                }
            }

            if(relFound) {
                //Contextual Features
                longWordl = relStart - 1 >= 0 ? wt.get(relStart - 1).getForm() : "START-W";
                longTagl = relStart - 1 >= 0 ? wt.get(relStart - 1).getTag() : "START-PO";
                longWordr = relEnd + 1< wt.size() ?  wt.get(relEnd + 1).getForm() : "END-W";
                longTagr = relEnd + 1 < wt.size()  ? wt.get(relEnd + 1).getTag() : "END-PO";
                fs.add(_param_g.toFeature(network, FeatType.longContext.name()+"-wl", "REL" , longWordl));
                fs.add(_param_g.toFeature(network, FeatType.longContext.name()+"-wr", "REL" , longWordr));
                fs.add(_param_g.toFeature(network, FeatType.longContext.name()+"-tl", "REL" , longTagl));
                fs.add(_param_g.toFeature(network, FeatType.longContext.name()+"-tl", "REL" , longTagr));

                //Path-Based Features
                String arg1PathWords="";
                String arg1PathTags="";
                if(relStart>arg1Idx){
                    for(int i=arg1Idx+1; i<relStart; i++){
                        arg1PathWords=arg1PathWords+wt.get(i).getForm()+" ";
                        arg1PathTags=arg1PathTags+wt.get(i).getTag()+" ";
                    }
                }
                else{
                    for(int i=relEnd+1; i<arg1Idx; i++){
                        arg1PathWords=arg1PathTags+wt.get(i).getForm()+" ";
                        arg1PathTags=arg1PathTags+wt.get(i).getTag()+" ";
                    }
                }

                String arg2PathWords="";
                String arg2PathTags="";
                if(relStart>arg2Idx){
                    for(int i=arg2Idx+1; i<relStart; i++){
                        arg2PathWords=arg1PathWords+wt.get(i).getForm()+" ";
                        arg2PathTags=arg2PathTags+wt.get(i).getTag();
                    }
                }
                else{
                    for(int i=relEnd+1; i<arg2Idx; i++){
                        arg2PathWords=arg1PathWords+wt.get(i).getForm()+" ";
                        arg2PathTags=arg2PathTags+wt.get(i).getTag();
                    }
                }
                String arg12PathWords="";
                String arg12PathTags="";


                int arg12Start=Math.min(arg1Idx, arg2Idx);
                arg12Start = Math.min(arg12Start, relStart);

                int arg12End=Math.max(arg1Idx, arg2Idx);
                arg12End = Math.max(arg12End, relEnd);

                for(int i=arg12Start; i<=arg12End; i++){
                    arg12PathWords=arg12PathWords+wt.get(i).getForm() + " ";
                    arg12PathTags=arg12PathTags+wt.get(i).getTag() + " ";
                }

                fs.add(_param_g.toFeature(network, FeatType.longPath+"-w-arg1", "REL", arg1PathWords));
                fs.add(_param_g.toFeature(network, FeatType.longPath+"-t-arg1", "REL", arg1PathTags));
                fs.add(_param_g.toFeature(network, FeatType.longPath+"-w-arg2", "REL", arg2PathWords));
                fs.add(_param_g.toFeature(network, FeatType.longPath+"-t-arg2", "REL", arg2PathTags));
                fs.add(_param_g.toFeature(network, FeatType.longPath+"-w-arg12", "REL", arg12PathWords));
                fs.add(_param_g.toFeature(network, FeatType.longPath+"-w-arg12", "REL", arg12PathTags));


            }
        }

        return this.createFeatureArray(network, fs);
    }
}
