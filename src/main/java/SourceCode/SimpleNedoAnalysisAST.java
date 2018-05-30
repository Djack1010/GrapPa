package SourceCode;

import soot.Unit;
import soot.jimple.*;
import soot.jimple.parser.analysis.DepthFirstAdapter;
import soot.jimple.parser.lexer.Lexer;
import soot.jimple.parser.lexer.LexerException;
import soot.jimple.parser.node.*;
import soot.jimple.parser.parser.Parser;
import soot.jimple.parser.parser.ParserException;
import soot.util.Switch;

import java.io.*;

public class SimpleNedoAnalysisAST implements StmtSwitch {
    Node stmtNode = null;
    private Unit unit;
    private Node node;

    public SimpleNedoAnalysisAST(Unit u, Node n){
        super();
        this.unit = u;
        this.node = n;
        this.start();
    }

    private void start(){
        this.unit.apply(this);
    }

    public Node getStmtNode(){ return this.stmtNode; }

    @Override
    public void caseBreakpointStmt(BreakpointStmt stmt) {

    }

    @Override
    public void caseInvokeStmt(InvokeStmt stmt) {

    }

    @Override
    public void caseAssignStmt(AssignStmt stmt) {

    }

    @Override
    public void caseIdentityStmt(IdentityStmt stmt) {

    }

    @Override
    public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {

    }

    @Override
    public void caseExitMonitorStmt(ExitMonitorStmt stmt) {

    }

    @Override
    public void caseGotoStmt(GotoStmt stmt) {

    }

    @Override
    public void caseIfStmt(IfStmt stmt) {
        if(this.node instanceof AIfStatement) {
            AIfStatement node = (AIfStatement) this.node;
            System.err.println(stmt);
            System.err.println(stmt.getTags());
            System.err.println(node);
        }
    }

    @Override
    public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {

    }

    @Override
    public void caseNopStmt(NopStmt stmt) {

    }

    @Override
    public void caseRetStmt(RetStmt stmt) {

    }

    @Override
    public void caseReturnStmt(ReturnStmt stmt) {

    }

    @Override
    public void caseReturnVoidStmt(ReturnVoidStmt stmt) {

    }

    @Override
    public void caseTableSwitchStmt(TableSwitchStmt stmt) {

    }

    @Override
    public void caseThrowStmt(ThrowStmt stmt) {

    }

    @Override
    public void defaultCase(Object obj) {

    }
}
