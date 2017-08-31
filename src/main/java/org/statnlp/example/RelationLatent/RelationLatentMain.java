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
    private static List<String> relTypes=new ArrayList<String>();
    private static int iterCount=1000;
    private static int threadCount;
    private static double L2;

    public static void main(String...args) throws IOException, InterruptedException{
        //preprocessed file stored at processedFilePath
        //Preprocessor preprocessor=new Preprocessor(unprocessedFilePath, processedFilePath);
        NetworkConfig.NUM_THREADS=Integer.parseInt(args[0]);
        NetworkConfig.L2_REGULARIZATION_CONSTANT=Double.parseDouble(args[1]);
        NetworkConfig.PARALLEL_FEATURE_EXTRACTION = true;
        NetworkConfig.AVOID_DUPLICATE_FEATURES = true;
        RelationInstance[] insts=readData(processedFilePath, true);

        List<RelationInstance> trainInsts=new ArrayList<RelationInstance>();
        List<RelationInstance> testInsts=new ArrayList<RelationInstance>();
        for(int i=0; i<840; i++){
            trainInsts.add(insts[i]);
            System.out.println(trainInsts.get(i).input.wts.toString());
            System.out.println(trainInsts.get(i).output.relType);
            System.out.println();
        }
        for(int i=840; i<insts.length; i++){
            insts[i].setUnlabeled();
            testInsts.add(insts[i]);
        }

        GlobalNetworkParam gnp=new GlobalNetworkParam();
        LatentFeatureManager fman=new LatentFeatureManager(gnp);
        LatentNetworkCompiler networkCompiler=new LatentNetworkCompiler(relTypes);
        NetworkModel model= DiscriminativeNetworkModel.create(fman, networkCompiler);
        model.train(trainInsts.toArray(new RelationInstance[trainInsts.size()]), iterCount);
        Instance[] results=model.decode(testInsts.toArray(new RelationInstance[testInsts.size()]));
        LatentEvaluator eval=new LatentEvaluator(results);
        double metrics[]=eval.metrics;
        System.out.println("Precision:"+metrics[0]+" Recall:"+metrics[1]+" F1-score:"+metrics[2]+" Accuracy"+metrics[3]);
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
