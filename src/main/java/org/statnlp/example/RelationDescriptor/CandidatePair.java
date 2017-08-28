package org.statnlp.example.RelationDescriptor;

import org.statnlp.commons.types.WordToken;

import java.util.List;

public class CandidatePair {
    public List<WordToken> wts;
    public int arg1Idx;
    public int arg2Idx;

    CandidatePair(List<WordToken> wts, int arg1Idx, int arg2Idx){
        this.wts=wts;
        this.arg1Idx=arg1Idx;
        this.arg2Idx=arg2Idx;
    }

    public int size(){
        return this.wts.size();
    }

}
