package org.statnlp.example.RelDescriptor;

import java.util.List;

public class Evaluator {
    private List<String> gold;
    private List<String> pred;
    public int partialCount = 0;
    public int totalCount = 1;
    public int completeCount = 0;
    private boolean goldContainsRel = false;

    Evaluator(List<String> gold, List<String> pred) {
        for (int i = 0; i < gold.size(); i++) {
            System.out.print(gold.get(i) + "(" + i + ") ");
            if (!gold.get(i).equals("O")) {
                goldContainsRel = true;
            }
        }
        System.out.println();
        for (int i = 0; i < pred.size(); i++) {
            System.out.print(pred.get(i) + "(" + i + ") ");
        }
        System.out.println();

        if (goldContainsRel) {
            completeCount = 1;
            partialCount = 0;
            for (int i = 0; i < gold.size(); i++) {
                if ((gold.get(i).equals("B-R") || gold.get(i).equals("I-R")) && !gold.get(i).equals(pred.get(i))) {
                    completeCount = 0;
                    //System.out.println("Mis-Match found at:" + i);
                    break;
                }
            }
            for (int i = 0; i < gold.size(); i++) {
                if ( (gold.get(i).equals("B-R") || gold.get(i).equals("I-R")) && gold.get(i).equals(pred.get(i))) {
                    partialCount = 1;
                    //System.out.println("Match found at:" + i);
                }
            }
        }
        else {
            completeCount = 1;
            partialCount = 1;
            for (int i = 0; i < gold.size(); i++) {
                if (!gold.get(i).equals(pred.get(i))) {
                    completeCount = 0;
                    partialCount = 0;
                }
            }
        }
        System.out.println("COMPLETE:"+completeCount+" PARTIAL:"+partialCount);
        System.out.println("\n\n");

    }
}
