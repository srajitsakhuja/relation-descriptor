package org.statnlp.example.RelDescriptor;

import jdk.internal.util.xml.impl.Input;
import org.statnlp.commons.io.RAWF;
import org.statnlp.commons.types.Instance;
import org.statnlp.commons.types.WordToken;
import org.statnlp.example.POSTagging.POSFeatureManager;
import org.statnlp.example.POSTagging.POSNetworkCompiler;
import org.statnlp.hypergraph.DiscriminativeNetworkModel;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.NetworkConfig;
import org.statnlp.hypergraph.NetworkModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RelMain {
    private static int fileId;
    private static List<String> partialAccuracies=new ArrayList<>();
    private static List<String> completeAccuracies=new ArrayList<>();
    private static String trainFilePath="data/RelDataSet/nyt_train";
    private static String testFilePath="data/RelDataSet/nyt_test";
    private static int trainNum=1000;
    private static int testNum=1000;
    private static int iterCount=1000;
    private static int threadCount=8;
    private static List<String> RELTags=new ArrayList<String>();
    public static void main(String...args) throws IOException, InterruptedException{
        fileId=Integer.parseInt(args[0]);
//        trainNum=Integer.parseInt(args[1]);
//        testNum=Integer.parseInt(args[2]);
        threadCount=Integer.parseInt(args[1]);
        boolean oneFile=Boolean.parseBoolean(args[2]);
        NetworkConfig.L2_REGULARIZATION_CONSTANT=Double.parseDouble(args[3]);

        int i=1;
        if(oneFile){
            i=fileId;
        }
        for(; i<=fileId; i++){
            RelInstance[] trainInsts=readData(trainFilePath+i, true, trainNum);
            NetworkConfig.NUM_THREADS=40;
            NetworkConfig.PARALLEL_FEATURE_EXTRACTION = true;
            NetworkConfig.AVOID_DUPLICATE_FEATURES = true;
            GlobalNetworkParam gnp=new GlobalNetworkParam();
            RelFeatureManager fman=new RelFeatureManager(gnp);
            RelNetworkCompiler compiler=new RelNetworkCompiler(RELTags);
            for(int j=0; j<RELTags.size(); j++){
                System.out.println(RELTags.get(j));
            }
            NetworkModel model= DiscriminativeNetworkModel.create(fman, compiler);
            model.train(trainInsts, iterCount);

            RelInstance testInsts[]=readData(testFilePath+i, false, testNum);
            Instance[] results=model.decode(testInsts);

            int totalCount=0;
            int partialCount=0;
            int completeCount=0;
            for(Instance res:results){
                RelInstance inst=(RelInstance) res;
                List<String> gold=inst.getOutput();
                List<String> pred=inst.getPrediction();
                Evaluator eval=new Evaluator(gold,pred);
                totalCount+=eval.totalCount;
                partialCount+=eval.partialCount;
                completeCount+=eval.completeCount;
            }
            //System.out.println("TOTAL"+totalCount);
            double partialAccuracy=100.0;
            double completeAccuracy=100.0;
            if(totalCount>0) {
                partialAccuracy = (double) partialCount / totalCount;
                completeAccuracy = (double) completeCount / totalCount;
            }
            //System.out.println(partialAccuracy);
            //System.out.println(completeAccuracy);
            partialAccuracies.add(partialAccuracy+"");
            completeAccuracies.add(completeAccuracy+"");
        }
        double partialAverage=0.0;
        double completeAverage=0.0;
        int limit=fileId;
        if(oneFile){
            System.out.println("HI");
            limit=1;
        }

        for(i=0; i<limit; i++){
            System.out.println(partialAccuracies.get(i)+" "+completeAccuracies.get(i));
            partialAverage+=Double.parseDouble(partialAccuracies.get(i));
            completeAverage+=Double.parseDouble(completeAccuracies.get(i));
        }
        partialAverage/=fileId;
        completeAverage/=fileId;
        System.out.println("CROSS VALIDATION ACCURACIES:");
        System.out.println("PARTIAL:"+partialAverage);
        System.out.println("COMPLETE:"+completeAverage);
    }

    public static RelInstance[] readData(String fpath, boolean isTraining, int limit) throws IOException{
        List<RelInstance> insts=new ArrayList<RelInstance>();
        String line=null;
        BufferedReader br= RAWF.reader(fpath);
        List<WordToken> wts=new ArrayList<WordToken>();
        int index=0;
        int arg1Idx=-1;
        int arg2Idx=-1;
        List<String> relTags=new ArrayList<String>();
        int count=0;
        while((line=br.readLine())!=null){
            if(line.length()>0){
                String[] line_split=line.split(" ");
                String word=line_split[0];
                String posTag=line_split[1];
                String phraseTag=line_split[2];
                String reltag=line_split[3];
                String relTag=reltag;

                WordToken wt=new WordToken(word, posTag);
                wt.setPhraseTag(phraseTag);
                wts.add(wt);
                if(word.equalsIgnoreCase("arg1")){
                    arg1Idx=index;
                }
                else if(word.equalsIgnoreCase("arg2")){
                    arg2Idx=index;
                }
                relTags.add(relTag);
                if(isTraining && !RELTags.contains(relTag)){
                    RELTags.add(relTag);
                }
                index++;
            }
            else{
                count++;
                if(count>limit){break;}
                InputData input=new InputData(wts, arg1Idx, arg2Idx);
                //System.out.println("COUNT"+count);
                RelInstance inst=new RelInstance(count, 1.0, input, relTags);
                if(isTraining){
                    inst.setLabeled();
                }
                else{
                    inst.setUnlabeled();
                }
                insts.add(inst);
                index=0;
                arg1Idx=-1;
                arg2Idx=-1;
                relTags=new ArrayList<String>();
                wts=new ArrayList<WordToken>();

            }
        }
        br.close();
        System.out.println("INSTANCE COUNT:"+insts.size());
        /*for(int i=0; i<insts.size(); i++){
            insts.get(i).toString();
        }*/
        return insts.toArray(new RelInstance[insts.size()]);
    }
}
