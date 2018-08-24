package SourceCode;

import soot.util.dot.DotGraph;

import java.io.File;

public class SlicingCPG {

    static String nedoPath;
    final static SlicingCPG.infoExec info = new SlicingCPG.infoExec();

    public static void main(String[] args) {

        String filePath=null;

        if (args.length == 0) {
            nedoPath ="/home/djack/IdeaProjects/nedo";
            filePath="/home/djack/Desktop/MainTest_foo_0.nedo";
            info.setSimply();
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
                    case "-simply":
                        info.setSimply();
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
        if(info.isSimply())cpg.simplifyGraph();
        System.out.println(cpg.getNameCPG()+" COMPLETE!");

        File directory = new File("/home/djack/Desktop/cheSucc");
        if (! directory.exists()){
            directory.mkdirs();
        }

        System.out.print("Printing "+cpg.getNameCPG()+"... ");
        CPGToDotGraph cpgToDotPDGORI = new CPGToDotGraph(cpg.getCPGNodes().get(0), cpg.getNameCPG());
        DotGraph CPGdotGraphPDGORI = cpgToDotPDGORI.drawCPG();
        CPGdotGraphPDGORI.plot("/home/djack/Desktop/cheSucc/ORIGINAL:" + cpg.getNameCPG() + ".dot");
        System.out.println(cpg.getNameCPG()+" COMPLETE!");

        cpg.generateSlicingCPG();

        for(CodePropertyGraph tempCPG: cpg.getSlicingsCPG()){
            System.out.print("Printing "+tempCPG.getNameCPG()+"... ");
            CPGToDotGraph cpgToDotPDG = new CPGToDotGraph(tempCPG.getCPGNodes().get(0), tempCPG.getNameCPG());
            DotGraph CPGdotGraphPDG = cpgToDotPDG.drawCPG();
            CPGdotGraphPDG.plot("/home/djack/Desktop/cheSucc/" + tempCPG.getNameCPG() + ".dot");
            System.out.println(cpg.getNameCPG()+" COMPLETE!");
        }


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
            //cgmm.printAdjListOnFile_COMPLETE(nedoPath + "/extTool/CGMM/graph/base_COM/");
            cgmm.printAdjListOnFile_STMTandTNodes(nedoPath + "/extTool/CGMM/graph/base_SeT/");
            System.out.println("DONE!");
        }


        //CPGToDotGraph cpgToDotPDG = new CPGToDotGraph(cpg.getCPGNodes().get(0), cpg.getNameCPG());
        //DotGraph CPGdotGraphPDG = cpgToDotPDG.drawCPG();
        //CPGdotGraphPDG.plot(nedoPath + "/graphs/3a/" + cpg.getNameCPG() + ".dot");

        System.out.println("ALL DONE!");

    }

    private static class infoExec{

        boolean struc2vec;
        boolean CGMM;
        boolean senFormat;
        boolean simply;

        public infoExec(){
            this.struc2vec=false;
            this.CGMM=false;
            this.senFormat=false;
            this.simply=false;
        }

        public void setStruc2vec(){ this.struc2vec=true; }
        public boolean isStruc2vec(){ return this.struc2vec; }

        public void setCGMM(){ this.CGMM=true; }
        public boolean isCGMM(){ return this.CGMM; }

        public void setSenFormat(){ this.senFormat=true; }
        public boolean isSenFormat(){ return this.senFormat; }

        public void setSimply() { this.simply=true; }
        public boolean isSimply() { return this.simply; }

    }
}
