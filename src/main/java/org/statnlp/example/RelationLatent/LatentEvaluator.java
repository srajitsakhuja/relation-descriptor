package org.statnlp.example.RelationLatent;

import org.statnlp.commons.types.Instance;

import java.util.List;

public class LatentEvaluator {
    public double metrics[];

    @SuppressWarnings("unchecked")
	public LatentEvaluator(Instance[] results){
        String relType;
        List<String> pred;
        int truep[]={0,0,0,0,0,0,0,0,0};
        int falsep[]={0,0,0,0,0,0,0,0,0};
        int truen[]={0,0,0,0,0,0,0,0,0};
        int falsen[]={0,0,0,0,0,0,0,0,0};
        double precision[]={0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
        double recall[]={0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
        double fScore[]={0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
        double accuracy[]={0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
        int tp=0;
        int fp=0;
        int tn=0;
        int fn=0;
        for(Instance result:results){
            RelationInstance rel=(RelationInstance)result;
            relType=rel.getOutput().relType;
            pred=(List<String>)rel.getPrediction();
            boolean isPositive=false;
            boolean matches=false;
            String predTag="O";
            for(int i=0; i<pred.size(); i++){
                if(pred.get(i)!="O"){
                    predTag=pred.get(i).split("-")[1];
                    System.out.println(predTag);
                    System.out.println(relType);
                    if(predTag.equals(relType)){
                        matches=true;
                    }
                    if(!predTag.equals("o")){
                        isPositive=true;
                    }
                    break;
                }
            }
            int index=tag2Index(relType);
            System.out.println(isPositive);
            System.out.println(matches);
            System.out.println();
            if(isPositive){
                if(matches){
                    tp++;
                    truep[index]++;
                }
                else{
                    fp++;
                    falsep[index]++;
                }
            }
            else {
                if(matches){
                    tn++;
                    truen[index]++;
                }
                else {
                    fn++;
                    falsen[index]++;
                }
            }
        }
        System.out.println(tp+" "+fp+" "+tn+" "+fn);
        double macroPrec=0.0;double macroRec=0.0;double macroF=0.0;double macroAcc=0.0;
        for(int i=0; i<truep.length-1; i++){
            precision[i]=(truep[i]+falsep[i]>0)?truep[i]*1.0/(truep[i]+falsep[i]):0.0;
            recall[i]=(truep[i]+falsen[i])>0?truep[i]*1.0/(truep[i]+falsen[i]):0.0;
            accuracy[i]=(truep[i]+truen[i]+falsep[i]+falsen[i])>0?(truep[i]+truen[i])*1.0/(truep[i]+truen[i]+falsep[i]+falsen[i]):0.0;
            fScore[i]=(precision[i]+recall[i])>0?precision[i]*recall[i]*2.0/(precision[i]+recall[i]):0.0;
            macroPrec+=precision[i];macroRec+=recall[i];macroF+=fScore[i];macroAcc+=accuracy[i];

            System.out.println(index2Tag(i)+":");
            System.out.println("TP\tFP\tTN\tFN");
            System.out.println(truep[i]+"\t"+falsep[i]+"\t"+truen[i]+"\t"+falsen[i]);
            System.out.println("Precision\tRecall\tF-score\tAccuracy");
            System.out.println(precision[i]+"\t"+recall[i]+"\t"+fScore[i]+"\t"+accuracy[i]);
            System.out.println();
        }

        macroPrec/=(truep.length-1);
        macroRec/=(truep.length-1);
        macroF/=(truep.length-1);
        macroAcc/=(truep.length-1);
        System.out.println("MACRO VALUES:");
        System.out.println("Precision\tRecall\tF-score\tAccuracy");
        System.out.println(macroPrec+"\t"+macroRec+"\t"+macroF+"\t"+macroAcc);
    }
    private int tag2Index(String predTag){
	    predTag=predTag.split("_")[0];
	    if(predTag.equals("ce")){return 0;}
        else if(predTag.equals("ia")){return 1;}
        else if(predTag.equals("pp")){return 2;}
        else if(predTag.equals("cc")){return 3;}
        else if(predTag.equals("eo")){return 4;}
        else if(predTag.equals("ed")){return 5;}
        else if(predTag.equals("cw")){return 6;}
        else if(predTag.equals("mc")){return 7;}
        else if(predTag.equals("o")){return 8;}
        return 0;
    }
    private String index2Tag(int i){
        switch(i){
            case 0:return "Cause-Effect";
            case 1:return "Instrument-Agency";
            case 2:return "Producer-Product";
            case 3:return "Content-Container";
            case 4:return "Entity-Origin";
            case 5:return "Entity-Destination";
            case 6:return "Component-Whole";
            case 7:return "Member-Collection";
            default: return "Others";
        }
    }
}
