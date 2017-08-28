package org.statnlp.example.RelDescriptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.statnlp.commons.io.RAWF;
import org.statnlp.commons.types.Instance;
import org.statnlp.commons.types.WordToken;
import org.statnlp.hypergraph.DiscriminativeNetworkModel;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.NetworkConfig;
import org.statnlp.hypergraph.NetworkModel;

public class RelMain {
    private static int fileId;
    private static double[][] allMetrics = new double[2][4];
    private static String dataFile="data/RelDataSet/nyt.txt";
    private static int dataNum=1000;
    private static int iterCount=1000;
    private static int threadCount=8;
    private static List<String> RELTags=new ArrayList<String>();
    public static long randomSeed = 1234;
    
    public static void main(String...args) throws IOException, InterruptedException{
        fileId=Integer.parseInt(args[0]);;
//        trainNum=Integer.parseInt(args[1]);
//        testNum=Integer.parseInt(args[2]);
        threadCount=Integer.parseInt(args[1]);;
        boolean oneFile=Boolean.parseBoolean(args[2]);
        NetworkConfig.L2_REGULARIZATION_CONSTANT=Double.parseDouble(args[3]);
        NetworkConfig.NUM_THREADS=threadCount;
        NetworkConfig.PARALLEL_FEATURE_EXTRACTION = true;
        NetworkConfig.AVOID_DUPLICATE_FEATURES = true;
        Evaluator evaluator = new Evaluator();
        int i= oneFile ? fileId :  0;
        
        Random rand = new Random(randomSeed);
        Instance[] allInstances =readData(dataFile, true, dataNum);
        allInstances = shuffle(allInstances, rand);
        
        Instance[][] splits = splitIntoKFolds(allInstances, fileId);
        for(int j = 0; j < RELTags.size(); j++){
            System.out.println("id: " + j + ", tag:" + RELTags.get(j));
        }
        
        for(; i < fileId; i++){
        	Instance[] testData = splits[fileId - i - 1];
			for(Instance inst : testData) {
				inst.setUnlabeled();
			}
			Instance[] trainData = new Instance[allInstances.length - testData.length];
			int idx = 0;
			for (int t = 0; t < fileId; t++) {
				if (t == fileId - i - 1) continue;
				for (int k = 0; k < splits[t].length; k++) {
					splits[t][k].setLabeled();
					splits[t][k].setPrediction(null);
					trainData[idx++] = splits[t][k];
				}
			}
            GlobalNetworkParam gnp=new GlobalNetworkParam();
            RelFeatureManager fman=new RelFeatureManager(gnp);
            RelNetworkCompiler compiler=new RelNetworkCompiler(RELTags);
            
            NetworkModel model = DiscriminativeNetworkModel.create(fman, compiler);
            model.train(trainData, iterCount);

            Instance[] results = model.decode(testData);
            
            double[][] metrics = evaluator.evaluate(results);
            for (int m = 0; m < metrics.length; m++) {
            	for (int j = 0; j < metrics[m].length; j++) {
            		allMetrics[m][j] += metrics[m][j];
            	}
            }
            System.out.printf("[curr Partial] Acc.: %.3f\tPrec.: %.3f%%\tRec.: %.3f%%\tF1.: %.3f%%\n", metrics[0][0], metrics[0][1], metrics[0][2], metrics[0][3]);
            System.out.printf("[curr Complete] Acc.: %.3f\tPrec.: %.3f%%\tRec.:%.3f%%\tF1.: %.3f%%\n", metrics[1][0], metrics[1][1], metrics[1][2], metrics[0][3]);
        }
        int limit = oneFile ? 1 : fileId;
        double avgPartialAcc = allMetrics[0][0]/ limit;
        double avgPartialPrec = allMetrics[0][1] / limit;
        double avgPartialRec = allMetrics[0][2] / limit;
        double avgPartialF1 = allMetrics[0][3] / limit;
        double avgCompAcc = allMetrics[1][0] / limit;
        double avgCompPrec = allMetrics[1][1] / limit;
        double avgCompRec = allMetrics[1][2] / limit;
        double avgCompF1 = allMetrics[1][3] / limit;
        System.out.printf("[Avg Partial] Acc.: %.3f\tPrec.: %.3f%%\tRec.: %.3f%%\tF1.: %.3f%%\n", avgPartialAcc, avgPartialPrec, avgPartialRec, avgPartialF1);
        System.out.printf("[Avg Complete] Acc.: %.3f\tPrec.: %.3f%%\tRec.: %.3f%%\tF1.: %.3f%%\n", avgCompAcc, avgCompPrec, avgCompRec, avgCompF1);
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
        	if (line.startsWith("NYT-") || line.startsWith("Wikipedia-"))  continue; //sentence ID number
            if(line.length()>0){
                String[] line_split=line.split("\\t");
                String word=line_split[1];
                String posTag=line_split[2];
                String phraseTag=line_split[3];
                String reltag=line_split[4];
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
    
    public static Instance[][] splitIntoKFolds(Instance[] allInsts, int k) {
		Instance[][] splits = new Instance[k][];
		int num = allInsts.length / k; 
		for (int f = 0; f < k; f++) {
			int currNum = f != k - 1 ? num : allInsts.length - (k - 1) * num;
			splits[f] = new Instance[currNum];
			for (int i = 0; i < currNum; i++) {
				splits[f][i] = allInsts[f * num + i];
			}
			System.out.printf("[Info] Fold %d: %d\n", (f+1), splits[f].length);
		}
		return splits;
	}
    
    public static Instance[] shuffle(Instance[] insts, Random rand) {
		List<Instance> list = new ArrayList<>(insts.length);
		for (int i = 0; i < insts.length; i++) {
			list.add(insts[i]);
		}
		Collections.shuffle(list, rand);
		System.out.println("[Info] shuffle insts:"+list.size());
		return list.toArray(new Instance[list.size()]);
	}
}
