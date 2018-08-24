package SourceCode;

import soot.jimple.parser.node.Node;

import java.util.HashSet;
import java.util.Set;

public class CPGNode {

    public enum NodeTypes {//fixed set of elements for variable Type
        AST_NODE,
        EXTRA_NODE,
        CFG_NODE
    }

    private NodeTypes nodeType;
    private String name;
    private int nodeId;
    private String content;
    private Set<CPGEdge> edgesOut;
    private Set<CPGEdge> edgesIn;
    private astNodeInfo astNode;
    private boolean visited;
    private boolean markedSlicing;


    public CPGNode(NodeTypes type, String name, String content, int nodeId, Node astNode){
        this.visited=false;
        this.markedSlicing=false;
        this.nodeType = type;
        this.content = content.replaceAll("\\s","");
        this.name = name.replaceAll("\\s","");
        this.nodeId=nodeId;
        this.edgesOut = new HashSet<CPGEdge>();
        this.edgesIn = new HashSet<CPGEdge>();
        if(astNode!=null) this.astNode=new astNodeInfo(astNode.getClass().getSimpleName().replaceAll("\\s",""),astNode.toString().replaceAll("\\s",""));
        else{//ENTRY and EXIT case
            //ENTRY case
            if (this.nodeId==0) this.astNode=new astNodeInfo("ENTRY","Entry_node");
            else if (this.nodeId==1) this.astNode=new astNodeInfo("EXIT","Exit_node");
            else {
                System.err.println("ERROR in CPGNode constructor, astNode is null for " + this.nodeId + " " + this.name);
                System.exit(0);
            }

        }
    }

    public CPGNode(NodeTypes type, String name, String content, int nodeId, String astNodeName, String astNodeContent){
        this.visited=false;
        this.markedSlicing=false;
        this.nodeType = type;
        this.content = content;
        this.name = name;
        this.nodeId=nodeId;
        this.edgesOut = new HashSet<CPGEdge>();
        this.edgesIn = new HashSet<CPGEdge>();
        this.astNode=new astNodeInfo(astNodeName,astNodeContent);
    }

    public CPGNode (CPGNode nodeToClone){
        this.visited=false;
        this.markedSlicing=false;
        this.nodeType = nodeToClone.getNodeType();
        this.content = nodeToClone.getContent();
        this.name = nodeToClone.getName();
        this.nodeId=nodeToClone.getId();
        this.edgesOut = new HashSet<CPGEdge>();
        this.edgesIn = new HashSet<CPGEdge>();
        this.astNode=new astNodeInfo(nodeToClone.getAstNodeClass(),nodeToClone.getAstNodeContent());
    }

    public void addEdgeIn(CPGEdge edge){ this.edgesIn.add(edge); }


    public Set<CPGEdge> getEdgesIn(){
        return this.edgesIn;
    }


    public void addEdgeOut(CPGEdge edge){
        this.edgesOut.add(edge);
    }

    public Set<CPGEdge> getEdgesOut(){
        return this.edgesOut;
    }

    public NodeTypes getNodeType(){
        return this.nodeType;
    }

    public String getNodeTypeToString(){
        return String.valueOf(this.nodeType);
    }

    public String getName(){
        return this.name;
    }

    public String getContent(){
        return this.content;
    }

    public int getId(){
        return this.nodeId;
    }

    public void setId(int id) { this.nodeId = id;}

    public boolean isVisited() { return this.visited; }

    public void setVisited(boolean set) { this.visited=set; }

    public boolean isMarked() { return this.markedSlicing; }

    public void setMarked(boolean set) { this.markedSlicing=set; }

    public String getNameId() { return this.name + "_" + this.nodeId; }

    public String getAstNodeClass(){
        return this.astNode.classInstance;
    }

    public String getAstNodeContent(){
        return this.astNode.token;
    }

    public String[] getAstNodeInfo(){
        String[] toReturn = new String[2];
        toReturn[0]=this.astNode.classInstance;
        toReturn[1]=this.astNode.token;
        return toReturn;
    }

    public void setTypeToCFG(){
        this.nodeType=NodeTypes.CFG_NODE;
    }

    public CPGNode getParent(){
        if(this.nodeType == NodeTypes.AST_NODE){
            if(this.edgesIn.size()==1){
                return this.edgesIn.iterator().next().getSource();
            }
        }
        System.err.println("NODE " + this.getName() + "_" + this.getId() + " TYPE NOT AST_NODE, cannot use get Parent");
        return null;
    }

    private class astNodeInfo{
        String classInstance;
        String token;

        public astNodeInfo(String ci, String t){
            this.classInstance=ci;
            this.token=t;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((content == null) ? 0 : content.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result	+ ((nodeType == null) ? 0 : nodeType.hashCode());
        result = 17 * result + nodeId;
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CPGNode other = (CPGNode) obj;
        if (this.content == null) {
            if (other.getContent() != null)
                return false;
        } else if (!this.content.equals(other.getContent()))
            return false;
        if (this.name == null) {
            if (other.getName() != null)
                return false;
        } else if (!this.name.equals(other.getName()))
            return false;
        if (this.nodeType != other.getNodeType())
            return false;
        if (this.nodeId != other.getId())
            return false;
        return true;
    }

    public String toString(){
        String toReturn="";
        toReturn=this.astNode.toString();
        return toReturn;
    }
}
