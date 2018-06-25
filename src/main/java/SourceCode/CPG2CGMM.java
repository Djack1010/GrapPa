package SourceCode;

import java.io.PrintWriter;

public class CPG2CGMM extends CPG2vec{

    public CPG2CGMM(CodePropertyGraph cpg, boolean needDecr) {
        super(cpg,needDecr);
        //this.pathStru2vec = pathStruc2vec;
        this.edgeList=this.createAdjacentListOnlyCPG();
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

    public void printNodeLabels_CONF1(String pathOutput) {
        try {
            PrintWriter out = new PrintWriter(pathOutput + "labels_C1-" + this.cpg.getNameCPG() + ".txt", "UTF-8");
            out.println(this.createNodeLabelsOnlyCPG_CONF1());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printNodeLabels_CONF2(String pathOutput) {
        try {
            PrintWriter out = new PrintWriter(pathOutput + "labels_C2-" + this.cpg.getNameCPG() + ".txt", "UTF-8");
            out.println(this.createNodeLabelsOnlyCPG_CONF2());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printNodeLabels_CONF3(String pathOutput) {
        try {
            PrintWriter out = new PrintWriter(pathOutput + "labels_C3-" + this.cpg.getNameCPG() + ".txt", "UTF-8");
            out.println(this.createNodeLabelsOnlyCPG_CONF3());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
