package org.statnlp.example.RelationLatent;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import jdk.nashorn.internal.objects.Global;
import org.statnlp.commons.io.RAWF;
import org.statnlp.commons.types.WordToken;
import java.io.BufferedReader;
import edu.stanford.nlp.maxent.*;
import org.statnlp.hypergraph.DiscriminativeNetworkModel;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.NetworkConfig;
import org.statnlp.hypergraph.NetworkModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/*
*POS Tagging done using the Stanford CoreNLP POSTagger
*/
public class RelationLatentMain {
    private static String unprocessedFilePath="data/RelDataSet/sem-eval-task8.txt";
    private static String processedFilePath="data/RelDataSet/sem-eval-task8-processed.txt";
    private static List<String> relTypes=new ArrayList<String>();
    private static int iterCount=1000;
    private static int threadCount;
    private static double L2;
    public static void main(String...args) throws IOException, InterruptedException{
        //preprocessed file stored at processedFilePath
        //Preprocessor preprocessor=new Preprocessor(unprocessedFilePath, processedFilePath);
        NetworkConfig.L2_REGULARIZATION_CONSTANT=Double.parseDouble(args[1]);
        NetworkConfig.NUM_THREADS=Integer.parseInt(args[0]);
        NetworkConfig.PARALLEL_FEATURE_EXTRACTION = true;
        NetworkConfig.AVOID_DUPLICATE_FEATURES = true;
        RelationInstance[] insts=readData(processedFilePath, true);
        insts=posTagger(insts);
        List<RelationInstance> trainInsts=new ArrayList<RelationInstance>();
        List<RelationInstance> testInsts=new ArrayList<RelationInstance>();
        for(int i=0; i<700; i++){
            trainInsts.add(insts[i]);
        }
        for(int i=700; i<insts.length; i++){
            testInsts.add(insts[i]);
        }
        GlobalNetworkParam gnp=new GlobalNetworkParam();
        LatentFeatureManager fman=new LatentFeatureManager(gnp);
        LatentNetworkCompiler networkCompiler=new LatentNetworkCompiler(relTypes);
        NetworkModel model= DiscriminativeNetworkModel.create(fman, networkCompiler);
        model.train(trainInsts.toArray(new RelationInstance[trainInsts.size()]), iterCount);
    }

    private static RelationInstance[] posTagger(RelationInstance[] insts){
        MaxentTagger tagger=new MaxentTagger("/Users/srajitsakhuja/Downloads/postagger/models/english-bidirectional-distsim.tagger");
        for(int i=0; i<insts.length; i++){
            List<WordToken> wt=insts[i].input.wts;
            String sent="";
            for(int j=0; j<wt.size(); j++){
                sent=sent+wt.get(j).getForm()+" ";
            }
            String tagged=tagger.tagString(sent);
            String[] w_tag=tagged.split(" ");

            List<WordToken> newTokens=new ArrayList<WordToken>();
            for(int j=0; j<w_tag.length; j++){
                String form=w_tag[j].split("_")[0];
                String tag=w_tag[j].split("_")[1];
                WordToken token=new WordToken(form,tag);
                newTokens.add(token);
            }
            insts[i].input.wts=newTokens;
        }
        return insts;
    }

    public static RelationInstance[] readData(String processedFilePath, boolean isTraining) throws IOException{
        BufferedReader br= RAWF.reader(processedFilePath);
        String line=null;
        List<RelationInstance> insts=new ArrayList<RelationInstance>();
        int lnum=0;
        List<WordToken> wts=new ArrayList<WordToken>();
        int e1Start=-1;
        int e2Start=-1;
        int count=0;
        while((line=br.readLine())!=null){
            if(line.length()!=0){
                if(lnum%3==0){
                    String[] line_split=line.split(" ");
                    for(int i=0; i<line_split.length; i++){
                        WordToken wt=new WordToken(line_split[i], " ");
                        wts.add(wt);
                    }
                }
                else if(lnum%3==1){
                    e1Start=Integer.parseInt(line.split("/")[0]);
                    e2Start=Integer.parseInt(line.split("/")[1]);
                }
                else{
                    count++;
                    Output output=new Output(line, wts.size());
                    if(!relTypes.contains(output.relType) && isTraining){
                        relTypes.add(output.relType);
                    }
                    Input input=new Input(e1Start, e2Start, wts);
                    wts=new ArrayList<WordToken>();
                    RelationInstance inst=new RelationInstance(count, 1.0,input, output);
                    if(isTraining){
                        inst.setLabeled();
                    }
                    insts.add(inst);
                }
                lnum+=1;
            }
        }
        return insts.toArray(new RelationInstance[insts.size()]);
    }

}
