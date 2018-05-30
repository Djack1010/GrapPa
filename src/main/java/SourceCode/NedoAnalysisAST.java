package SourceCode;

import soot.jimple.parser.JimpleAST;
import soot.jimple.parser.analysis.DepthFirstAdapter;
import soot.jimple.parser.lexer.Lexer;
import soot.jimple.parser.lexer.LexerException;
import soot.jimple.parser.node.*;
import soot.jimple.parser.parser.Parser;
import soot.jimple.parser.parser.ParserException;

import java.io.*;

public class NedoAnalysisAST extends DepthFirstAdapter
{
    private CodePropertyGraph cpg;
    private CPGNode rootNode = null;
    private CPGNode tempParent = null;
    private Start jimpleASTRootNode = null;
    boolean debug = false;


    public NedoAnalysisAST(CodePropertyGraph cpg){
        super();
        this.cpg = cpg;
    }

    /*
    Override defaultCase of AnalysisAdapter -> handle TOKENS
     */
    @Override
    public void defaultCase(Node node)
    {
        Token tokenNode = (Token) node;
        this.cpg.newNodeAST(node.getClass().getSimpleName(), tokenNode.toString() , this.tempParent, node);
        if(debug){
            System.err.println("new LEAF " + node.getClass().getSimpleName() + " connected to " + node.parent().getClass().getSimpleName());
            System.err.println("TOSTRING " + node.toString() + " AND " + tokenNode.toString());
        }

    }

    /*
    Override defaultCase of DepthFirstAdapter -> handle all NODE except TOKENS
     */
    @Override
    public void defaultIn(Node node)
    {
        CPGNode nNode = this.cpg.newNodeAST(node.getClass().getSimpleName(), node.getClass().toString(), this.tempParent, node);
        this.tempParent = nNode;
        if(node instanceof Start){
            if(debug){ System.err.println("new NODE " + node.getClass().getSimpleName() + " is ROOT!"); }
            //this.rootNode= nNode;
        }else{
            if(debug){ System.err.println("new NODE " + node.getClass().getSimpleName() + " connected to " + node.parent().getClass().getSimpleName()); }
        }
    }

    @Override
    public void defaultOut(Node node)
    {
        if(debug){ System.err.println("Exit NODE " + node.getClass().getSimpleName()); }
        if(node instanceof Start){
            //do nothing
        }else {
            //ASSUME that this.tempParent is an AST_NODE and so has only one parent
            //NB. Some nodes (the statements) are marked as CFG_NODE already but still one parent because NedoAnalysysAST work before introducing the CFGEdges
            CPGNode tempNode = this.tempParent.getEdgesIn().iterator().next().getSource();
            this.tempParent = tempNode;
        }
    }

    public void parse(InputStream is) throws ParserException, LexerException, IOException {
        Parser p = new Parser(new Lexer(new PushbackReader(new BufferedReader(new InputStreamReader(is)), 1024)));
        this.jimpleASTRootNode =  p.parse();
    }

    public void generateAST4CPG(){
        this.jimpleASTRootNode.apply(this);
    }

    public CPGNode getRootNode(){ return this.rootNode; }
}
