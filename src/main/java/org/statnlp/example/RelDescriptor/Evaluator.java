package org.statnlp.example.RelDescriptor;

import java.util.List;

public class Evaluator {
    private List<String> gold;
    private List<String> pred;
    private boolean partialFlag = false;
    private boolean completeFlag = false;
    private boolean goldRelFlag = false;
    private boolean predRelFlag=false;
    public int complete[]={0,0,0,0};
    public int partial[]={0,0,0,0};

    Evaluator(List<String> gold, List<String> pred) {
        for(int i=0; i<gold.size(); i++){
            if(gold.get(i).equals("B-R") || gold.get(i).equals("I-R")) {
                goldRelFlag = true;
                break;
            }
        }
        for(int i=0; i<gold.size(); i++) {
            if (pred.get(i).equals("B-R") || pred.get(i).equals("I-R")) {
                predRelFlag = true;
                break;
            }
        }
        //TP, TN, FP, FN
        if(!goldRelFlag && !predRelFlag){
            complete[1]=1; //True Negative
            partial[1]=1; //True Negative
        }
        else if(goldRelFlag && !predRelFlag){
            complete[3]=1; //False Negative
            partial[3]=1; //False Negative
        }
        else if(!goldRelFlag && predRelFlag){
            complete[2]=1; //False Positive
            partial[2]=1; //False Positive
        }
        //goldRelFlag==true && predRelFlag==true
        else if(goldRelFlag && predRelFlag) {
            completeFlag = true;
            partialFlag = false;
            for (int i = 0; i < gold.size(); i++) {
                if (!gold.get(i).equals(pred.get(i))) {
                    completeFlag = false;
                    break;
                }
            }
            for (int i = 0; i < gold.size(); i++) {
                if ((gold.get(i).equals("B-R") || gold.get(i).equals("I-R")) && gold.get(i).equals(pred.get(i))) {
                    partialFlag = true;
                    break;
                }
                if (completeFlag) {
                    complete[0] = 1; //True Positive
                } else {
                    complete[2] = 1; //False Positive
                }
                if (partialFlag) {
                    partial[0] = 1; //True Positive
                } else {
                    partial[2] = 1; //False Positive
                }
            }
        }

    }
}
