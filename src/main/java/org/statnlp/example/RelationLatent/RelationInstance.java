package org.statnlp.example.RelationLatent;

import org.statnlp.example.base.BaseInstance;

public class RelationInstance extends BaseInstance<RelationInstance, Input, Output> {

	private static final long serialVersionUID = 4081914458845526401L;
	
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
    public String toString(){
        for(int i=0; i<this.size(); i++){
            System.out.print(this.getInstanceId()+""+this.input.wts.get(i).getForm()+" ");
        }
        System.out.println();
        return "";
    }
}
