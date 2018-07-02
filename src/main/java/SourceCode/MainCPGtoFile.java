package SourceCode;

import SourceCode.CPGEdge;
import SourceCode.CPGNode;
import SourceCode.CodePropertyGraph;
import ppg.code.Code;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class MainCPGtoFile {
    //PATH
    static String nedoPath;
    final static infoExec info = new infoExec();


    private static class infoExec{

        boolean mutMode;
        boolean struc2vec;
        boolean CGMM;
        boolean senFormat;

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
        }

        public String getClassToAnalyzed(){ return this.classToAnalyzed; }
        public String getMethodToAnalyzed(){ return this.methodToAnalyzed; }

        public void setClassToAnalyzed(String newClass){ this.classToAnalyzed=newClass; }
        public void setMethodToAnalyzed(String newMethod){ this.methodToAnalyzed=newMethod; }

        public void setMutMode(){ this.mutMode=true; }
        public boolean isMutMode(){ return this.mutMode; }

        public void setOverloading(){ this.overloading=true; }
        public boolean isOverloading(){ return this.overloading; }

        public void setMethodFound(){ this.methodFound=true; }
        public boolean isMethodFound(){ return this.methodFound; }

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
                    "org.apache.commons.lang3.text.ExtendedMessageFormat"
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
            info.setClassToAnalyzed("org.apache.commons.text.ExtendedMessageFormat");
            info.setMethodToAnalyzed("readArgumentIndex:327");
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

                        //System.err.println("QUA CI ARRIVO2");
                        SootMethod m = (SootMethod) methodIt.next();
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
                            checkAndCreateFolder(nedoPath + "/graphDB/mutated");
                            File f = new File(nedoPath + "/graphDB/mutated/" + nameMethod + "_" + countMut + ".nedo");
                            while(f.exists() && !f.isDirectory()) {
                                countMut++;
                                f= new File(nedoPath + "/graphDB/mutated/" + nameMethod + "_" + countMut + ".nedo");
                            }
                            nameMethod=nameMethod+"_"+countMut;
                        }else{
                            nameMethod=nameMethod+"_0";
                        }


                        //De-comment for printing Jimple Code of Body method
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        Printer.v().printTo(body, pw);
                        String inputString = "public class WrapClass \n{\n" + sw.toString() + "}";
                        try{
                            checkAndCreateFolder(nedoPath + "/graphDB/JimpleCode");
                            PrintWriter out = new PrintWriter(nedoPath + "/graphDB/JimpleCode/"+nameMethod+".txt", "UTF-8");
                            out.println(inputString);
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        try {

                            System.out.println("Creating Code Property Graph for " + m.getName());
                            CodePropertyGraph cpg = new CodePropertyGraph(body, nameMethod);

                            if(!(cpg.isInitializedSuccessfully())){
                                System.err.println("Skipping " + m.getName());
                                stats.addFailCPG();
                                continue;
                            }

                            //Print on file the cpg-part1 using CPGToDotGraph
                            System.out.println("\tCreating AST");
                            cpg.buildCPGphase("AST");

                            //Print on file the cpg-part2 using CPGToDotGraph
                            System.out.println("\tCreating CPG=AST+CFG");
                            cpg.buildCPGphase("CFG");

                            //Print on file the cpg-part3 using CPGToDotGraph
                            System.out.println("\tCreating CPG=AST+CFG+PDG");
                            cpg.buildCPGphase("PDG");

                            System.out.println("\tSaving complete CPG");
                            String CPGGraph = cpg.getCPGtoString();
                            try {
                                PrintWriter out;
                                if(info.isMutMode()) out = new PrintWriter( nedoPath + "/graphDB/mutated/" + nameMethod + ".nedo", "UTF-8");
                                else out = new PrintWriter( nedoPath + "/graphDB/original/" + nameMethod + ".nedo", "UTF-8");
                                    out.println(CPGGraph);
                                out.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            System.out.println("\tALL DONE!");

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                }

            }//end internalTransfor

        }));

        //run Soot
        try {
            //System.err.print("ARGUMENTS: ");
            //for(int i=0; i<sootArgs.length;i++){
            //    System.err.print(sootArgs[i] + " ");
            //}
            //System.err.println();
            Options.v().set_keep_line_number(true);
            soot.Main.main(sootArgs);
        } catch (OutOfMemoryError e) {
            System.out.println("ERROR -> OutOfMemoryError: " + e);
            System.exit(0);
        } catch (NullPointerException ex) {
            System.out.println("ERROR -> NullPointerException: " + ex);
            System.exit(0);
        }

        //stats.printStats();
        stats.printStatsShort();
        System.exit(1);

    }



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
