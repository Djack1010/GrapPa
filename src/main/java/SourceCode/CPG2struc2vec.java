package SourceCode;

import java.io.PrintWriter;

public class CPG2struc2vec {
    CodePropertyGraph cpg;
    String pathStru2vec;
    String pathNedo;

    public CPG2struc2vec(CodePropertyGraph cpg, String pathStruc2vec, String pathNedo){
        this.cpg=cpg;
        this.pathStru2vec = pathStruc2vec;
        this.pathNedo= pathNedo;
    }

    public void printEdgeListOnFile(){
        String edgeList = this.createEdgeList();
        try{
            PrintWriter out = new PrintWriter(this.pathNedo+ "/extTool/struc2vec/graph/" + this.cpg.getNameCPG()+".edgelist", "UTF-8");
            out.println(edgeList);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createEdgeList(){
        String toReturn = "";
        for(CPGEdge tempEdge: this.cpg.getCPGEdges()){
            if(toReturn.equals(""))toReturn=tempEdge.getSource().getId()+" "+tempEdge.getDest().getId();
            else toReturn=toReturn+"\n"+tempEdge.getSource().getId()+" "+tempEdge.getDest().getId();
        }
        return toReturn;
    }

}
