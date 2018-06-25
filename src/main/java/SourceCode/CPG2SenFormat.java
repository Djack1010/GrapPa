package SourceCode;

import java.io.PrintWriter;

public class CPG2SenFormat extends CPG2vec {

    public CPG2SenFormat(CodePropertyGraph cpg, boolean needDecr) {
        super(cpg,needDecr);
        //this.pathStru2vec = pathStruc2vec;
        this.edgeList=this.createEdgeListJSONOnlyCPG();
    }

    public void printGraphOnFile(String pathOutput) {
        try {
            PrintWriter out = new PrintWriter(pathOutput + this.cpg.getNameCPG() + ".json", "UTF-8");
            String mut = "1";
            if (this.cpg.getNameCPG().contains("_0")) mut = "0";
            out.println("{\n\t\"targets\": [\n\t\t["+mut+"]\n\t],\n\t\"graph\": [");
            out.println(this.edgeList);
            out.println("\t],");
            out.println("\t"+createNodeLabelsJSONOnlyCPG());
            out.println("}");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printGraphOnFile2(String pathOutput) {
        try {
            PrintWriter out = new PrintWriter(pathOutput + this.cpg.getNameCPG() + ".json", "UTF-8");
            String mut = "1";
            if (this.cpg.getNameCPG().contains("_0")) mut = "0";
            out.println("{\n\t\"targets\": [\n\t\t["+mut+"]\n\t],\n\t\"graph\": [");
            out.println(this.edgeList);
            out.println("\t],");
            out.println("\t"+createNodeLabelsJSONOnlyCPGStmts());
            out.println("}");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
