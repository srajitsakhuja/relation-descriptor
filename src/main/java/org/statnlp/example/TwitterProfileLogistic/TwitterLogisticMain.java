package org.statnlp.example.TwitterProfileLogistic;

import org.statnlp.commons.io.RAWF;
import org.statnlp.commons.ml.opt.OptimizerFactory;
import org.statnlp.commons.types.WordToken;
import org.statnlp.hypergraph.DiscriminativeNetworkModel;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.NetworkConfig;
import org.statnlp.hypergraph.NetworkModel;
import scala.Int;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TwitterLogisticMain {
    private static String trainFile="data/TwitterDataSet/education_training.txt";
    private static String testFile="data/TwitterDataSet/education_testing.txt";
    private static int iterCount=1000;
    public static void main(String...args) throws IOException, InterruptedException{
        TwitterInstance[] trainInsts=readData(trainFile, true);
//        for(int i=0; i<trainInsts.length; i++){
//            trainInsts[i].toString();
//        }
        NetworkModel model=null;
        NetworkConfig.PARALLEL_FEATURE_EXTRACTION = true;
        NetworkConfig.AVOID_DUPLICATE_FEATURES = true;
        GlobalNetworkParam gnp= new GlobalNetworkParam(OptimizerFactory.getLBFGSFactory());
        TwitterLogisticFeatureManager fman = new TwitterLogisticFeatureManager(gnp);
        TwitterLogisticNetworkCompiler networkCompiler = new TwitterLogisticNetworkCompiler();
        model = DiscriminativeNetworkModel.create(fman, networkCompiler);
        model.train(trainInsts, iterCount);
    }
    public static void findEntityString(List<WordToken> wts, String entityString, List<Integer> eStart, List<Integer> eEnd){
//        eStart=new ArrayList<Integer>();
//        eEnd=new ArrayList<Integer>();

        String[] e=entityString.split(" ");
        for(int i=0; i<wts.size(); i++){

            if(wts.get(i).getForm().equals(e[0])){
                int j;
                for(j=1; j<e.length && i+j<wts.size(); j++){
                    if(!e[j].equals(wts.get(i+j).getForm())){
                        break;
                    }

                }
                if(j==e.length){
                    eStart.add(i);
                    eEnd.add(i+j-1);
                }
            }
        }
    }
    public static TwitterInstance[] readData(String textFname, boolean isTraining) throws IOException{
        List<TwitterInstance> insts=new ArrayList<TwitterInstance>();
        BufferedReader br=RAWF.reader(trainFile);
        String line=null;
        int count=0;
        String userString="";
        List<WordToken> wts=new ArrayList<WordToken>();
        String entityString="";
        int out=-1;
        int instanceId=0;
        List<Integer> eStart;
        List<Integer> eEnd;
        while((line=br.readLine())!=null) {
            count++;
            if(instanceId>1){
                break;
            }
            if(count%4==1){
                userString=new String(line);
            }
            else if(count%4==2){
                wts=new ArrayList<WordToken>();
                if(line.charAt(1)=='-'){
                    out=-1;
                }
                else{
                    out=1;
                }
                line=line.substring(4);
                String line_split[]=line.split(" ");
                for(String word:line_split){
                    WordToken wt=new WordToken(word, "TAG");
                    wts.add(wt);
                }
            }
            else if(count%4==3) {
                entityString = new String(line);
                eStart=new ArrayList<Integer>();
                eEnd=new ArrayList<Integer>();
                findEntityString(wts,entityString, eStart, eEnd);
                TwitterInput input = new TwitterInput(userString, wts, entityString, eStart, eEnd);
                instanceId++;
                TwitterInstance inst=new TwitterInstance(instanceId, 1.0, input,out);
                if(isTraining){inst.setLabeled();}
                else{inst.setUnlabeled();}
//                inst.toString();
                insts.add(inst);
            }
        }
        return insts.toArray(new TwitterInstance[insts.size()]);
    }
}
