package org.statnlp.example.RelationLatent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Output {
    public String relType;
    //public List<String> relTags;
    protected enum RelType{
      other, cw, ce, eo, pp, cc, ia, ed, mc, ct;
    };
    public Map<String, String> relTypeMap=new HashMap<String, String>();
    public void initialiseRelTypeMap(){
        this.relTypeMap.put("Cause-Effect", "ce");
        this.relTypeMap.put("Instrument-Agency", "ia");
        this.relTypeMap.put("Product-Producer", "pp");
        this.relTypeMap.put("Content-Container", "cc");
        this.relTypeMap.put("Entity-Origin", "eo");
        this.relTypeMap.put("Entity-Destination", "ed");
        this.relTypeMap.put("Component-Whole", "cw");
        this.relTypeMap.put("Member-Collection", "mc");
        this.relTypeMap.put("Communication-Topic", "ct");
        this.relTypeMap.put("Other", "o");
    }
    Output(String relType, int size){
        initialiseRelTypeMap();
        String suffix="";
        if(relType.contains("(")){
            boolean rev= relType.split("[(]")[1].equals("e1,e2)")?false:true;
            relType=relType.split("[(]")[0];
            suffix=rev?"_rev":"_norm";
        }
        this.relType=this.relTypeMap.get(relType)+suffix;
        //this.relTags=new ArrayList<String>(size);
    }

}
