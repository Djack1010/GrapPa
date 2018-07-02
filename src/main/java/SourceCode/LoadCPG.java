package SourceCode;

import soot.util.dot.DotGraph;

import java.io.*;

public class LoadCPG {

    static String nedoPath;
    final static infoExec info = new infoExec();

    public static void main(String[] args) {

        String filePath=null;

        if (args.length == 0) {
            nedoPath ="/home/djack/IdeaProjects/nedo";
            filePath="/home/djack/IdeaProjects/nedo/graphs/example/MainTest_foo_0.nedo";
            info.setCGMM();
        }else{
            int i=0;
            while(i<args.length){
                switch (args[i]){
                    case "-cp":
                        i++;
                        filePath=args[i];
                        break;
                    case "-pf":
                        i++;
                        nedoPath=args[i];
                        break;
                    case "-graph2vec":
                        i++;
                        String[] toolArray = args[i].split(":");
                        for (String t: toolArray){
                            switch (t){
                                case "struc2vec":
                                    info.setStruc2vec();
                                    break;
                                case "CGMM":
                                    info.setCGMM();
                                    break;
                                case "SenFormat":
                                    info.setSenFormat();
                                    break;
                                default:
                                    System.err.println("Invalid vec tool " + args[i] + ", exiting...");
                                    System.exit(0);
                                    break;
                            }
                        }
                        break;
                    default:
                        System.err.println("MainCPG:ERROR:Invalid arguments " + args[i] + ", exiting...");
                        System.exit(0);
                        break;
                }
                i++;
            }
        }

        File f = new File(filePath);
        if(!f.exists()){
            System.err.println("ERROR, file not exist, exiting...");
            System.exit(0);
        }

        System.out.print("Loading CPG... ");
        CodePropertyGraph cpg = new CodePropertyGraph(filePath);
        System.out.println(cpg.getNameCPG()+" COMPLETE!");

        if (info.isStruc2vec()){
            System.out.print("\tPrinting CPG in input format for struct2vec...");
            //CPG2struc2vec s2v = new CPG2struc2vec(cpg,true);
            //s2v.printEdgeListOnFile(nedoPath + "/extTool/struc2vec/graph/base/");
            //s2v.printNodeListOnFile(nedoPath + "/extTool/struc2vec/graph/label/");
            //s2v.printNodeLabels_CONF1(nedoPath + "/extTool/struc2vec/graph/label_CONF1/");
            //s2v.printNodeLabels_CONF2(nedoPath + "/extTool/struc2vec/graph/label_CONF2/");
            //s2v.printNodeLabels_CONF3(nedoPath + "/extTool/struc2vec/graph/label_CONF3/");
            //s2v.printNodeLabels_CONF4(nedoPath + "/extTool/struc2vec/graph/label_CONF4/");
            //s2v.printNodeLabels_CONF5(nedoPath + "/extTool/struc2vec/graph/label_CONF5/");
            //s2v.printNodeListOnFile2(nedoPath + "/extTool/struc2vec/graph/label2/");
            System.out.println("DONE!");
            //if(cpg.getSize()==cpg.getCPGNodes().size())System.out.println("ALLRIGHT!");
            //else System.out.println(cpg.getSize()+" not equals to "+cpg.getCPGNodes().size());
        }
        if (info.isCGMM()){
            System.out.print("\tPrinting CPG in input format for CGMM...");
            CPG2CGMM cgmm = new CPG2CGMM(cpg,true);
            cgmm.printAdjListOnFile_COMPLETE(nedoPath + "/extTool/CGMM/graph/base_COM/");
            cgmm.printAdjListOnFile_STMTandTNodes(nedoPath + "/extTool/CGMM/graph/base_SeT/");
            System.out.println("DONE!");
        }
        if (info.isSenFormat()){
            System.out.print("\tPrinting CPG in input format for SenFormat...");
            //CPG2SenFormat sf = new CPG2SenFormat(cpg,true);
            //sf.printGraphOnFile(nedoPath + "/extTool/senFormat/graph1/");
            //sf.printGraphOnFile2(nedoPath + "/extTool/senFormat/graph2/");
            System.out.println("DONE!");
        }


        //CPGToDotGraph cpgToDotPDG = new CPGToDotGraph(cpg.getCPGNodes().get(0), cpg.getNameCPG());
        //DotGraph CPGdotGraphPDG = cpgToDotPDG.drawCPG();
        //CPGdotGraphPDG.plot(nedoPath + "/graphs/3a/" + cpg.getNameCPG() + ".dot");

    }

    private static class infoExec{

        boolean struc2vec;
        boolean CGMM;
        boolean senFormat;

        public infoExec(){
            this.struc2vec=false;
            this.CGMM=false;
            this.senFormat=false;
        }

        public void setStruc2vec(){ this.struc2vec=true; }
        public boolean isStruc2vec(){ return this.struc2vec; }

        public void setCGMM(){ this.CGMM=true; }
        public boolean isCGMM(){ return this.CGMM; }

        public void setSenFormat(){ this.senFormat=true; }
        public boolean isSenFormat(){ return this.senFormat; }

    }

}
