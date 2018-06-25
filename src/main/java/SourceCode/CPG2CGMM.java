package SourceCode;

import java.io.PrintWriter;

public class CPG2CGMM extends CPG2vec{
    String nodeLabels2;

    public CPG2CGMM(CodePropertyGraph cpg, boolean needDecr) {
        super(cpg,needDecr);
        //this.pathStru2vec = pathStruc2vec;
        this.edgeList=this.createAdjacentListOnlyCPG();
        this.nodeLabels=this.createNodeLabelsListOnlyCPGStmts();
        this.nodeLabels2=this.createNodeLabelsListOnlyCPG();
    }

    public void printEdgeListOnFile(String pathOutput) {
        try {
            PrintWriter out = new PrintWriter(pathOutput + this.cpg.getNameCPG() + ".adjlist", "UTF-8");
            out.println(this.edgeList);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printNodeLabelsSPECIAL1(String pathOutput) {
        try {
            PrintWriter out = new PrintWriter(pathOutput + "labels-" + this.cpg.getNameCPG() + ".txt", "UTF-8");
            out.println(createNodeLabelsListSPECIAL1OnlyCPG());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printNodeLabelsSPECIAL2(String pathOutput) {
        try {
            PrintWriter out = new PrintWriter(pathOutput + "labels2-" + this.cpg.getNameCPG() + ".txt", "UTF-8");
            out.println(createNodeLabelsListSPECIAL2OnlyCPG());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
