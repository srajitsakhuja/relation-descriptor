package org.statnlp.example.RelationLatent;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import org.statnlp.commons.io.RAWF;
import org.statnlp.commons.types.WordToken;
import java.io.BufferedReader;
import edu.stanford.nlp.maxent.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/*
*POS Tagging done using the Stanford CoreNLP POSTagger
*/
public class RelationLatentMain {
    private static String unprocessedFilePath="data/RelDataSet/sem-eval-task8.txt";
    private static String processedFilePath="data/RelDataSet/sem-eval-task8-processed.txt";
    private static List<String> relTypes=new ArrayList<String>();


    public static void main(String...args) throws IOException{
        //preprocessed file stored at processedFilePath
        //Preprocessor preprocessor=new Preprocessor(unprocessedFilePath, processedFilePath);
        RelationInstance[] insts=readData(processedFilePath);


        insts=posTagger(insts);
      LatentNetworkCompiler networkCompiler=new LatentNetworkCompiler(relTypes);
    }

    private static RelationInstance[] posTagger(RelationInstance[] insts){
        MaxentTagger tagger=new MaxentTagger("/Users/srajitsakhuja/Downloads/postagger/models/english-bidirectional-distsim.tagger");
        for(int i=0; i<insts.length; i++){
            List<WordToken> wt=insts[i].input.wts;
            String sent="";
            for(int j=0; j<wt.size(); j++){
                sent=sent+wt.get(j).getForm()+" ";
            }
            String tagged=tagger.tagString(sent);
            String[] w_tag=tagged.split(" ");

            List<WordToken> newTokens=new ArrayList<WordToken>();
            for(int j=0; j<w_tag.length; j++){
                String form=w_tag[j].split("_")[0];
                String tag=w_tag[j].split("_")[1];
                WordToken token=new WordToken(form,tag);
                newTokens.add(token);
            }
            insts[i].input.wts=newTokens;
        }
        return insts;
    }

    public static RelationInstance[] readData(String processedFilePath) throws IOException{
        BufferedReader br= RAWF.reader(processedFilePath);
        String line=null;
        List<RelationInstance> insts=new ArrayList<RelationInstance>();
        int lnum=0;
        List<WordToken> wts=new ArrayList<WordToken>();
        int e1Start=-1;
        int e2Start=-1;
        int count=0;
        while((line=br.readLine())!=null){
            if(line.length()!=0){
                if(lnum%3==0){
                    String[] line_split=line.split(" ");
                    for(int i=0; i<line_split.length; i++){
                        WordToken wt=new WordToken(line_split[i], " ");
                        wts.add(wt);
                    }
                }
                else if(lnum%3==1){
                    e1Start=Integer.parseInt(line.split("/")[0]);
                    e2Start=Integer.parseInt(line.split("/")[1]);
                }
                else{
                    count++;
                    Output output=new Output(line, wts.size());
                    if(!relTypes.contains(output.relType)){
                        relTypes.add(output.relType);
                    }
                    Input input=new Input(e1Start, e2Start, wts);
                    wts=new ArrayList<WordToken>();
                    RelationInstance inst=new RelationInstance(count, 1.0,input, output);
                    insts.add(inst);
                }
                lnum+=1;
            }
        }
        return insts.toArray(new RelationInstance[insts.size()]);
    }

}
