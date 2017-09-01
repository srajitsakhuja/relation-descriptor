package org.statnlp.example.RelationLatent;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.statnlp.commons.io.RAWF;
import org.statnlp.commons.ml.opt.OptimizerFactory;
import org.statnlp.commons.types.Instance;
import org.statnlp.commons.types.WordToken;
import org.statnlp.hypergraph.DiscriminativeNetworkModel;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.NetworkConfig;
import org.statnlp.hypergraph.NetworkModel;
import org.statnlp.hypergraph.neural.GlobalNeuralNetworkParam;
import org.statnlp.hypergraph.neural.NeuralNetworkCore;

public class RelationLatentMain {
    private static String trainPath="data/sem-eval/trainFileProcessed.txt";
    private static String testPath="data/sem-eval/testFileProcessed.txt";
    private static List<String> relTypes=new ArrayList<String>();
    private static int iterCount=1000;
    private static int trainNum = 2;
    private static int testNum = 2;

    public static void main(String...args) throws IOException, InterruptedException{
    	NetworkConfig.NUM_THREADS = 5;
        NetworkConfig.L2_REGULARIZATION_CONSTANT= 0.01;

        NetworkConfig.PARALLEL_FEATURE_EXTRACTION = true;
        NetworkConfig.AVOID_DUPLICATE_FEATURES = true;
        NetworkConfig.USE_NEURAL_FEATURES = true;

        //Importing test and train data
        RelationInstance[] trainInsts=readData(trainPath, true, trainNum);
        RelationInstance[] testInsts=readData(testPath, false, testNum);
        System.out.println(trainInsts.length);
        System.out.println(testInsts.length);

        System.out.println(relTypes.toString());
        List<NeuralNetworkCore> nets = new ArrayList<>();
        if (NetworkConfig.USE_NEURAL_FEATURES) {
        	nets.add(new RelationLSTM(100, 2 * relTypes.size() + 1, -1, "random"));
        }
        GlobalNeuralNetworkParam gnnp = new GlobalNeuralNetworkParam(nets);
        GlobalNetworkParam gnp=new GlobalNetworkParam(OptimizerFactory.getLBFGSFactory() ,gnnp);

        LatentFeatureManager fman=new LatentFeatureManager(gnp);
        LatentNetworkCompiler networkCompiler=new LatentNetworkCompiler(relTypes);
        NetworkModel model= DiscriminativeNetworkModel.create(fman, networkCompiler);
        model.train(trainInsts, iterCount);
        Instance[] results=model.decode(testInsts);
        LatentEvaluator eval=new LatentEvaluator(results);
        double metrics[]=eval.metrics;
        System.out.println("Precision:"+metrics[0]+" Recall:"+metrics[1]+" F1-score:"+metrics[2]+" Accuracy:"+metrics[3]);

    }


    public static RelationInstance[] readData(String processedFilePath, boolean isTraining, int lim) throws IOException{
        BufferedReader br= RAWF.reader(processedFilePath);
        String line=null;
        List<RelationInstance> insts=new ArrayList<RelationInstance>();
        int lnum=0;
        List<WordToken> wts=new ArrayList<WordToken>();
        int e1Start=-1;
        int e1End=-1;
        int e2Start=-1;
        int e2End=-1;
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
                    e1Start=Integer.parseInt(line.split("/")[0].split("[*]")[0]);
                    e1End=Integer.parseInt(line.split("/")[0].split("[*]")[1]);
                    e2Start=Integer.parseInt(line.split("/")[1].split("[*]")[0]);
                    e2End=Integer.parseInt(line.split("/")[1].split("[*]")[1]);
                }
                else if(lnum%5==4){
                    count++;
                    if(count>lim){break;}
                    Output output=new Output(line, wts.size());
                    if(!relTypes.contains(output.relType) && isTraining){
                        relTypes.add(output.relType);
                    }
                    Input input= new Input(e1Start, e1End, e2Start, e2End,  wts);
                    wts=new ArrayList<WordToken>();
                    RelationInstance inst=new RelationInstance(count, 1.0,input, output);
                    if(isTraining){
                        inst.setLabeled();
                    }
                    else{
                        inst.setUnlabeled();
                    }
                    insts.add(inst);
                    if (insts.size() == lim) {
                    	break;
                    }
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
        br.close();
        return insts.toArray(new RelationInstance[insts.size()]);
    }

}
