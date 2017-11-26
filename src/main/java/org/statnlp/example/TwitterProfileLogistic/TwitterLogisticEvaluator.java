package org.statnlp.example.TwitterProfileLogistic;

import org.statnlp.commons.types.Instance;

import java.util.*;

public class TwitterLogisticEvaluator {
    Instance[] insts;
    double precision;
    double recall;
    double fscore;
    Set<String> userEntity;
    public TwitterLogisticEvaluator(Instance[] insts, Set<String> userEntity, Map<String, Integer> groundTruth, boolean global){
        if(!global) {
            Map<String, Integer> predictionMap = new HashMap<String, Integer>();
            this.userEntity = userEntity;
            Iterator<String> itr = userEntity.iterator();
            String[] newUserEntity = userEntity.toArray(new String[userEntity.size()]);
//        System.out.println("SET:"+userEntity.size());

//        System.out.println("NEWUSER:"+newUserEntity.length);
            for (int i = 0; i < newUserEntity.length; i++) {
                predictionMap.put(newUserEntity[i], -1);
            }

            for (int i = 0; i < insts.length; i++) {
                if (insts[i].getPrediction().toString().equals("1")) {
                    TwitterInstance inst = (TwitterInstance) insts[i];
                    predictionMap.put(inst.input.user.split("\\$")[0] + inst.input.entity, 1);
                }
            }
            int tp = 0;
            int fp = 0;
            int tn = 0;
            int fn = 0;

            for (int i = 0; i < newUserEntity.length; i++) {
                int pred = predictionMap.get(newUserEntity[i]);
                int gold = groundTruth.get(newUserEntity[i]);
                if (pred == 1 && gold == -1) {
                    fp++;
                }
                if (pred == -1 && gold == 1) {
                    fn++;
                }
                if (pred == 1 && gold == 1) {
                    tp++;
                }
                if (pred == -1 && gold == -1) {
                    tn++;
                }
            }
            System.out.println("TP:" + tp);
            System.out.println("TN:" + tn);
            System.out.println("FP:" + fp);
            System.out.println("FN:" + fn);
            precision = tp * 100.0 / (tp + fp);
            recall = tp * 100.0 / (tp + fn);
            fscore = precision * recall * 2 / (precision + recall);
        }
        else{
            int tp=0;
            int fp=0;
            int tn=0;
            int fn=0;
//            System.out.println("COUNT::"+insts.length+"\n");
            for(int i=0; i<insts.length; i++){
                String pred=insts[i].getPrediction().toString();
                String gold=insts[i].getOutput().toString();
                if(gold.equals("1") && pred.equals("1")){
                    tp++;
                }
                else if(gold.equals("1") && pred.equals("0")){
                    fn++;
                }
                else if(gold.equals("-1") && pred.equals("1")){
                    fp++;
                }
                else if(gold.equals("-1") && pred.equals("0")){
                    tn++;
                }
            }
            System.out.println("TP:" + tp);
            System.out.println("TN:" + tn);
            System.out.println("FP:" + fp);
            System.out.println("FN:" + fn);
            precision=tp*100.0/(tp+fp);
            recall=tp*100.0/(tp+fn);
            fscore=2*precision*recall/(precision+recall);
        }

    }

}
