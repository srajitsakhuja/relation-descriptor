package org.statnlp.example.RelationLatent;

import org.statnlp.commons.types.Instance;
import org.statnlp.example.base.BaseInstance;

public class RelationInstance extends BaseInstance<RelationInstance, Input, Output> {

    public RelationInstance(int instanceId, double weight){
        super(instanceId, weight);
    }

    public RelationInstance(int instanceId, double weight, Input input, Output output){
        super(instanceId, weight);
        this.input=input;
        this.output=output;
    }

    public int size(){
        return this.input.size();
    }

    public Input duplicateInput(){
        return this.input;
    }
    public Output duplicateOutput(){
        return this.output;
    }

}
