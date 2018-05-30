package SourceCode;

import soot.jimple.parser.analysis.DepthFirstAdapter;
import soot.jimple.parser.node.*;

public class NedoWalker extends DepthFirstAdapter
{
    CPGNode rootNode;

    public NedoWalker(CPGNode node){
        super();
        this.rootNode = node;
    }

    /*
    Override defaultCase of AnalysisAdapter -> handle TOKENS
     */
    @Override
    public void defaultCase(Node node)
    {
        System.err.println("new LEAF " + node.getClass().getSimpleName() + " connected to " + node.parent().getClass().getSimpleName());
        System.err.println("TOSTRING " + node.toString());
    }

    /*
    Override defaultCase of DepthFirstAdapter -> handle all NODE except TOKENS
     */
    @Override
    public void defaultIn(Node node)
    {
        if(node instanceof Start){
            System.err.println("new NODE " + node.getClass().getSimpleName() + " is ROOT!");
            this.rootNode=newNode(null);
        }else{
            System.err.println("new NODE " + node.getClass().getSimpleName() + " connected to " + node.parent().getClass().getSimpleName());
        }
    }

    @Override
    public void defaultOut(Node node)
    {
        //do nothing
    }

    private CPGNode newNode(CPGNode parent){
        CPGNode nNode = null;
        //TODO
        return nNode;
    }
}
