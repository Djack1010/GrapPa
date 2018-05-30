package SourceCode;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.toolkits.graph.pdg.PDGNode;
import soot.toolkits.graph.pdg.ProgramDependenceGraph;
import soot.util.dot.DotGraph;
import soot.util.dot.DotGraphNode;

/**
 * This class print on file a ProgramDependenceGraph class as a graph in DOT language
 * @author Giacomo Iadarola - giachi.iada(at)gmail.com
*/

public class PDGToDotGraph {
	Map<String, String> dotNodes;
	Set<String> visitedNodes;
	ProgramDependenceGraph pdg = null;
	DotGraph dotGraph = null;
	String name = null;

    /**
	 * Constructor, create an instance of a PDGToDotGraph class
	 * @param pdg, the ProgramDependenceGraph to print
     * @param name, the PDG name
	 */	
	PDGToDotGraph(ProgramDependenceGraph pdg, String name){
		this.pdg = pdg;
		this.name = name;	
		this.dotGraph = new DotGraph(this.name);		
		this.dotNodes= new HashMap<String, String>();
		this.visitedNodes = new HashSet<String>();
	}
	
    /**
	 * Create a DotGraph object with the info of the PDG in DOT language
	 * @return DotGraph object 
	 */
	public DotGraph drawPDG(){
		this.dotGraph.setNodeShape("record");		
		this.recursiveDrawPDG(this.pdg.GetStartNode());
		return dotGraph;
	}
	/**
	 * Visit the PDG and create nodes and edges in the DotGraph for each PDG element
	 * @param pdgNode, PDG node to visit
	 */
	private void recursiveDrawPDG(PDGNode pdgNode){
		String dotNodeName = pdgNode.toShortString().replaceFirst("^Type: ", "");
		if(this.visitedNodes.contains(dotNodeName)){
			return;
		}else{
			this.visitedNodes.add(dotNodeName);
			if(!this.dotNodes.containsKey(dotNodeName)){
				this.createNode(dotNodeName, pdgNode.toString());
			}
		}
		if(pdgNode.getDependets().isEmpty() && pdgNode.getBackDependets().isEmpty()){
			return;
		}else{
			Set<PDGNode> nextVisits = new HashSet<PDGNode>();
			if(!pdgNode.getDependets().isEmpty()){
				Iterator<PDGNode> iteraNodes = pdgNode.getDependets().iterator();
				while (iteraNodes.hasNext()){
					PDGNode tempNode = iteraNodes.next();
					String nextNode = tempNode.toShortString().replaceFirst("^Type: ", "");
					if(!this.dotNodes.containsKey(nextNode)){
						this.createNode(nextNode, tempNode.toString());
					}
					this.dotGraph.drawEdge(dotNodeName, nextNode);
					nextVisits.add(tempNode);
				}
			}
			if(!pdgNode.getBackDependets().isEmpty()){
				Iterator<PDGNode> iteraNodes = pdgNode.getBackDependets().iterator();
				while (iteraNodes.hasNext()){
					PDGNode tempNode = iteraNodes.next();
					String nextNode = tempNode.toShortString().replaceFirst("^Type: ", "");
					if(!this.dotNodes.containsKey(nextNode)){
						this.createNode(nextNode, tempNode.toString());
					}
					this.dotGraph.drawEdge(dotNodeName, nextNode).setStyle("dotted");
					nextVisits.add(tempNode);
				}
			}
			Iterator<PDGNode> iteraVisits = nextVisits.iterator();
			while(iteraVisits.hasNext()){
				recursiveDrawPDG(iteraVisits.next());
			}
			
		}
	}
	/**
	 * Create a txt file and print the info of the PDG on it
	 * @param nameFile, name of the file 
	 */	
	public void printInfoNodesOnFile(String nameFile){
		try{
			PrintWriter out = new PrintWriter(nameFile + ".txt");
			for (Map.Entry<String, String> entry : this.dotNodes.entrySet()){
				out.println(entry.getKey() + "--->" + entry.getValue());
			}
		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	 /**
    //Create a new DotGraphNode with nameNode as name and infoNode as content
    //Set shape with regard of the type (Region or CFGNODE)
     * @param nameNode, name of the DotGraphNode
     * @param infoNode, content of the DotGraphNode
     */
	private void createNode(String nameNode, String infoNode){
		DotGraphNode newNode = this.dotGraph.drawNode(nameNode);
		this.dotNodes.put(nameNode, infoNode);
		if(nameNode.startsWith("REG")){
			newNode.setShape("diamond");
		}else{
			String info = "";
			String units = "";
			String type=infoNode.split("\\[pred")[0];
			String rest=infoNode.split("\\[succs:")[1].replace("\n", "").replace("<", "\\<").replace(">", "\\>").split("\\s\\]")[1];//\\s.\\s\\]
			info = "{" + type.split(":")[1] + "|" + type.split(":")[2] + "}";
			//IF TRUE, rest contains more statement that are separated and inserted in the node block
			if(rest.length() > 0){
				String pieces[]=rest.split(";");
				for(int i = 0; i < pieces.length; i++){
					units = units + "|" + pieces[i];
				}
				newNode.setLabel("{" + info + units + "}");
			}else{
				newNode.setLabel(info);
			}
		}
	}
	
}
