package org.statnlp.example.RelDescriptor;

import java.util.List;

public class Evaluator {
    private List<String> gold;
    private List<String> pred;
    private boolean partialFlag = false;
    private boolean completeFlag = false;
    private boolean relFlag = false;
    public int complete[]={0,0,0,0};
    public int partial[]={0,0,0,0};

    Evaluator(List<String> gold, List<String> pred) {
        for (int i = 0; i < gold.size(); i++) {
            System.out.print(gold.get(i) + "(" + i + ") ");
            if (!gold.get(i).equals("O")) {
                relFlag = true;
            }
        }
        System.out.println();
        for (int i = 0; i < pred.size(); i++) {
            System.out.print(pred.get(i) + "(" + i + ") ");
        }
        System.out.println();

        if (relFlag) {
            completeFlag = true;
            partialFlag = false;
            for (int i = 0; i < gold.size(); i++) {
                if ((gold.get(i).equals("B-R") || gold.get(i).equals("I-R")) && !gold.get(i).equals(pred.get(i))) {
                    completeFlag = false;
                    //System.out.println("Mis-Match found at:" + i);
                    break;
                }
            }
            for (int i = 0; i < gold.size(); i++) {
                if ( (gold.get(i).equals("B-R") || gold.get(i).equals("I-R")) && gold.get(i).equals(pred.get(i))) {
                    partialFlag = true;
                    //System.out.println("Match found at:" + i);
                }
            }
        }
        else {
            completeFlag = true;
            partialFlag = true;
            for (int i = 0; i < gold.size(); i++) {
                if (!gold.get(i).equals(pred.get(i))) {
                    completeFlag = false;
                    partialFlag = false;
                }
            }
        }
        System.out.println("COMPLETE:"+completeFlag+" PARTIAL:"+partialFlag);

        if(relFlag){
            if(completeFlag){
                complete[0]=1;
            }
            else{
                complete[2]=1;
            }
            if(partialFlag){
                partial[0]=1;
            }
            else{
                partial[2]=1;
            }
        }
        else{
            if(completeFlag){
                complete[1]=1;
            }
            else{
                complete[3]=1;
            }
            if(partialFlag){
                partial[1]=1;
            }
            else{
                partial[3]=1;
            }
        }
        System.out.print("COMPLETE OVERLAP ARRAY:");
        for(int i=0; i<complete.length; i++){
            System.out.print(complete[i]+" ");
        }
        System.out.print("\nPARTIAL OVERLAP ARRAY:");
        for(int i=0; i<complete.length; i++){
            System.out.print(partial[i]+" ");
        }
        System.out.println("\n\n");


    }
}
