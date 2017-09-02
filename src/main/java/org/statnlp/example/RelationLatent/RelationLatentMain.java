package org.statnlp.example.RelationLatent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

import org.statnlp.commons.io.RAWF;
import org.statnlp.commons.types.Instance;
import org.statnlp.commons.types.Word;
import org.statnlp.commons.types.WordToken;
import org.statnlp.example.RelationDescriptor.RelationFeatureManager;
import org.statnlp.example.RelationDescriptor.RelationNetworkCompiler;
import org.statnlp.hypergraph.DiscriminativeNetworkModel;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.NetworkConfig;
import org.statnlp.hypergraph.NetworkModel;

public class RelationLatentMain{
    private static final long serialVersionUID = 2046107789709874963L;

    private static String unprocessedFilePath="data/sem-eval/testFile.txt";
    private static String processedFilePath="data/sem-eval/testFileProcessed.txt";
    private static int trainCount;
    private static int testCount;
    private static String trainPath="data/sem-eval/trainFileProcessed.txt";
    private static String testPath="data/sem-eval/testFileProcessed.txt";
    private static List<String> relTypes=new ArrayList<String>();
    private static int iterCount=1000;
    private static int threadCount;
    private static double L2;
    private static boolean readModel;
    private static boolean saveModel;
    private static String modelFile;
    public static void main(String...args) throws IOException, InterruptedException{
        //Preprocessing to add POSTags and parsing the data to usable form
        //Preprocessor processor=new Preprocessor(unprocessedFilePath, processedFilePath);

        //Param initialisation
        trainCount=Integer.parseInt(args[0]);
        testCount=Integer.parseInt(args[1]);
        NetworkConfig.NUM_THREADS=Integer.parseInt(args[2]);
        NetworkConfig.L2_REGULARIZATION_CONSTANT=Double.parseDouble(args[3]);
        readModel=Boolean.parseBoolean(args[4]);
        saveModel=Boolean.parseBoolean(args[5]);
        modelFile="data/sem-eval/"+args[6];



        //Importing test and train data
        boolean reduceSpace=false;
        boolean tagCategorize=false;
        RelationInstance[] trainInsts=readData(trainPath, true, trainCount, tagCategorize, reduceSpace);
        RelationInstance[] testInsts=readData(testPath, false, testCount, tagCategorize, reduceSpace);
        printInst(testInsts);
        //build, train, test repeat...
        NetworkModel model=null;
        NetworkConfig.PARALLEL_FEATURE_EXTRACTION = true;
        NetworkConfig.AVOID_DUPLICATE_FEATURES = true;
        if(!readModel) {
            GlobalNetworkParam gnp = new GlobalNetworkParam();
            LatentFeatureManager fman = new LatentFeatureManager(gnp);
            LatentNetworkCompiler networkCompiler = new LatentNetworkCompiler(relTypes);
            model = DiscriminativeNetworkModel.create(fman, networkCompiler);
            model.train(trainInsts, iterCount);
        }
        else{
            try {
                ObjectInputStream ois = (ObjectInputStream) RAWF.objectReader(modelFile);
                model = (NetworkModel) ois.readObject();
                ois.close();
            }
            catch(Exception e){
                System.out.println(e);
            }
        }
        Instance[] results=model.decode(testInsts);
        LatentEvaluator eval=new LatentEvaluator(results);
        if(saveModel){
            ObjectOutputStream oos=RAWF.objectWriter(modelFile);
            oos.writeObject(model);
            oos.close();
        }
    }


    public static RelationInstance[] readData(String processedFilePath, boolean isTraining, int lim, boolean tagCategorize, boolean reduceSpace) throws IOException{
        BufferedReader br= RAWF.reader(processedFilePath);
        String line=null;
        List<RelationInstance> insts=new ArrayList<RelationInstance>();
        int lnum=0;
        List<WordToken> wts=new ArrayList<WordToken>();
        int e1Start=-1;
        int e1End=-1;
        int e2Start=-1;
        int e2End=-1;
        int count=0;
        while((line=br.readLine())!=null){
            if(line.length()!=0){
                if(lnum%5==1){
                    String[] line_split=line.split(" ");
                    for(int i=0; i<line_split.length; i++){
                        WordToken wt=new WordToken(line_split[i], " ");
                        wts.add(wt);
                    }
                }
                else if(lnum%5==3){
                    e1Start=Integer.parseInt(line.split("/")[0].split("[*]")[0]);
                    e1End=Integer.parseInt(line.split("/")[0].split("[*]")[1]);
                    e2Start=Integer.parseInt(line.split("/")[1].split("[*]")[0]);
                    e2End=Integer.parseInt(line.split("/")[1].split("[*]")[1]);
                }
                else if(lnum%5==4){
                    count++;
                    if(count>lim){break;}
                    Output output=new Output(line, wts.size());
                    if(!relTypes.contains(output.relType) && isTraining){
                        relTypes.add(output.relType);
                    }
                    Input input=new Input(e1Start, e1End, e2Start, e2End,  wts);
                    wts=new ArrayList<WordToken>();
                    RelationInstance inst=new RelationInstance(count, 1.0,input, output);
                    if(reduceSpace){
                        inst=reduceSpace(inst);
                    }
                    if(isTraining){
                        inst.setLabeled();
                    }
                    else{
                        inst.setUnlabeled();
                    }
                    insts.add(inst);
                }
                else if(lnum%5==2){
                    String[] tag_split=line.split(" ");
                    for(int i=0; i<tag_split.length; i++){
                        if(tagCategorize){
                            tag_split[i]=groupTags(tag_split[i]);
                        }
                        wts.get(i).setTag(tag_split[i]);
                    }
                }
                lnum+=1;
            }
        }
        return insts.toArray(new RelationInstance[insts.size()]);
    }

    private static String groupTags(String tagString){
        String s=tagString;
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
        return s;
    }
    public static void printInst(RelationInstance[] insts){
        for(int i=0; i<insts.length; i++){
            for(int j=0; j<insts[i].input.wts.size(); j++) {
                System.out.print(insts[i].input.wts.get(j).getForm()+" ");
            }
            System.out.println();
            for(int j=0; j<insts[i].input.wts.size(); j++) {
                System.out.print(insts[i].input.wts.get(j).getTag()+" ");
            }
            System.out.println();
            System.out.println(insts[i].input.e1Start+"*"+insts[i].input.e1End+"/"+insts[i].input.e2Start+"*"+insts[i].input.e2End);
            System.out.println(insts[i].output.relType);
            System.out.println();
        }
    }
    public static RelationInstance reduceSpace(RelationInstance inst){
        List<WordToken> wts=new ArrayList<WordToken>();
        for(int i=inst.input.e1Start; i<=inst.input.e2End; i++){
            WordToken wt=new WordToken(inst.input.wts.get(i).getForm(), inst.input.wts.get(i).getTag());
            wts.add(wt);
        }
        inst.input.wts=wts;
        inst.input.e1End-=inst.input.e1Start;
        inst.input.e2Start-=inst.input.e1Start;
        inst.input.e2End-=inst.input.e1Start;
        inst.input.e1Start-=inst.input.e1Start;
        return inst;
    }
}
