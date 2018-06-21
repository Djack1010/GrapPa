package SourceCode;

import java.io.PrintWriter;

public class CPG2SenFormat extends CPG2vec {
    String nodeLabels2;

    public CPG2SenFormat(CodePropertyGraph cpg, String pathNedo, boolean needDecr) {
        super(cpg,pathNedo,needDecr);
        //this.pathStru2vec = pathStruc2vec;
        this.edgeList=this.createEdgeListJSONOnlyCPG();
        this.nodeLabels=this.createNodeLabelsJSONOnlyCPG();
        this.nodeLabels2=this.createNodeLabelsJSONOnlyCPGStmts();
    }

    public void printGraphOnFile() {
        try {
            PrintWriter out = new PrintWriter(this.pathNedo + "/extTool/senFormat/graph1/" + this.cpg.getNameCPG() + ".json", "UTF-8");
            String mut = "1";
            if (this.cpg.getNameCPG().contains("_0")) mut = "0";
            out.println("{\n\t\"targets\": [\n\t\t["+mut+"]\n\t],\n\t\"graph\": [");
            out.println(this.edgeList);
            out.println("\t],");
            out.println("\t"+this.nodeLabels);
            out.println("}");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printGraphOnFile2() {
        try {
            PrintWriter out = new PrintWriter(this.pathNedo + "/extTool/senFormat/graph2/" + this.cpg.getNameCPG() + ".json", "UTF-8");
            String mut = "1";
            if (this.cpg.getNameCPG().contains("_0")) mut = "0";
            out.println("{\n\t\"targets\": [\n\t\t["+mut+"]\n\t],\n\t\"graph\": [");
            out.println(this.edgeList);
            out.println("\t],");
            out.println("\t"+this.nodeLabels2);
            out.println("}");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
