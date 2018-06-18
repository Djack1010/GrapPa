package SourceCode;

import java.io.PrintWriter;

public class CPG2CGMM extends CPG2vec{
    String nodeLabels2;

    public CPG2CGMM(CodePropertyGraph cpg, String pathNedo, boolean needDecr) {
        super(cpg,pathNedo,needDecr);
        //this.pathStru2vec = pathStruc2vec;
        this.edgeList=this.createAdjacentListOnlyCPG();
        this.nodeLabels=this.createNodeLabelsListOnlyCPGStmts();
        this.nodeLabels2=this.createNodeLabelsListOnlyCPG();
    }

    public void printEdgeListOnFile() {
        try {
            PrintWriter out = new PrintWriter(this.pathNedo + "/extTool/CGMM/graph/base/" + this.cpg.getNameCPG() + ".adjlist", "UTF-8");
            out.println(this.edgeList);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printNodeListOnFile() {
        try {
            PrintWriter out = new PrintWriter(this.pathNedo + "/extTool/CGMM/graph/label/labels-" + this.cpg.getNameCPG() + ".txt", "UTF-8");
            out.println(this.nodeLabels);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printNodeListOnFile2() {
        try {
            PrintWriter out = new PrintWriter(this.pathNedo + "/extTool/CGMM/graph/label2/labels2-" + this.cpg.getNameCPG() + ".txt", "UTF-8");
            out.println(this.nodeLabels2);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
