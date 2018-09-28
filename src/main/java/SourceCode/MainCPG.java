package SourceCode;

import soot.*;
import soot.options.Options;
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
    static String nedoPath=null;
    final static infoExec info = new infoExec();

    private static class infoExec{

        boolean mutMode;
        boolean struc2vec;
        boolean CGMM;
        boolean senFormat;

        boolean simply;
        boolean overloading;
        boolean methodFound;
        String classToAnalyzed;
        String methodToAnalyzed;

        public infoExec(){
            this.mutMode=false;
            this.struc2vec=false;
            this.CGMM=false;
            this.senFormat=false;
            this.overloading=false;
            this.methodFound=false;
            this.methodToAnalyzed=null;
            this.simply=false;
        }

        public String getClassToAnalyzed(){ return this.classToAnalyzed; }
        public String getMethodToAnalyzed(){ return this.methodToAnalyzed; }

        public void setClassToAnalyzed(String newClass){ this.classToAnalyzed=newClass; }
        public void setMethodToAnalyzed(String newMethod){ this.methodToAnalyzed=newMethod; }

        public void setMutMode(){ this.mutMode=true; }
        public boolean isMutMode(){ return this.mutMode; }

        public void setStruc2vec(){ this.struc2vec=true; }
        public boolean isStruc2vec(){ return this.struc2vec; }

        public void setCGMM(){ this.CGMM=true; }
        public boolean isCGMM(){ return this.CGMM; }

        public void setSenFormat(){ this.senFormat=true; }
        public boolean isSenFormat(){ return this.senFormat; }

        public void setOverloading(){ this.overloading=true; }
        public boolean isOverloading(){ return this.overloading; }

        public void setMethodFound(){ this.methodFound=true; }
        public boolean isMethodFound(){ return this.methodFound; }

        public void setSimply() { this.simply=true; }
        public boolean isSimply() { return this.simply; }

    }

    private static String[] handleArgs(String[] args){
        String [] myArrayArgs = new String[args.length];
        int i=0;
        int j=0;
        while(i<args.length){
            switch (args[i]){
                case "-p":
                case "cg":
                case "all-reachable:true":
                case "-w":
                case "-no-bodies-for-excluded":
                case "-full-resolver":
                    myArrayArgs[j]=args[i];
                    j++;
                    break;
                case "-cp":
                case "-process-dir":
                    myArrayArgs[j]=args[i];
                    i++;
                    j++;
                    myArrayArgs[j]=args[i];
                    j++;
                    break;
                case "-pf":
                    i++;
                    nedoPath=args[i];
                    break;
                case "-mainClass":
                    i++;
                    myArrayArgs[j]=args[i];
                    j++;
                    break;
                case "-targetClass":
                    i++;
                    myArrayArgs[j]=args[i];
                    j++;
                    info.setClassToAnalyzed(args[i]);
                    break;
                case "-mutationClass":
                    i++;
                    myArrayArgs[j]=args[i];
                    j++;
                    info.setClassToAnalyzed(args[i]);
                    info.setMutMode();
                    break;
                case "-targetMethod":
                    i++;
                    info.setMethodToAnalyzed(args[i]);
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
                case "-simply":
                    info.setSimply();
                    break;
                default:
                    System.err.println("MainCPG:ERROR:Invalid arguments " + args[i] + ", exiting...");
                    System.exit(0);
                    break;
            }
            i++;
        }
        if(j!=myArrayArgs.length){
            String[] tempArray = new String[j];
            System.arraycopy( myArrayArgs, 0, tempArray, 0, tempArray.length );
            return tempArray;
        }else return myArrayArgs;
    }

    public static void main(String[] args) {

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
                    //"/home/djack/Desktop/Test_Folder/LANG3.4-MutGenerator/ApacheLang/src/main/java" +
                    "/home/djack/Desktop/Test_Folder/Box4Test/sourceCode" +
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
                    "/home/djack/Desktop/Test_Folder/Box4Test/sourceCode",//IF /home/djack/Desktop/Test_Folder/LANG3.4-MutGenerator/ApacheLang/src/main/java gets a NULL POINTER EXCEPTION...
                    "-main-class",
                    "box4test.MainTest",
                    //"org.apache.commons.lang3.AnnotationUtils"
                    //
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
            nedoPath ="/home/djack/IdeaProjects/nedo";
            info.setClassToAnalyzed("box4test.test1");
            info.setSimply();
            //info.setMethodToAnalyzed("abbreviate:6460");
            //info.setStruc2vec();
            //info.setCGMM();
        }else sootArgs=handleArgs(args);

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

                    stats.setClass(cl.getName());

                    Iterator<SootMethod> methodIt = cl.getMethods().iterator();
                    while (methodIt.hasNext()) {

                        SootMethod m = (SootMethod) methodIt.next();
                        System.err.println("method "+m.getName());
                        Body body = null;

                        if((info.getMethodToAnalyzed()!=null) && !(m.getName().equals(info.getMethodToAnalyzed().split(":")[0]))){
                            stats.setMethod(info.getMethodToAnalyzed());
                            continue;
                        }
                        else if ((info.getMethodToAnalyzed()!=null) && (m.getName().equals(info.getMethodToAnalyzed().split(":")[0]))){
                            if(!(m.hasActiveBody())){
                                stats.addMethod();
                                System.err.println("No active body for method " + m.getName());
                                stats.addFailBDY();
                                continue;
                            }
                            body = m.retrieveActiveBody();
                            String methodLineTag = body.getUnits().getFirst().getTags().toString();
                            //unicodeEscaped:354
                            if(methodLineTag.contains("Source Line Pos Tag: sline: ")){
                                methodLineTag=methodLineTag.split(" sline: ")[1].split(" eline: ")[0];
                                if( (methodLineTag.equals(info.getMethodToAnalyzed().split(":")[1])) ||
                                         ( Math.abs(Integer.parseInt(methodLineTag)-Integer.parseInt(info.getMethodToAnalyzed().split(":")[1])) <= 2 ) ){
                                    info.setMethodFound();
                                    if(info.isOverloading()){
                                        System.err.println("OVERLOADING for method "+m.getName());
                                        stats.setOverload();
                                        break;
                                    }else info.setOverloading();
                                }else continue;
                            }
                        } else {//(info.getMethodToAnalyzed()==null)
                            if(!(m.hasActiveBody())){
                                stats.addMethod();
                                System.err.println("No active body for method " + m.getName());
                                stats.addFailBDY();
                                continue;
                            }
                            body = m.retrieveActiveBody();
                        }
                        stats.addMethod();

                        String[] partNameCl = cl.getName().split("\\.");
                        String nameMethod = partNameCl[partNameCl.length - 1] + "_";
                        if(info.getMethodToAnalyzed()!=null){
                            nameMethod=nameMethod+info.getMethodToAnalyzed();
                        }else {
                            nameMethod=nameMethod+m.getName();
                        }

                        if(info.isMutMode()){
                            int countMut = 1;
                            checkAndCreateFolder(nedoPath + "/graphs/3_mut");
                            File f = new File(nedoPath + "/graphs/3_mut/" + nameMethod + "_" + countMut + ".dot");
                            while(f.exists() && !f.isDirectory()) {
                                countMut++;
                                f= new File(nedoPath + "/graphs/3_mut/" + nameMethod + "_" + countMut + ".dot");
                            }
                            nameMethod=nameMethod+"_"+countMut;
                        }else{
                            nameMethod=nameMethod+"_0";
                        }


                        //De-comment for printing Jimple Code of Body method
                        //StringWriter sw = new StringWriter();
                        //PrintWriter pw = new PrintWriter(sw);
                        //Printer.v().printTo(body, pw);
                        //String inputString = "public class WrapClass \n{\n" + sw.toString() + "}";
                        //try{
                        //    checkAndCreateFolder(nedoPath + "/graphs/JimpleCode");
                        //    PrintWriter out = new PrintWriter(nedoPath + "/graphs/JimpleCode/"+nameMethod+".txt", "UTF-8");
                        //    out.println(inputString);
                        //    out.close();
                        //} catch (Exception e) {
                        //    e.printStackTrace();
                        //}


                        try {

                            System.out.println("Creating Code Property Graph for " + m.getName());
                            CodePropertyGraph cpg = new CodePropertyGraph(body, nameMethod);

                            if(!(cpg.isInitializedSuccessfully())){
                                System.err.println("Skipping " + m.getName());
                                stats.addFailCPG();
                                continue;
                            }

                            //Print on file the cfg using CFGToDotGraph
                            //ExceptionalUnitGraph cfg = new ExceptionalUnitGraph(body);
                            //System.out.println("\tPrinting CFG on file");
                            //CFGToDotGraph cfgToDot = new CFGToDotGraph();
                            //DotGraph CFGdotGraph = cfgToDot.drawCFG(cfg, body);
                            //checkAndCreateFolder(nedoPath + "/graphs/CFGs");
                            //CFGdotGraph.plot(nedoPath + "/graphs/CFGs/" + nameMethod + ".dot");

                            //Print on file the pdg using PDGToDotGraph
                            //ProgramDependenceGraph pdg = new HashMutablePDG(cfg);
                            //System.out.println("\tPrinting PDG on file");
                            //PDGToDotGraph pdgToDot = new PDGToDotGraph(pdg, nameMethod);
                            //DotGraph PDGdotGraph = pdgToDot.drawPDG();
                            //checkAndCreateFolder(nedoPath + "/graphs/PDGs");
                            //PDGdotGraph.plot(nedoPath + "/graphs/PDGs/" + nameMethod + ".dot");

                            //Print on file the cpg-part1 using CPGToDotGraph
                            //System.out.println("\tPrinting CPG=AST on file");
                            cpg.buildCPGphase("AST");
                            //CPGToDotGraph cpgToDotAST = new CPGToDotGraph(cpg.getASTrootNode(), m.getName());
                            //DotGraph CPGdotGraphAST = cpgToDotAST.drawCPG();
                            //checkAndCreateFolder(nedoPath + "/graphs/1");
                            //CPGdotGraphAST.plot(nedoPath + "/graphs/1/" + nameMethod + ".dot");

                            //Print on file the cpg-part2 using CPGToDotGraph
                            //System.out.println("\tPrinting CPG=AST+CFG on file");
                            cpg.buildCPGphase("CFG");
                            //CPGToDotGraph cpgToDotCFG = new CPGToDotGraph(cpg.getRootNode(), m.getName());
                            //DotGraph CPGdotGraphCFG = cpgToDotCFG.drawCPG();
                            //checkAndCreateFolder(nedoPath + "/graphs/2");
                            //CPGdotGraphCFG.plot(nedoPath + "/graphs/2/" + nameMethod + ".dot");

                            //Print on file the cpg-part3 using CPGToDotGraph
                            System.out.println("\tPrinting CPG=AST+CFG+PDG on file");
                            cpg.buildCPGphase("PDG");
                            if(info.isSimply()) cpg.simplifyGraph();
                            //CPGToDotGraph cpgToDotPDG = new CPGToDotGraph(cpg.getRootNode(), m.getName());
                            //DotGraph CPGdotGraphPDG = cpgToDotPDG.drawCPG();
                            //if(info.isMutMode()){
                                //checkAndCreateFolder(nedoPath + "/graphs/3_mut");
                                //CPGdotGraphPDG.plot(nedoPath + "/graphs/3_mut/" + nameMethod + ".dot");
                            //}else {
                                //checkAndCreateFolder(nedoPath + "/graphs/3");
                                //CPGdotGraphPDG.plot(nedoPath + "/graphs/3/" + nameMethod + ".dot");
                            //}

                            //String CPGGraph = cpg.getCPGtoString();
                            //try {
                                //PrintWriter out;
                                //out = new PrintWriter( nedoPath + "/graphs/example/" + nameMethod + ".nedo", "UTF-8");
                                //out.println(CPGGraph);
                                //out.close();
                            //} catch (Exception e) {
                                //e.printStackTrace();
                            //}

                            System.out.println("\tALL DONE!");

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
                            if (info.isSenFormat()){
                                System.out.print("\tPrinting CPG in input format for SenFormat...");
                                //CPG2SenFormat sf = new CPG2SenFormat(cpg,true);
                                //sf.printGraphOnFile(nedoPath + "/extTool/senFormat/graph1/");
                                //sf.printGraphOnFile2(nedoPath + "/extTool/senFormat/graph2/");
                                System.out.println("DONE!");
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
        //RunSootThread runSoot = new RunSootThread("RunSootThread",sootArgs);
        try {
            //System.err.print("ARGUMENTS: ");
            //for(int i=0; i<sootArgs.length;i++){
            //    System.err.print(sootArgs[i] + " ");
            //}
            //System.err.println();
            Options.v().set_keep_line_number(true);
            soot.Main.main(sootArgs);
            //runSoot.run();
            //runSoot.join(60);
            //if(runSoot.isAlive())throw new SootExecMyException("Time limit for Soot execution!");
        } catch (OutOfMemoryError e) {
            System.out.println("ERROR -> OutOfMemoryError: " + e);
            System.exit(0);
        } catch (NullPointerException ex) {
            System.out.println("ERROR -> NullPointerException: " + ex);
            System.exit(0);
        }
        /*
        catch (SootExecMyException exxx) {
            //System.out.println("ERRORONE, è na borgia... " + exxx);
            //runSoot.myStop();
            System.exit(0);
        }*/

        //stats.printStats();
        stats.printStatsShort();
        System.exit(1);

    }

    /*
    private static class RunSootThread extends Thread{
        private String name;
        private String[] inputArgs;

        RunSootThread(String name, String[] inputArgs) {
            this.inputArgs=inputArgs;
        }

        @Override
        public void run() {
            soot.Main.main(this.inputArgs);
        }

        public void myStop() {
            this.interrupt();
        }

    }
    */

    private static void checkAndCreateFolder(String folderPath){
        File directory = new File(folderPath);
        if (! directory.exists()){
            directory.mkdirs();
        }
    }

    private static class MainStats{
        String className=null;
        String methodName=null;
        int failedCPG =0;
        int failedBDY =0;
        int totMethod =0;
        boolean overload=false;
        //Set<String> nameManCheck;

        public MainStats(){}

        public void printStats(){
            System.out.println("\nFINAL RESULT");
            System.out.println("Fail-CPG ------->\t"+this.failedCPG);
            System.out.println("No-Body -------->\t"+this.failedBDY);
            System.out.println("Total success -->\t"+(this.totMethod-this.failedBDY-this.failedCPG));
            System.out.println("Total-Analyzed ->\t"+this.totMethod);
            System.out.println("OVERLOADING ---->\t"+this.overload);
            if(info.getMethodToAnalyzed()!=null){
                System.out.println("METHOD-FOUND---->\t"+info.isMethodFound());
            }
        }

        public void printStatsShort(){
            String percentage = "N.A.";
            if(this.totMethod!=0){
                DecimalFormat df = new DecimalFormat("##.##");
                df.setRoundingMode(RoundingMode.CEILING);
                Number totSuc = (((double) this.totMethod - this.failedBDY - this.failedCPG) / this.totMethod) * 100;
                Double d = totSuc.doubleValue();
                percentage = df.format(d);
            }
            System.out.print("RESULT " + this.className +" ::: Fail-CPG ("+this.failedCPG+") No-Body ("+this.failedBDY+") Total-Analyzed ("+this.totMethod+") MUT ("+info.isMutMode()+") Total Success perc. ("+percentage+")");
            if(this.overload){
                System.out.println(" OVERLOADING ("+this.overload+")");
            } else if((info.getMethodToAnalyzed()!=null) && (!info.isMethodFound())){
                System.out.println(" METHOD-NOT-FOUND! ("+this.methodName+")");
            } else System.out.println();
        }

        public void addFailCPG(){ this.failedCPG++;}
        public void setClass(String name){ this.className=name;}
        public void setMethod(String name){ this.methodName=name;}
        public void addFailBDY(){ this.failedBDY++;}
        public void addMethod(){ this.totMethod++;}
        public void setOverload(){this.overload=true;}
        //public void addCheckMethod(String name){ this.nameManCheck.add(name);}
    }
}