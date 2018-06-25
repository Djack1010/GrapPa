package SourceCode;

import soot.jimple.parser.node.*;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public abstract class CPG2vec {
    CodePropertyGraph cpg;
    //String pathNedo;
    String edgeList;
    String nodeLabels;
    //int runValue;
    ArrayList<Integer> visitCPGid;
    boolean decr;

    public CPG2vec(CodePropertyGraph cpg, boolean needDecr) {
        this.cpg = cpg;
        //this.pathNedo = pathNedo;
        this.decr = needDecr;
        this.visitCPGid = new ArrayList<Integer>();
        this.visitCPG(this.cpg.getCPGNodes().get(0));
        Collections.sort(this.visitCPGid);
    }

    private void visitCPG(CPGNode node) {
        if(this.visitCPGid.contains(node.getId())) return;
        this.visitCPGid.add(node.getId());
        if(node.getEdgesOut().isEmpty()) return;
        else {
            for(CPGEdge tempEdge: node.getEdgesOut()){
                this.visitCPG(tempEdge.getDest());
            }
        }
    }

    protected String createAdjacentListOnlyCPG() {
        String toReturn = "node adjacencyList";
        for (int i = 0; i < this.visitCPGid.size(); i++) {
            CPGNode tempNode = this.cpg.getCPGNodes().get(this.visitCPGid.get(i));
            toReturn = toReturn +"\n" + this.getNodeNumber(tempNode)+ " ";
            for (CPGEdge tempEdge: tempNode.getEdgesIn()){
                if (!(this.visitCPGid.contains((tempEdge.getSource().getId())))
                        || !(this.visitCPGid.contains((tempEdge.getDest().getId())))) continue;
                toReturn = toReturn + "(" + this.getNodeNumber(tempEdge.getSource()) + " " + mapEdgeLabel(tempEdge) + ") ";
            }
        }
        return toReturn;
    }

    protected String createEdgeListComplete () {
        String toReturn = "";
        for (CPGEdge tempEdge : this.cpg.getCPGEdges()) {
            if (toReturn.equals(""))
                toReturn = this.getNodeNumber(tempEdge.getSource()) + " " + this.getNodeNumber(tempEdge.getDest());
            else
                toReturn = toReturn + "\n" + this.getNodeNumber(tempEdge.getSource()) + " " + this.getNodeNumber(tempEdge.getDest());
        }
        return toReturn;
    }

    protected String createEdgeListOnlyCPG () {
        String toReturn = "";
        for (CPGEdge tempEdge : this.cpg.getCPGEdges()) {
            if (!(this.visitCPGid.contains((tempEdge.getSource().getId())))
                    || !(this.visitCPGid.contains((tempEdge.getDest().getId())))) continue;
            if (toReturn.equals(""))
                toReturn = this.getNodeNumber(tempEdge.getSource()) + " " + this.getNodeNumber(tempEdge.getDest());
            else
                toReturn = toReturn + "\n" + this.getNodeNumber(tempEdge.getSource()) + " " + this.getNodeNumber(tempEdge.getDest());
        }
        return toReturn;
    }

    protected String createEdgeListJSONOnlyCPG () {
        String toReturn = "";
        for (CPGEdge tempEdge : this.cpg.getCPGEdges()) {
            if (!(this.visitCPGid.contains((tempEdge.getSource().getId())))
                    || !(this.visitCPGid.contains((tempEdge.getDest().getId())))) continue;
            if (toReturn.equals(""))
                toReturn = "\t\t["+this.getNodeNumber(tempEdge.getSource()) + ", " + this.mapEdgeLabel(tempEdge) + ", " + this.getNodeNumber(tempEdge.getDest()) + "]";
            else
                toReturn = toReturn + ",\n\t\t" + "["+this.getNodeNumber(tempEdge.getSource()) + ", " + this.mapEdgeLabel(tempEdge) + ", " + this.getNodeNumber(tempEdge.getDest()) + "]";
        }
        return toReturn;
    }

    protected String createNodeLabelsListComplete () {
        String toReturn = "node label\n0 0";
        for (int i = 2; i < this.cpg.getSize(); i++) {
            CPGNode tempNode = this.cpg.getCPGNodes().get(i);
            toReturn = toReturn + "\n" + this.getNodeNumber(tempNode) + " " + mapNodeLabel(tempNode.getAstNode());
        }
        return toReturn + "\n1 0";
    }

    protected String createNodeLabelsListSPECIAL1OnlyCPG() {
        String toReturn = "node label";
        for (int i = 0; i < this.visitCPGid.size(); i++) {
            CPGNode tempNode = this.cpg.getCPGNodes().get(this.visitCPGid.get(i));
            toReturn = toReturn + "\n" + this.getNodeNumber(tempNode) + " " + mapNodeLabelFrequentToken1(tempNode.getAstNode());
        }
        return toReturn;
    }

    protected String createNodeLabelsListSPECIAL2OnlyCPG() {
        String toReturn = "node label";
        for (int i = 0; i < this.visitCPGid.size(); i++) {
            CPGNode tempNode = this.cpg.getCPGNodes().get(this.visitCPGid.get(i));
            toReturn = toReturn + "\n" + this.getNodeNumber(tempNode) + " " + mapNodeLabelFrequentToken2(tempNode.getAstNode());
        }
        return toReturn;
    }

    protected String createNodeLabelsListOnlyCPG () {
        String toReturn = "node label";
        for (int i = 0; i < this.visitCPGid.size(); i++) {
            CPGNode tempNode = this.cpg.getCPGNodes().get(this.visitCPGid.get(i));
            toReturn = toReturn + "\n" + this.getNodeNumber(tempNode) + " " + mapNodeLabel(tempNode.getAstNode());
        }
        return toReturn;
    }

    protected String createNodeLabelsListOnlyCPGStmts () {
        String toReturn = "node label";
        for (int i = 0; i < this.visitCPGid.size(); i++) {
            CPGNode tempNode = this.cpg.getCPGNodes().get(this.visitCPGid.get(i));
            toReturn = toReturn + "\n" + this.getNodeNumber(tempNode) + " " + mapStmtLabel(tempNode.getAstNode());
        }
        return toReturn;
    }

    protected String createNodeLabelsJSONOnlyCPG () {
        String toReturn = "\"node_features\": [";
        for (int i = 0; i < this.visitCPGid.size(); i++) {
            CPGNode tempNode = this.cpg.getCPGNodes().get(this.visitCPGid.get(i));
            if (toReturn.equals("\"node_features\": [")) toReturn = toReturn + mapNodeLabel(tempNode.getAstNode());
            else toReturn = toReturn + ", " + mapNodeLabel(tempNode.getAstNode());
        }
        return toReturn +"]";
    }

    protected String createNodeLabelsJSONOnlyCPGStmts () {
        String toReturn = "\"node_features\": [";
        for (int i = 0; i < this.visitCPGid.size(); i++) {
            CPGNode tempNode = this.cpg.getCPGNodes().get(this.visitCPGid.get(i));
            if (toReturn.equals("\"node_features\": [")) toReturn = toReturn + mapStmtLabel(tempNode.getAstNode());
            else toReturn = toReturn + ", " + mapStmtLabel(tempNode.getAstNode());
        }
        return toReturn +"]";
    }

    private int getNodeNumber (CPGNode node){
        if ((node.getId() == 0) || (node.getId() == 1)) return node.getId();
        if (this.decr){
            if(this.visitCPGid.contains(node.getId())){
                return this.visitCPGid.indexOf(node.getId());
                //return node.getId() - (this.cpg.startCPGid - 2) - this.runValue;
            }else{
                System.err.println("ERROR CPG2vec! Node " + node.getId() + " not found, exiting...");
                System.exit(0);
                return -1;
            }
        }
        else return node.getId();
    }

    private int mapNodeLabel (Node tempNode){
        if (tempNode == null) {//case ENTRY and EXIT Node
            return 0;
        } else if (tempNode instanceof EOF) {
            return 258;
        } else if (tempNode instanceof Start) {
            return 257;
        } else if (tempNode instanceof AFile) {
            return 256;
        } else if (tempNode instanceof AAbstractModifier) {
            return 1;
        } else if (tempNode instanceof AFinalModifier) {
            return 2;
        } else if (tempNode instanceof ANativeModifier) {
            return 3;
        } else if (tempNode instanceof APublicModifier) {
            return 4;
        } else if (tempNode instanceof AProtectedModifier) {
            return 5;
        } else if (tempNode instanceof APrivateModifier) {
            return 6;
        } else if (tempNode instanceof AStaticModifier) {
            return 7;
        } else if (tempNode instanceof ASynchronizedModifier) {
            return 8;
        } else if (tempNode instanceof ATransientModifier) {
            return 9;
        } else if (tempNode instanceof AVolatileModifier) {
            return 10;
        } else if (tempNode instanceof AStrictfpModifier) {
            return 11;
        } else if (tempNode instanceof AEnumModifier) {
            return 12;
        } else if (tempNode instanceof AAnnotationModifier) {
            return 13;
        } else if (tempNode instanceof AClassFileType) {
            return 14;
        } else if (tempNode instanceof AInterfaceFileType) {
            return 15;
        } else if (tempNode instanceof AExtendsClause) {
            return 16;
        } else if (tempNode instanceof AImplementsClause) {
            return 17;
        } else if (tempNode instanceof AFileBody) {
            return 18;
        } else if (tempNode instanceof ASingleNameList) {
            return 19;
        } else if (tempNode instanceof AMultiNameList) {
            return 20;
        } else if (tempNode instanceof AClassNameSingleClassNameList) {
            return 21;
        } else if (tempNode instanceof AClassNameMultiClassNameList) {
            return 22;
        } else if (tempNode instanceof AFieldMember) {
            return 23;
        } else if (tempNode instanceof AMethodMember) {
            return 24;
        } else if (tempNode instanceof AVoidType) {
            return 25;
        } else if (tempNode instanceof ANovoidType) {
            return 26;
        } else if (tempNode instanceof ASingleParameterList) {
            return 27;
        } else if (tempNode instanceof AMultiParameterList) {
            return 28;
        } else if (tempNode instanceof AParameter) {
            return 29;
        } else if (tempNode instanceof AThrowsClause) {
            return 30;
        } else if (tempNode instanceof ABooleanBaseTypeNoName) {
            return 31;
        } else if (tempNode instanceof AByteBaseTypeNoName) {
            return 32;
        } else if (tempNode instanceof ACharBaseTypeNoName) {
            return 33;
        } else if (tempNode instanceof AShortBaseTypeNoName) {
            return 34;
        } else if (tempNode instanceof AIntBaseTypeNoName) {
            return 35;
        } else if (tempNode instanceof ALongBaseTypeNoName) {
            return 36;
        } else if (tempNode instanceof AFloatBaseTypeNoName) {
            return 37;
        } else if (tempNode instanceof ADoubleBaseTypeNoName) {
            return 38;
        } else if (tempNode instanceof ANullBaseTypeNoName) {
            return 39;
        } else if (tempNode instanceof ABooleanBaseType) {
            return 40;
        } else if (tempNode instanceof AByteBaseType) {
            return 41;
        } else if (tempNode instanceof ACharBaseType) {
            return 42;
        } else if (tempNode instanceof AShortBaseType) {
            return 43;
        } else if (tempNode instanceof AIntBaseType) {
            return 44;
        } else if (tempNode instanceof ALongBaseType) {
            return 45;
        } else if (tempNode instanceof AFloatBaseType) {
            return 46;
        } else if (tempNode instanceof ADoubleBaseType) {
            return 47;
        } else if (tempNode instanceof ANullBaseType) {
            return 48;
        } else if (tempNode instanceof AClassNameBaseType) {
            return 49;
        } else if (tempNode instanceof ABaseNonvoidType) {
            return 50;
        } else if (tempNode instanceof AQuotedNonvoidType) {
            return 51;
        } else if (tempNode instanceof AIdentNonvoidType) {
            return 52;
        } else if (tempNode instanceof AFullIdentNonvoidType) {
            return 53;
        } else if (tempNode instanceof AArrayBrackets) {
            return 54;
        } else if (tempNode instanceof AEmptyMethodBody) {
            return 55;
        } else if (tempNode instanceof AFullMethodBody) {
            return 56;
        } else if (tempNode instanceof ADeclaration) {
            return 57;
        } else if (tempNode instanceof AUnknownJimpleType) {
            return 58;
        } else if (tempNode instanceof ANonvoidJimpleType) {
            return 59;
        } else if (tempNode instanceof ALocalName) {
            return 60;
        } else if (tempNode instanceof ASingleLocalNameList) {
            return 61;
        } else if (tempNode instanceof AMultiLocalNameList) {
            return 62;
        } else if (tempNode instanceof ALabelStatement) {
            return 63;
        } else if (tempNode instanceof ABreakpointStatement) {
            return 64;
        } else if (tempNode instanceof AEntermonitorStatement) {
            return 65;
        } else if (tempNode instanceof AExitmonitorStatement) {
            return 66;
        } else if (tempNode instanceof ATableswitchStatement) {
            return 67;
        } else if (tempNode instanceof ALookupswitchStatement) {
            return 68;
        } else if (tempNode instanceof AIdentityStatement) {
            return 69;
        } else if (tempNode instanceof AIdentityNoTypeStatement) {
            return 70;
        } else if (tempNode instanceof AAssignStatement) {
            return 71;
        } else if (tempNode instanceof AIfStatement) {
            return 72;
        } else if (tempNode instanceof AGotoStatement) {
            return 73;
        } else if (tempNode instanceof ANopStatement) {
            return 74;
        } else if (tempNode instanceof ARetStatement) {
            return 75;
        } else if (tempNode instanceof AReturnStatement) {
            return 76;
        } else if (tempNode instanceof AThrowStatement) {
            return 77;
        } else if (tempNode instanceof AInvokeStatement) {
            return 78;
        } else if (tempNode instanceof ALabelName) {
            return 79;
        } else if (tempNode instanceof ACaseStmt) {
            return 80;
        } else if (tempNode instanceof AConstantCaseLabel) {
            return 81;
        } else if (tempNode instanceof ADefaultCaseLabel) {
            return 82;
        } else if (tempNode instanceof AGotoStmt) {
            return 83;
        } else if (tempNode instanceof ACatchClause) {
            return 84;
        } else if (tempNode instanceof ANewExpression) {
            return 85;
        } else if (tempNode instanceof ACastExpression) {
            return 86;
        } else if (tempNode instanceof AInstanceofExpression) {
            return 87;
        } else if (tempNode instanceof AInvokeExpression) {
            return 88;
        } else if (tempNode instanceof AReferenceExpression) {
            return 89;
        } else if (tempNode instanceof ABinopExpression) {
            return 90;
        } else if (tempNode instanceof AUnopExpression) {
            return 91;
        } else if (tempNode instanceof AImmediateExpression) {
            return 92;
        } else if (tempNode instanceof ASimpleNewExpr) {
            return 93;
        } else if (tempNode instanceof AArrayNewExpr) {
            return 94;
        } else if (tempNode instanceof AMultiNewExpr) {
            return 95;
        } else if (tempNode instanceof AArrayDescriptor) {
            return 96;
        } else if (tempNode instanceof AReferenceVariable) {
            return 97;
        } else if (tempNode instanceof ALocalVariable) {
            return 98;
        } else if (tempNode instanceof ABinopBoolExpr) {
            return 99;
        } else if (tempNode instanceof AUnopBoolExpr) {
            return 100;
        } else if (tempNode instanceof ANonstaticInvokeExpr) {
            return 101;
        } else if (tempNode instanceof AStaticInvokeExpr) {
            return 102;
        } else if (tempNode instanceof ADynamicInvokeExpr) {
            return 103;
        } else if (tempNode instanceof ABinopExpr) {
            return 104;
        } else if (tempNode instanceof AUnopExpr) {
            return 105;
        } else if (tempNode instanceof ASpecialNonstaticInvoke) {
            return 106;
        } else if (tempNode instanceof AVirtualNonstaticInvoke) {
            return 107;
        } else if (tempNode instanceof AInterfaceNonstaticInvoke) {
            return 108;
        } else if (tempNode instanceof AUnnamedMethodSignature) {
            return 109;
        } else if (tempNode instanceof AMethodSignature) {
            return 110;
        } else if (tempNode instanceof AArrayReference) {
            return 111;
        } else if (tempNode instanceof AFieldReference) {
            return 112;
        } else if (tempNode instanceof AIdentArrayRef) {
            return 113;
        } else if (tempNode instanceof AQuotedArrayRef) {
            return 114;
        } else if (tempNode instanceof ALocalFieldRef) {
            return 115;
        } else if (tempNode instanceof ASigFieldRef) {
            return 116;
        } else if (tempNode instanceof AFieldSignature) {
            return 117;
        } else if (tempNode instanceof AFixedArrayDescriptor) {
            return 118;
        } else if (tempNode instanceof ASingleArgList) {
            return 119;
        } else if (tempNode instanceof AMultiArgList) {
            return 120;
        } else if (tempNode instanceof ALocalImmediate) {
            return 121;
        } else if (tempNode instanceof AConstantImmediate) {
            return 122;
        } else if (tempNode instanceof AIntegerConstant) {
            return 123;
        } else if (tempNode instanceof AFloatConstant) {
            return 124;
        } else if (tempNode instanceof AStringConstant) {
            return 125;
        } else if (tempNode instanceof AClzzConstant) {
            return 126;
        } else if (tempNode instanceof ANullConstant) {
            return 127;
        } else if (tempNode instanceof AAndBinop) {
            return 128;
        } else if (tempNode instanceof AOrBinop) {
            return 129;
        } else if (tempNode instanceof AXorBinop) {
            return 130;
        } else if (tempNode instanceof AModBinop) {
            return 131;
        } else if (tempNode instanceof ACmpBinop) {
            return 132;
        } else if (tempNode instanceof ACmpgBinop) {
            return 133;
        } else if (tempNode instanceof ACmplBinop) {
            return 134;
        } else if (tempNode instanceof ACmpeqBinop) {
            return 135;
        } else if (tempNode instanceof ACmpneBinop) {
            return 136;
        } else if (tempNode instanceof ACmpgtBinop) {
            return 137;
        } else if (tempNode instanceof ACmpgeBinop) {
            return 138;
        } else if (tempNode instanceof ACmpltBinop) {
            return 139;
        } else if (tempNode instanceof ACmpleBinop) {
            return 140;
        } else if (tempNode instanceof AShlBinop) {
            return 141;
        } else if (tempNode instanceof AShrBinop) {
            return 142;
        } else if (tempNode instanceof AUshrBinop) {
            return 143;
        } else if (tempNode instanceof APlusBinop) {
            return 144;
        } else if (tempNode instanceof AMinusBinop) {
            return 145;
        } else if (tempNode instanceof AMultBinop) {
            return 146;
        } else if (tempNode instanceof ADivBinop) {
            return 147;
        } else if (tempNode instanceof ALengthofUnop) {
            return 148;
        } else if (tempNode instanceof ANegUnop) {
            return 149;
        } else if (tempNode instanceof AQuotedClassName) {
            return 150;
        } else if (tempNode instanceof AIdentClassName) {
            return 151;
        } else if (tempNode instanceof AFullIdentClassName) {
            return 152;
        } else if (tempNode instanceof AQuotedName) {
            return 153;
        } else if (tempNode instanceof AIdentName) {
            return 154;
        } else if (tempNode instanceof TIgnored) {
            return 155;
        } else if (tempNode instanceof TAbstract) {
            return 156;
        } else if (tempNode instanceof TFinal) {
            return 157;
        } else if (tempNode instanceof TNative) {
            return 158;
        } else if (tempNode instanceof TPublic) {
            return 159;
        } else if (tempNode instanceof TProtected) {
            return 160;
        } else if (tempNode instanceof TPrivate) {
            return 161;
        } else if (tempNode instanceof TStatic) {
            return 162;
        } else if (tempNode instanceof TSynchronized) {
            return 163;
        } else if (tempNode instanceof TTransient) {
            return 164;
        } else if (tempNode instanceof TVolatile) {
            return 165;
        } else if (tempNode instanceof TStrictfp) {
            return 166;
        } else if (tempNode instanceof TEnum) {
            return 167;
        } else if (tempNode instanceof TAnnotation) {
            return 168;
        } else if (tempNode instanceof TClass) {
            return 169;
        } else if (tempNode instanceof TInterface) {
            return 170;
        } else if (tempNode instanceof TVoid) {
            return 171;
        } else if (tempNode instanceof TBoolean) {
            return 172;
        } else if (tempNode instanceof TByte) {
            return 173;
        } else if (tempNode instanceof TShort) {
            return 174;
        } else if (tempNode instanceof TChar) {
            return 175;
        } else if (tempNode instanceof TInt) {
            return 176;
        } else if (tempNode instanceof TLong) {
            return 177;
        } else if (tempNode instanceof TFloat) {
            return 178;
        } else if (tempNode instanceof TDouble) {
            return 179;
        } else if (tempNode instanceof TNullType) {
            return 180;
        } else if (tempNode instanceof TUnknown) {
            return 181;
        } else if (tempNode instanceof TExtends) {
            return 182;
        } else if (tempNode instanceof TImplements) {
            return 183;
        } else if (tempNode instanceof TBreakpoint) {
            return 184;
        } else if (tempNode instanceof TCase) {
            return 185;
        } else if (tempNode instanceof TCatch) {
            return 186;
        } else if (tempNode instanceof TCmp) {
            return 187;
        } else if (tempNode instanceof TCmpg) {
            return 188;
        } else if (tempNode instanceof TCmpl) {
            return 189;
        } else if (tempNode instanceof TDefault) {
            return 190;
        } else if (tempNode instanceof TEntermonitor) {
            return 191;
        } else if (tempNode instanceof TExitmonitor) {
            return 192;
        } else if (tempNode instanceof TGoto) {
            return 193;
        } else if (tempNode instanceof TIf) {
            return 194;
        } else if (tempNode instanceof TInstanceof) {
            return 195;
        } else if (tempNode instanceof TInterfaceinvoke) {
            return 196;
        } else if (tempNode instanceof TLengthof) {
            return 197;
        } else if (tempNode instanceof TLookupswitch) {
            return 198;
        } else if (tempNode instanceof TNeg) {
            return 199;
        } else if (tempNode instanceof TNew) {
            return 200;
        } else if (tempNode instanceof TNewarray) {
            return 201;
        } else if (tempNode instanceof TNewmultiarray) {
            return 202;
        } else if (tempNode instanceof TNop) {
            return 203;
        } else if (tempNode instanceof TRet) {
            return 204;
        } else if (tempNode instanceof TReturn) {
            return 205;
        } else if (tempNode instanceof TSpecialinvoke) {
            return 206;
        } else if (tempNode instanceof TStaticinvoke) {
            return 207;
        } else if (tempNode instanceof TDynamicinvoke) {
            return 208;
        } else if (tempNode instanceof TTableswitch) {
            return 209;
        } else if (tempNode instanceof TThrow) {
            return 210;
        } else if (tempNode instanceof TThrows) {
            return 211;
        } else if (tempNode instanceof TVirtualinvoke) {
            return 212;
        } else if (tempNode instanceof TNull) {
            return 213;
        } else if (tempNode instanceof TFrom) {
            return 214;
        } else if (tempNode instanceof TTo) {
            return 215;
        } else if (tempNode instanceof TWith) {
            return 216;
        } else if (tempNode instanceof TCls) {
            return 217;
        } else if (tempNode instanceof TComma) {
            return 218;
        } else if (tempNode instanceof TLBrace) {
            return 219;
        } else if (tempNode instanceof TRBrace) {
            return 220;
        } else if (tempNode instanceof TSemicolon) {
            return 221;
        } else if (tempNode instanceof TLBracket) {
            return 222;
        } else if (tempNode instanceof TRBracket) {
            return 223;
        } else if (tempNode instanceof TLParen) {
            return 224;
        } else if (tempNode instanceof TRParen) {
            return 225;
        } else if (tempNode instanceof TColon) {
            return 226;
        } else if (tempNode instanceof TDot) {
            return 227;
        } else if (tempNode instanceof TQuote) {
            return 228;
        } else if (tempNode instanceof TColonEquals) {
            return 229;
        } else if (tempNode instanceof TEquals) {
            return 230;
        } else if (tempNode instanceof TAnd) {
            return 231;
        } else if (tempNode instanceof TOr) {
            return 232;
        } else if (tempNode instanceof TXor) {
            return 233;
        } else if (tempNode instanceof TMod) {
            return 234;
        } else if (tempNode instanceof TCmpeq) {
            return 235;
        } else if (tempNode instanceof TCmpne) {
            return 236;
        } else if (tempNode instanceof TCmpgt) {
            return 237;
        } else if (tempNode instanceof TCmpge) {
            return 238;
        } else if (tempNode instanceof TCmplt) {
            return 239;
        } else if (tempNode instanceof TCmple) {
            return 240;
        } else if (tempNode instanceof TShl) {
            return 241;
        } else if (tempNode instanceof TShr) {
            return 242;
        } else if (tempNode instanceof TUshr) {
            return 243;
        } else if (tempNode instanceof TPlus) {
            return 244;
        } else if (tempNode instanceof TMinus) {
            return 245;
        } else if (tempNode instanceof TMult) {
            return 246;
        } else if (tempNode instanceof TDiv) {
            return 247;
        } else if (tempNode instanceof TQuotedName) {
            return 248;
        } else if (tempNode instanceof TFullIdentifier) {
            return 249;
        } else if (tempNode instanceof TIdentifier) {
            return 250;
        } else if (tempNode instanceof TAtIdentifier) {
            return 251;
        } else if (tempNode instanceof TBoolConstant) {
            return 252;
        } else if (tempNode instanceof TIntegerConstant) {
            return 253;
        } else if (tempNode instanceof TFloatConstant) {
            return 254;
        } else if (tempNode instanceof TStringConstant) {
            return 255;
        } else {
            System.err.println("CPG2vec: Invalid node " + tempNode.getClass().getSimpleName() + ", exiting...");
            System.exit(0);
        }
        return -1;
    }

    private String mapNodeLabelFrequentToken1(Node tempNode){
        if (tempNode == null) {//case ENTRY and EXIT Node
            return "0";
        } else if (tempNode instanceof ALabelStatement) {
            return "1";
        } else if (tempNode instanceof ABreakpointStatement) {
            return "2";
        } else if (tempNode instanceof AEntermonitorStatement) {
            return "3";
        } else if (tempNode instanceof AExitmonitorStatement) {
            return "4";
        } else if (tempNode instanceof ATableswitchStatement) {
            return "5";
        } else if (tempNode instanceof ALookupswitchStatement) {
            return "6";
        } else if (tempNode instanceof AIdentityStatement) {
            return "7";
        } else if (tempNode instanceof AIdentityNoTypeStatement) {
            return "8";
        } else if (tempNode instanceof AAssignStatement) {
            return "9";
        } else if (tempNode instanceof AIfStatement) {
            return "10";
        } else if (tempNode instanceof AGotoStatement) {
            return "11";
        } else if (tempNode instanceof ANopStatement) {
            return "12";
        } else if (tempNode instanceof ARetStatement) {
            return "13";
        } else if (tempNode instanceof AReturnStatement) {
            return "14";
        } else if (tempNode instanceof AThrowStatement) {
            return "15";
        } else if (tempNode instanceof AInvokeStatement) {
            return "16";
        } else if (tempNode instanceof TIgnored) {
            return "17";
        } else if (tempNode instanceof TAbstract) {
            return "18";
        } else if (tempNode instanceof TFinal) {
            return "19";
        } else if (tempNode instanceof TNative) {
            return "20";
        } else if (tempNode instanceof TPublic) {
            return "21";
        } else if (tempNode instanceof TProtected) {
            return "22";
        } else if (tempNode instanceof TPrivate) {
            return "23";
        } else if (tempNode instanceof TStatic) {
            return "24";
        } else if (tempNode instanceof TSynchronized) {
            return "25";
        } else if (tempNode instanceof TTransient) {
            return "26";
        } else if (tempNode instanceof TVolatile) {
            return "27";
        } else if (tempNode instanceof TStrictfp) {
            return "28";
        } else if (tempNode instanceof TEnum) {
            return "29";
        } else if (tempNode instanceof TAnnotation) {
            return "30";
        } else if (tempNode instanceof TClass) {
            return "31";
        } else if (tempNode instanceof TInterface) {
            return "32";
        } else if (tempNode instanceof TVoid) {
            return "33";
        } else if (tempNode instanceof TBoolean) {
            return "34";
        } else if (tempNode instanceof TByte) {
            return "35";
        } else if (tempNode instanceof TShort) {
            return "36";
        } else if (tempNode instanceof TChar) {
            return "37";
        } else if (tempNode instanceof TInt) {
            return "38";
        } else if (tempNode instanceof TLong) {
            return "39";
        } else if (tempNode instanceof TFloat) {
            return "40";
        } else if (tempNode instanceof TDouble) {
            return "41";
        } else if (tempNode instanceof TNullType) {
            return "42";
        } else if (tempNode instanceof TUnknown) {
            return "43";
        } else if (tempNode instanceof TExtends) {
            return "44";
        } else if (tempNode instanceof TImplements) {
            return "45";
        } else if (tempNode instanceof TBreakpoint) {
            return "46";
        } else if (tempNode instanceof TCase) {
            return "47";
        } else if (tempNode instanceof TCatch) {
            return "48";
        } else if (tempNode instanceof TCmp) {
            return "49";
        } else if (tempNode instanceof TCmpg) {
            return "50";
        } else if (tempNode instanceof TCmpl) {
            return "51";
        } else if (tempNode instanceof TDefault) {
            return "52";
        } else if (tempNode instanceof TEntermonitor) {
            return "53";
        } else if (tempNode instanceof TExitmonitor) {
            return "54";
        } else if (tempNode instanceof TGoto) {
            return "55";
        } else if (tempNode instanceof TIf) {
            return "56";
        } else if (tempNode instanceof TInstanceof) {
            return "57";
        } else if (tempNode instanceof TInterfaceinvoke) {
            return "58";
        } else if (tempNode instanceof TLengthof) {
            return "59";
        } else if (tempNode instanceof TLookupswitch) {
            return "60";
        } else if (tempNode instanceof TNeg) {
            return "61";
        } else if (tempNode instanceof TNew) {
            return "62";
        } else if (tempNode instanceof TNewarray) {
            return "63";
        } else if (tempNode instanceof TNewmultiarray) {
            return "64";
        } else if (tempNode instanceof TNop) {
            return "65";
        } else if (tempNode instanceof TRet) {
            return "66";
        } else if (tempNode instanceof TReturn) {
            return "67";
        } else if (tempNode instanceof TSpecialinvoke) {
            return "68";
        } else if (tempNode instanceof TStaticinvoke) {
            return "69";
        } else if (tempNode instanceof TDynamicinvoke) {
            return "70";
        } else if (tempNode instanceof TTableswitch) {
            return "71";
        } else if (tempNode instanceof TThrow) {
            return "72";
        } else if (tempNode instanceof TThrows) {
            return "73";
        } else if (tempNode instanceof TVirtualinvoke) {
            return "74";
        } else if (tempNode instanceof TNull) {
            return "75";
        } else if (tempNode instanceof TFrom) {
            return "76";
        } else if (tempNode instanceof TTo) {
            return "77";
        } else if (tempNode instanceof TWith) {
            return "78";
        } else if (tempNode instanceof TCls) {
            return "79";
        } else if (tempNode instanceof TComma) {
            return "80";
        } else if (tempNode instanceof TLBrace) {
            return "81";
        } else if (tempNode instanceof TRBrace) {
            return "82";
        } else if (tempNode instanceof TSemicolon) {
            return "83";
        } else if (tempNode instanceof TLBracket) {
            return "84";
        } else if (tempNode instanceof TRBracket) {
            return "85";
        } else if (tempNode instanceof TLParen) {
            return "86";
        } else if (tempNode instanceof TRParen) {
            return "87";
        } else if (tempNode instanceof TColon) {
            return "88";
        } else if (tempNode instanceof TDot) {
            return "89";
        } else if (tempNode instanceof TQuote) {
            return "90";
        } else if (tempNode instanceof TColonEquals) {
            return "91";
        } else if (tempNode instanceof TEquals) {
            return "92";
        } else if (tempNode instanceof TAnd) {
            return "93";
        } else if (tempNode instanceof TOr) {
            return "94";
        } else if (tempNode instanceof TXor) {
            return "95";
        } else if (tempNode instanceof TMod) {
            return "96";
        } else if (tempNode instanceof TCmpeq) {
            return "97";
        } else if (tempNode instanceof TCmpne) {
            return "98";
        } else if (tempNode instanceof TCmpgt) {
            return "99";
        } else if (tempNode instanceof TCmpge) {
            return "100";
        } else if (tempNode instanceof TCmplt) {
            return "101";
        } else if (tempNode instanceof TCmple) {
            return "102";
        } else if (tempNode instanceof TShl) {
            return "103";
        } else if (tempNode instanceof TShr) {
            return "104";
        } else if (tempNode instanceof TUshr) {
            return "105";
        } else if (tempNode instanceof TPlus) {
            return "106";
        } else if (tempNode instanceof TMinus) {
            return "107";
        } else if (tempNode instanceof TMult) {
            return "108";
        } else if (tempNode instanceof TDiv) {
            return "109";
        } else if (tempNode instanceof TQuotedName) {
            return "110";
        //TFullIdentifier, TIdentifier, TAtIdentifier, TBoolConstant, TIntegerConstant, TFloatConstant, TStringConstant
        } else if (tempNode.getClass().getSimpleName().startsWith("T")){
            return "lab_"+tempNode.toString();
        } else return "111";
    }

    private String mapNodeLabelFrequentToken2(Node tempNode){
        if (tempNode == null) {//case ENTRY and EXIT Node
            return "0";
        } else if (tempNode instanceof ALabelStatement) {
            return "1";
        } else if (tempNode instanceof ABreakpointStatement) {
            return "2";
        } else if (tempNode instanceof AEntermonitorStatement) {
            return "3";
        } else if (tempNode instanceof AExitmonitorStatement) {
            return "4";
        } else if (tempNode instanceof ATableswitchStatement) {
            return "5";
        } else if (tempNode instanceof ALookupswitchStatement) {
            return "6";
        } else if (tempNode instanceof AIdentityStatement) {
            return "7";
        } else if (tempNode instanceof AIdentityNoTypeStatement) {
            return "8";
        } else if (tempNode instanceof AAssignStatement) {
            return "9";
        } else if (tempNode instanceof AIfStatement) {
            return "10";
        } else if (tempNode instanceof AGotoStatement) {
            return "11";
        } else if (tempNode instanceof ANopStatement) {
            return "12";
        } else if (tempNode instanceof ARetStatement) {
            return "13";
        } else if (tempNode instanceof AReturnStatement) {
            return "14";
        } else if (tempNode instanceof AThrowStatement) {
            return "15";
        } else if (tempNode instanceof AInvokeStatement) {
            return "16";
        } else if (tempNode instanceof TIgnored) {
            return "17";
        } else if (tempNode instanceof TAbstract) {
            return "18";
        } else if (tempNode instanceof TFinal) {
            return "19";
        } else if (tempNode instanceof TNative) {
            return "20";
        } else if (tempNode instanceof TPublic) {
            return "21";
        } else if (tempNode instanceof TProtected) {
            return "22";
        } else if (tempNode instanceof TPrivate) {
            return "23";
        } else if (tempNode instanceof TStatic) {
            return "24";
        } else if (tempNode instanceof TSynchronized) {
            return "25";
        } else if (tempNode instanceof TTransient) {
            return "26";
        } else if (tempNode instanceof TVolatile) {
            return "27";
        } else if (tempNode instanceof TStrictfp) {
            return "28";
        } else if (tempNode instanceof TEnum) {
            return "29";
        } else if (tempNode instanceof TAnnotation) {
            return "30";
        } else if (tempNode instanceof TClass) {
            return "31";
        } else if (tempNode instanceof TInterface) {
            return "32";
        } else if (tempNode instanceof TVoid) {
            return "33";
        } else if (tempNode instanceof TBoolean) {
            return "34";
        } else if (tempNode instanceof TByte) {
            return "35";
        } else if (tempNode instanceof TShort) {
            return "36";
        } else if (tempNode instanceof TChar) {
            return "37";
        } else if (tempNode instanceof TInt) {
            return "38";
        } else if (tempNode instanceof TLong) {
            return "39";
        } else if (tempNode instanceof TFloat) {
            return "40";
        } else if (tempNode instanceof TDouble) {
            return "41";
        } else if (tempNode instanceof TNullType) {
            return "42";
        } else if (tempNode instanceof TUnknown) {
            return "43";
        } else if (tempNode instanceof TExtends) {
            return "44";
        } else if (tempNode instanceof TImplements) {
            return "45";
        } else if (tempNode instanceof TBreakpoint) {
            return "46";
        } else if (tempNode instanceof TCase) {
            return "47";
        } else if (tempNode instanceof TCatch) {
            return "48";
        } else if (tempNode instanceof TCmp) {
            return "49";
        } else if (tempNode instanceof TCmpg) {
            return "50";
        } else if (tempNode instanceof TCmpl) {
            return "51";
        } else if (tempNode instanceof TDefault) {
            return "52";
        } else if (tempNode instanceof TEntermonitor) {
            return "53";
        } else if (tempNode instanceof TExitmonitor) {
            return "54";
        } else if (tempNode instanceof TGoto) {
            return "55";
        } else if (tempNode instanceof TIf) {
            return "56";
        } else if (tempNode instanceof TInstanceof) {
            return "57";
        } else if (tempNode instanceof TInterfaceinvoke) {
            return "58";
        } else if (tempNode instanceof TLengthof) {
            return "59";
        } else if (tempNode instanceof TLookupswitch) {
            return "60";
        } else if (tempNode instanceof TNeg) {
            return "61";
        } else if (tempNode instanceof TNew) {
            return "62";
        } else if (tempNode instanceof TNewarray) {
            return "63";
        } else if (tempNode instanceof TNewmultiarray) {
            return "64";
        } else if (tempNode instanceof TNop) {
            return "65";
        } else if (tempNode instanceof TRet) {
            return "66";
        } else if (tempNode instanceof TReturn) {
            return "67";
        } else if (tempNode instanceof TSpecialinvoke) {
            return "68";
        } else if (tempNode instanceof TStaticinvoke) {
            return "69";
        } else if (tempNode instanceof TDynamicinvoke) {
            return "70";
        } else if (tempNode instanceof TTableswitch) {
            return "71";
        } else if (tempNode instanceof TThrow) {
            return "72";
        } else if (tempNode instanceof TThrows) {
            return "73";
        } else if (tempNode instanceof TVirtualinvoke) {
            return "74";
        } else if (tempNode instanceof TNull) {
            return "75";
        } else if (tempNode instanceof TFrom) {
            return "76";
        } else if (tempNode instanceof TTo) {
            return "77";
        } else if (tempNode instanceof TWith) {
            return "78";
        } else if (tempNode instanceof TCls) {
            return "79";
        } else if (tempNode instanceof TComma) {
            return "80";
        } else if (tempNode instanceof TLBrace) {
            return "81";
        } else if (tempNode instanceof TRBrace) {
            return "82";
        } else if (tempNode instanceof TSemicolon) {
            return "83";
        } else if (tempNode instanceof TLBracket) {
            return "84";
        } else if (tempNode instanceof TRBracket) {
            return "85";
        } else if (tempNode instanceof TLParen) {
            return "86";
        } else if (tempNode instanceof TRParen) {
            return "87";
        } else if (tempNode instanceof TColon) {
            return "88";
        } else if (tempNode instanceof TDot) {
            return "89";
        } else if (tempNode instanceof TQuote) {
            return "90";
        } else if (tempNode instanceof TColonEquals) {
            return "91";
        } else if (tempNode instanceof TEquals) {
            return "92";
        } else if (tempNode instanceof TAnd) {
            return "93";
        } else if (tempNode instanceof TOr) {
            return "94";
        } else if (tempNode instanceof TXor) {
            return "95";
        } else if (tempNode instanceof TMod) {
            return "96";
        } else if (tempNode instanceof TCmpeq) {
            return "97";
        } else if (tempNode instanceof TCmpne) {
            return "98";
        } else if (tempNode instanceof TCmpgt) {
            return "99";
        } else if (tempNode instanceof TCmpge) {
            return "100";
        } else if (tempNode instanceof TCmplt) {
            return "101";
        } else if (tempNode instanceof TCmple) {
            return "102";
        } else if (tempNode instanceof TShl) {
            return "103";
        } else if (tempNode instanceof TShr) {
            return "104";
        } else if (tempNode instanceof TUshr) {
            return "105";
        } else if (tempNode instanceof TPlus) {
            return "106";
        } else if (tempNode instanceof TMinus) {
            return "107";
        } else if (tempNode instanceof TMult) {
            return "108";
        } else if (tempNode instanceof TDiv) {
            return "109";
        } else if (tempNode instanceof TQuotedName) {
            return "110";
        } else if (tempNode instanceof TFullIdentifier) {
            return "111";
        } else if (tempNode instanceof TIdentifier) {
            return "112";
        } else if (tempNode instanceof TAtIdentifier) {
            return "113";
        } else if (tempNode instanceof TBoolConstant || tempNode instanceof TIntegerConstant
                || tempNode instanceof TFloatConstant || tempNode instanceof TStringConstant){
            return "lab_"+tempNode.toString();
        } else return "114";
    }

    /*
    else if (tempNode instanceof TIgnored || tempNode instanceof TAbstract || tempNode instanceof TFinal
                || tempNode instanceof TNative || tempNode instanceof TPublic || tempNode instanceof TProtected
                || tempNode instanceof TPrivate || tempNode instanceof TStatic || tempNode instanceof TSynchronized
                || tempNode instanceof TTransient || tempNode instanceof TVolatile || tempNode instanceof TStrictfp
                || tempNode instanceof TEnum || tempNode instanceof TAnnotation || tempNode instanceof TClass
                || tempNode instanceof TInterface || tempNode instanceof TVoid || tempNode instanceof TBoolean
                || tempNode instanceof TByte || tempNode instanceof TShort || tempNode instanceof TChar
                || tempNode instanceof TInt || tempNode instanceof TLong || tempNode instanceof TFloat
                || tempNode instanceof TDouble || tempNode instanceof TNullType || tempNode instanceof TUnknown
                || tempNode instanceof TExtends || tempNode instanceof TImplements || tempNode instanceof TBreakpoint
                || tempNode instanceof TCase || tempNode instanceof TCatch || tempNode instanceof TCmp
                || tempNode instanceof TCmpg || tempNode instanceof TCmpl || tempNode instanceof TDefault
                || tempNode instanceof TEntermonitor || tempNode instanceof TExitmonitor || tempNode instanceof TGoto
                || tempNode instanceof TIf || tempNode instanceof TInstanceof || tempNode instanceof TInterfaceinvoke
                || tempNode instanceof TLengthof || tempNode instanceof TLookupswitch || tempNode instanceof TNeg
                || tempNode instanceof TNew || tempNode instanceof TNewarray || tempNode instanceof TNewmultiarray
                || tempNode instanceof TNop || tempNode instanceof TRet || tempNode instanceof TReturn
                || tempNode instanceof TSpecialinvoke || tempNode instanceof TStaticinvoke
                || tempNode instanceof TDynamicinvoke || tempNode instanceof TTableswitch || tempNode instanceof TThrow
                || tempNode instanceof TThrows || tempNode instanceof TVirtualinvoke || tempNode instanceof TNull
                || tempNode instanceof TFrom || tempNode instanceof TTo || tempNode instanceof TWith
                || tempNode instanceof TCls || tempNode instanceof TComma || tempNode instanceof TLBrace
                || tempNode instanceof TRBrace || tempNode instanceof TSemicolon || tempNode instanceof TLBracket
                || tempNode instanceof TRBracket || tempNode instanceof TLParen || tempNode instanceof TRParen
                || tempNode instanceof TColon || tempNode instanceof TDot || tempNode instanceof TQuote
                || tempNode instanceof TColonEquals || tempNode instanceof TEquals || tempNode instanceof TAnd
                || tempNode instanceof TOr || tempNode instanceof TXor || tempNode instanceof TMod
                || tempNode instanceof TCmpeq || tempNode instanceof TCmpne || tempNode instanceof TCmpgt
                || tempNode instanceof TCmpge || tempNode instanceof TCmplt || tempNode instanceof TCmple
                || tempNode instanceof TShl || tempNode instanceof TShr || tempNode instanceof TUshr
                || tempNode instanceof TPlus || tempNode instanceof TMinus || tempNode instanceof TMult
                || tempNode instanceof TDiv || tempNode instanceof TQuotedName || tempNode instanceof TFullIdentifier
                || tempNode instanceof TIdentifier || tempNode instanceof TAtIdentifier
                || tempNode instanceof TBoolConstant || tempNode instanceof TIntegerConstant
                || tempNode instanceof TFloatConstant || tempNode instanceof TStringConstant)
            ) {
     */

    private int mapStmtLabel (Node tempNode){
        if (tempNode == null) {//case ENTRY and EXIT Node
            return 0;
        } else if (tempNode instanceof ALabelStatement) {
            return 1;
        } else if (tempNode instanceof ABreakpointStatement) {
            return 2;
        } else if (tempNode instanceof AEntermonitorStatement) {
            return 3;
        } else if (tempNode instanceof AExitmonitorStatement) {
            return 4;
        } else if (tempNode instanceof ATableswitchStatement) {
            return 5;
        } else if (tempNode instanceof ALookupswitchStatement) {
            return 6;
        } else if (tempNode instanceof AIdentityStatement) {
            return 7;
        } else if (tempNode instanceof AIdentityNoTypeStatement) {
            return 8;
        } else if (tempNode instanceof AAssignStatement) {
            return 9;
        } else if (tempNode instanceof AIfStatement) {
            return 10;
        } else if (tempNode instanceof AGotoStatement) {
            return 11;
        } else if (tempNode instanceof ANopStatement) {
            return 12;
        } else if (tempNode instanceof ARetStatement) {
            return 13;
        } else if (tempNode instanceof AReturnStatement) {
            return 14;
        } else if (tempNode instanceof AThrowStatement) {
            return 15;
        } else if (tempNode instanceof AInvokeStatement) {
            return 16;
        } else {
            return 17;
        }
    }

    private String mapEdgeLabel (CPGEdge tempEdge){
        if (tempEdge.getEdgeType() == CPGEdge.EdgeTypes.AST_EDGE) {
            return "0";
        } else if (tempEdge.getEdgeType() == CPGEdge.EdgeTypes.CFG_EDGE_C) {
            return "1";
        } else if (tempEdge.getEdgeType() == CPGEdge.EdgeTypes.PDG_EDGE_C) {
            return "2";
        } else if (tempEdge.getEdgeType() == CPGEdge.EdgeTypes.PDG_EDGE_D) {
            return "3";
        } else {
            System.err.println("CPG2vec: Invalid edge from " + tempEdge.getSource().getId() + " to " + tempEdge.getDest().getId() + ", exiting...");
            System.exit(0);
        }
        return "-1";
    }


}
