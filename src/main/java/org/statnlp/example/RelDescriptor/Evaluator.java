package org.statnlp.example.RelDescriptor;

import java.util.List;

import org.statnlp.commons.types.Instance;

public class Evaluator {

    public Evaluator () {
    	
    }
    
    /**
     * Pass the instance return the metrics
     * @param insts
     * @return [0][]: partial, [1][]: complete
     * [0][0]: partialAcc, [0][1]: partialPrec, [0][2]: partialRec, [0][3]: partialF1
     * [1][0]: compAcc, [1][1]: compPrec, [1][2]: compRec, [1][3]: compF1
     */
    public double[][] evaluate(Instance[] results) {
    	double[][] values = new double[2][4];
    	for(Instance res : results){
		    RelInstance inst=(RelInstance) res;
		    List<String> gold=inst.getOutput();
		    List<String> pred=inst.getPrediction();
		    double[][] oneInstValues = this.evaluateOneInstance(gold, pred);
		    for (int i = 0; i < oneInstValues.length; i++) {
		    	for(int k = 0; k < oneInstValues[i].length; k++){
		    		values[i][k] += oneInstValues[i][k];
		        }
		    }
		}
    	double[][] metrics = new double[2][4];
    	for (int i = 0; i < metrics.length; i++) {
    		//acc.
    		metrics[i][0] = (values[i][0] + values[i][1]) * 100.0/ (values[i][0] + values[i][1] + values[i][2] + values[i][3]);
    		//precision:
    		metrics[i][1] = values[i][0] * 100.0 / (values[i][0] + values[i][2]);
    		//recall
    		metrics[i][2] = values[i][0] * 100.0 / (values[i][0] + values[i][3]);
    		//f1
    		metrics[i][3] = 2.0 * values[i][0] * 100 / (2 * values[i][0] + values[i][2] + values[i][3]);
    	}
    	return metrics;
    }
    
    /**
     * Evaluate one instance and return the values for TP,TN, FP, FN.
     * @param gold
     * @param pred
     * @return [0][]: partial, [1][]: complete
     * [0][0]: partialTP, [0][1]: partialTN, [0][2]: partialFP, [0][3]: partialFN
     * [1][0]: compTP, [1][1]: compTN, [1][2]: compTP, [1][3]: compFN
     */
    private double[][] evaluateOneInstance(List<String> gold, List<String> pred) {
    	double[][] values = new double[2][4];
        boolean partialFlag = false;
        boolean completeFlag = false;
        boolean goldRelFlag = false;
        boolean predRelFlag=false;
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
            values[1][1]=1; //True Negative
            values[0][1]=1; //True Negative
        }
        else if(goldRelFlag && !predRelFlag){
        	values[1][3]=1; //False Negative
            values[0][3]=1; //False Negative
        }
        else if(!goldRelFlag && predRelFlag){
        	values[1][2]=1; //False Positive
            values[0][2]=1; //False Positive
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
            }
            if (completeFlag) {
            	values[1][0] = 1; //True Positive
            } else {
            	values[1][2] = 1; //False Positive
            }
            if (partialFlag) {
            	values[0][0] = 1; //True Positive
            } else {
            	values[0][2] = 1; //False Positive
            }
         }
        return values;
    }

}

