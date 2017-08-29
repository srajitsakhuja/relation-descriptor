package org.statnlp.example.RelationLatent;

import org.statnlp.example.base.BaseInstance;

public class RelationInstance extends BaseInstance<RelationInstance, Input, Output> {

    Input input;
    Output output;

    RelationInstance(int instanceId, double weight){
        super(instanceId, weight);
    }

    RelationInstance(int instanceId, double weight, Input input, Output output){
        super(instanceId, weight);
        this.input=input;
        this.output=output;
    }

    public int size(){
        return this.input.size();
    }
}
