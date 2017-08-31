package org.statnlp.example.RelationLatent;

import org.statnlp.commons.types.WordToken;

import java.util.List;

public class Input {

    int e1Start;
    int e1End;
    int e2Start;
    int e2End;
    List<WordToken> wts;

    Input(int e1Start, int e1End, int e2Start, int e2End, List<WordToken> wts){
        this.e1Start=e1Start;
        this.e1End=e1End;
        this.e2Start=e2Start;
        this.e2End=e2End;
        this.wts=wts;
    }

    public int size(){
        return this.wts.size();
    }


}
