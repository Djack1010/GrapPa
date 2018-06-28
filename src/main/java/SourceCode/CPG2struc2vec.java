package SourceCode;

import soot.jimple.parser.node.*;

import java.io.PrintWriter;

public class CPG2struc2vec extends CPG2vec {
    //String pathStru2vec;

    public CPG2struc2vec(CodePropertyGraph cpg, boolean needDecr) {
        super(cpg,needDecr);
        //this.pathStru2vec = pathStruc2vec;
        this.edgeList=this.createEdgeListOnlyCPG();
    }

    /*
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
            out.println(this.createNodeLabelsListOnlyCPGStmts());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printNodeListOnFile2(String pathOutput) {
        try {
            PrintWriter out = new PrintWriter(pathOutput +"labels2-" + this.cpg.getNameCPG() + ".txt", "UTF-8");
            out.println(this.createNodeLabelsListOnlyCPG());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printNodeLabels_CONF1(String pathOutput){
        try {
            PrintWriter out = new PrintWriter(pathOutput +"labels_C1-" + this.cpg.getNameCPG() + ".txt", "UTF-8");
            out.println(this.createNodeLabelsOnlyCPG_CONF1());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printNodeLabels_CONF2(String pathOutput){
        try {
            PrintWriter out = new PrintWriter(pathOutput +"labels_C2-" + this.cpg.getNameCPG() + ".txt", "UTF-8");
            out.println(this.createNodeLabelsOnlyCPG_CONF2());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printNodeLabels_CONF3(String pathOutput){
        try {
            PrintWriter out = new PrintWriter(pathOutput +"labels_C3-" + this.cpg.getNameCPG() + ".txt", "UTF-8");
            out.println(this.createNodeLabelsOnlyCPG_CONF3());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printNodeLabels_CONF4(String pathOutput){
        try {
            PrintWriter out = new PrintWriter(pathOutput +"labels_C4-" + this.cpg.getNameCPG() + ".txt", "UTF-8");
            out.println(this.createNodeLabelsOnlyCPG_CONF4());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printNodeLabels_CONF5(String pathOutput){
        try {
            PrintWriter out = new PrintWriter(pathOutput +"labels_C5-" + this.cpg.getNameCPG() + ".txt", "UTF-8");
            out.println(this.createNodeLabelsOnlyCPG_CONF5());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

}

