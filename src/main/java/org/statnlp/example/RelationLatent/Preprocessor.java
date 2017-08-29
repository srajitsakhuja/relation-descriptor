package org.statnlp.example.RelationLatent;

import org.statnlp.commons.io.RAWF;
import org.statnlp.commons.types.Sentence;
import org.statnlp.commons.types.WordToken;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
/**
 *padding added around punctuations including commas, colons, full-stops, question-marks etc.
 */
public class Preprocessor {
    Preprocessor(String rPath, String oPath) throws IOException{
        List<String>lines=readData(rPath);
        FileWriter fw=new FileWriter(oPath);
        BufferedWriter bw=new BufferedWriter(fw);
        String wString="";
        for(String line:lines){
            //System.out.print(line);
            wString=wString+line;
        }
        System.out.print(wString);
        bw.write(wString);
        bw.close();
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
        while((line=br.readLine())!=null){

            String[] line_split=line.split(" ");
            if(isInteger(line_split[0])){
                String punc=line.substring(line.length()-2, line.length()-1);
                line=line.substring(5, line.length()-2);
                line=line+" "+punc;
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
                String bef="";
                for(int i=0; i<arg1Start; i++){
                    bef=bef+newLine.split(" ")[i]+" ";
                }
                String el1Compound=newLine.split(" ")[arg1Start];
                for(int i=arg1Start+1; i<=arg1End; i++){
                    el1Compound=el1Compound+"-"+newLine.split(" ")[i];
                }
                String bet=" ";
                for(int i=arg1End+1; i<arg2Start; i++){
                    bet=bet+newLine.split(" ")[i]+" ";
                }
                String el2Compound=newLine.split(" ")[arg2Start];
                for(int i=arg2Start+1; i<=arg2End; i++){
                    el2Compound=el2Compound+"-"+newLine.split(" ")[i];
                }
                String aft=" ";
                for(int i=arg2End+1; i<newLine.split(" ").length; i++){
                    aft=aft+newLine.split(" ")[i]+" ";
                }
                newLine=bef+el1Compound+bet+el2Compound+aft;
                arg2Start=arg2Start-(arg1End-arg1Start);
                arg1End=arg1Start;
                arg2End=arg2Start;
                lineArr=newLine.toCharArray();

                for(int j=0; j<lineArr.length; j++){
                    if(isPunctuation(lineArr[j])){
                        if(lineArr[j-1]!=' ' && lineArr[j+1]==' '){
                            String splt1=newLine.substring(0, j);
                            String splt2=" "+lineArr[j];
                            String splt3=newLine.substring(j+1);
                            newLine=splt1+splt2+splt3;
                            lineArr=newLine.toCharArray();
                            int pos=getPos(lineArr, j);

                            if(arg1Start>=pos){
                                arg1Start++;
                                arg2Start++;
                            }
                            else{
                               if(arg2Start>=pos){
                                   arg2Start++;
                               }
                            }
                        }
                        if(lineArr[j-1]==' '){
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
                                }
                                else{
                                    if(arg2Start>=pos){
                                        arg2Start++;
                                    }
                                }
                            }

                        }
                    }
                }
                arg1End=arg1Start;
                arg2End=arg2Start;
                lines.add(newLine+"\n");
                lines.add(arg1Start+"/"+arg2Start+"\n");

            }
            else{
                if(!line.startsWith("Comment")){
                    lines.add(line+"\n");
                }
            }
        }
        return lines;
    }
}
