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
        NetworkConfig.L2_REGULARIZATION_CONSTANT=Double.parseDouble(args[1]);
        NetworkConfig.NUM_THREADS=Integer.parseInt(args[0]);
        NetworkConfig.PARALLEL_FEATURE_EXTRACTION = true;
        NetworkConfig.AVOID_DUPLICATE_FEATURES = true;
        RelationInstance[] insts=readData(processedFilePath, true);

        List<RelationInstance> trainInsts=new ArrayList<RelationInstance>();
        List<RelationInstance> testInsts=new ArrayList<RelationInstance>();
        for(int i=0; i<10; i++){
            trainInsts.add(insts[i]);
//            System.out.println(insts[i].isLabeled());
        }
        for(int i=20; i<insts.length; i++){
            testInsts.add(insts[i]);
        }
        RelationInstance[] trainInstsArr=trainInsts.toArray(new RelationInstance[trainInsts.size()]);
//        for(int i=0; i<trainInstsArr.length; i++){
//            System.out.println(trainInstsArr[i].isLabeled());
//        }
        GlobalNetworkParam gnp=new GlobalNetworkParam();
        LatentFeatureManager fman=new LatentFeatureManager(gnp);
        LatentNetworkCompiler networkCompiler=new LatentNetworkCompiler(relTypes);
        NetworkModel model= DiscriminativeNetworkModel.create(fman, networkCompiler);
        model.train(trainInsts.toArray(new RelationInstance[trainInsts.size()]), iterCount);
//        Instance[] results=model.decode(testInsts.toArray(new RelationInstance[testInsts.size()]));

    }
//    private void printOutputs(RelationInstance[] results){
//        for(int i=0; i<results.length; i++){
//            Output output=results[i].getOutput();
//            String relType=output.relType;
//            System.out.println(relType);
//            for(int j=0; j<results[i].getPrediction().)
//
//        }
//    }

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
