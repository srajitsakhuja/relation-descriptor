package org.statnlp.example.RelDescriptor;

import org.statnlp.commons.types.Sentence;
import org.statnlp.commons.types.WordToken;

import java.util.List;

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
