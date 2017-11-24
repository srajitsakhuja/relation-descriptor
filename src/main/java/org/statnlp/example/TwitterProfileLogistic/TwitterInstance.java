package org.statnlp.example.TwitterProfileLogistic;

import org.statnlp.commons.types.WordToken;
import org.statnlp.example.base.BaseInstance;

import java.util.List;

public class TwitterInstance extends BaseInstance<TwitterInstance, TwitterInput, Integer> {

    public TwitterInstance(int instanceId, double weight){
        super(instanceId, weight);
    }

    public TwitterInstance(int instanceId, double weight, TwitterInput input, Integer output){
        super(instanceId, weight);
        this.input=input;
        this.output=output;
    }
    public int size(){
        return this.input.size();
    }
    public String toString(){
        System.out.println("ID:"+this.getInstanceId());
        System.out.println("INPUT:::");
        System.out.println(this.input.user);
        for(WordToken wt:this.input.wts){
            System.out.print(wt.getForm()+" ");
        }
        System.out.println();
        for(WordToken wt:this.input.wts){
            System.out.print(wt.getTag()+" ");
        }
        System.out.println();
        for(WordToken wt:this.input.wts){
            System.out.print(wt.getPhraseTag()+" ");
        }
        System.out.println();
        System.out.println(this.input.entity);
        System.out.println("START, END:");
        for(int i=0; i<this.input.eStart.size(); i++){
            System.out.printf("%d,%d\n", this.input.eStart.get(i), this.input.eEnd.get(i));
        }
        System.out.println("OUTPUT:"+this.output);
        System.out.println();
        return "";
    }
    public TwitterInput duplicateInput(){
        System.out.println(this.input.toString());
        TwitterInput newInput = new TwitterInput(
                this.input.user,
                this.input.wts, this.input.entity, this.input.eStart, this.input.eEnd);
        return newInput;

    }

    public Integer duplicateOutput(){
        return output;
    }
}
