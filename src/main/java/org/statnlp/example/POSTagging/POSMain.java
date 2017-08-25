package org.statnlp.example.POSTagging;

import org.statnlp.commons.io.RAWF;
import org.statnlp.commons.types.Instance;
import org.statnlp.commons.types.WordToken;
import org.statnlp.hypergraph.DiscriminativeNetworkModel;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.NetworkConfig;
import org.statnlp.hypergraph.NetworkModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class POSMain {
    public static List<String> POStags;
    public static String trainFile="/Users/srajitsakhuja/Downloads/statnlp-core-master-7aa7077e35db2ad0d8c4f3e3d69329fbf12fee6e/data/train.txt";
    public static String testFile="/Users/srajitsakhuja/Downloads/statnlp-core-master-7aa7077e35db2ad0d8c4f3e3d69329fbf12fee6e/data/test.txt";
    public static int trainNum=100;
    public static int testNum=5;

    public static int iterCount=1000;
    public static void main(String...args) throws IOException, InterruptedException {
        POStags=new ArrayList<String>();
        POSInstance trainInsts[]=readData(trainFile, true, trainNum);
        NetworkConfig.L2_REGULARIZATION_CONSTANT=0.01;
        NetworkConfig.NUM_THREADS=8;
        GlobalNetworkParam gnp=new GlobalNetworkParam();
        POSFeatureManager fman=new POSFeatureManager(gnp);
        POSNetworkCompiler compiler=new POSNetworkCompiler(POStags);
        NetworkModel model= DiscriminativeNetworkModel.create(fman, compiler);
        model.train(trainInsts, iterCount);

        POSInstance testInsts[]=readData(testFile, false, testNum);
        Instance[] results=model.decode(testInsts);
        int total=0;
        int correct=0;
        for(Instance res:results){
            POSInstance inst=(POSInstance)res;
            List<String> gold=inst.getOutput();
            List<String> pred=inst.getPrediction();

            for(int i=0; i<gold.size(); i++){
                if(gold.get(i).equals(pred.get(i))){
                    correct++;
                }
                total++;
            }
        }
        System.out.printf("Accuracy:%f", correct*100.0/total);

    }

    public static POSInstance[] readData(String path, boolean isTraining, int number) throws IOException{
        BufferedReader br= RAWF.reader(path);
        String line=null;
        List<POSInstance> insts=new ArrayList<POSInstance>();
        int index=1;
        List<String> words=new ArrayList<String>();
        List<String> tags=new ArrayList<String>();
        int count=0;
        while((line=br.readLine())!=null){
            if(line.length()==0){
                count+=1;
                POSInstance inst=new POSInstance(index++, 1.0, words, tags);
                if(isTraining){
                    inst.setLabeled();
                }
                else{
                    inst.setUnlabeled();
                }
                insts.add(inst);
                words=new ArrayList<String>();
                tags=new ArrayList<String>();
                if(number!=-1 && insts.size()==number){
                    break;
                }
                continue;
            }
            String values[]=line.split(" ");
            /*System.out.println(line);
            System.out.printf("\n");
            System.out.printf(values[1]);
            System.out.printf("split\n");*/
            String tag=values[1];
            if(isTraining && !POStags.contains(tag)){
                POStags.add(tag);
            }
            words.add(values[0]);
            tags.add(tag);
        }
        System.out.print(count);
        br.close();
        List<POSInstance> myInsts=insts;
        System.out.println("Instance:"+myInsts.size());

        return myInsts.toArray(new POSInstance[myInsts.size()]);
    }

}
