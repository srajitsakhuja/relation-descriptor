package org.statnlp.example.RelDescriptor;

import org.statnlp.commons.types.WordToken;
import org.statnlp.example.base.BaseInstance;

import java.util.List;

public class RelInstance extends BaseInstance<RelInstance, InputData, List<String>> {

    public RelInstance(int instanceId, double weight) {
        super(instanceId, weight);
    }

    public RelInstance(int instanceId, double weight, InputData input, List<String> output) {
        super(instanceId, weight);
        this.input=input;
        this.output=output;
    }

    public int size(){
        return this.input.sent.size();
    }

    public String toString(){
        System.out.print("INPUT:\n");
        List<WordToken> sent=this.input.sent;
        for(int i=0; i<sent.size(); i++){
            System.out.print(sent.get(i).getForm()+"("+i+")"+"["+this.output.get(i)+"] ");
        }
        System.out.println();
        System.out.println(input.arg1Idx+" "+input.arg2Idx);


        return "";
    }

    public InputData duplicateInput(){
        return this.input;
    }



}
