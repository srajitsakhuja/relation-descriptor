package org.statnlp.example.RelationLatent;

import org.statnlp.commons.types.WordToken;

import java.util.List;

public class Input {

    int[] e1Pos;
    int[] e2Pos;
    List<WordToken> wts;

    Input(int e1Start, int e1End, int e2Start, int e2End, List<WordToken> wts){
        this.e1Pos=new int[2];
        this.e2Pos=new int[2];
        this.e1Pos[0]=e1Start; this.e1Pos[1]=e1End;
        this.e2Pos[0]=e2Start; this.e2Pos[1]=e2End;
        this.wts=wts;
    }

    public int size(){
        return this.wts.size();
    }


}
