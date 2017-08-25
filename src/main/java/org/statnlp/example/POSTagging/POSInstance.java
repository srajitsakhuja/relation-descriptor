package org.statnlp.example.POSTagging;

import org.statnlp.example.base.BaseInstance;

import java.util.List;

public class POSInstance extends BaseInstance<POSInstance, List<String>, List<String>> {


    public POSInstance(int instanceId, double weight) {
        this(instanceId, weight, null, null);
    }
    public POSInstance(int instanceId, double weight, List<String> input, List<String> output) {
        super(instanceId, weight);
        this.input=input;
        this.output=output;
    }


        @Override
    public int size() {
        return this.input.size();
    }
}
