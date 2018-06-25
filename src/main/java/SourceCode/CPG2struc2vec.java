package SourceCode;

import soot.jimple.parser.node.*;

import java.io.PrintWriter;

public class CPG2struc2vec extends CPG2vec {
    //String pathStru2vec;
    String nodeLabels2;

    public CPG2struc2vec(CodePropertyGraph cpg, boolean needDecr) {
        super(cpg,needDecr);
        //this.pathStru2vec = pathStruc2vec;
        this.edgeList=this.createEdgeListOnlyCPG();
        this.nodeLabels=this.createNodeLabelsListOnlyCPGStmts();
        this.nodeLabels2=this.createNodeLabelsListOnlyCPG();
    }

    public void printEdgeListOnFile(String pathOutput) {
        try {
            PrintWriter out = new PrintWriter(pathOutput + this.cpg.getNameCPG() + ".edgelist", "UTF-8");
            out.println(this.edgeList);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printNodeListOnFile(String pathOutput) {
        try {
            PrintWriter out = new PrintWriter(pathOutput + "labels-" + this.cpg.getNameCPG() + ".txt", "UTF-8");
            out.println(this.nodeLabels);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printNodeListOnFile2(String pathOutput) {
        try {
            PrintWriter out = new PrintWriter(pathOutput +"labels2-" + this.cpg.getNameCPG() + ".txt", "UTF-8");
            out.println(this.nodeLabels2);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printNodeLabelsSPECIAL1(String pathOutput){
        try {
            PrintWriter out = new PrintWriter(pathOutput +"labelsS-" + this.cpg.getNameCPG() + ".txt", "UTF-8");
            out.println(this.createNodeLabelsListSPECIAL1OnlyCPG());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printNodeLabelsSPECIAL2(String pathOutput){
        try {
            PrintWriter out = new PrintWriter(pathOutput +"labelsS-" + this.cpg.getNameCPG() + ".txt", "UTF-8");
            out.println(this.createNodeLabelsListSPECIAL2OnlyCPG());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

