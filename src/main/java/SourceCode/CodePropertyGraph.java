package SourceCode;

import soot.Body;
import soot.Printer;
import soot.Unit;
import soot.jimple.internal.*;
import soot.jimple.parser.lexer.LexerException;
import soot.jimple.parser.node.*;
import soot.jimple.parser.parser.ParserException;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.*;
import soot.toolkits.scalar.SimpleLiveLocals;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.SmartLocalDefs;
import soot.toolkits.scalar.UnitValueBoxPair;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.io.*;
import java.util.*;

public class CodePropertyGraph {
    int unId;
    int startCPGid;
    Body body;
    String nameCPG;
    ExceptionalUnitGraph unitGraph;
    ProgramDependenceGraph pdg;
    Unit UGrootNode;
    CPGNode ASTrootNode;
    CPGNode CPGrootNode;
    LinkedList<CPGNode> cpgStmntNodes;
    Set<Integer> indexesLabelStmnt;
    Set<Unit> visitedStmt;
    Set<PDGNode> visitedNode;
    Map<Integer,CPGNode> cpgAllNodes;
    Set<CPGEdge> cpgAllEdges;
    Map<Unit,Integer> mapUnitToStmtIndex;
    Set<Unit> skippedNops;
    ArrayList<Integer> visitableCPGid;
    Set<CodePropertyGraph> slicingsCPG=null;
    boolean bAST = false;
    boolean bCFG = false;
    boolean bPDG = false;
    //boolean needsManualCheck = false;
    //int tempSkippedNops = 0;
    boolean debug = false;

    public CodePropertyGraph(Body body, String nameCPG){
        this.unId=2;//0 is reserved for EntryNode and 1 for ExitNode
        this.ASTrootNode=null;
        this.CPGrootNode=null;
        this.body=body;
        this.nameCPG = nameCPG;
        this.unitGraph = new ExceptionalUnitGraph(this.body);
        this.visitableCPGid=new ArrayList<Integer>();
        if(this.unitGraph.getHeads().size()!=1){
           for(Unit unitHeads: this.unitGraph.getHeads()){
               if(unitHeads instanceof JNopStmt) continue;
               else{
                   if(this.UGrootNode==null)this.UGrootNode=unitHeads;
                   else{
                       System.err.println("UGrootNode not null! exiting...");
                       System.exit(0);
                   }
               }
           }
           if(this.UGrootNode==null){
               System.err.println("UGrootNodenot found, exiting...");
               System.exit(0);
           }
        }else this.UGrootNode=this.unitGraph.getHeads().get(0);
        this.pdg = null;
        RunPDGThread createPDG = new RunPDGThread("thread4PDG", this.unitGraph);
        try{
            createPDG.start();// It just run HashMutablePDG(this.unitGraph) in a new thread
            createPDG.join(180000);
            if(createPDG.isAlive())throw new ConstructPDGException("Time limit for constructing PDG exceeded!");
            this.pdg = createPDG.getPDG();
        }catch(Exception e){
            System.err.println("Thread exception catched: " + e);
            if (e instanceof ConstructPDGException){
                System.err.println(e);
                createPDG.myStop();
            }else{
                System.err.print(e+" -> Aborting....");
                System.exit(0);
            }
        }
        if(createPDG.isAlive()) createPDG.stop();
        if(this.pdg==null){
            System.err.print("PDG still null, failing to construct CPG... ");
        } else {
            this.cpgStmntNodes = new LinkedList<CPGNode>();
            this.cpgAllNodes = new TreeMap<Integer, CPGNode>();
            this.cpgAllEdges = new HashSet<CPGEdge>();
            this.indexesLabelStmnt = new HashSet<Integer>();
            this.skippedNops = new HashSet<Unit>();
            this.visitedStmt = new HashSet<Unit>();
            this.visitedNode = new HashSet<PDGNode>();
        }
    }

    public CodePropertyGraph(String fileName) {
        this.visitableCPGid=new ArrayList<Integer>();
        this.cpgAllNodes=new TreeMap<Integer, CPGNode>();
        this.cpgAllEdges=new HashSet<CPGEdge>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            //StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            this.nameCPG=line.split("\\s+")[0];
            line = br.readLine();
            String[] nodeInfo;
            String edgeOut;
            ArrayList<String> edgeList=new ArrayList<>();
            while (line != null) {
                //sb.append(line);
                //sb.append(System.lineSeparator());
                //NodeId-0 TypeNode-1 Name-2 Content-3 AstName-4 AstContent-5 (OutNodeId_1;TypeEdge_1)-6 ... (OutNodeId_n;TypeEdge_n)-n+5
                nodeInfo=line.split("\\s+");
                if(nodeInfo.length<6){
                    System.err.println("ERROR parsing node, line contains too few informationat " + nodeInfo);
                    System.exit(0);
                }
                int nodeId = Integer.parseInt(nodeInfo[0]);
                //IF cpgAllNodes does not contain the NodeId, create a new CPGNode
                if(!this.cpgAllNodes.containsKey(nodeId))
                    this.cpgAllNodes.put(nodeId,
                            new CPGNode(parseNodeTypes(nodeInfo[1]),nodeInfo[2],nodeInfo[3],Integer.parseInt(nodeInfo[0]), nodeInfo[4], nodeInfo[5]));
                for(int i=6;i<nodeInfo.length;i++){
                    edgeOut = nodeId+";"+nodeInfo[i].replace("(","").replace(")","");
                    edgeList.add(edgeOut);
                }
                line = br.readLine();
                //System.err.println("Line "+nodeId);
            }
            for(String edge: edgeList){
                String[] edgeInfo=edge.split(";");
                if(edgeInfo.length!=3){
                    System.err.println("ERROR parsing edge, " + edgeInfo);
                    System.exit(0);
                }
                this.cpgAllEdges.add(new CPGEdge(parseEdgeTypes(edgeInfo[2]),
                        this.getCPGNodes().get(Integer.parseInt(edgeInfo[0])), this.getCPGNodes().get(Integer.parseInt(edgeInfo[1]))));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CodePropertyGraph(CodePropertyGraph cpg4slicing, String name) {
        //COPIARE il cpg4slicing solo i nodi markati!
        this.cpgAllNodes = new TreeMap<Integer, CPGNode>();
        this.cpgAllEdges = new HashSet<CPGEdge>();
        this.nameCPG=cpg4slicing.nameCPG + ":SL" + cpg4slicing.getSlicingsCPG().size() +":" + name;
        this.cpgAllNodes.put(0,new CPGNode(CPGNode.NodeTypes.EXTRA_NODE,"ENTRY","Entry node",0, null));
        this.cpgAllNodes.put(1,new CPGNode(CPGNode.NodeTypes.EXTRA_NODE,"EXIT","Exit node",1, null));
        Set<Integer> visitedNodeId = new HashSet<>();
        copyCPG4Slicing(this.cpgAllNodes.get(0),cpg4slicing.getCPGNodes().get(0), visitedNodeId);
        this.clearVisit();
        CPGNode entryNode = this.cpgAllNodes.get(0);
        this.cpgAllNodes.clear();
        this.renumeredCPG(entryNode, 2);
    }


    private void copyCPG4Slicing(CPGNode slicingNode, CPGNode originalNode, Set<Integer> visitedId){
        if(originalNode.getAstNodeClass().equals("ALabelStatement") && slicingNode.getAstNodeClass().equals("AGotoStatement")){
            CPGNode labNode;
            if(this.cpgAllNodes.containsKey(originalNode.getId())){
                labNode=this.cpgAllNodes.get(originalNode.getId());
                CPGEdge gotoLabEdge=new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_C,slicingNode,labNode);
                if(!this.cpgAllEdges.contains(gotoLabEdge))this.cpgAllEdges.add(gotoLabEdge);
            }
            else{
                labNode = new CPGNode(originalNode);
                this.cpgAllNodes.put(labNode.getId(),labNode);
                this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_C,slicingNode,labNode));
            }
        }
        if(originalNode.getNodeType()== CPGNode.NodeTypes.EXTRA_NODE && originalNode.getId()==1) {
            this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_C, slicingNode, this.cpgAllNodes.get(1)));
            return;
        }else if(visitedId.contains(originalNode.getId())){
            if(originalNode.isMarked())
                this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_C,slicingNode,this.cpgAllNodes.get(originalNode.getId())));
            else if (slicingNode.getNodeType()!= CPGNode.NodeTypes.EXTRA_NODE)
                this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_C,slicingNode,this.cpgAllNodes.get(1)));
            return;
        } else visitedId.add(originalNode.getId());
        if(!originalNode.isMarked()){
            for(CPGEdge tempEdge: originalNode.getEdgesOut()){
                if(tempEdge.getEdgeType()== CPGEdge.EdgeTypes.CFG_EDGE_C){
                    copyCPG4Slicing(slicingNode,tempEdge.getDest(), visitedId);
                }
            }
        }else{
            CPGNode newNode;
            if(this.cpgAllNodes.containsKey(originalNode.getId())){
                newNode = this.cpgAllNodes.get(originalNode.getId());
            }else {
                newNode = new CPGNode(originalNode);
                this.cpgAllNodes.put(newNode.getId(),newNode);
            }
            for(CPGEdge tempEdge: originalNode.getEdgesIn()){
                if(!(tempEdge.getEdgeType()== CPGEdge.EdgeTypes.CFG_EDGE_C)){
                    CPGNode sourceNode;
                    if(!this.getCPGNodes().containsKey(tempEdge.getSource().getId())){
                        sourceNode = new CPGNode(tempEdge.getSource());
                        this.cpgAllNodes.put(sourceNode.getId(),sourceNode);
                    }else{
                        sourceNode=this.getCPGNodes().get(tempEdge.getSource().getId());
                    }
                    this.cpgAllEdges.add(new CPGEdge(tempEdge.getEdgeType(),sourceNode,newNode));
                }
            }
            for(CPGEdge tempEdge: originalNode.getEdgesOut()){
                if(tempEdge.getEdgeType()== CPGEdge.EdgeTypes.AST_EDGE){
                    CPGNode tempNode=new CPGNode(tempEdge.getDest());
                    this.cpgAllNodes.put(tempNode.getId(),tempNode);
                    this.cpgAllEdges.add(new CPGEdge(tempEdge.getEdgeType(),newNode,tempNode));
                }
            }
            this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_C,slicingNode,newNode));
            for(CPGEdge tempEdge: originalNode.getEdgesOut()){
                if(tempEdge.getEdgeType()== CPGEdge.EdgeTypes.CFG_EDGE_C){
                    copyCPG4Slicing(newNode,tempEdge.getDest(), visitedId);
                }
            }
        }
    }

    private CPGNode.NodeTypes parseNodeTypes(String snt){
        if(snt.equals("AST_NODE")) return CPGNode.NodeTypes.AST_NODE;
        else if (snt.equals("EXTRA_NODE")) return CPGNode.NodeTypes.EXTRA_NODE;
        else if (snt.equals("CFG_NODE")) return CPGNode.NodeTypes.CFG_NODE;
        else{
            System.err.println("ERROR parsing NodeTypes, undefined type " + snt);
            System.exit(0);
            return CPGNode.NodeTypes.AST_NODE;
        }
    }

    private CPGEdge.EdgeTypes parseEdgeTypes(String snt){
        if(snt.equals("AST_EDGE")) return CPGEdge.EdgeTypes.AST_EDGE;
        else if (snt.equals("CFG_EDGE_C")) return CPGEdge.EdgeTypes.CFG_EDGE_C;
        else if (snt.equals("PDG_EDGE_C")) return CPGEdge.EdgeTypes.PDG_EDGE_C;
        else if (snt.equals("PDG_EDGE_D")) return CPGEdge.EdgeTypes.PDG_EDGE_D;
        else{
            System.err.println("ERROR parsing NodeTypes, undefined type " + snt);
            System.exit(0);
            return CPGEdge.EdgeTypes.AST_EDGE;
        }
    }

    public CPGNode getRootNode(){
        return this.cpgAllNodes.get(0);
    }

    public CPGNode getASTrootNode(){
        return this.ASTrootNode;
    }

    //public int getStartCPGid() { return this.startCPGid; }

    public String getNameCPG() { return this.nameCPG; }

    public boolean isInitializedSuccessfully(){
        if(this.pdg==null)return false;
        else return true;
    }

    public Set<CodePropertyGraph> getSlicingsCPG() { return this.slicingsCPG; }

    //public boolean isNeededManualCheck(){ return this.needsManualCheck;}

    private void generateAST(){
        if(bAST)return;
        NedoAnalysisAST ASTtree = new NedoAnalysisAST(this);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Printer.v().printTo(body, pw);
        String inputString = "public class WrapClass \n{\n" + sw.toString() + "}";
        InputStream is = new ByteArrayInputStream(inputString.getBytes());
        try{
            ASTtree.parse(is);
        } catch (LexerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }
        ASTtree.generateAST4CPG();
        bAST=true;
    }

    private void generateCFG(){
        if(bCFG)return;
        if(!bAST) generateAST();
        mapUnitToStmtNodes(this.body);
        //this.tempSkippedNops = this.skippedNops*2;
        createCFGEdges(this.unitGraph);
        //if(this.tempSkippedNops!=0){
            //System.err.println("tempSwitchStmnt not equal to 0 after createCFG, exiting...");
            //System.exit(0);
        //}
        bCFG=true;
    }

    private void generatePDG(){
        if(bPDG)return;
        //mapUnitToStmtNodes(this.body);
        //System.err.print("BriefGraph...");
        //BriefUnitGraph unitGraph = new BriefUnitGraph(this.body);
        //System.err.println("Done!");
        //System.err.print("EnhancedUnitGraph...");
        //EnhancedUnitGraph enUnitGraph = new EnhancedUnitGraph(this.body);
        //System.err.println("Done!");
        //System.err.print("ExceptionalGraph...");
        //ExceptionalUnitGraph unitGraph = new ExceptionalUnitGraph(this.body);
        //System.err.println("Done!");

        //System.err.print("PDG from BriefGraph...");
        //ProgramDependenceGraph pdg = new HashMutablePDG(this.unitGraph);
        //System.err.println("Done!");
        //System.err.print("PDG from EnhancedUnitGraph...");
        //ProgramDependenceGraph pdg = new HashMutablePDG(unitGraph);
        //System.err.println("Done!");
        //System.err.print("PDG from ExceptionalGraph...");

        //this.tempSkippedNops = this.skippedNops;
        //System.err.println(this.tempSkippedNops);
        //System.err.println(pdg.GetStartNode());
        createPDGControlEdges(this.pdg.GetStartNode());
        //if(this.tempSkippedNops!=0){
            //System.err.println("tempSwitchStmnt not equal to 0 after createPDGControlEdges");
            //System.err.println("GRAPH NEEDS TO BE CHECKED MANUALLY!!!");
            //this.needsManualCheck=true;
            //System.exit(0);
        //}
        createPDGDataEdge(unitGraph);
        bPDG=true;
    }

    private class RunPDGThread extends Thread{
        private String threadName;
        private ProgramDependenceGraph pdg;
        private UnitGraph cfg;

        RunPDGThread(String name, UnitGraph cfg) {
            this.cfg = cfg;
            this.pdg = null;
            this.threadName = name;
        }

        @Override
        public void run() {
            this.pdg = new HashMutablePDG(cfg);
        }

        public void myStop() {
            this.interrupt();
        }

        public ProgramDependenceGraph getPDG(){
            return this.pdg;
        }
    }

    /*
    private class RunBUGThread extends Thread{
        private String threadName;
        private BriefUnitGraph bug;
        private Body tbody;
        ProgramDependenceGraph pdg;

        RunBUGThread(String name, Body tbody) {
            this.bug = null;
            this.pdg=null;
            this.threadName = name;
            this.tbody=tbody;
        }

        @Override
        public void run() {
            this.bug = new BriefUnitGraph(this.tbody);
            if(this.bug!=null && this.bug.getHeads().size()==1) {
                this.pdg = new HashMutablePDG(this.bug);
            }else{
                this.pdg=null;
            }
        }

        public void myStop() {
            this.interrupt();
        }

        public ProgramDependenceGraph getPDG(){
            return this.pdg;
        }
    }
    */

    public void buildCPGphase(String phase){
        if(phase.equals("AST") && !bAST) generateAST();
        else if(phase.equals("CFG") && !bCFG) generateCFG();
        else if(phase.equals("PDG") && !bPDG) generatePDG();
        else if(phase.equals("TOT")){
            generateAST();
            generateCFG();
            generatePDG();
        }else{
            System.err.println("buildCPGphase input "+ phase +" incorrect. Available phase are AST, CFG, PDG and TOT");
            System.exit(0);
        }
    }

    private void createPDGDataEdge(UnitGraph graph){
        SimpleLiveLocals s = new SimpleLiveLocals(graph);
        Iterator<Unit> gIt = graph.iterator();
        // generate du-pairs
        while (gIt.hasNext()) {

            Unit defUnit = gIt.next();
            SmartLocalDefs des = new SmartLocalDefs(graph, s); // defs of local variables
            SimpleLocalUses uses = new SimpleLocalUses(graph, des);

            List<UnitValueBoxPair> ul = uses.getUsesOf(defUnit);
            if (ul != null && ul.size() != 0) {
                Iterator<UnitValueBoxPair> iteraBoxPair=ul.iterator();
                while(iteraBoxPair.hasNext()) {
                    Unit useUnit = iteraBoxPair.next().getUnit();
                    //System.err.println("DEF: " + defUnit.toString() + " USE: " + useUnit.toString());
                    //System.err.println("DDep from " + this.mapUnitToStmtIndex.get(defUnit) + " to " + this.mapUnitToStmtIndex.get(useUnit));
                    this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.PDG_EDGE_D,this.cpgStmntNodes.get(this.mapUnitToStmtIndex.get(defUnit)),this.cpgStmntNodes.get(this.mapUnitToStmtIndex.get(useUnit))));
                }
            }
        }
    }

    private void createPDGControlEdges(PDGNode pdgNode){
        if(this.visitedNode.contains(pdgNode))return;
        else this.visitedNode.add(pdgNode);
        if(pdgNode.getDependets().isEmpty() && pdgNode.getBackDependets().isEmpty()) return;
        if(!(pdgNode.getDependets().isEmpty())) {
            if (pdgNode.getType() == PDGNode.Type.CFGNODE) {//CASE NODE -> REGION
                Unit fromDepUnit = null;
                boolean isCDToAdd=false;
                if (pdgNode.getNode() instanceof Block) {
                    Block blockNode = (Block) pdgNode.getNode();
                    fromDepUnit = blockNode.getTail();
                    if(fromDepUnit.branches()){//there is an IFstmt, set fromDepUnit
                        isCDToAdd=true;
                    }
                } else {
                    System.err.println("Node " + pdgNode.toShortString() + " NOT contains a BLOCK!!!");
                    System.exit(0);
                }
                for (PDGNode tempNode : pdgNode.getDependets()) {
                    if (tempNode.getType() != PDGNode.Type.REGION) {
                        System.err.println("DEPENDENTS " + tempNode.toShortString() + " of " + pdgNode.toShortString() + " NOT REGION!!!");
                        System.exit(0);
                    } else{
                        IRegion regionNode = null;
                        if(tempNode.getNode() instanceof PDGRegion){
                            regionNode = (PDGRegion) tempNode.getNode();
                        }else if(tempNode.getNode() instanceof Region){
                            regionNode = (Region) tempNode.getNode();
                        }else{
                            System.err.println("NODE " + tempNode.toShortString() + " of " + pdgNode.toShortString() + " NOT CASTABLE REGION!!!");
                            System.exit(0);
                        }
                        for (Unit toDepUnit : regionNode.getUnits()) {
                            Integer indexFrom = this.mapUnitToStmtIndex.get(fromDepUnit);
                            Integer indexTo = this.mapUnitToStmtIndex.get(toDepUnit);
                            if( indexTo==null || indexFrom == null){
                                if((indexTo==null && toDepUnit instanceof JNopStmt && this.skippedNops.contains(toDepUnit)) ||
                                        (indexFrom==null && fromDepUnit instanceof JNopStmt && this.skippedNops.contains(fromDepUnit)) ){
                                    continue;
                                }else {
                                    System.err.println("indexFrom ("+indexFrom+") or indexTo("+indexTo+") are null and fromDepUnit ("+fromDepUnit.getClass().getSimpleName()+") and toDepUnit ("+toDepUnit.getClass().getSimpleName()+"), exiting...");
                                    System.exit(0);
                                }
                            }else if (isCDToAdd){
                                //System.err.println("CDep from " + fromDepUnit + " to " + toDepUnit);
                                this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.PDG_EDGE_C, this.cpgStmntNodes.get(indexFrom), this.cpgStmntNodes.get(indexTo)));
                            }
                        }
                    }
                    createPDGControlEdges(tempNode);
                }
            } else {//CASE REGION, iterate
                for (PDGNode tempNode : pdgNode.getDependets()) {
                    if (debug)
                        System.err.println("FROM " + pdgNode.toShortString() + " TO " + tempNode.toShortString());
                    createPDGControlEdges(tempNode);
                }
            }
        }
        if(!(pdgNode.getBackDependets().isEmpty())){
            if (pdgNode.getType() == PDGNode.Type.CFGNODE) {//CASE NODE -> REGION, insert Control Dependencies
                Unit fromDepUnit = null;
                boolean isCDToAdd=false;
                if (pdgNode.getNode() instanceof Block) {
                    Block blockNode = (Block) pdgNode.getNode();
                    fromDepUnit = blockNode.getTail();
                    if(fromDepUnit.branches()){//there is an IFstmt, set fromDepUnit
                        isCDToAdd=true;
                    }
                } else {
                    System.err.println("Node " + pdgNode.toShortString() + " NOT contains a BLOCK!!!");
                    System.exit(0);
                }
                for (PDGNode tempNode : pdgNode.getBackDependets()) {
                    if (tempNode.getType() != PDGNode.Type.REGION) {
                        System.err.println("DEPENDENTS " + tempNode.toShortString() + " of " + pdgNode.toShortString() + " NOT REGION!!!");
                        System.exit(0);
                    } else {
                        IRegion regionNode = null;
                        if(tempNode.getNode() instanceof PDGRegion){
                            regionNode = (PDGRegion) tempNode.getNode();
                        }else if(tempNode.getNode() instanceof Region){
                            regionNode = (Region) tempNode.getNode();
                        }else{
                            System.err.println("NODE " + tempNode.toShortString() + " of " + pdgNode.toShortString() + " NOT CASTABLE REGION!!!");
                            System.exit(0);
                        }
                        for (Unit toDepUnit : regionNode.getUnits()) {
                            Integer indexFrom = this.mapUnitToStmtIndex.get(fromDepUnit);
                            Integer indexTo = this.mapUnitToStmtIndex.get(toDepUnit);
                            if( indexTo==null || indexFrom == null){
                                if((indexTo==null && toDepUnit instanceof JNopStmt && this.skippedNops.contains(toDepUnit)) ||
                                        (indexFrom==null && fromDepUnit instanceof JNopStmt && this.skippedNops.contains(fromDepUnit)) ){
                                    continue;
                                }else {
                                    System.err.println("indexFrom ("+indexFrom+") or indexTo("+indexTo+") are null and fromDepUnit ("+fromDepUnit.getClass().getSimpleName()+") and toDepUnit ("+toDepUnit.getClass().getSimpleName()+"), exiting...");
                                    System.exit(0);
                                }
                            }else if (isCDToAdd){
                                CPGEdge tempEdge = new CPGEdge(CPGEdge.EdgeTypes.PDG_EDGE_C, this.cpgStmntNodes.get(indexFrom), this.cpgStmntNodes.get(indexTo));
                                if(!(this.cpgAllEdges.contains(tempEdge))){
                                    this.cpgAllEdges.add(tempEdge);
                                }else{
                                    if(debug) System.err.println("FROM " + pdgNode.toShortString() + " BACK TO " + tempNode.toShortString() + " ALREADY EXIST!");
                                }
                            }
                        }
                    }
                }
            } else {//CASE REGION
                for (PDGNode tempNode : pdgNode.getDependets()) {
                    if (debug)
                        System.err.println("FROM " + pdgNode.toShortString() + " BACK TO " + tempNode.toShortString());
                    //createPDGControlEdges(tempNode);
                }
            }
        }
    }

    private void createCFGEdges(UnitGraph graph){
        if(this.mapUnitToStmtIndex.get(this.UGrootNode)!=null){
            this.startCPGid=this.cpgStmntNodes.get(this.mapUnitToStmtIndex.get(this.UGrootNode)).getId();
            this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_C,this.cpgAllNodes.get(0),this.cpgAllNodes.get(this.startCPGid)));
        }else{
            System.err.println("ERROR! Constructing CFG, UGrootNode not Found, exiting...");
            System.exit(0);
        }
        Iterator<Unit> gIt = graph.iterator();
        // generate du-pairs
        while (gIt.hasNext()) {
            Unit fromUnit = gIt.next();
            if(this.visitedStmt.contains(fromUnit)) continue;
            else this.visitedStmt.add(fromUnit);
            Integer indexFrom = this.mapUnitToStmtIndex.get(fromUnit);
            //System.err.println("INFO: " + fromUnit.toString() + " va in " + graph.getSuccsOf(fromUnit).toString());
            for(Unit toUnit: graph.getSuccsOf(fromUnit)){
                //if(fromUnit instanceof JIfStmt){
                //    JIfStmt tempIf = (JIfStmt) fromUnit;
                //    if(toUnit.equals(tempIf.getTarget()))System.err.println("TRUE branch of " + fromUnit.toString() + " is " + tempIf.getTarget().toString());
                //}
                Integer indexTo = this.mapUnitToStmtIndex.get(toUnit);
                if(indexFrom!=null && indexTo!=null){
                    this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_C,this.cpgStmntNodes.get(indexFrom),this.cpgStmntNodes.get(indexTo)));
                }else{//Try to resolve corner case (SwitchTable)
                    //System.err.println("INFO: " + this.cpgStmntNodes.get(indexFrom) + fromUnit + toUnit + graph.getSuccsOf(toUnit));
                    if(indexTo==null && toUnit instanceof JNopStmt && this.skippedNops.contains(toUnit)){
                        //System.err.println("Skipping NOP node, case 1");
                        Unit tempToUnit = graph.getSuccsOf(toUnit).get(0);
                        while(tempToUnit instanceof JNopStmt){
                            if(graph.getSuccsOf(tempToUnit).size()==1){
                                tempToUnit=graph.getSuccsOf(tempToUnit).get(0);
                            }else{
                                System.err.println("ERROR...");
                                System.exit(0);
                            }
                        }
                        if(this.mapUnitToStmtIndex.get(tempToUnit)!=null){
                            this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_C,this.cpgStmntNodes.get(indexFrom),this.cpgStmntNodes.get(this.mapUnitToStmtIndex.get(tempToUnit))));
                            continue;
                        }
                    }else if(indexFrom==null && fromUnit instanceof JNopStmt && this.skippedNops.contains(fromUnit)){//skipping node NOP before Switch, case (fromUnit is the NOP) -> switch
                        //System.err.println("Skipping NOP node, case 2");
                        continue;
                    }

                    System.err.println("ERROR! Constructing CFG, indexFrom (" + indexFrom + ") of " + fromUnit.toString() + " and (" + indexTo + ") of " + toUnit.toString() + ", exiting...");
                    System.exit(0);


                    /*
                    if(indexFrom!= null && fromUnit instanceof JNopStmt &&
                            graph.getSuccsOf(toUnit).get(0) instanceof JTableSwitchStmt &&
                            this.mapUnitToStmtIndex.get(graph.getSuccsOf(toUnit).get(0))!=null &&
                            this.tempSkippedNops>0){//skipping node NOP before Switch, case fromUnit -> (toUnit is the NOP) -> switch, connect fromUnit to switch
                        //System.err.println("Skipping NOP node before TableSwitch, case 1");
                        this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_C,this.cpgStmntNodes.get(indexFrom),this.cpgStmntNodes.get(this.mapUnitToStmtIndex.get(graph.getSuccsOf(toUnit).get(0)))));
                        this.tempSkippedNops--;
                        continue;
                    }else if(indexTo!= null && fromUnit instanceof JNopStmt && toUnit instanceof JTableSwitchStmt &&
                            this.tempSkippedNops>0){//skipping node NOP before Switch, case (fromUnit is the NOP) -> switch
                        //System.err.println("Skipping NOP node before TableSwitch, case 2");
                        this.tempSkippedNops--;
                        continue;
                    }else{
                        System.err.println("ERROR! Constructing CFG, indexFrom (" + indexFrom + ") of " + fromUnit.toString() + " and (" + indexTo + ") of " + toUnit.toString() + ", exiting...");
                        System.exit(0);
                    }
                    */

                }
                if(toUnit instanceof JReturnStmt || toUnit instanceof JRetStmt ||
                        this.cpgStmntNodes.get(indexTo).getAstNodeClass().equals("AReturnStatement") ||
                        this.cpgStmntNodes.get(indexTo).getAstNodeClass().equals("ARetStatement") ){
                    this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_C,this.cpgStmntNodes.get(indexTo),this.cpgAllNodes.get(1)));
                }
            }
        }
    }

    public CPGNode newNodeAST(String name, String content, CPGNode parent, Node astNode){
        CPGNode nNode = new CPGNode(CPGNode.NodeTypes.AST_NODE,name,content,this.unId, astNode);
        if(parent == null) {
            this.ASTrootNode = nNode;
            this.cpgAllNodes.put(0,new CPGNode(CPGNode.NodeTypes.EXTRA_NODE,"ENTRY","Entry node",0, null));
            this.cpgAllNodes.put(1,new CPGNode(CPGNode.NodeTypes.EXTRA_NODE,"EXIT","Exit node",1, null));
            //REMOVE NEXT LINE and the comment in 4 lines TO INCLUDE METHOD INFO (visibility, type, name, ecc...)
            this.CPGrootNode=this.cpgAllNodes.get(0);
        }else{
            this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.AST_EDGE, parent, nNode));
            /*
            if(astNode instanceof AMethodMember){
                if(this.CPGrootNode == null){
                    this.CPGrootNode=nNode;
                }else{
                    System.err.println("CPGrootNode not null!");
                    System.exit(0);
                }
            }
            */
        }

        if (astNode instanceof ALabelStatement || astNode instanceof ABreakpointStatement || astNode instanceof AEntermonitorStatement
                || astNode instanceof AExitmonitorStatement || astNode instanceof ATableswitchStatement
                || astNode instanceof ALookupswitchStatement || astNode instanceof AIdentityStatement
                || astNode instanceof AIdentityNoTypeStatement || astNode instanceof AAssignStatement
                || astNode instanceof AIfStatement || astNode instanceof AGotoStatement || astNode instanceof ANopStatement
                || astNode instanceof ARetStatement || astNode instanceof AReturnStatement || astNode instanceof AThrowStatement
                || astNode instanceof AInvokeStatement){
            nNode.setTypeToCFG();
            if(!(astNode instanceof ANopStatement))this.cpgStmntNodes.add(nNode);//
            if(astNode instanceof ALabelStatement) this.indexesLabelStmnt.add(this.cpgStmntNodes.size()-1);
        }
        this.cpgAllNodes.put(nNode.getId(),nNode);
        this.unId++;
        return nNode;
    }

    public Map<Integer,CPGNode> getCPGNodes(){
        return this.cpgAllNodes;
    }

    public Set<CPGEdge> getCPGEdges() { return this.cpgAllEdges; }

    //public LinkedList<CPGNode> getStmntNodes(){
        //return this.cpgStmntNodes;
    //}

    public void mapUnitToStmtNodes(Body body){
        boolean forcingMode = false;
        if(this.cpgStmntNodes.size()!=body.getUnits().size()){
            System.err.println("ERROR, CPG stmts (" + this.cpgStmntNodes.size() +") and BODY stmts (" + (body.getUnits().size()) + ") numbers do not match");
            System.err.print("Forcing Map Unit-Stmts...");
            forcingMode = true;
        }
        this.mapUnitToStmtIndex=new HashMap<Unit,Integer>();
        Iterator<Unit> iteraUnit = body.getUnits().iterator();
        int index=0;
        boolean error = false;
        while(iteraUnit.hasNext() && index<this.cpgStmntNodes.size()){
            Unit tempUnit = iteraUnit.next();
            String tempNode = this.cpgStmntNodes.get(index).getAstNodeClass();
            //System.err.println("Checking " + tempUnit + " and " + tempNode);
            if(tempNode.replaceAll("\\s","").equals(tempUnit.toString().replaceAll("\\s","")+";")){
                this.mapUnitToStmtIndex.put(tempUnit,index);
            }else {
                if (tempNode.equals("ALabelStatement") && tempUnit instanceof JNopStmt) {
                    this.mapUnitToStmtIndex.put(tempUnit, index);
                } else if (tempNode.equals("ABreakpointStatement") && tempUnit instanceof JBreakpointStmt) {
                    this.mapUnitToStmtIndex.put(tempUnit, index);
                } else if (tempNode.equals("AEntermonitorStatement") && tempUnit instanceof JEnterMonitorStmt) {
                    this.mapUnitToStmtIndex.put(tempUnit, index);
                } else if (tempNode.equals("AExitmonitorStatement") && tempUnit instanceof JExitMonitorStmt) {
                    this.mapUnitToStmtIndex.put(tempUnit, index);
                } else if (tempNode.equals("ATableswitchStatement") && tempUnit instanceof JTableSwitchStmt) {
                    this.mapUnitToStmtIndex.put(tempUnit, index);
                    //this.skippedNops++;
                } else if (tempNode.equals("ALookupswitchStatement") && tempUnit instanceof JLookupSwitchStmt) {
                    this.mapUnitToStmtIndex.put(tempUnit, index);
                    //System.exit(0);
                } else if (tempNode.equals("AIdentityStatement") && tempUnit instanceof JIdentityStmt) {
                    this.mapUnitToStmtIndex.put(tempUnit, index);
                } else if (tempNode.equals("AIdentityNoTypeStatement") && tempUnit instanceof JIdentityStmt) {
                    this.mapUnitToStmtIndex.put(tempUnit, index);
                } else if (tempNode.equals("AAssignStatement") && tempUnit instanceof JAssignStmt) {
                    this.mapUnitToStmtIndex.put(tempUnit, index);
                } else if (tempNode.equals("AIfStatement") && tempUnit instanceof JIfStmt) {
                    this.mapUnitToStmtIndex.put(tempUnit, index);
                } else if (tempNode.equals("AGotoStatement") && tempUnit instanceof JGotoStmt) {
                    this.mapUnitToStmtIndex.put(tempUnit, index);
                } else if (tempUnit instanceof JNopStmt) {
                    if(tempNode.equals("ANopStatement")){
                        this.mapUnitToStmtIndex.put(tempUnit, index);
                    }else if(forcingMode){
                        //System.err.println(tempUnit.toString());
                        this.skippedNops.add(tempUnit);
                        index--;
                    }else{
                        error = true;
                    }
                } else if (tempNode.equals("ARetStatement") && tempUnit instanceof JRetStmt) {
                    this.mapUnitToStmtIndex.put(tempUnit, index);
                } else if (tempNode.equals("AReturnStatement") && (tempUnit instanceof JReturnStmt || tempUnit instanceof JReturnVoidStmt)) {
                    this.mapUnitToStmtIndex.put(tempUnit, index);
                } else if (tempNode.equals("AThrowStatement") && tempUnit instanceof JThrowStmt) {
                    this.mapUnitToStmtIndex.put(tempUnit, index);
                } else if (tempNode.equals("AInvokeStatement") && tempUnit instanceof JInvokeStmt) {
                    this.mapUnitToStmtIndex.put(tempUnit, index);
                } else {
                    error = true;
                }
            }
            if(error){
                System.err.println("ERROR!");
                System.err.println("BODY stmts " + tempUnit.getClass().getSimpleName() + " not found in CPG");
                System.err.println("It does not match " + tempNode + " exiting...");
                System.exit(0);
            }else{
                index++;
            }
        }
        if(this.cpgStmntNodes.size()!=body.getUnits().size()-this.skippedNops.size()){
            System.err.println("ERROR!");
            System.err.println("CPG stmts (" + this.cpgStmntNodes.size() + ") and BODY stmts (" + (body.getUnits().size()-this.skippedNops.size()) + ") numbers STILL do not match, exiting...");
            System.exit(0);
        }else if(forcingMode){
            System.err.println("PASSED!");
            System.err.println("CPG stmts (" + this.cpgStmntNodes.size() + ") and BODY stmts (" + (body.getUnits().size()-this.skippedNops.size()) + ") are matching now!");

        }
        //System.err.println("FINE");
    }

    private void visitCPG(CPGNode node) {
        if(this.visitableCPGid.contains(node.getId())) return;
        this.visitableCPGid.add(node.getId());
        if(node.getEdgesOut().isEmpty()) return;
        else {
            for(CPGEdge tempEdge: node.getEdgesOut()){
                this.visitCPG(tempEdge.getDest());
            }
        }
    }

    public String getCPGtoString() {
        if(this.visitableCPGid.isEmpty())this.visitCPG(this.getCPGNodes().get(0));
        else{
            this.visitableCPGid.clear();
            this.visitCPG(this.getCPGNodes().get(0));
        }
        Collections.sort(this.visitableCPGid);
        String toReturn = this.nameCPG + " " + this.visitableCPGid.size();
        for (int i = 0; i < this.visitableCPGid.size(); i++) {
            CPGNode tempNode = this.getCPGNodes().get(this.visitableCPGid.get(i));
            toReturn = toReturn +"\n" + this.getSortedID(tempNode)+ " "
                    + tempNode.getNodeTypeToString() + " "
                    + tempNode.getName() + " "
                    + tempNode.getContent() + " "
                    + tempNode.getAstNodeClass() + " "
                    + tempNode.getAstNodeContent() + " ";
            for (CPGEdge tempEdge: tempNode.getEdgesOut()){
                if (!(this.visitableCPGid.contains((tempEdge.getSource().getId())))
                        || !(this.visitableCPGid.contains((tempEdge.getDest().getId())))) continue;
                toReturn = toReturn + "(" + this.getSortedID(tempEdge.getDest()) + ";" + tempEdge.getEdgeTypeToString() + ") ";
            }
        }
        return toReturn;
    }

    private int getSortedID (CPGNode node){
        if ((node.getId() == 0) || (node.getId() == 1)) return node.getId();
        if(this.visitableCPGid.contains(node.getId())){
            return this.visitableCPGid.indexOf(node.getId());
        }else{
            System.err.println("ERROR CPG2vec! Node " + node.getId() + " not found, exiting...");
            System.exit(0);
            return -1;
        }
    }

    public void simplifyGraph(){
        this.clearVisit();
        visitGraphToSimplify(this.cpgAllNodes.get(0));
        CPGNode entryNode = this.cpgAllNodes.get(0);
        this.clearVisit();
        this.cpgAllNodes.clear();
        this.renumeredCPG(entryNode, 2);
    }

    private int renumeredCPG(CPGNode node, int num) {
        if(node.isVisited()) return num;
        else node.setVisited(true);
        if(node.getId()==0 || node.getId()==1){
            this.cpgAllNodes.put(node.getId(),node);
        }
        else{
            node.setId(num);
            this.cpgAllNodes.put(num,node);
            num++;
        }
        int toReturn = num;
        Map<Integer,CPGNode> sortedChildNode = new TreeMap<Integer, CPGNode>();
        for(CPGEdge edge: node.getEdgesOut()){
            sortedChildNode.put(edge.getDest().getId(),edge.getDest());
        }
        for(Map.Entry<Integer, CPGNode> recFun : sortedChildNode.entrySet()) {
            toReturn=renumeredCPG(recFun.getValue(),toReturn);
        }
        return toReturn;
    }


    private void visitGraphToSimplify(CPGNode node){
        if(node.isVisited()) return;
        else node.setVisited(true);
        List<CPGEdge> toRemove = new ArrayList<CPGEdge>();
        List<CPGNode> toAdd = new ArrayList<CPGNode>();
        for(CPGEdge edge: node.getEdgesOut()){
            if(edge.getEdgeType() == CPGEdge.EdgeTypes.CFG_EDGE_C) visitGraphToSimplify(edge.getDest());
            else if(edge.getEdgeType() == CPGEdge.EdgeTypes.AST_EDGE) {
                deleteIntermediateNodes(edge.getDest(), toAdd);
                toRemove.add(edge);
            }
        }
        node.getEdgesOut().removeAll(toRemove);
        for(CPGNode tempNode: toAdd){
            this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.AST_EDGE,node,tempNode));
        }
    }

    private void deleteIntermediateNodes(CPGNode now, List<CPGNode> toAdd){
        if(now.getEdgesOut().isEmpty()){
            now.getEdgesIn().clear();
            toAdd.add(now);
        }else{
            for(CPGEdge edge: now.getEdgesOut()){
                this.cpgAllEdges.remove(edge);
                deleteIntermediateNodes(edge.getDest(), toAdd);
            }
            now.getEdgesOut().clear();
            now.getEdgesIn().clear();
        }
    }

    public void clearVisit(){
        for (Map.Entry<Integer, CPGNode> entry : this.cpgAllNodes.entrySet())
        {
            entry.getValue().setVisited(false);
        }
    }

    public void clearMarkedSlicing(){
        for (Map.Entry<Integer, CPGNode> entry : this.cpgAllNodes.entrySet())
        {
            entry.getValue().setMarked(false);
        }
    }

    public void generateSlicingCPG(){
        this.clearVisit();
        if(this.slicingsCPG==null)this.slicingsCPG=new HashSet<CodePropertyGraph>();
        else {
            System.err.println("slicingsCPG not null, exiting...");
            System.exit(0);
        }
        iterateForSlicingCPG(this.cpgAllNodes.get(0));
    }

    private void iterateForSlicingCPG(CPGNode node){
        if(node.isVisited()) return;
        else node.setVisited(true);
        if(!(node.getNodeType()== CPGNode.NodeTypes.EXTRA_NODE)){
            this.clearMarkedSlicing();
            slicingCPG(node);
            CodePropertyGraph tempCPG=new CodePropertyGraph(this, node.getName());
            this.slicingsCPG.add(tempCPG);
        }
        for(CPGEdge tempEdge: node.getEdgesOut()){
            if(tempEdge.getEdgeType() == CPGEdge.EdgeTypes.CFG_EDGE_C){
                iterateForSlicingCPG(tempEdge.getDest());
            }
        }
    }

    private void slicingCPG(CPGNode criterion){
        if(criterion.isMarked()) return;
        else criterion.setMarked(true);
        for(CPGEdge tempEdge: criterion.getEdgesOut()){
            if(tempEdge.getEdgeType()== CPGEdge.EdgeTypes.PDG_EDGE_C || tempEdge.getEdgeType()== CPGEdge.EdgeTypes.PDG_EDGE_D){
                slicingCPG(tempEdge.getDest());
            }else if(criterion.getAstNodeClass().equals("AGotoStatement")
                    && tempEdge.getEdgeType()== CPGEdge.EdgeTypes.CFG_EDGE_C
                    && tempEdge.getDest().getAstNodeClass().equals("ALabelStatement")){
                slicingCPG(tempEdge.getDest());
            }
        }
        for(CPGEdge tempEdge: criterion.getEdgesIn()){
            if(tempEdge.getEdgeType()== CPGEdge.EdgeTypes.PDG_EDGE_C || tempEdge.getEdgeType()== CPGEdge.EdgeTypes.PDG_EDGE_D){
                slicingCPG(tempEdge.getSource());
            }
        }
    }

}

/*
private void newEdgeCFG(Unit statement, CPGNode precNode){
        //TODO Check se nodo già visitato, per ora funziona perchè non ci sono cicli!
        //TODO con la linked list posso fare RemoveFirst qua dentro ogni volta ottenere il CPGNode in questione, controllare se è l'AST node è instance of il correlato tipo del Unit node
        //
        //if(!this.cpgStmntNodes.isEmpty()){
       //     CPGNode cpgNode = this.cpgStmntNodes.removeFirst();
            //System.err.println("ASTNode: "+cpgNode.getAstNode().getClass().getName()+" --- UnitNode: "+statement.getClass().getName());
        //}else{
        //    System.err.println("Statements Nodes struct EMPTY!");
        //}
        CPGNode actNode = this.cpgStmntNodes.removeFirst();
        System.err.println("ASTNode: "+actNode.getAstNode().getClass().getName()+" --- UnitNode: "+statement.getClass().getName());
        int check = nodeClasses(statement,actNode.getAstNode());
        if(precNode == null){
            if( check !=1 ){
                System.err.println("ERROR, Root case, exiting...");
                System.exit(0);
            }
        }else{
            if( check > 0){
                new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_C,precNode,actNode);
            }else{
                System.err.println("ERROR, exiting...");
                //System.exit(0);
            }
        }

        //go recursion
        if(check == 2){
            newEdgeCFG(statement, actNode);
        }else{
            if(!this.cfg.getSuccsOf(statement).isEmpty()) {
                if (this.cfg.getSuccsOf(statement).size() == 2) {
                    System.err.println("NODO BR " + this.cfg.getSuccsOf(statement).toString());
                    newEdgeCFG(this.cfg.getSuccsOf(statement).get(1),actNode);
                    newEdgeCFG(this.cfg.getSuccsOf(statement).get(0),actNode);
                } else if (this.cfg.getSuccsOf(statement).size() == 1) {
                    //System.err.println("NODO ST " + statement.toString());
                    newEdgeCFG(this.cfg.getSuccsOf(statement).get(0),actNode);
                }else{
                    System.err.println("ERROR, too many getSuccsOf nodes, " + this.cfg.getSuccsOf(statement).toString());
                    System.exit(0);
                }
            }
        }
    }
 */

//newEdgeCFG(this.cfg.getBody().getUnits().getFirst(), null);
//Chain<Unit> unitsChain = this.cfg.getBody().getUnits();
//System.err.println("CFG size: "+ unitsChain.size());
//System.err.println("CPG size: "+ this.cpgStmntNodes.size());

        /*

        Unit temp = cfg2.getBody().getUnits().getFirst();
        Unit temp2 = null;
        while(!cfg2.getSuccsOf(temp).isEmpty()){
            System.err.println("QUI " + temp.toString() + " che va " + cfg2.getSuccsOf(temp).toString());
            if(cfg2.getSuccsOf(temp).size() == 1){
                temp = cfg2.getSuccsOf(temp).get(0);
            }else if(cfg2.getSuccsOf(temp).size() > 1){
                System.err.println("C'era un bivio...");
                temp2 = cfg2.getSuccsOf(temp).get(1);
                temp = cfg2.getSuccsOf(temp).get(0);
            }else{
                System.err.println("Inaspettata situa");
                System.exit(0);

            }
        }
        */

        /*
        public int mapUnitToStmtNodes(Unit u){
        int index=-1;
        //String inputString = "public class WrapClass { void wrapMethod(){" + u.toString() + ";}}";
        //InputStream is = new ByteArrayInputStream(inputString.getBytes());
        //SimpleNedoAnalysisAST walker = new SimpleNedoAnalysisAST(is);
        //Node ASTNode = walker.getStmtNode();
        //if(ASTNode!=null){
            for(CPGNode stmtNode: this.cpgStmntNodes){
                //System.err.println(stmtNode.getAstNode().toString() + "|");
                SimpleNedoAnalysisAST walker = new SimpleNedoAnalysisAST(u,stmtNode.getAstNode());
                //if(stmtNode.getAstNode().toString().replaceAll("\\s","").equals(u.toString().replaceAll("\\s","")+";")){
                //    index = stmtNode.getId();
                //    break;
                //}
            }
            if(index==-1){
                System.err.println("CPGNODE not found for Unit " + u.toString());
            }
        //}else{
        //    System.err.println("PARSING ERROR for Unit " + u.toString());
        //}
        return index;
    }
         */

        /*
        private void createCFGEdges2(int in){
        if(this.visitedStmnt.contains(in)) return;
        //else this.visitedStmnt.add(in);
        int index = in;
        int size = this.cpgStmntNodes.size();
        CPGNode actNode = this.cpgStmntNodes.get(index);
        int check = this.StmtClass(actNode);
        if(index == 0){//First node, connect to Entry node
            this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_C,this.cpgAllNodes.get(0),actNode));
        }
        if( check == 0 ){
            if(index+1<size){
                this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_C,actNode,this.cpgStmntNodes.get(index+1)));
                createCFGEdges(index+1);
            }
        }else if(check == 1){//IF STATEMENT
            if(index+1<size){//False Branch
                this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_F,actNode,this.cpgStmntNodes.get(index+1)));
                createCFGEdges(index+1);
            }else{
                System.err.println("ERROR, Unexpected False Branch case, exiting...");
                System.exit(0);
            }
            if(index+2<size){//True Branch
                this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_T,actNode,this.cpgStmntNodes.get(index+2)));
                createCFGEdges(index+2);
            }else{
                System.err.println("ERROR, Unexpected True Branch case, exiting...");
                System.exit(0);
            }
        }else if (check == 2){//GOTO STATEMENT
            CPGNode identifierGoTo = this.cpgAllNodes.get(actNode.getId()+4);
            int labelIndex=this.ConnectToGoANDLabelStmt(identifierGoTo);
            this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_C,actNode,this.cpgStmntNodes.get(labelIndex)));
            createCFGEdges(labelIndex);
        }else if(check == 3){//RETURN statement or THROW statement, connect to Exit Point and NO RECURSION
            this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_C,actNode,this.cpgAllNodes.get(1)));
        }else if(check == 4){//ATableSwitchStatement
            //DEBUG
            boolean alright = false;
            this.switchStmnt++;
            Set<Integer> indexCaseStmt = new HashSet<Integer>();
            for (CPGEdge tableChild : actNode.getEdgesOut()) {//Look for case stmts
                if (tableChild.getDest().getAstNode() instanceof ACaseStmt) {
                    tableChild.getDest().setTypeToCFG();
                    indexCaseStmt.add(tableChild.getDest().getId());
                    alright = false;
                    //Look for colon in the case stmt, which is +4 id far by the TIdentifier (it represents end of line)
                    for (CPGEdge caseStmtChild : tableChild.getDest().getEdgesOut()) {
                        if (caseStmtChild.getDest().getAstNode() instanceof TColon) {
                            CPGNode tIdentifier = this.cpgAllNodes.get(caseStmtChild.getDest().getId() + 4);
                            int labelIndex = this.ConnectToGoANDLabelStmt(tIdentifier);
                            this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_C, tableChild.getDest(), this.cpgStmntNodes.get(labelIndex)));
                            createCFGEdges(labelIndex);
                            alright = true;
                            break;
                        }
                    }
                }
            }
            for(int indexCStmt: indexCaseStmt){
                this.cpgAllEdges.add(new CPGEdge(CPGEdge.EdgeTypes.CFG_EDGE_C, actNode, this.cpgAllNodes.get(indexCStmt)));
            }
            if(!alright){
                System.err.println("ERROR, something went wrong in the SwitchTableCase "+ actNode.getNameId() +", exiting...");
                System.exit(0);
            }

        }else if(check == -1){//Not implemeted yet
            System.err.println("ERROR, Case not implemented yet, node "+ actNode.getNameId() +", exiting...");
            System.exit(0);
        }else{
            System.err.println("ERROR, Unexpected case check: "+ check +", exiting...");
            System.exit(0);
        }
    }


    private int StmtClass(CPGNode cpgNode){
        Node astNode = cpgNode.getAstNode();
        if(astNode instanceof AIfStatement) return 1;
        if(astNode instanceof AGotoStatement) return 2;
        if(astNode instanceof ARetStatement || astNode instanceof AReturnStatement || astNode instanceof AThrowStatement) return 3;
        if(astNode instanceof ATableswitchStatement) return 4;
        if(astNode instanceof AEntermonitorStatement || astNode instanceof AExitmonitorStatement
                || astNode instanceof ALookupswitchStatement ) return -1;
        return 0;
    }

    private int ConnectToGoANDLabelStmt(CPGNode identifierGoTo){
        int labelIndex = -1;
        if(!(identifierGoTo.getAstNode() instanceof TIdentifier)){
            System.err.println("ERROR, TIdentifier for GoToStatement not found, exiting...");
            System.exit(0);
        }
        Iterator<Integer> iterLabel = this.indexesLabelStmnt.iterator();
        while(labelIndex < 0 && iterLabel.hasNext()){
            int tempLabelIndex=iterLabel.next();
            CPGNode tempIdentifierLabel = this.cpgAllNodes.get(this.cpgStmntNodes.get(tempLabelIndex).getId()+2);
            if(!(tempIdentifierLabel.getAstNode() instanceof TIdentifier)){
                System.err.println("ERROR, TIdentifier for LabelStatement not found, exiting...");
                System.exit(0);
            }
            if(identifierGoTo.getContent().equals(tempIdentifierLabel.getContent())){
                labelIndex=tempLabelIndex;
            }
        }
        if(labelIndex == -1){
            System.err.println("ERROR, LabelStatement for GoToStatement "+ identifierGoTo.getNameId() +" not found, exiting...");
            System.exit(0);
        }
        return labelIndex;
    }

        private void generateCFG2(){
        if(bCFG)return;
        if(!bAST) generateAST();
        createCFGEdges(0);
        bCFG=true;
    }

        */
