package org.statnlp.example.RelationLatent;

import org.statnlp.commons.types.Instance;

import java.util.List;

public class LatentEvaluator {
    public double metrics[];

    @SuppressWarnings("unchecked")
	public LatentEvaluator(Instance[] results){
        metrics=new double[4];
        String relType;
        List<String> pred;
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
            for(int i=0; i<pred.size(); i++){
                if(pred.get(i)!="O"){
                    String predTag=pred.get(i).split("-")[1];
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
            System.out.println(isPositive);
            System.out.println(matches);
            System.out.println();
            if(isPositive){
                if(matches){ tp++; }
                else{fp++;}
            }
            else {
                if(matches){tn++;}
                else {fn++;}
            }
        }
        System.out.println(tp+" "+fp+" "+tn+" "+fn);
        System.out.println();
        metrics[0]=tp*1.0/(tp+fp);
        metrics[1]=tp*1.0/(tp+fn);
        metrics[2]=metrics[0]*metrics[1]*2.0/(metrics[0]+metrics[1]);
        metrics[3]=(tp+tn)*1.0/(tp+tn+fp+fn);
    }
}
