package org.statnlp.example.TwitterProfileLogistic;

import org.statnlp.commons.types.WordToken;
import org.statnlp.example.base.BaseInstance;

import java.util.List;

public class TwitterInstance extends BaseInstance<TwitterInstance, TwitterInput, Integer> {
    public TwitterInput in;
    public int out;
    public TwitterInstance(int instanceId, double weight){
        super(instanceId, weight);
    }

    public TwitterInstance(int instanceId, double weight, TwitterInput input, Integer output){
        super(instanceId, weight);
        this.in=input;
        this.out=output;
    }
    public int size(){
        return this.in.size();
    }
    public String toString(){
        System.out.println("ID:"+this.getInstanceId());
        System.out.println("INPUT:::");
        System.out.println(this.in.user);
        for(WordToken wt:this.in.wts){
            System.out.print(wt.getForm()+" ");
        }
        System.out.println();
        System.out.println(this.in.entity);
        System.out.println("START, END:");
        for(int i=0; i<this.in.eStart.size(); i++){
            System.out.printf("%d,%d\n", this.in.eStart.get(i), this.in.eEnd.get(i));
        }
        System.out.println("OUTPUT:"+this.out);
        System.out.println();
        return "";
    }
}
