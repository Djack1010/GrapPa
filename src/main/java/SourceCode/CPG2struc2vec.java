package SourceCode;

import soot.jimple.parser.node.*;

import java.io.PrintWriter;

public class CPG2struc2vec extends CPG2vec {
    //String pathStru2vec;

    public CPG2struc2vec(CodePropertyGraph cpg, String pathNedo, boolean needDecr) {
        super(cpg,pathNedo,needDecr);
        //this.pathStru2vec = pathStruc2vec;
        this.edgeList=this.createEdgeListOnlyCPG();
        this.nodeLabels=this.createNodeLabelsListOnlyCPG();
    }

    public void printEdgeListOnFile() {
        try {
            PrintWriter out = new PrintWriter(this.pathNedo + "/extTool/struc2vec/graph/base/" + this.cpg.getNameCPG() + ".edgelist", "UTF-8");
            out.println(this.edgeList);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printNodeListOnFile() {
        try {
            PrintWriter out = new PrintWriter(this.pathNedo + "/extTool/struc2vec/graph/label/labels-" + this.cpg.getNameCPG() + ".txt", "UTF-8");
            out.println(this.nodeLabels);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

