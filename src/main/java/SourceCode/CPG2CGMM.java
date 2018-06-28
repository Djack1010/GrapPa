package SourceCode;

import java.io.PrintWriter;

public class CPG2CGMM extends CPG2vec{

    public CPG2CGMM(CodePropertyGraph cpg, boolean needDecr) {
        super(cpg,needDecr);
        //this.pathStru2vec = pathStruc2vec;
    }

    public void printAdjListOnFile_COMPLETE(String pathOutput) {
        try {
            PrintWriter out = new PrintWriter(pathOutput + this.cpg.getNameCPG() + ".adjlist", "UTF-8");
            out.println(this.createAdjacentListOnlyCPG_COMPLETE());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printAdjListOnFile_STMTandTNodes(String pathOutput) {
        try {
            PrintWriter out = new PrintWriter(pathOutput + this.cpg.getNameCPG() + ".adjlist", "UTF-8");
            out.println(this.createAdjacentListOnlyCPG_STMTandTNodes());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
