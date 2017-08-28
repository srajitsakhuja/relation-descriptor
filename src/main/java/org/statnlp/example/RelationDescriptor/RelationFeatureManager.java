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
        int arg1Idx=inst.getInput().arg1Idx;
        int arg2Idx=inst.getInput().arg2Idx;

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
            List<Integer> relTags=new ArrayList<Integer>();
            int currIdx=parent_k;
            int[] currArr=network.getNodeArray(currIdx);
            relTags.add(0, currArr[1]);
            currIdx=children_k[0];
            currArr=network.getNodeArray(currIdx);
            while(true){
                if(currArr[2]== RelationNetworkCompiler.NodeType.leaf.ordinal()){
                    break;
                }
                relTags.add(0, currArr[1]);
                currIdx=network.getChildren(currIdx)[0][0];
                currArr=network.getNodeArray(currIdx);
            }
            int relStart=-1;
            int relEnd=-1;
            boolean relFound=false;
            int prev=0;

            for(int i=0; i<inst.size(); i++){
                if(relTags.get(i)!=0){
                    relFound=true;
                    relStart=i;
                }
                if(relTags.get(i)==0 && prev!=0){
                    relEnd=i-1;
                }
                prev=relTags.get(i);
            }


            if(relFound){
                if(relEnd==-1){relEnd=inst.size()-1;}
                //CONTEXT-FEATURES
                String longWl=relStart>0?wt.get(relStart-1).getForm():"START-W";
                String longPOl=relStart>0?wt.get(relStart-1).getTag():"START-P";
                String longWr=(relEnd+1)<inst.size()?wt.get(relEnd+1).getForm():"END-W";
                String longPOr=(relEnd+1)<inst.size()?wt.get(relEnd+1).getTag():"END-P";
                fs.add(_param_g.toFeature(network, FeatType.longContext+"-w-1", "REL", longWl));
                fs.add(_param_g.toFeature(network, FeatType.longContext+"-po-1", "REL", longPOl));
                fs.add(_param_g.toFeature(network, FeatType.longContext+"-w+1", "REL", longWr));
                fs.add(_param_g.toFeature(network, FeatType.longContext+"-po+1", "REL", longPOr));

                //PATH BASED FEATURES
                String wPath1=""; //words between the rel and arg1
                String wPath2=""; //words between the rel and arg2
                String wPath3=""; //words containing rel, arg1 and arg2
                String poPath1=""; //POStags between the rel and arg1
                String poPath2=""; //POStags between the rel and arg2
                String poPath3=""; //POStags containing rel, arg1 and arg2

                int pathStart=-1;
                int pathEnd=-1;

                if(arg1Idx>relEnd){
                    pathStart=relEnd+1;
                    pathEnd=arg1Idx-1;
                }
                else{
                    pathStart=arg1Idx+1;
                    pathEnd=relStart-1;
                }
                for(int i=pathStart; i<=pathEnd; i++){
                    wPath1=wPath1+wt.get(i).getForm();
                    poPath1=poPath1+wt.get(i).getTag();
                }

                if(arg2Idx>relEnd){
                    pathStart=relEnd+1;
                    pathEnd=arg2Idx-1;
                }
                else{
                    pathStart=arg2Idx+1;
                    pathEnd=relStart-1;
                }
                for(int i=pathStart; i<=pathEnd; i++){
                    wPath2=wPath2+wt.get(i).getForm();
                    poPath2=poPath2+wt.get(i).getTag();
                }

                if(arg1Idx>arg2Idx){
                    if(relStart<arg1Idx){
                        pathStart=relStart;
                        pathEnd=arg2Idx;
                    }
                    else{
                        pathStart=arg1Idx;
                        if(relStart<arg2Idx){
                            pathEnd=arg2Idx;
                        }
                        else{
                            pathEnd=relEnd;
                        }
                    }
                }
                else{
                    if(relStart<arg2Idx){
                        pathStart=relStart;
                        pathEnd=arg1Idx;
                    }
                    else{
                        pathStart=arg2Idx;
                        if(relStart<arg1Idx){
                            pathEnd=arg1Idx;
                        }
                        else{
                            pathEnd=relEnd;
                        }
                    }
                }

                for(int i=pathStart; i<=pathEnd; i++){
                    wPath3=wPath3+wt.get(i).getForm();
                    poPath3=poPath3+wt.get(i).getTag();
                }

                fs.add(_param_g.toFeature(network, FeatType.longPath+"-w-arg1", "REL", wPath1));
                fs.add(_param_g.toFeature(network, FeatType.longPath+"-w-arg2", "REL", wPath2));
                fs.add(_param_g.toFeature(network, FeatType.longPath+"-w-arg12", "REL", wPath3));
                fs.add(_param_g.toFeature(network, FeatType.longPath+"-po-arg1", "REL", poPath1));
                fs.add(_param_g.toFeature(network, FeatType.longPath+"-po-arg2", "REL", poPath2));
                fs.add(_param_g.toFeature(network, FeatType.longPath+"-po-arg12", "REL", poPath3));



            }
        }



        return this.createFeatureArray(network, fs);
    }
}
