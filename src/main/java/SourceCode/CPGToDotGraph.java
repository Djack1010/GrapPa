package SourceCode;

import soot.util.dot.DotGraph;
import soot.util.dot.DotGraphEdge;
import soot.util.dot.DotGraphNode;

import java.io.PrintWriter;
import java.util.*;

public class CPGToDotGraph {
    //Map<String, String> dotNodes;
    Set<Integer> visitedNodes;
    CPGNode rootNode = null;
    DotGraph dotGraph = null;
    String name = null;

    /**
     * Constructor, create an instance of a CPGToDotGraph class
     * @param rootNode, the CodePropertyGraph root node
     * @param name, the PDG name
     */
    CPGToDotGraph(CPGNode rootNode, String name){
        this.rootNode = rootNode;
        this.name = name;
        this.dotGraph = new DotGraph(this.name);
        this.visitedNodes = new HashSet<Integer>();
    }

    /**
     * Create a DotGraph object with the info of the PDG in DOT language
     * @return DotGraph object
     */
    public DotGraph drawCPG(){
        this.dotGraph.setNodeShape("record");
        this.recursiveDrawCPG(this.rootNode);
        return dotGraph;
    }
    /**
     * Visit the CPG and create nodes and edges in the DotGraph for each CPG element
     * @param cpgNode, CPG node to visit
     */
    private void recursiveDrawCPG(CPGNode cpgNode){
        if(this.visitedNodes.contains(cpgNode.getId())) return;
        else this.visitedNodes.add(cpgNode.getId());
        if(cpgNode.getEdgesIn().isEmpty() && cpgNode.getEdgesOut().isEmpty()){//Only one node
            DotGraphNode dotNode = this.dotGraph.drawNode(cpgNode.getNameId());
            dotNode.setLabel("{" + cpgNode.getNameId() + "|" + cpgNode.getContent()
                    .replace("<", "\\<").replace(">", "\\>")
                    .replace("{", "\\{").replace("}", "\\}") + "}");
            if(cpgNode.getNodeType() == CPGNode.NodeTypes.CFG_NODE || cpgNode.getNodeType() == CPGNode.NodeTypes.EXTRA_NODE)
                dotNode.setAttribute("color","red");
            return;
        }
        if(cpgNode.getEdgesIn().isEmpty()){//Root node
            DotGraphNode dotNode = this.dotGraph.drawNode(cpgNode.getNameId());
            dotNode.setLabel("{" + cpgNode.getNameId() + "|" + cpgNode.getContent()
                    .replace("<", "\\<").replace(">", "\\>")
                    .replace("{", "\\{").replace("}", "\\}") + "}");
            if(cpgNode.getNodeType() == CPGNode.NodeTypes.CFG_NODE || cpgNode.getNodeType() == CPGNode.NodeTypes.EXTRA_NODE)
                dotNode.setAttribute("color","red");
        }else if(cpgNode.getEdgesOut().isEmpty()){//Leaf node
            DotGraphNode dotNode = this.dotGraph.getNode(cpgNode.getNameId());
            dotNode.setLabel("{" + cpgNode.getNameId() + "|" + cpgNode.getContent()
                    .replace("<", "\\<").replace(">", "\\>")
                    .replace("{", "\\{").replace("}", "\\}") + "}");
            if(cpgNode.getNodeType() == CPGNode.NodeTypes.CFG_NODE || cpgNode.getNodeType() == CPGNode.NodeTypes.EXTRA_NODE)
                dotNode.setAttribute("color","red");
            return;
        }else{//generic node
            DotGraphNode dotNode = this.dotGraph.getNode(cpgNode.getNameId());
            if(cpgNode.getNodeType() == CPGNode.NodeTypes.CFG_NODE) dotNode.setAttribute("color","red");
        }

        Iterator<CPGEdge> iteraVisits = cpgNode.getEdgesOut().iterator();
        while (iteraVisits.hasNext()) {
            CPGEdge tempEdge = iteraVisits.next();
            //CPGNode tempNode = tempEdge.getDest();
            //DotGraphNode dotNode = this.dotGraph.drawNode(cpgNode.getNameId());
            //if(tempNode.getNodeType() == CPGNode.NodeTypes.CFG_NODE) dotNode.setAttribute("color","red");
            DotGraphEdge dotEdge = this.dotGraph.drawEdge(cpgNode.getNameId(), tempEdge.getDest().getNameId());
            //System.err.println("DRAWING EDGE from " + cpgNode.getName() + " to " + tempNode.getName());
            if(tempEdge.getEdgeType() == CPGEdge.EdgeTypes.CFG_EDGE_C) dotEdge.setAttribute("color","blue");
            //if(tempEdge.getEdgeType() == CPGEdge.EdgeTypes.CFG_EDGE_T) dotEdge.setAttribute("color","green");
            //if(tempEdge.getEdgeType() == CPGEdge.EdgeTypes.CFG_EDGE_F) dotEdge.setAttribute("color","red");
            if(tempEdge.getEdgeType() == CPGEdge.EdgeTypes.PDG_EDGE_C) {
                dotEdge.setAttribute("color","gold");
                dotEdge.setLabel("CONTROL");
            }
            if(tempEdge.getEdgeType() == CPGEdge.EdgeTypes.PDG_EDGE_D) {
                dotEdge.setAttribute("color","goldenrod4");
                dotEdge.setLabel("DATA");
            }
            recursiveDrawCPG(tempEdge.getDest());
        }
    }

}
