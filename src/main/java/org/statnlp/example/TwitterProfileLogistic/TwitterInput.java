package org.statnlp.example.TwitterProfileLogistic;

import org.statnlp.commons.types.WordToken;

import java.util.List;

public class TwitterInput {
    List<WordToken> wts;
    String entity;
    String user;
    List<Integer> eStart;
    List<Integer> eEnd;
    TwitterInput(String userString,List<WordToken> wts, String entityString, List<Integer> eStart, List<Integer> eEnd){
        this.wts=wts;
        this.entity=entityString;
        this.user=userString;
        this.eStart=eStart;
        this.eEnd=eEnd;
    }
    public void setEntity(String entity){
        this.entity=entity;
    }
    int size(){
        return wts.size();
    }
}
