package org.statnlp.example.RelDescriptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.statnlp.commons.io.RAWF;
import org.statnlp.commons.types.Instance;
import org.statnlp.commons.types.WordToken;
import org.statnlp.hypergraph.DiscriminativeNetworkModel;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.NetworkConfig;
import org.statnlp.hypergraph.NetworkModel;

public class RelMain {
    private static int fileId;
    private static List<String> partialAccuracies=new ArrayList<>();
    private static List<String> completeAccuracies=new ArrayList<>();
    private static List<String> partialPrecs=new ArrayList<>();
    private static List<String> completePrecs=new ArrayList<>();
    private static List<String> partialRecs=new ArrayList<>();
    private static List<String> completeRecs=new ArrayList<>();
    private static String trainFilePath="data/RelDataSet/nyt_train";
    private static String testFilePath="data/RelDataSet/nyt_test";
    private static int trainNum=1000;
    private static int testNum=1000;
    private static int iterCount=1000;
    private static int threadCount=8;
    private static List<String> RELTags=new ArrayList<String>();
    public static void main(String...args) throws IOException, InterruptedException{
        fileId=Integer.parseInt(args[0]);;

        threadCount=Integer.parseInt(args[1]);;
        boolean oneFile=Boolean.parseBoolean(args[2]);
        NetworkConfig.L2_REGULARIZATION_CONSTANT=Double.parseDouble(args[3]);
        NetworkConfig.NUM_THREADS=threadCount;
        NetworkConfig.PARALLEL_FEATURE_EXTRACTION = true;
        NetworkConfig.AVOID_DUPLICATE_FEATURES = true;

        int i=1;
        if(oneFile){
            i=fileId;
        }
        for(; i<=fileId; i++){
            RelInstance[] trainInsts=readData(trainFilePath+i, true, trainNum);

            GlobalNetworkParam gnp=new GlobalNetworkParam();
            RelFeatureManager fman=new RelFeatureManager(gnp);
            RelNetworkCompiler compiler=new RelNetworkCompiler(RELTags);
            for(int j=0; j<RELTags.size(); j++){
                System.out.println("id: " + j + ", tag:" + RELTags.get(j));
            }
            NetworkModel model= DiscriminativeNetworkModel.create(fman, compiler);
            model.train(trainInsts, iterCount);

            RelInstance testInsts[]=readData(testFilePath+i, false, testNum);
            Instance[] results=model.decode(testInsts);


            int partial[]={0,0,0,0};
            int complete[]={0,0,0,0};
            for(Instance res:results){
                RelInstance inst=(RelInstance) res;
                List<String> gold=inst.getOutput();
                List<String> pred=inst.getPrediction();
                Evaluator eval=new Evaluator(gold,pred);
                for(int k=0; k<complete.length; k++){
                    complete[k]+=eval.complete[k];
                    partial[k]+=eval.partial[k];
                }
            }
            int partialTP=partial[0]; int partialTN=partial[1]; int partialFP=partial[2]; int partialFN=partial[3];
            System.out.println("TP\tTN\tFP\tFN");
            double pprecision=(partialTP*100.0/(partialTP+partialFP));
            double precall=(partialTP*100.0/(partialTP+partialFN));
            double paccuracy=(partialTP+partialTN)*100.0/(partialTP+partialTN+partialFP+partialFN);
            partialPrecs.add(pprecision+"");
            partialRecs.add(precall+"");
            partialAccuracies.add(paccuracy+"");

            int completeTP=complete[0];int completeTN=complete[1];int completeFP=complete[2]; int completeFN=complete[3];
            double cprecision=(completeTP*100.0)/(completeTP+completeFP);
            double crecall=(completeTP*100.0)/(completeTP+completeFN);
            double caccuracy=(completeTP+completeTN)*100.0/(completeTP+completeTN+completeFP+completeFN);
            completePrecs.add(cprecision+"");
            completeRecs.add(crecall+"");
            completeAccuracies.add(caccuracy+"");
        }
        double partialAccuracyAverage=0.0;
        double partialPrecAverage=0.0;
        double partialRecAverage=0.0;

        double completeAccuracyAverage=0.0;
        double completePrecAverage=0.0;
        double completeRecAverage=0.0;
        int limit=fileId;
        if(oneFile){
            limit=1;
        }
        System.out.println("PARTIAL:");
        System.out.println("ACCURACY\tPRECISION\tRECALL");
        for(i=0; i<limit; i++){
            System.out.println(partialAccuracies.get(i)+"\t"+partialPrecs.get(i)+"\t"+partialRecs.get(i));
            partialAccuracyAverage+=Double.parseDouble(partialAccuracies.get(i));
            partialPrecAverage+=Double.parseDouble(partialPrecs.get(i));
            partialRecAverage+=Double.parseDouble(partialRecs.get(i));
        }
        partialAccuracyAverage/=limit;
        partialPrecAverage/=limit;
        partialRecAverage/=limit;
        System.out.println(partialAccuracyAverage+"\t"+partialPrecAverage+"\t"+partialRecAverage+"\n\n");

        System.out.println("COMPLETE:");
        System.out.println("ACCURACY\tPRECISION\tRECALL");
        for(i=0; i<limit; i++){
            System.out.println(completeAccuracies.get(i)+"\t"+completePrecs.get(i)+"\t"+completeRecs.get(i));
            completeAccuracyAverage+=Double.parseDouble(completeAccuracies.get(i));
            completePrecAverage+=Double.parseDouble(completePrecs.get(i));
            completeRecAverage+=Double.parseDouble(completeRecs.get(i));
        }
        completeAccuracyAverage/=limit;
        completePrecAverage/=limit;
        completeRecAverage/=limit;
        System.out.println(completeAccuracyAverage+"\t"+completePrecAverage+"\t"+completeRecAverage+"\n\n");
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
