package SourceCode;

import soot.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.ProgramDependenceGraph;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;

import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class MainCPG {

    //PATH
    final static String nedoPath ="/home/djack/IdeaProjects/nedo";
    final static String struct2vecPath ="/home/djack/Dropbox/thesis/external_material/struc2vec";

    private static class infoExec{
        boolean mutMode;
        boolean struc2vec;
        String classToAnalyzed;

        public infoExec(){
            this.mutMode=false;
            this.struc2vec=false;
        }

        public infoExec(String classToAnalyzed){
            this.classToAnalyzed=classToAnalyzed;
        }

        public String getClassToAnalyzed(){ return this.classToAnalyzed; }

        public void setClassToAnalyzed(String newClass){ this.classToAnalyzed=newClass; }

        public void setMutMode(){ this.mutMode=true; }
        public boolean isMutMode(){ return this.mutMode; }

        public void setStruc2vec(){ this.struc2vec=true; }
        public boolean isStruc2vec(){ return this.struc2vec; }

    }

    public static void main(String[] args) {

        final infoExec info = new infoExec();
        String[] sootArgs=null;

        if (args.length == 0) {
            sootArgs = new String[]{//pass arguments for Soot.Main
                    "-p",
                    "cg",
                    "verbose:false,all-reachable:true",//,library:any-subtype
                    "-w",
                    "-no-bodies-for-excluded",
                    "-full-resolver",
                    //"-pp",
                    "-cp",
                    "/home/djack/Desktop/Test_Folder/LANG3.4-MutGenerator/ApacheLang/src/main/java" +
                    ":/home/djack/Dropbox/thesis/external_material/jdk1.7.0_80/jre/lib/alt-rt.jar" +
                    ":/home/djack/Dropbox/thesis/external_material/jdk1.7.0_80/jre/lib/charsets.jar" +
                    ":/home/djack/Dropbox/thesis/external_material/jdk1.7.0_80/jre/lib/deploy.jar" +
                    ":/home/djack/Dropbox/thesis/external_material/jdk1.7.0_80/jre/lib/javaws.jar" +
                    ":/home/djack/Dropbox/thesis/external_material/jdk1.7.0_80/jre/lib/jce.jar" +
                    ":/home/djack/Dropbox/thesis/external_material/jdk1.7.0_80/jre/lib/jfr.jar" +
                    ":/home/djack/Dropbox/thesis/external_material/jdk1.7.0_80/jre/lib/jfxrt.jar" +
                    ":/home/djack/Dropbox/thesis/external_material/jdk1.7.0_80/jre/lib/jsse.jar" +
                    ":/home/djack/Dropbox/thesis/external_material/jdk1.7.0_80/jre/lib/management-agent.jar" +
                    ":/home/djack/Dropbox/thesis/external_material/jdk1.7.0_80/jre/lib/plugin.jar" +
                    ":/home/djack/Dropbox/thesis/external_material/jdk1.7.0_80/jre/lib/resource.jar" +
                    ":/home/djack/Dropbox/thesis/external_material/jdk1.7.0_80/jre/lib/rt.jar",
                    //"-allow-phantom-refs",
                    "-process-dir",
                    "/home/djack/Desktop/Test_Folder/LANG3.4-MutGenerator/ApacheLang/src/main/java/org/apache/commons/lang3",//IF /home/djack/Desktop/Test_Folder/LANG3.4-MutGenerator/ApacheLang/src/main/java gets a NULL POINTER EXCEPTION...
                    //"-main-class",
                    "org.apache.commons.lang3.MainTest",
                    //"org.apache.commons.lang3.reflect.ConstructorUtils"
                    //
                    //
                    //"-cp",
                    //"/home/djack/Desktop/Test_Folder/LANG3.4-MutGenerator/ApacheLang/src/main/java",
                    //"/home/djack/IdeaProjects/nedo/src/main/java",
                    //"-pp",
                    //"-w",//whole-body mode
                    //"-no-bodies-for-excluded",
                    //"org.apache.commons.lang3.AnnotationUtils"//,
            };
            info.setClassToAnalyzed("org.apache.commons.lang3.MainTest");
        }else if(args.length==11){//-> use the passed arguments
            sootArgs = new String[11];
            System.arraycopy( args, 0, sootArgs, 0, args.length );
            info.setClassToAnalyzed(args[args.length-1]);
        }else if(args.length==13){
            sootArgs = new String[12];
            String temp = null;
            switch (args[args.length-2]){
                case "-targetClass":
                    temp=args[args.length-1];
                    break;
                case "-mutationClass":
                    info.setMutMode();
                    temp=args[args.length-1];
                    break;
                default:
                    System.err.println("Invalid arguments " + args[args.length-2] + ", exiting...");
                    System.exit(0);
            }
            System.arraycopy( args, 0, sootArgs, 0, args.length-3 );
            sootArgs[sootArgs.length-2]=args[args.length-3];//DEFAULT MAIN CLASS
            sootArgs[sootArgs.length-1]=temp;//TARGET CLASS
            info.setClassToAnalyzed(temp);
        }else if(args.length==15){
            sootArgs = new String[12];
            String temp = null;
            String vecTool =null;
            switch (args[args.length-4]){
                case "-targetClass":
                    temp=args[args.length-3];
                    break;
                case "-mutationClass":
                    info.setMutMode();
                    temp=args[args.length-3];
                    break;
                default:
                    System.err.println("Invalid arguments " + args[args.length-3] + ", exiting...");
                    System.exit(0);
            }
            switch (args[args.length-2]){
                case "-graph2vec":
                    vecTool=args[args.length-1];
                    break;
                default:
                    System.err.println("Invalid arguments " + args[args.length-2] + ", exiting...");
                    System.exit(0);
            }
            switch (vecTool){
                case "struc2vec":
                    info.setStruc2vec();
                    break;
                default:
                    System.err.println("Invalid vec tool " + args[args.length-1] + ", exiting...");
                    System.exit(0);
            }
            System.arraycopy( args, 0, sootArgs, 0, args.length-5 );
            sootArgs[sootArgs.length-2]=args[args.length-5];//DEFAULT MAIN CLASS
            sootArgs[sootArgs.length-1]=temp;//TARGET CLASS
            info.setClassToAnalyzed(temp);
        }else{
            System.err.println("Invalid number of arguments, exiting...");
            System.exit(0);
        }

        final MainStats stats = new MainStats();

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans", new SceneTransformer() {//wjtp OR cg

            @Override
            protected void internalTransform(String phaseName, Map options) {//implement and add MyTrans

                System.err.println("STARTING MY TRANSFORMATION");


                //SootClass cl = Scene.v().getMainClass();
                for (SootClass cl : Scene.v().getApplicationClasses()) {

                    //if(cl.getName().equals(Scene.v().getMainClass().getName()))System.exit(0);
                    if(cl.getName().equals(info.getClassToAnalyzed())){
                        System.err.println("Starting Transformation for class " + cl.getName());
                        //System.exit(0);
                    }else{
                        //System.err.println(cl.getName());
                        continue;
                    }

                    stats.addClass(cl.getName());

                    Iterator<SootMethod> methodIt = cl.getMethods().iterator();
                    while (methodIt.hasNext()) {

                        stats.addMethod();

                        //System.err.println("QUA CI ARRIVO2");
                        SootMethod m = (SootMethod) methodIt.next();
                        //if(!(m.getName().equals("reflectionAppend")))continue;
                        if(!(m.hasActiveBody())){
                            System.err.println("No active body for method " + m.getName());
                            stats.addFailBDY();
                            continue;
                        }
                        Body body = m.retrieveActiveBody();

                        String[] partNameCl = cl.getName().split("\\.");
                        String nameMethod = partNameCl[partNameCl.length-1] + "_" + m.getName();


                        //De-comment for printing Jimple Code of Body method

                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        Printer.v().printTo(body, pw);
                        String inputString = "public class WrapClass \n{\n" + sw.toString() + "}";
                        try{
                            PrintWriter out = new PrintWriter(nedoPath + "/graphs/JimpleCode/"+nameMethod+".txt", "UTF-8");
                            out.println(inputString);
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        try {

                            System.out.println("Creating Code Property Graph for " + m.getName());
                            CodePropertyGraph cpg = new CodePropertyGraph(body, m.getName());

                            if(!(cpg.isInitializedSuccessfully())){
                                System.err.println("Skipping " + m.getName());
                                stats.addFailCPG();
                                continue;
                            }

                            //Print on file the cfg using CFGToDotGraph
                            ExceptionalUnitGraph cfg = new ExceptionalUnitGraph(body);
                            System.out.println("\tPrinting CFG on file");
                            CFGToDotGraph cfgToDot = new CFGToDotGraph();
                            DotGraph CFGdotGraph = cfgToDot.drawCFG(cfg, body);
                            CFGdotGraph.plot(nedoPath + "/graphs/CFGs/" + nameMethod + ".dot");

                            //Print on file the cfg using CFGToDotGraph
                            ProgramDependenceGraph pdg = new HashMutablePDG(cfg);
                            System.out.println("\tPrinting PDG on file");
                            PDGToDotGraph pdgToDot = new PDGToDotGraph(pdg, nameMethod);
                            DotGraph PDGdotGraph = pdgToDot.drawPDG();
                            PDGdotGraph.plot(nedoPath + "/graphs/PDGs/" + nameMethod + ".dot");

                            //Print on file the cfg using CFGToDotGraph
                            System.out.println("\tPrinting CPG=AST on file");
                            cpg.buildCPGphase("AST");
                            CPGToDotGraph cpgToDotAST = new CPGToDotGraph(cpg.getASTrootNode(), m.getName());
                            DotGraph CPGdotGraphAST = cpgToDotAST.drawCPG();
                            CPGdotGraphAST.plot(nedoPath + "/graphs/1/" + nameMethod + ".dot");

                            //Print on file the cfg using CFGToDotGraph
                            System.out.println("\tPrinting CPG=AST+CFG on file");
                            cpg.buildCPGphase("CFG");
                            CPGToDotGraph cpgToDotCFG = new CPGToDotGraph(cpg.getRootNode(), m.getName());
                            DotGraph CPGdotGraphCFG = cpgToDotCFG.drawCPG();
                            CPGdotGraphCFG.plot(nedoPath + "/graphs/2/" + nameMethod + ".dot");

                            if(info.isMutMode()){
                                //Print on file the cfg using CFGToDotGraph
                                System.out.println("\tPrinting CPG=AST+CFG+PDG on file");
                                cpg.buildCPGphase("PDG");
                                CPGToDotGraph cpgToDotPDG = new CPGToDotGraph(cpg.getRootNode(), m.getName());
                                DotGraph CPGdotGraphPDG = cpgToDotPDG.drawCPG();
                                int countMut = 0;
                                File f = new File(nedoPath + "/graphs/3_mut/" + nameMethod + "_" + countMut + ".dot");
                                while(f.exists() && !f.isDirectory()) {
                                    countMut++;
                                    f= new File(nedoPath + "/graphs/3_mut/" + nameMethod + "_" + countMut + ".dot");
                                }
                                CPGdotGraphPDG.plot(nedoPath + "/graphs/3_mut/" + nameMethod + "_" + countMut + ".dot");
                            }else {
                                //Print on file the cfg using CFGToDotGraph
                                System.out.println("\tPrinting CPG=AST+CFG+PDG on file");
                                cpg.buildCPGphase("PDG");
                                CPGToDotGraph cpgToDotPDG = new CPGToDotGraph(cpg.getRootNode(), m.getName());
                                DotGraph CPGdotGraphPDG = cpgToDotPDG.drawCPG();
                                CPGdotGraphPDG.plot(nedoPath + "/graphs/3/" + nameMethod + ".dot");
                            }

                            System.out.println("\tALL DONE!");

                            if (info.isStruc2vec()){
                                System.out.print("\tPrinting CPG in input format for struct2vec...");
                                CPG2struc2vec s2v = new CPG2struc2vec(cpg,struct2vecPath,nedoPath);
                                s2v.printEdgeListOnFile();
                                System.out.println("DONE!");
                                //if(cpg.getSize()==cpg.getCPGNodes().size())System.out.println("ALLRIGHT!");
                                //else System.out.println(cpg.getSize()+" not equals to "+cpg.getCPGNodes().size());
                            }

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                }

                //System.err.println("Forcing exit...");
                //System.exit(0);

            }//end internalTransfor

        }));

        //run Soot
        //try {
            //System.err.print("ARGUMENTS: ");
            //for(int i=0; i<sootArgs.length;i++){
            //    System.err.print(sootArgs[i] + " ");
            //}
            //System.err.println();
            soot.Main.main(sootArgs);
        //} catch (Exception e) {
        //    System.out.println("Exception catched: " + e);
        //    System.exit(0);
        //}

        stats.printStats();
        stats.printStatsShort();
        System.exit(1);

    }

    private static class MainStats{
        String className=null;
        int failedCPG =0;
        int failedBDY =0;
        //boolean requiredCheck = false;
        int totMethod =0;
        //Set<String> nameManCheck;

        public MainStats(){}

        public void printStats(){
            System.out.println("\nFINAL RESULT");
            System.out.println("Fail-CPG ------->\t"+this.failedCPG);
            System.out.println("No-Body -------->\t"+this.failedBDY);
            System.out.println("Total success -->\t"+(this.totMethod-this.failedBDY-this.failedCPG));
            System.out.println("Total-Analyzed ->\t"+this.totMethod);
            //System.out.println("Required check ->\t"+this.requiredCheck);
        }

        public void printStatsShort(){
            String percentage = "0";
            if(this.totMethod!=0){
                DecimalFormat df = new DecimalFormat("##.##");
                df.setRoundingMode(RoundingMode.CEILING);
                Number totSuc = (((double) this.totMethod - this.failedBDY - this.failedCPG) / this.totMethod) * 100;
                Double d = totSuc.doubleValue();
                percentage = df.format(d);
            }
            System.out.println("RESULT " + this.className +" ::: Fail-CPG ("+this.failedCPG+") No-Body ("+this.failedBDY+") Total-Analyzed ("+this.totMethod+") Total Success ("+percentage+"%)");
            //if(this.requiredCheck)System.out.println("RESULT Method(s) to check -> " + nameManCheck.toString());
        }

        public void addFailCPG(){ this.failedCPG++;}
        public void addClass(String name){ this.className=name;}
        public void addFailBDY(){ this.failedBDY++;}
        public void addMethod(){ this.totMethod++;}
        //public void setRequiredCheck(){this.requiredCheck=true;}
        //public void addCheckMethod(String name){ this.nameManCheck.add(name);}
    }
}