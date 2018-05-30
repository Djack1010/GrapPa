package SourceCode;

public class CPGEdge {

    public enum EdgeTypes {//fixed set of elements for variable Type
        AST_EDGE,
        CFG_EDGE_C,
        //CFG_EDGE_T,
        //CFG_EDGE_F,
        PDG_EDGE_C,
        PDG_EDGE_D
    }

    private EdgeTypes edgeType;
    private CPGNode source;
    private CPGNode dest;

    public CPGEdge(EdgeTypes type, CPGNode source, CPGNode dest){
        this.edgeType = type;
        this.source = source;
        this.dest = dest;
        if(!(this.source.getEdgesOut().contains(this)))this.source.addEdgeOut(this);
        if(!(this.dest.getEdgesIn().contains(this)))this.dest.addEdgeIn(this);
    }

    public EdgeTypes getEdgeType(){
        return this.edgeType;
    }

    public String getEdgeTypeToString(){
        return String.valueOf(this.edgeType);
    }

    public CPGNode getSource(){
        return this.source;
    }

    public CPGNode getDest(){
        return this.dest;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dest == null) ? 0 : dest.hashCode());
        result = prime * result	+ ((edgeType == null) ? 0 : edgeType.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CPGEdge other = (CPGEdge) obj;
        if (this.dest == null) {
            if (other.getDest() != null)
                return false;
        } else if (!this.dest.equals(other.getDest()))
            return false;
        if (this.edgeType != other.getEdgeType())
            return false;
        if (this.source == null) {
            if (other.getSource() != null)
                return false;
        } else if (!this.source.equals(other.getSource()))
            return false;
        return true;
    }

}
