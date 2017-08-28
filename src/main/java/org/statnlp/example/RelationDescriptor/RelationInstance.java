package org.statnlp.example.RelationDescriptor;

import org.statnlp.example.base.BaseInstance;

import java.util.List;

public class RelationInstance extends BaseInstance<RelationInstance, CandidatePair, List<String>> {

    public RelationInstance(int instanceId, double weight){
        super(instanceId, weight);
    }

    public RelationInstance(int instanceId, double weight, CandidatePair input, List<String> output){
        super(instanceId, weight);
        this.input=input;
        this.output=output;
    }
    public int size(){
        return input.size();
    }
    public CandidatePair duplicateInput(){
        return this.input;
    }
}
