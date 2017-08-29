package org.statnlp.example.RelationLatent;

import org.statnlp.commons.types.WordToken;

import java.util.List;

public class Input {

    int e1Pos;
    int e2Pos;
    List<WordToken> wts;

    Input(int e1, int e2, List<WordToken> wts){
        this.e1Pos=e1;
        this.e2Pos=e2;
        this.wts=wts;
    }

    public int size(){
        return this.wts.size();
    }


}
