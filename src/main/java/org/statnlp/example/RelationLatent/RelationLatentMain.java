package org.statnlp.example.RelationLatent;
import jdk.nashorn.internal.objects.Global;
import org.statnlp.commons.io.RAWF;
import org.statnlp.commons.types.Instance;
import org.statnlp.commons.types.WordToken;
import java.io.BufferedReader;
import org.statnlp.hypergraph.DiscriminativeNetworkModel;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.NetworkConfig;
import org.statnlp.hypergraph.NetworkModel;

import javax.management.relation.Relation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RelationLatentMain {
    private static String unprocessedFilePath="data/RelDataSet/sem-eval-task8.txt";
    private static String processedFilePath="data/RelDataSet/sem-eval-task8-processed.txt";
    private static int fileStart=1;
    private static int fileEnd=10;
    private static String trainFPath="data/sem-eval/sem-eval-train";
    private static String testFPath="data/sem-eval/sem-eval-test";
    private static List<String> relTypes=new ArrayList<String>();
    private static int iterCount=1000;
    private static int threadCount;
    private static double L2;

    public static void main(String...args) throws IOException, InterruptedException{
        //Param initialisation
        fileStart=Integer.parseInt(args[0]);
        fileEnd=Integer.parseInt(args[1]);
        NetworkConfig.NUM_THREADS=Integer.parseInt(args[2]);
        NetworkConfig.L2_REGULARIZATION_CONSTANT=Double.parseDouble(args[3]);
        NetworkConfig.PARALLEL_FEATURE_EXTRACTION = true;
        NetworkConfig.AVOID_DUPLICATE_FEATURES = true;

        double prec_av=0.0;
        double rec_av=0.0;
        double f_av=0.0;
        double acc_av=0.0;

        for(int file=fileStart; file<=fileEnd; file++){
            String testPath=testFPath+file;
            String trainPath=trainFPath+file;
            RelationInstance[] trainInsts=readData(trainPath, true);
            RelationInstance[] testInsts=readData(testPath, false);
            System.out.println(trainInsts.length);
            System.out.println(testInsts.length);

            GlobalNetworkParam gnp=new GlobalNetworkParam();
            LatentFeatureManager fman=new LatentFeatureManager(gnp);
            LatentNetworkCompiler networkCompiler=new LatentNetworkCompiler(relTypes);
            NetworkModel model= DiscriminativeNetworkModel.create(fman, networkCompiler);
            model.train(trainInsts, iterCount);
            Instance[] results=model.decode(testInsts);
            LatentEvaluator eval=new LatentEvaluator(results);
            double metrics[]=eval.metrics;
            System.out.println("Precision:"+metrics[0]+" Recall:"+metrics[1]+" F1-score:"+metrics[2]+" Accuracy:"+metrics[3]);
            prec_av+=metrics[0]; rec_av+=metrics[1]; f_av+=metrics[2]; acc_av+=metrics[3];
        }
        prec_av/=(fileEnd-fileStart+1);
        rec_av/=(fileEnd-fileStart+1);
        f_av/=(fileEnd-fileStart+1);
        acc_av/=(fileEnd-fileStart+1);
        System.out.println("AVERAGE VALUES:");
        System.out.println("Precision:"+prec_av+" Recall:"+rec_av+" F1-score:"+f_av+" Accuracy:"+acc_av);


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
                if(lnum%5==1){
                    String[] line_split=line.split(" ");
                    for(int i=0; i<line_split.length; i++){
                        WordToken wt=new WordToken(line_split[i], " ");
                        wts.add(wt);
                    }
                }
                else if(lnum%5==3){
                    e1Start=Integer.parseInt(line.split("/")[0]);
                    e2Start=Integer.parseInt(line.split("/")[1]);
                }
                else if(lnum%5==4){
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
                    else{
                        inst.setUnlabeled();
                    }
                    insts.add(inst);
                }
                else if(lnum%5==2){
                    String[] tag_split=line.split(" ");
                    for(int i=0; i<tag_split.length; i++){
                        wts.get(i).setTag(tag_split[i]);
                    }
                }
                lnum+=1;
            }
        }
        return insts.toArray(new RelationInstance[insts.size()]);
    }

}
