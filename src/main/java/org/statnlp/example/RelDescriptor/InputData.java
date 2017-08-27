package org.statnlp.example.RelDescriptor;

import java.util.List;

import org.statnlp.commons.types.WordToken;

public class InputData {
    public List<WordToken> sent;
    int arg1Idx;
    int arg2Idx;
    InputData(List<WordToken> sent, int arg1Idx, int arg2Idx){
        this.sent=sent;
        this.arg1Idx=arg1Idx;
        this.arg2Idx=arg2Idx;
    }
}
