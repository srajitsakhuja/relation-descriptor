package org.statnlp.example.RelationLatent;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import org.statnlp.commons.io.RAWF;
import org.statnlp.commons.types.Sentence;
import org.statnlp.commons.types.WordToken;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import edu.stanford.nlp.maxent.*;
/*
 * padding added around punctuations including commas, colons, full-stops, question-marks etc.
 * POS Tagged using the Stanford CoreNLP POSTagger
*/
public class Preprocessor {
    Preprocessor(String rPath, String oPath) throws IOException{
        List<String>lines=readData(rPath);
        System.out.println(lines.size()/4+" instances read from "+rPath);

        FileWriter fw=new FileWriter(oPath);
        BufferedWriter bw=new BufferedWriter(fw);
        String wString="";
        int count=0;
        MaxentTagger tagger=new MaxentTagger("/Users/srajitsakhuja/Downloads/postagger/models/english-bidirectional-distsim.tagger");

        for(String line:lines){
            // System.out.print(line);
            wString=wString+line;
            if(count%4==0){
                String[] word_tag=tagger.tagString(line).split(" ");
                String tagString="";
                String wordString="";
                for(int i=0; i<word_tag.length; i++){
                    tagString=tagString+word_tag[i].split("_")[1]+" ";
                    wordString=wordString+word_tag[i].split("_")[0]+" ";
                }
                tagString=groupTags(tagString);
                tagString=tagString+"\n";
                wString=wString+wordString+"\n"+tagString;
            }
            count++;
        }
        System.out.println("POS Tagging complete");

        //System.out.print(wString);
        bw.write(wString);
        bw.close();
        System.out.println(count/4+" lines written to"+oPath);
    }
    private boolean isInteger(String x){
        try{
            Integer.parseInt(x);
            return true;
        }
        catch(NumberFormatException e){
            return false;
        }
    }
    private boolean isPunctuation(char ch){
        return ch==',' || ch==':' || ch=='`' || ch=='\'' || ch=='*'|| ch==')'|| ch=='(' || ch=='\"';
    }
    private int getPos(char[] line, int j){
        int spaces=0;
        for(int i=0; i<=j; i++){
            if(line[i]==' '){
                spaces++;
            }
        }
        return spaces;
    }
    private List<String> readData(String fpath) throws IOException{
        BufferedReader br= RAWF.reader(fpath);
        String line=null;
        List<String> lines=new ArrayList<String>();
        int count=0;
        while((line=br.readLine())!=null){
            count++;
            String[] line_split=line.split("\t");

            if(isInteger(line_split[0])){
                line=line_split[1];
                String punc=line.substring(line.length()-2, line.length()-1);
                line=line.substring(1, line.length()-2);
                line=line+" "+punc;
                //System.out.println(line);
                int e1Start=line.indexOf("<e1>");
                int e1End=line.indexOf("</e1>");
                int e2Start=line.indexOf("<e2>");
                int e2End=line.indexOf("</e2>");
                String be1=line.substring(0, e1Start);
                String e1=line.substring(e1Start+4, e1End);
                String ae1=line.substring(e1End+5, e2Start);
                String e2=line.substring(e2Start+4, e2End);
                String ae2=line.substring(e2End+5);
                String newLine=be1+e1+ae1+e2+ae2;
                char[] lineArr=line.toCharArray();
                int spaces=0;
                int arg1Start=-1;
                int arg1End=-1;
                int arg2Start=-1;
                int arg2End=-1;
                for(int j=0; j<e1End; j++){
                    if(lineArr[j]==' '){
                        spaces++;
                    }
                    if(j==e1Start){
                        arg1Start=spaces;
                    }
                }
                arg1End=spaces;
                spaces=0;
                for(int j=0; j<e2End; j++){
                    if(lineArr[j]==' '){
                        spaces++;
                    }
                    if(j==e2Start){
                        arg2Start=spaces;
                    }
                }
                arg2End=spaces;

                //Uncomment all code in function to form entity compounds
                //Change the arg1Start, arg1End, arg2Start, arg2End (L.146-L.218)
//                String bef="";
//                for(int i=0; i<arg1Start; i++){
//                    bef=bef+newLine.split(" ")[i]+" ";
//                }
//                String el1Compound=newLine.split(" ")[arg1Start];
//                for(int i=arg1Start+1; i<=arg1End; i++){
//                    el1Compound=el1Compound+"-"+newLine.split(" ")[i];
//                }
//                String bet=" ";
//                for(int i=arg1End+1; i<arg2Start; i++){
//                    bet=bet+newLine.split(" ")[i]+" ";
//                }
//                String el2Compound=newLine.split(" ")[arg2Start];
//                for(int i=arg2Start+1; i<=arg2End; i++){
//                    el2Compound=el2Compound+"-"+newLine.split(" ")[i];
//                }
//                String aft=" ";
//                for(int i=arg2End+1; i<newLine.split(" ").length; i++){
//                    aft=aft+newLine.split(" ")[i]+" ";
//                }
//                newLine=bef+el1Compound+bet+el2Compound+aft;
//                arg2Start=arg2Start-(arg1End-arg1Start);
//                arg1End=arg1Start;
//                arg2End=arg2Start;
                lineArr=newLine.toCharArray();

                for(int j=0; j<lineArr.length; j++){
                    if(isPunctuation(lineArr[j])){
                        if(j>0 && lineArr[j-1]!=' ' && lineArr[j+1]==' '){
                            String splt1=newLine.substring(0, j);
                            String splt2=" "+lineArr[j];
                            String splt3=newLine.substring(j+1);
                            newLine=splt1+splt2+splt3;
                            lineArr=newLine.toCharArray();
                            int pos=getPos(lineArr, j);

                            if(arg1Start>=pos){
                                arg1Start++;
                                arg2Start++;
                                arg1End++;
                                arg2End++;
                            }
                            else{
                                if(arg1End>=pos){
                                    arg1End++;
                                    arg2Start++;
                                    arg2End++;
                                }
                                else{
                                    if(arg2Start>=pos){
                                        arg2Start++;
                                        arg2End++;
                                    }
                                    else{
                                        if(arg2End>=pos){
                                            arg2End++;
                                        }
                                    }
                                }
                            }
                        }
                        if(j==0 || lineArr[j-1]==' '){
                            if(j+1<lineArr.length && lineArr[j+1]!=' ') {
                                String splt1=newLine.substring(0, j);
                                String splt2=lineArr[j]+" ";
                                String splt3=newLine.substring(j+1);
                                newLine=splt1+splt2+splt3;
                                lineArr=newLine.toCharArray();
                                int pos=getPos(lineArr, j);

                                if(arg1Start>=pos){
                                    arg1Start++;
                                    arg2Start++;
                                    arg1End++;
                                    arg2End++;
                                }
                                else{
                                    if(arg1End>=pos){
                                        arg1End++;
                                        arg2Start++;
                                        arg2End++;
                                    }
                                    else{
                                        if(arg2Start>=pos){
                                            arg2Start++;
                                            arg2End++;
                                        }
                                        else{
                                            if(arg2End>=pos){
                                                arg2End++;
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
//                arg1End=arg1Start;
//                arg2End=arg2Start;
                lines.add(newLine+"\n");
                lines.add(arg1Start+"*"+arg1End+"/"+arg2Start+"*"+arg2End+"\n");
//                System.out.println(newLine);
//                System.out.println(arg1Start+"*"+arg1End+"/"+arg2Start+"*"+arg2End);
            }
            else{
                if(!line.startsWith("Comment")){
                    lines.add(line+"\n");
                }
            }
        }
        return lines;
    }

    private String groupTags(String tagString){
        String newTagString="";
        for(int i=0; i<tagString.split(" ").length; i++){
            String s=tagString.split(" ")[i];
            if(s.equals("CD")){ s="ADJ";}
            else if(s.equals("JJ")){ s="ADJ";}
            else if(s.equals("JJR")){ s="ADJ";}
            else if(s.equals("JJS")){ s="ADJ";}
            else if(s.equals("VB")){ s="V";}
            else if(s.equals("VBD")){ s="V";}
            else if(s.equals("VBG")){ s="V";}
            else if(s.equals("VBG")){ s="V";}
            else if(s.equals("VBN")){ s="V";}
            else if(s.equals("VBP")){ s="V";}
            else if(s.equals("VBZ")){ s="V";}
            else if(s.equals("VB")){ s="V";}
            else if(s.equals("MD")){ s="V";}
            else if(s.equals("NN")){ s="N";}
            else if(s.equals("NNP")){ s="N";}
            else if(s.equals("NNS")){ s="N";}
            else if(s.equals("NNPS")){ s="N";}
            else if(s.equals("RB")){ s="ADV";}
            else if(s.equals("RBR")){ s="ADV";}
            else if(s.equals("RBS")){ s="ADV";}
            else if(s.equals("RP")){ s="ADV";}
            else if(s.equals("WRB")){ s="ADV";}
            else if(s.equals("DT")){ s="DET";}
            else if(s.equals("PDT")){ s="DET";}
            else if(s.equals("WDT")){ s="DET";}
            else if(s.equals("POS")){ s="DET";}
            else if(s.equals("PRP")){ s="PRP";}
            else if(s.equals("WP")){ s="PRP";}
            else if(s.equals("WP$")){ s="PRP$";}
            else if(s.equals("TO")){ s="PREP";}
            else if(s.equals("IN")){ s="PREP";}
            else if(s.equals("CC")){ s="CONJ";}
            else if(s.equals("EX")){ s="OTHER";}
            else if(s.equals("FW")){ s="OTHER";}
            else if(s.equals("SYM")){ s="OTHER";}
            else if(s.equals("UH")){ s="OTHER";}
            else if(s.equals("LS")){ s="OTHER";}
            newTagString=newTagString+s+" ";
        }
        newTagString=newTagString;
        return newTagString;
    }
}
