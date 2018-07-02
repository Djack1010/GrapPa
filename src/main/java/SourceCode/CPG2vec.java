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

    protected String createAdjacentListOnlyCPG_COMPLETE() {
        String toReturn = "node adjacencyList";
        for (int i = 0; i < this.visitCPGid.size(); i++) {
            CPGNode tempNode = this.cpg.getCPGNodes().get(this.visitCPGid.get(i));
            toReturn = toReturn +"\n" + this.getNodeNumber(tempNode)+ " " + this.mapNodeLabel_COMPLETE(tempNode.getAstNodeInfo())+ " ";
            for (CPGEdge tempEdge: tempNode.getEdgesIn()){
                if (!(this.visitCPGid.contains((tempEdge.getSource().getId())))
                        || !(this.visitCPGid.contains((tempEdge.getDest().getId())))) continue;
                toReturn = toReturn + "(" + this.getNodeNumber(tempEdge.getSource()) + " " + mapEdgeLabel(tempEdge) + ") ";
            }
        }
        return toReturn;
    }

    protected String createAdjacentListOnlyCPG_STMTandTNodes() {
        String toReturn = "node adjacencyList";
        for (int i = 0; i < this.visitCPGid.size(); i++) {
            CPGNode tempNode = this.cpg.getCPGNodes().get(this.visitCPGid.get(i));
            toReturn = toReturn +"\n" + this.getNodeNumber(tempNode)+ " " + this.mapNodeLabel_STMTandTNodes(tempNode.getAstNodeInfo())+ " ";
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

    protected String createNodeLabelsOnlyCPG_COMPLETE() {
        String toReturn = "node label";
        for (int i = 0; i < this.visitCPGid.size(); i++) {
            CPGNode tempNode = this.cpg.getCPGNodes().get(this.visitCPGid.get(i));
            toReturn = toReturn + "\n" + this.getNodeNumber(tempNode) + " " + mapNodeLabel_COMPLETE(tempNode.getAstNodeInfo());
        }
        return toReturn;
    }

    protected String createNodeLabelsOnlyCPG_STMTandTNodes() {
        String toReturn = "node label";
        for (int i = 0; i < this.visitCPGid.size(); i++) {
            CPGNode tempNode = this.cpg.getCPGNodes().get(this.visitCPGid.get(i));
            toReturn = toReturn + "\n" + this.getNodeNumber(tempNode) + " " + mapNodeLabel_STMTandTNodes(tempNode.getAstNodeInfo());
        }
        return toReturn;
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

    //259 Nodes - numered till 252
    //All AST nodes
    private String mapNodeLabel_COMPLETE(String[] tempNode){
        if (tempNode[0].equals("ENTRY") || tempNode[0].equals("EXIT")) {//case ENTRY and EXIT Node
            return "0";
        } else if (tempNode[0].equals("EOF")) {
            return "250";
        } else if (tempNode[0].equals("Start")) {
            return "251";
        } else if (tempNode[0].equals("AFile")) {
            return "252";
        } else if (tempNode[0].equals("AAbstractModifier")) {
            return "1";
        } else if (tempNode[0].equals("AFinalModifier")) {
            return "2";
        } else if (tempNode[0].equals("ANativeModifier")) {
            return "3";
        } else if (tempNode[0].equals("APublicModifier")) {
            return "4";
        } else if (tempNode[0].equals("AProtectedModifier")) {
            return "5";
        } else if (tempNode[0].equals("APrivateModifier")) {
            return "6";
        } else if (tempNode[0].equals("AStaticModifier")) {
            return "7";
        } else if (tempNode[0].equals("ASynchronizedModifier")) {
            return "8";
        } else if (tempNode[0].equals("ATransientModifier")) {
            return "9";
        } else if (tempNode[0].equals("AVolatileModifier")) {
            return "10";
        } else if (tempNode[0].equals("AStrictfpModifier")) {
            return "11";
        } else if (tempNode[0].equals("AEnumModifier")) {
            return "12";
        } else if (tempNode[0].equals("AAnnotationModifier")) {
            return "13";
        } else if (tempNode[0].equals("AClassFileType")) {
            return "14";
        } else if (tempNode[0].equals("AInterfaceFileType")) {
            return "15";
        } else if (tempNode[0].equals("AExtendsClause")) {
            return "16";
        } else if (tempNode[0].equals("AImplementsClause")) {
            return "17";
        } else if (tempNode[0].equals("AFileBody")) {
            return "18";
        } else if (tempNode[0].equals("ASingleNameList")) {
            return "19";
        } else if (tempNode[0].equals("AMultiNameList")) {
            return "20";
        } else if (tempNode[0].equals("AClassNameSingleClassNameList")) {
            return "21";
        } else if (tempNode[0].equals("AClassNameMultiClassNameList")) {
            return "22";
        } else if (tempNode[0].equals("AFieldMember")) {
            return "23";
        } else if (tempNode[0].equals("AMethodMember")) {
            return "24";
        } else if (tempNode[0].equals("AVoidType")) {
            return "25";
        } else if (tempNode[0].equals("ANovoidType")) {
            return "26";
        } else if (tempNode[0].equals("ASingleParameterList")) {
            return "27";
        } else if (tempNode[0].equals("AMultiParameterList")) {
            return "28";
        } else if (tempNode[0].equals("AParameter")) {
            return "29";
        } else if (tempNode[0].equals("AThrowsClause")) {
            return "30";
        } else if (tempNode[0].equals("ABooleanBaseTypeNoName")) {
            return "31";
        } else if (tempNode[0].equals("AByteBaseTypeNoName")) {
            return "32";
        } else if (tempNode[0].equals("ACharBaseTypeNoName")) {
            return "33";
        } else if (tempNode[0].equals("AShortBaseTypeNoName")) {
            return "34";
        } else if (tempNode[0].equals("AIntBaseTypeNoName")) {
            return "35";
        } else if (tempNode[0].equals("ALongBaseTypeNoName")) {
            return "36";
        } else if (tempNode[0].equals("AFloatBaseTypeNoName")) {
            return "37";
        } else if (tempNode[0].equals("ADoubleBaseTypeNoName")) {
            return "38";
        } else if (tempNode[0].equals("ANullBaseTypeNoName")) {
            return "39";
        } else if (tempNode[0].equals("ABooleanBaseType")) {
            return "40";
        } else if (tempNode[0].equals("ACharBaseType")) {
            return "41";
        } else if (tempNode[0].equals("AByteBaseType")) {
            return "42";
        } else if (tempNode[0].equals("AShortBaseType")) {
            return "43";
        } else if (tempNode[0].equals("AIntBaseType")) {
            return "44";
        } else if (tempNode[0].equals("ALongBaseType")) {
            return "45";
        } else if (tempNode[0].equals("AFloatBaseType")) {
            return "46";
        } else if (tempNode[0].equals("ADoubleBaseType")) {
            return "47";
        } else if (tempNode[0].equals("ANullBaseType")) {
            return "48";
        } else if (tempNode[0].equals("AClassNameBaseType")) {
            return "49";
        } else if (tempNode[0].equals("ABaseNonvoidType")) {
            return "50";
        } else if (tempNode[0].equals("AQuotedNonvoidType")) {
            return "51";
        } else if (tempNode[0].equals("AIdentNonvoidType")) {
            return "52";
        } else if (tempNode[0].equals("AFullIdentNonvoidType")) {
            return "53";
        } else if (tempNode[0].equals("AArrayBrackets")) {
            return "54";
        } else if (tempNode[0].equals("AEmptyMethodBody")) {
            return "55";
        } else if (tempNode[0].equals("AFullMethodBody")) {
            return "56";
        } else if (tempNode[0].equals("ADeclaration")) {
            return "57";
        } else if (tempNode[0].equals("AUnknownJimpleType")) {
            return "58";
        } else if (tempNode[0].equals("ANonvoidJimpleType")) {
            return "59";
        } else if (tempNode[0].equals("ALocalName")) {
            return "60";
        } else if (tempNode[0].equals("ASingleLocalNameList")) {
            return "61";
        } else if (tempNode[0].equals("AMultiLocalNameList")) {
            return "62";
        } else if (tempNode[0].equals("ALabelStatement")) {
            return "63";
        } else if (tempNode[0].equals("ABreakpointStatement")) {
            return "64";
        } else if (tempNode[0].equals("AEntermonitorStatement")) {
            return "65";
        } else if (tempNode[0].equals("AExitmonitorStatement")) {
            return "66";
        } else if (tempNode[0].equals("ATableswitchStatement")) {
            return "67";
        } else if (tempNode[0].equals("ALookupswitchStatement")) {
            return "68";
        } else if (tempNode[0].equals("AIdentityStatement")) {
            return "69";
        } else if (tempNode[0].equals("AIdentityNoTypeStatement")) {
            return "70";
        } else if (tempNode[0].equals("AAssignStatement")) {
            return "71";
        } else if (tempNode[0].equals("AIfStatement")) {
            return "72";
        } else if (tempNode[0].equals("AGotoStatement")) {
            return "73";
        } else if (tempNode[0].equals("ANopStatement")) {
            return "74";
        } else if (tempNode[0].equals("ARetStatement")) {
            return "75";
        } else if (tempNode[0].equals("AReturnStatement")) {
            return "76";
        } else if (tempNode[0].equals("AThrowStatement")) {
            return "77";
        } else if (tempNode[0].equals("AInvokeStatement")) {
            return "78";
        } else if (tempNode[0].equals("ALabelName")) {
            return "79";
        } else if (tempNode[0].equals("ACaseStmt")) {
            return "80";
        } else if (tempNode[0].equals("AConstantCaseLabel")) {
            return "81";
        } else if (tempNode[0].equals("ADefaultCaseLabel")) {
            return "82";
        } else if (tempNode[0].equals("AGotoStmt")) {
            return "83";
        } else if (tempNode[0].equals("ACatchClause")) {
            return "84";
        } else if (tempNode[0].equals("ANewExpression")) {
            return "85";
        } else if (tempNode[0].equals("ACastExpression")) {
            return "86";
        } else if (tempNode[0].equals("AInstanceofExpression")) {
            return "87";
        } else if (tempNode[0].equals("AInvokeExpression")) {
            return "88";
        } else if (tempNode[0].equals("AReferenceExpression")) {
            return "89";
        } else if (tempNode[0].equals("ABinopExpression")) {
            return "90";
        } else if (tempNode[0].equals("AUnopExpression")) {
            return "91";
        } else if (tempNode[0].equals("AImmediateExpression")) {
            return "92";
        } else if (tempNode[0].equals("ASimpleNewExpr")) {
            return "93";
        } else if (tempNode[0].equals("AArrayNewExpr")) {
            return "94";
        } else if (tempNode[0].equals("AMultiNewExpr")) {
            return "95";
        } else if (tempNode[0].equals("AArrayDescriptor")) {
            return "96";
        } else if (tempNode[0].equals("AReferenceVariable")) {
            return "97";
        } else if (tempNode[0].equals("ALocalVariable")) {
            return "98";
        } else if (tempNode[0].equals("ABinopBoolExpr")) {
            return "99";
        } else if (tempNode[0].equals("AUnopBoolExpr")) {
            return "100";
        } else if (tempNode[0].equals("ANonstaticInvokeExpr")) {
            return "101";
        } else if (tempNode[0].equals("AStaticInvokeExpr")) {
            return "102";
        } else if (tempNode[0].equals("ADynamicInvokeExpr")) {
            return "103";
        } else if (tempNode[0].equals("ABinopExpr")) {
            return "104";
        } else if (tempNode[0].equals("AUnopExpr")) {
            return "105";
        } else if (tempNode[0].equals("ASpecialNonstaticInvoke")) {
            return "106";
        } else if (tempNode[0].equals("AVirtualNonstaticInvoke")) {
            return "107";
        } else if (tempNode[0].equals("AInterfaceNonstaticInvoke")) {
            return "108";
        } else if (tempNode[0].equals("AUnnamedMethodSignature")) {
            return "109";
        } else if (tempNode[0].equals("AMethodSignature")) {
            return "110";
        } else if (tempNode[0].equals("AArrayReference")) {
            return "111";
        } else if (tempNode[0].equals("AFieldReference")) {
            return "112";
        } else if (tempNode[0].equals("AIdentArrayRef")) {
            return "113";
        } else if (tempNode[0].equals("AQuotedArrayRef")) {
            return "114";
        } else if (tempNode[0].equals("ALocalFieldRef")) {
            return "115";
        } else if (tempNode[0].equals("ASigFieldRef")) {
            return "116";
        } else if (tempNode[0].equals("AFieldSignature")) {
            return "117";
        } else if (tempNode[0].equals("AFixedArrayDescriptor")) {
            return "118";
        } else if (tempNode[0].equals("ASingleArgList")) {
            return "119";
        } else if (tempNode[0].equals("AMultiArgList")) {
            return "120";
        } else if (tempNode[0].equals("ALocalImmediate")) {
            return "121";
        } else if (tempNode[0].equals("AConstantImmediate")) {
            return "122";
        } else if (tempNode[0].equals("AIntegerConstant")) {
            return "123";
        } else if (tempNode[0].equals("AFloatConstant")) {
            return "124";
        } else if (tempNode[0].equals("AStringConstant")) {
            return "125";
        } else if (tempNode[0].equals("AClzzConstant")) {
            return "126";
        } else if (tempNode[0].equals("ANullConstant")) {
            return "127";
        } else if (tempNode[0].equals("AAndBinop")) {
            return "128";
        } else if (tempNode[0].equals("AOrBinop")) {
            return "129";
        } else if (tempNode[0].equals("AXorBinop")) {
            return "130";
        } else if (tempNode[0].equals("AModBinop")) {
            return "131";
        } else if (tempNode[0].equals("ACmpBinop")) {
            return "132";
        } else if (tempNode[0].equals("ACmpgBinop")) {
            return "133";
        } else if (tempNode[0].equals("ACmplBinop")) {
            return "134";
        } else if (tempNode[0].equals("ACmpeqBinop")) {
            return "135";
        } else if (tempNode[0].equals("ACmpneBinop")) {
            return "136";
        } else if (tempNode[0].equals("ACmpgtBinop")) {
            return "137";
        } else if (tempNode[0].equals("ACmpgeBinop")) {
            return "138";
        } else if (tempNode[0].equals("ACmpltBinop")) {
            return "139";
        } else if (tempNode[0].equals("ACmpleBinop")) {
            return "140";
        } else if (tempNode[0].equals("AShlBinop")) {
            return "141";
        } else if (tempNode[0].equals("AShrBinop")) {
            return "142";
        } else if (tempNode[0].equals("AUshrBinop")) {
            return "143";
        } else if (tempNode[0].equals("APlusBinop")) {
            return "144";
        } else if (tempNode[0].equals("AMinusBinop")) {
            return "145";
        } else if (tempNode[0].equals("AMultBinop")) {
            return "146";
        } else if (tempNode[0].equals("ADivBinop")) {
            return "147";
        } else if (tempNode[0].equals("ALengthofUnop")) {
            return "148";
        } else if (tempNode[0].equals("ANegUnop")) {
            return "149";
        } else if (tempNode[0].equals("AQuotedClassName")) {
            return "150";
        } else if (tempNode[0].equals("AIdentClassName")) {
            return "151";
        } else if (tempNode[0].equals("AFullIdentClassName")) {
            return "152";
        } else if (tempNode[0].equals("AQuotedName")) {
            return "153";
        } else if (tempNode[0].equals("AIdentName")) {
            return "154";
        } else if (tempNode[0].equals("TIgnored")) {
            return "155";
        } else if (tempNode[0].equals("TAbstract")) {
            return "156";
        } else if (tempNode[0].equals("TFinal")) {
            return "157";
        } else if (tempNode[0].equals("TNative")) {
            return "158";
        } else if (tempNode[0].equals("TPublic")) {
            return "159";
        } else if (tempNode[0].equals("TProtected")) {
            return "160";
        } else if (tempNode[0].equals("TPrivate")) {
            return "161";
        } else if (tempNode[0].equals("TStatic")) {
            return "162";
        } else if (tempNode[0].equals("TSynchronized")) {
            return "163";
        } else if (tempNode[0].equals("TTransient")) {
            return "164";
        } else if (tempNode[0].equals("TVolatile")) {
            return "165";
        } else if (tempNode[0].equals("TStrictfp")) {
            return "166";
        } else if (tempNode[0].equals("TEnum")) {
            return "167";
        } else if (tempNode[0].equals("TAnnotation")) {
            return "168";
        } else if (tempNode[0].equals("TClass")) {
            return "169";
        } else if (tempNode[0].equals("TInterface")) {
            return "170";
        } else if (tempNode[0].equals("TVoid")) {
            return "171";
        } else if (tempNode[0].equals("TBoolean")) {
            return "172";
        } else if (tempNode[0].equals("TByte")) {
            return "173";
        } else if (tempNode[0].equals("TShort")) {
            return "174";
        } else if (tempNode[0].equals("TChar")) {
            return "175";
        } else if (tempNode[0].equals("TInt")) {
            return "176";
        } else if (tempNode[0].equals("TLong")) {
            return "177";
        } else if (tempNode[0].equals("TFloat")) {
            return "178";
        } else if (tempNode[0].equals("TDouble")) {
            return "179";
        } else if (tempNode[0].equals("TNullType")) {
            return "180";
        } else if (tempNode[0].equals("TUnknown")) {
            return "181";
        } else if (tempNode[0].equals("TExtends")) {
            return "182";
        } else if (tempNode[0].equals("TImplements")) {
            return "183";
        } else if (tempNode[0].equals("TBreakpoint")) {
            return "184";
        } else if (tempNode[0].equals("TCase")) {
            return "185";
        } else if (tempNode[0].equals("TCatch")) {
            return "186";
        } else if (tempNode[0].equals("TCmp")) {
            return "187";
        } else if (tempNode[0].equals("TCmpg")) {
            return "188";
        } else if (tempNode[0].equals("TCmpl")) {
            return "189";
        } else if (tempNode[0].equals("TDefault")) {
            return "190";
        } else if (tempNode[0].equals("TEntermonitor")) {
            return "191";
        } else if (tempNode[0].equals("TExitmonitor")) {
            return "192";
        } else if (tempNode[0].equals("TGoto")) {
            return "193";
        } else if (tempNode[0].equals("TIf")) {
            return "194";
        } else if (tempNode[0].equals("TInstanceof")) {
            return "195";
        } else if (tempNode[0].equals("TInterfaceinvoke")) {
            return "196";
        } else if (tempNode[0].equals("TLengthof")) {
            return "197";
        } else if (tempNode[0].equals("TLookupswitch")) {
            return "198";
        } else if (tempNode[0].equals("TNeg")) {
            return "199";
        } else if (tempNode[0].equals("TNew")) {
            return "200";
        } else if (tempNode[0].equals("TNewarray")) {
            return "201";
        } else if (tempNode[0].equals("TNewmultiarray")) {
            return "202";
        } else if (tempNode[0].equals("TNop")) {
            return "203";
        } else if (tempNode[0].equals("TRet")) {
            return "204";
        } else if (tempNode[0].equals("TReturn")) {
            return "205";
        } else if (tempNode[0].equals("TSpecialinvoke")) {
            return "206";
        } else if (tempNode[0].equals("TStaticinvoke")) {
            return "207";
        } else if (tempNode[0].equals("TDynamicinvoke")) {
            return "208";
        } else if (tempNode[0].equals("TTableswitch")) {
            return "209";
        } else if (tempNode[0].equals("TThrow")) {
            return "210";
        } else if (tempNode[0].equals("TThrows")) {
            return "211";
        } else if (tempNode[0].equals("TVirtualinvoke")) {
            return "212";
        } else if (tempNode[0].equals("TNull")) {
            return "213";
        } else if (tempNode[0].equals("TFrom")) {
            return "214";
        } else if (tempNode[0].equals("TTo")) {
            return "215";
        } else if (tempNode[0].equals("TWith")) {
            return "216";
        } else if (tempNode[0].equals("TCls")) {
            return "217";
        } else if (tempNode[0].equals("TComma")) {
            return "218";
        } else if (tempNode[0].equals("TLBrace")) {
            return "219";
        } else if (tempNode[0].equals("TRBrace")) {
            return "220";
        } else if (tempNode[0].equals("TSemicolon")) {
            return "221";
        } else if (tempNode[0].equals("TLBracket")) {
            return "222";
        } else if (tempNode[0].equals("TRBracket")) {
            return "223";
        } else if (tempNode[0].equals("TLParen")) {
            return "224";
        } else if (tempNode[0].equals("TRParen")) {
            return "225";
        } else if (tempNode[0].equals("TColon")) {
            return "226";
        } else if (tempNode[0].equals("TDot")) {
            return "227";
        } else if (tempNode[0].equals("TQuote")) {
            return "228";
        } else if (tempNode[0].equals("TColonEquals")) {
            return "229";
        } else if (tempNode[0].equals("TEquals")) {
            return "230";
        } else if (tempNode[0].equals("TAnd")) {
            return "231";
        } else if (tempNode[0].equals("TOr")) {
            return "232";
        } else if (tempNode[0].equals("TXor")) {
            return "233";
        } else if (tempNode[0].equals("TMod")) {
            return "234";
        } else if (tempNode[0].equals("TCmpeq")) {
            return "235";
        } else if (tempNode[0].equals("TCmpne")) {
            return "236";
        } else if (tempNode[0].equals("TCmpgt")) {
            return "237";
        } else if (tempNode[0].equals("TCmpge")) {
            return "238";
        } else if (tempNode[0].equals("TCmplt")) {
            return "239";
        } else if (tempNode[0].equals("TCmple")) {
            return "240";
        } else if (tempNode[0].equals("TShl")) {
            return "241";
        } else if (tempNode[0].equals("TShr")) {
            return "242";
        } else if (tempNode[0].equals("TUshr")) {
            return "243";
        } else if (tempNode[0].equals("TPlus")) {
            return "244";
        } else if (tempNode[0].equals("TMinus")) {
            return "245";
        } else if (tempNode[0].equals("TMult")) {
            return "246";
        } else if (tempNode[0].equals("TDiv")) {
            return "247";
        } else if (tempNode[0].equals("TQuotedName")) {
            return "248";
        }else if (tempNode[0].equals("TAtIdentifier")) {
            return "249";
        } else if (tempNode[0].equals("TFullIdentifier")) {
            return "IDE_"+tempNode[1].replaceAll("_","us").replaceAll("\\\\","bs");
        } else if (tempNode[0].equals("TIdentifier")) {
            return "IDE_"+tempNode[1].replaceAll("_","us").replaceAll("\\\\","bs");
        } else if (tempNode[0].equals("TBoolConstant")) {
            return "LIT_"+tempNode[1].replaceAll("_","us").replaceAll("\\\\","bs");
        } else if (tempNode[0].equals("TIntegerConstant")){
            return "LIT_"+tempNode[1].replaceAll("_","us").replaceAll("\\\\","bs");
        } else if (tempNode[0].equals("TFloatConstant")) {
            return "LIT_"+tempNode[1].replaceAll("_","us").replaceAll("\\\\","bs");
        }else if (tempNode[0].equals("TStringConstant")) {
            return "LIT_"+tempNode[1].replaceAll("_","us").replaceAll("\\\\","bs");
        } else {
            System.err.println("CPG2vec - COMPLETE: Invalid node " + tempNode[0] + ", exiting...");
            System.exit(0);
        }
        return "-1";
    }

    //120 Nodes but numered till 113
    //STMTs node and TNodes
    private String mapNodeLabel_STMTandTNodes(String[] tempNode){
        if (tempNode[0].equals("ENTRY") || tempNode[0].equals("EXIT")) {//case ENTRY and EXIT Node
            return "0";
        } else if (tempNode[0].equals("ALabelStatement")) {
            return "1";
        } else if (tempNode[0].equals("ABreakpointStatement")) {
            return "2";
        } else if (tempNode[0].equals("AEntermonitorStatement")) {
            return "3";
        } else if (tempNode[0].equals("AExitmonitorStatement")) {
            return "4";
        } else if (tempNode[0].equals("ATableswitchStatement")) {
            return "5";
        } else if (tempNode[0].equals("ALookupswitchStatement")) {
            return "6";
        } else if (tempNode[0].equals("AIdentityStatement")) {
            return "7";
        } else if (tempNode[0].equals("AIdentityNoTypeStatement")) {
            return "8";
        } else if (tempNode[0].equals("AAssignStatement")) {
            return "9";
        } else if (tempNode[0].equals("AIfStatement")) {
            return "10";
        } else if (tempNode[0].equals("AGotoStatement")) {
            return "11";
        } else if (tempNode[0].equals("ANopStatement")) {
            return "12";
        } else if (tempNode[0].equals("ARetStatement")) {
            return "13";
        } else if (tempNode[0].equals("AReturnStatement")) {
            return "14";
        } else if (tempNode[0].equals("AThrowStatement")) {
            return "15";
        } else if (tempNode[0].equals("AInvokeStatement")) {
            return "16";
        } else if (tempNode[0].equals("ALabelName")) {
            return "17";
        } else if (tempNode[0].equals("TIgnored")) {
            return "18";
        } else if (tempNode[0].equals("TAbstract")) {
            return "19";
        } else if (tempNode[0].equals("TFinal")) {
            return "20";
        } else if (tempNode[0].equals("TNative")) {
            return "21";
        } else if (tempNode[0].equals("TPublic")) {
            return "22";
        } else if (tempNode[0].equals("TProtected")) {
            return "23";
        } else if (tempNode[0].equals("TPrivate")) {
            return "24";
        } else if (tempNode[0].equals("TStatic")) {
            return "25";
        } else if (tempNode[0].equals("TSynchronized")) {
            return "26";
        } else if (tempNode[0].equals("TTransient")) {
            return "27";
        } else if (tempNode[0].equals("TVolatile")) {
            return "28";
        } else if (tempNode[0].equals("TStrictfp")) {
            return "29";
        } else if (tempNode[0].equals("TEnum")) {
            return "30";
        } else if (tempNode[0].equals("TAnnotation")) {
            return "31";
        } else if (tempNode[0].equals("TClass")) {
            return "32";
        } else if (tempNode[0].equals("TInterface")) {
            return "33";
        } else if (tempNode[0].equals("TVoid")) {
            return "34";
        } else if (tempNode[0].equals("TBoolean")) {
            return "35";
        } else if (tempNode[0].equals("TByte")) {
            return "36";
        } else if (tempNode[0].equals("TShort")) {
            return "37";
        } else if (tempNode[0].equals("TChar")) {
            return "38";
        } else if (tempNode[0].equals("TInt")) {
            return "39";
        } else if (tempNode[0].equals("TLong")) {
            return "40";
        } else if (tempNode[0].equals("TFloat")) {
            return "41";
        } else if (tempNode[0].equals("TDouble")) {
            return "42";
        } else if (tempNode[0].equals("TNullType")) {
            return "43";
        } else if (tempNode[0].equals("TUnknown")) {
            return "44";
        } else if (tempNode[0].equals("TExtends")) {
            return "45";
        } else if (tempNode[0].equals("TImplements")) {
            return "46";
        } else if (tempNode[0].equals("TBreakpoint")) {
            return "47";
        } else if (tempNode[0].equals("TCase")) {
            return "48";
        } else if (tempNode[0].equals("TCatch")) {
            return "49";
        } else if (tempNode[0].equals("TCmp")) {
            return "50";
        } else if (tempNode[0].equals("TCmpg")) {
            return "51";
        } else if (tempNode[0].equals("TCmpl")) {
            return "52";
        } else if (tempNode[0].equals("TDefault")) {
            return "53";
        } else if (tempNode[0].equals("TEntermonitor")) {
            return "54";
        } else if (tempNode[0].equals("TExitmonitor")) {
            return "55";
        } else if (tempNode[0].equals("TGoto")) {
            return "56";
        } else if (tempNode[0].equals("TIf")) {
            return "57";
        } else if (tempNode[0].equals("TInstanceof")) {
            return "58";
        } else if (tempNode[0].equals("TInterfaceinvoke")) {
            return "59";
        } else if (tempNode[0].equals("TLengthof")) {
            return "60";
        } else if (tempNode[0].equals("TLookupswitch")) {
            return "61";
        } else if (tempNode[0].equals("TNeg")) {
            return "62";
        } else if (tempNode[0].equals("TNew")) {
            return "63";
        } else if (tempNode[0].equals("TNewarray")) {
            return "64";
        } else if (tempNode[0].equals("TNewmultiarray")) {
            return "65";
        } else if (tempNode[0].equals("TNop")) {
            return "66";
        } else if (tempNode[0].equals("TRet")) {
            return "67";
        } else if (tempNode[0].equals("TReturn")) {
            return "68";
        } else if (tempNode[0].equals("TSpecialinvoke")) {
            return "69";
        } else if (tempNode[0].equals("TStaticinvoke")) {
            return "70";
        } else if (tempNode[0].equals("TDynamicinvoke")) {
            return "71";
        } else if (tempNode[0].equals("TTableswitch")) {
            return "72";
        } else if (tempNode[0].equals("TThrow")) {
            return "73";
        } else if (tempNode[0].equals("TThrows")) {
            return "74";
        } else if (tempNode[0].equals("TVirtualinvoke")) {
            return "75";
        } else if (tempNode[0].equals("TNull")) {
            return "76";
        } else if (tempNode[0].equals("TFrom")) {
            return "77";
        } else if (tempNode[0].equals("TTo")) {
            return "78";
        } else if (tempNode[0].equals("TWith")) {
            return "79";
        } else if (tempNode[0].equals("TCls")) {
            return "80";
        } else if (tempNode[0].equals("TComma")) {
            return "81";
        } else if (tempNode[0].equals("TLBrace")) {
            return "82";
        } else if (tempNode[0].equals("TRBrace")) {
            return "83";
        } else if (tempNode[0].equals("TSemicolon")) {
            return "84";
        } else if (tempNode[0].equals("TLBracket")) {
            return "85";
        } else if (tempNode[0].equals("TRBracket")) {
            return "86";
        } else if (tempNode[0].equals("TLParen")) {
            return "87";
        } else if (tempNode[0].equals("TRParen")) {
            return "88";
        } else if (tempNode[0].equals("TColon")) {
            return "89";
        } else if (tempNode[0].equals("TDot")) {
            return "90";
        } else if (tempNode[0].equals("TQuote")) {
            return "91";
        } else if (tempNode[0].equals("TColonEquals")) {
            return "92";
        } else if (tempNode[0].equals("TEquals")) {
            return "93";
        } else if (tempNode[0].equals("TAnd")) {
            return "94";
        } else if (tempNode[0].equals("TOr")) {
            return "95";
        } else if (tempNode[0].equals("TXor")) {
            return "96";
        } else if (tempNode[0].equals("TMod")) {
            return "97";
        } else if (tempNode[0].equals("TCmpeq")) {
            return "98";
        } else if (tempNode[0].equals("TCmpne")) {
            return "99";
        } else if (tempNode[0].equals("TCmpgt")) {
            return "100";
        } else if (tempNode[0].equals("TCmpge")) {
            return "101";
        } else if (tempNode[0].equals("TCmplt")) {
            return "102";
        } else if (tempNode[0].equals("TCmple")) {
            return "103";
        } else if (tempNode[0].equals("TShl")) {
            return "104";
        } else if (tempNode[0].equals("TShr")) {
            return "105";
        } else if (tempNode[0].equals("TUshr")) {
            return "106";
        } else if (tempNode[0].equals("TPlus")) {
            return "107";
        } else if (tempNode[0].equals("TMinus")) {
            return "108";
        } else if (tempNode[0].equals("TMult")) {
            return "109";
        } else if (tempNode[0].equals("TDiv")) {
            return "110";
        } else if (tempNode[0].equals("TQuotedName")) {
            return "111";
        } else if (tempNode[0].equals("TAtIdentifier")) {
            return "112";
        } else if (tempNode[0].equals("TFullIdentifier")) {
            return "IDE_"+tempNode[1].replaceAll("_","us").replaceAll("\\\\","bs");
        } else if (tempNode[0].equals("TIdentifier")) {
            return "IDE_"+tempNode[1].replaceAll("_","us").replaceAll("\\\\","bs");
        } else if (tempNode[0].equals("TBoolConstant")) {
            return "LIT_"+tempNode[1].replaceAll("_","us").replaceAll("\\\\","bs");
        } else if (tempNode[0].equals("TIntegerConstant")){
            return "LIT_"+tempNode[1].replaceAll("_","us").replaceAll("\\\\","bs");
        } else if (tempNode[0].equals("TFloatConstant")) {
            return "LIT_"+tempNode[1].replaceAll("_","us").replaceAll("\\\\","bs");
        }else if (tempNode[0].equals("TStringConstant")) {
            return "LIT_"+tempNode[1].replaceAll("_","us").replaceAll("\\\\","bs");
        } else {
            return "113";
        }
    }

    /*
    else if (tempNode[0].equals("TIgnored || tempNode instanceof TAbstract || tempNode instanceof TFinal
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

/*
    protected String createNodeLabelsListComplete () {
        String toReturn = "node label\n0 0";
        for (int i = 2; i < this.cpg.getSize(); i++) {
            CPGNode tempNode = this.cpg.getCPGNodes().get(i);
            toReturn = toReturn + "\n" + this.getNodeNumber(tempNode) + " " + this.mapNodeLabel_COMPLETE(tempNode.getAstNode());
        }
        return toReturn + "\n1 0";
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

    */

/*

    private int mapStmtLabel (String[] tempNode){
        if (tempNode == null) {//case ENTRY and EXIT Node
            return 0;
        } else if (tempNode[0].equals("ALabelStatement")) {
            return 1;
        } else if (tempNode[0].equals("ABreakpointStatement")) {
            return 4;
        } else if (tempNode[0].equals("ATableswitchStatement")) {
            return 5;
        } else if (tempNode[0].equals("ALookupswitchStatement") ){
            return 6;
        } else if (tempNode[0].equals("AIdentityStatement")) {
            return 7;
        } else if (tempNode[0].equals("AIdentityNoTypeStatement")) {
            return 8;
        } else if (tempNode[0].equals("AAssignStatement")) {
            return 9;
        } else if (tempNode[0].equals("AIfStatement") ){
            return 10;
        } else if (tempNode[0].equals("AGotoStatement")) {
            return 11;
        } else if (tempNode[0].equals("ANopStatement") ){
            return 12;
        } else if (tempNode[0].equals("ARetStatement")) {
            return 13;
        } else if (tempNode[0].equals("AReturnStatement")) {
            return 14;
        } else if (tempNode[0].equals("AThrowStatement")) {
            return 15;
        } else if (tempNode[0].equals("AInvokeStatement")) {
            return 16;
        } else {
            return 17;
        }
    }

    private int mapNodeLabel (String[] tempNode){
        if (tempNode[0] == null) {//case ENTRY and EXIT Node
            return 0;
        } else if (tempNode[0].equals("EOF")) {
            return 258;
        } else if (tempNode[0].equals("Start")) {
            return 257;
        } else if (tempNode[0].equals("AFile")) {
            return 256;
        } else if (tempNode[0].equals("AAbstractModifier")) {
            return 1;
        } else if (tempNode[0].equals("AFinalModifier")) {
            return 2;
        } else if (tempNode[0].equals("ANativeModifier")) {
            return 3;
        } else if (tempNode[0].equals("APublicModifier")) {
            return 4;
        } else if (tempNode[0].equals("AProtectedModifier")) {
            return 5;
        } else if (tempNode[0].equals("APrivateModifier")) {
            return 6;
        } else if (tempNode[0].equals("AStaticModifier")) {
            return 7;
        } else if (tempNode[0].equals("ASynchronizedModifier")) {
            return 8;
        } else if (tempNode[0].equals("ATransientModifier")) {
            return 9;
        } else if (tempNode[0].equals("AVolatileModifier")) {
            return 10;
        } else if (tempNode[0].equals("AStrictfpModifier")) {
            return 11;
        } else if (tempNode[0].equals("AEnumModifier")) {
            return 12;
        } else if (tempNode[0].equals("AAnnotationModifier")) {
            return 13;
        } else if (tempNode[0].equals("AClassFileType")) {
            return 14;
        } else if (tempNode[0].equals("AInterfaceFileType")) {
            return 15;
        } else if (tempNode[0].equals("AExtendsClause")) {
            return 16;
        } else if (tempNode[0].equals("AImplementsClause")) {
            return 17;
        } else if (tempNode[0].equals("AFileBody")) {
            return 18;
        } else if (tempNode[0].equals("ASingleNameList")) {
            return 19;
        } else if (tempNode[0].equals("AMultiNameList")) {
            return 20;
        } else if (tempNode[0].equals("AClassNameSingleClassNameList")) {
            return 21;
        } else if (tempNode[0].equals("AClassNameMultiClassNameList")) {
            return 22;
        } else if (tempNode[0].equals("AFieldMember")) {
            return 23;
        } else if (tempNode[0].equals("AMethodMember")) {
            return 24;
        } else if (tempNode[0].equals("AVoidType")) {
            return 25;
        } else if (tempNode[0].equals("ANovoidType")) {
            return 26;
        } else if (tempNode[0].equals("ASingleParameterList")) {
            return 27;
        } else if (tempNode[0].equals("AMultiParameterList")) {
            return 28;
        } else if (tempNode[0].equals("AParameter")) {
            return 29;
        } else if (tempNode[0].equals("AThrowsClause")) {
            return 30;
        } else if (tempNode[0].equals("ABooleanBaseTypeNoName")) {
            return 31;
        } else if (tempNode[0].equals("AByteBaseTypeNoName")) {
            return 32;
        } else if (tempNode[0].equals("ACharBaseTypeNoName")) {
            return 33;
        } else if (tempNode[0].equals("AShortBaseTypeNoName")) {
            return 34;
        } else if (tempNode[0].equals("AIntBaseTypeNoName")) {
            return 35;
        } else if (tempNode[0].equals("ALongBaseTypeNoName")) {
            return 36;
        } else if (tempNode[0].equals("AFloatBaseTypeNoName")) {
            return 37;
        } else if (tempNode[0].equals("ADoubleBaseTypeNoName")) {
            return 38;
        } else if (tempNode[0].equals("ANullBaseTypeNoName")) {
            return 39;
        } else if (tempNode[0].equals("ABooleanBaseType")) {
            return 40;
        } else if (tempNode[0].equals("AByteBaseType")) {
            return 41;
        } else if (tempNode[0].equals("ACharBaseType")) {
            return 42;
        } else if (tempNode[0].equals("AShortBaseType")) {
            return 43;
        } else if (tempNode[0].equals("AIntBaseType")) {
            return 44;
        } else if (tempNode[0].equals("ALongBaseType")) {
            return 45;
        } else if (tempNode[0].equals("AFloatBaseType")) {
            return 46;
        } else if (tempNode[0].equals("ADoubleBaseType")) {
            return 47;
        } else if (tempNode[0].equals("ANullBaseType")) {
            return 48;
        } else if (tempNode[0].equals("AClassNameBaseType")) {
            return 49;
        } else if (tempNode[0].equals("ABaseNonvoidType")) {
            return 50;
        } else if (tempNode[0].equals("AQuotedNonvoidType")) {
            return 51;
        } else if (tempNode[0].equals("AIdentNonvoidTyp")) {{
            return 52;
        } else if (tempNode[0].equals("AFullIdentNonvoidType")) {
            return 53;
        } else if (tempNode[0].equals("AArrayBrackets")) {
            return 54;
        } else if (tempNode[0].equals("AEmptyMethodBody")) {
            return 55;
        } else if (tempNode[0].equals("AFullMethodBody")) {
            return 56;
        } else if (tempNode[0].equals("ADeclaration")) {
            return 57;
        } else if (tempNode[0].equals("AUnknownJimpleTyp")) {{
            return 58;
        } else if (tempNode[0].equals("ANonvoidJimpleTyp")) {{
            return 59;
        } else if (tempNode[0].equals("ALocalNam")) {{
            return 60;
        } else if (tempNode[0].equals("ASingleLocalNameList")) {
            return 61;
        } else if (tempNode[0].equals("AMultiLocalNameList")) {
            return 62;
        } else if (tempNode[0].equals("ALabelStatement")) {
            return 63;
        } else if (tempNode[0].equals("ABreakpointStatement")) {
            return 64;
        } else if (tempNode[0].equals("AEntermonitorStatement")) {
            return 65;
        } else if (tempNode[0].equals("AExitmonitorStatement")) {
            return 66;
        } else if (tempNode[0].equals("ATableswitchStatement")) {
            return 67;
        } else if (tempNode[0].equals("ALookupswitchStatement")) {
            return 68;
        } else if (tempNode[0].equals("AIdentityStatement")) {
            return 69;
        } else if (tempNode[0].equals("AIdentityNoTypeStatement")) {
            return 70;
        } else if (tempNode[0].equals("AAssignStatement")) {
            return 71;
        } else if (tempNode[0].equals("AIfStatement")) {
            return 72;
        } else if (tempNode[0].equals("AGotoStatement")) {
            return 73;
        } else if (tempNode[0].equals("ANopStatement")) {
            return 74;
        } else if (tempNode[0].equals("ARetStatement")) {
            return 75;
        } else if (tempNode[0].equals("AReturnStatement")) {
            return 76;
        } else if (tempNode[0].equals("AThrowStatement")) {
            return 77;
        } else if (tempNode[0].equals("AInvokeStatement")) {
            return 78;
        } else if (tempNode[0].equals("ALabelName")) {
            return 79;
        } else if (tempNode[0].equals("ACaseStmt")) {
            return 80;
        } else if (tempNode[0].equals("AConstantCaseLabel")) {
            return 81;
        } else if (tempNode[0].equals("ADefaultCaseLabel")) {
            return 82;
        } else if (tempNode[0].equals("AGotoStmt")) {
            return 83;
        } else if (tempNode[0].equals("ACatchClause")) {
            return 84;
        } else if (tempNode[0].equals("ANewExpression")) {
            return 85;
        } else if (tempNode[0].equals("ACastExpression")) {
            return 86;
        } else if (tempNode[0].equals("AInstanceofExpression")) {
            return 87;
        } else if (tempNode[0].equals("AInvokeExpression")) {
            return 88;
        } else if (tempNode[0].equals("AReferenceExpression")) {
            return 89;
        } else if (tempNode[0].equals("ABinopExpression")) {
            return 90;
        } else if (tempNode[0].equals("AUnopExpression")) {
            return 91;
        } else if (tempNode[0].equals("AImmediateExpression")) {
            return 92;
        } else if (tempNode[0].equals("ASimpleNewExpr")) {
            return 93;
        } else if (tempNode[0].equals("AArrayNewExpr")) {
            return 94;
        } else if (tempNode[0].equals("AMultiNewExpr")) {
            return 95;
        } else if (tempNode[0].equals("AArrayDescriptor")) {
            return 96;
        } else if (tempNode[0].equals("AReferenceVariable")) {
            return 97;
        } else if (tempNode[0].equals("ALocalVariable")) {
            return 98;
        } else if (tempNode[0].equals("ABinopBoolExpr")) {
            return 99;
        } else if (tempNode[0].equals("AUnopBoolExpr")) {
            return 100;
        } else if (tempNode[0].equals("ANonstaticInvokeExpr")) {
            return 101;
        } else if (tempNode[0].equals("AStaticInvokeExpr")) {
            return 102;
        } else if (tempNode[0].equals("ADynamicInvokeExpr")) {
            return 103;
        } else if (tempNode[0].equals("ABinopExpr")) {
            return 104;
        } else if (tempNode[0].equals("AUnopExpr")) {
            return 105;
        } else if (tempNode[0].equals("ASpecialNonstaticInvoke")) {
            return 106;
        } else if (tempNode[0].equals("AVirtualNonstaticInvoke")) {
            return 107;
        } else if (tempNode[0].equals("AInterfaceNonstaticInvoke")) {
            return 108;
        } else if (tempNode[0].equals("AUnnamedMethodSignature")) {
            return 109;
        } else if (tempNode[0].equals("AMethodSignature")) {
            return 110;
        } else if (tempNode[0].equals("AArrayReference")) {
            return 111;
        } else if (tempNode[0].equals("AFieldReference")) {
            return 112;
        } else if (tempNode[0].equals("AIdentArrayRef")) {
            return 113;
        } else if (tempNode[0].equals("AQuotedArrayRef")) {
            return 114;
        } else if (tempNode[0].equals("ALocalFieldRef")) {
            return 115;
        } else if (tempNode[0].equals("ASigFieldRef")) {
            return 116;
        } else if (tempNode[0].equals("AFieldSignature")) {
            return 117;
        } else if (tempNode[0].equals("AFixedArrayDescriptor")) {
            return 118;
        } else if (tempNode[0].equals("ASingleArgList")) {
            return 119;
        } else if (tempNode[0].equals("AMultiArgList")) {
            return 120;
        } else if (tempNode[0].equals("ALocalImmediate")) {
            return 121;
        } else if (tempNode[0].equals("AConstantImmediate")) {
            return 122;
        } else if (tempNode[0].equals("AIntegerConstant")) {
            return 123;
        } else if (tempNode[0].equals("AFloatConstant")) {
            return 124;
        } else if (tempNode[0].equals("AStringConstant")) {
            return 125;
        } else if (tempNode[0].equals("AClzzConstant")) {
            return 126;
        } else if (tempNode[0].equals("ANullConstant")) {
            return 127;
        } else if (tempNode[0].equals("AAndBinop")) {
            return 128;
        } else if (tempNode[0].equals("AOrBinop")) {
            return 129;
        } else if (tempNode[0].equals("AXorBinop")) {
            return 130;
        } else if (tempNode[0].equals("AModBinop")) {
            return 131;
        } else if (tempNode[0].equals("ACmpBinop")) {
            return 132;
        } else if (tempNode[0].equals("ACmpgBinop")) {
            return 133;
        } else if (tempNode[0].equals("ACmplBinop")) {
            return 134;
        } else if (tempNode[0].equals("ACmpeqBinop")) {
            return 135;
        } else if (tempNode[0].equals("ACmpneBinop")) {
            return 136;
        } else if (tempNode[0].equals("ACmpgtBinop")) {
            return 137;
        } else if (tempNode[0].equals("ACmpgeBinop")) {
            return 138;
        } else if (tempNode[0].equals("ACmpltBinop")) {
            return 139;
        } else if (tempNode[0].equals("ACmpleBinop")) {
            return 140;
        } else if (tempNode[0].equals("AShlBinop")) {
            return 141;
        } else if (tempNode[0].equals("AShrBinop")) {
            return 142;
        } else if (tempNode[0].equals("AUshrBinop")) {
            return 143;
        } else if (tempNode[0].equals("APlusBinop")) {
            return 144;
        } else if (tempNode[0].equals("AMinusBinop")) {
            return 145;
        } else if (tempNode[0].equals("AMultBinop")) {
            return 146;
        } else if (tempNode[0].equals("ADivBinop")) {
            return 147;
        } else if (tempNode[0].equals("ALengthofUnop")) {
            return 148;
        } else if (tempNode[0].equals("ANegUnop")) {
            return 149;
        } else if (tempNode[0].equals("AQuotedClasName")) {
            return 150;
        } else if (tempNode[0].equals("AIdentClassNa")) { {
            return 151;
        } else if (tempNode[0].equals("AFullIdentClassNam")) {{
            return 152;
        } else if (tempNode[0].equals("AQuotedName")) {
            return 153;
        } else if (tempNode[0].equals("AIdentName")) {
            return 154;
        } else if (tempNode[0].equals("TIgnored")) {
            return 155;
        } else if (tempNode[0].equals("TAbstract")) {
            return 156;
        } else if (tempNode[0].equals("TFinal")) {
            return 157;
        } else if (tempNode[0].equals("TNative")) {
            return 158;
        } else if (tempNode[0].equals("TPublic")) {
            return 159;
        } else if (tempNode[0].equals("TProtected")) {
            return 160;
        } else if (tempNode[0].equals("TPrivate")) {
            return 161;
        } else if (tempNode[0].equals("TStatic")) {
            return 162;
        } else if (tempNode[0].equals("TSynchronized")) {
            return 163;
        } else if (tempNode[0].equals("TTransient")) {
            return 164;
        } else if (tempNode[0].equals("TVolatile")) {
            return 165;
        } else if (tempNode[0].equals("TStrictfp")) {
            return 166;
        } else if (tempNode[0].equals("TEnum")) {
            return 167;
        } else if (tempNode[0].equals("TAnnotation")) {
            return 168;
        } else if (tempNode[0].equals("TClass")) {
            return 169;
        } else if (tempNode[0].equals("TInterface")) {
            return 170;
        } else if (tempNode[0].equals("TVoid")) {
            return 171;
        } else if (tempNode[0].equals("TBoolean")) {
            return 172;
        } else if (tempNode[0].equals("TByte")) {
            return 173;
        } else if (tempNode[0].equals("TShort")) {
            return 174;
        } else if (tempNode[0].equals("TChar")) {
            return 175;
        } else if (tempNode[0].equals("TInt")) {
            return 176;
        } else if (tempNode[0].equals("TLong")) {
            return 177;
        } else if (tempNode[0].equals("TFloat")) {")) {
            return 178;
        } else if (tempNode[0].equals("TD\")) {e") {
            return 179;
        } else if (tempNode[0].equals("TNullType") {
            return 180;
        } else if (tempNode[0].equals("TUnknown")) {")) {
            return 181;
        } else if (tempNode[0].equals("TEx\")) {s") {
            return 182;
        } else if (tempNode[0].equals("TImplemen")) { {
            return 183;
        } else if (tempNode[0].equals("TBreakpoint") {
            return 184;
        } else if (tempNode[0].equals("TCase")) {
            return 185;
        } else if (tempNode[0].equals("TCatch")) {")) {
            return 186;
        } else if (tempNode[0].equals(")) {p") {
            return 187;
        } else if (tempNode[0].equals("TCmpg")) {
            return 188;
        } else if (tempNode[0].equals("TCmpl")) {
            return 189;
        } else if (tempNode[0].equals("TDefault")) {
            return 190;
        } else if (tempNode[0].equals("TEntermonitor")) {
            return 191;
        } else if (tempNode[0].equals("TExitmonitor")) {
            return 192;
        } else if (tempNode[0].equals("TGoto")) {
            return 193;
        } else if (tempNode[0].equals("TIf")) {
            return 194;
        } else if (tempNode[0].equals("TInstanceof")) {
            return 195;
        } else if (tempNode[0].equals("TInterfaceinvoke")) {
            return 196;
        } else if (tempNode[0].equals("TLengthof")) {
            return 197;
        } else if (tempNode[0].equals("TLookupswitch")) {
            return 198;
        } else if (tempNode[0].equals("TNeg")) {
            return 199;
        } else if (tempNode[0].equals("TNew")) {
            return 200;
        } else if (tempNode[0].equals("TNewarray")) {
            return 201;
        } else if (tempNode[0].equals("TNewmultiarray")) {
            return 202;
        } else if (tempNode[0].equals("TNop")) {
            return 203;
        } else if (tempNode[0].equals("TRet")) {
            return 204;
        } else if (tempNode[0].equals("TReturn")) {
            return 205;
        } else if (tempNode[0].equals("TSpecialinvoke")) {
            return 206;
        } else if (tempNode[0].equals("TStaticinvoke")) {
            return 207;
        } else if (tempNode[0].equals("TDynamicinvoke")) {
            return 208;
        } else if (tempNode[0].equals("TTableswitch")) {
            return 209;
        } else if (tempNode[0].equals("TThrow")) {
            return 210;
        } else if (tempNode[0].equals("TThrows")) {
            return 211;
        } else if (tempNode[0].equals("TVirtualinvoke")) {
            return 212;
        } else if (tempNode[0].equals("TNull")) {
            return 213;
        } else if (tempNode[0].equals("TFrom")) {
            return 214;
        } else if (tempNode[0].equals("TTo")) {
            return 215;
        } else if (tempNode[0].equals("TWith")) {")) {
            return 216;
        } else if (tempNode[0].equals(")) {s") {
            return 217;
        } else if (tempNode[0].equals("TComma")) {")) {
            return 218;
        } else if (tempNode[0].equals("TL\")) {e")) {
            return 219;
        } else if (tempNode[0].equals("TRBra")) { {
            return 220;
        } else if (tempNode[0].equals("TSemicolon")) {
            return 221;
        } else if (tempNode[0].equals("TLBracke")) {{
            return 222;
        } else if (tempNode[0].equals("TRBrack")) { {
            return 223;
        } else if (tempNode[0].equals("TLPa")) {) {
            return 224;
        } else if (tempNode[0].equals("TRP\")) {")) {")) {
            return 225;
        } else if (tempNode[0].equals("TColon")) {
            return 226;
        } else if (tempNode[0].equals("TDot")) {
            return 227;
        } else if (tempNode[0].equals("TQuote")) {
            return 228;
        } else if (tempNode[0].equals("TColonEquals")) {
            return 229;
        } else if (tempNode[0].equals("TEquals")) {
            return 230;
        } else if (tempNode[0].equals("TAnd")) {
            return 231;
        } else if (tempNode[0].equals("TOr")) {
            return 232;
        } else if (tempNode[0].equals("TXor")) {
            return 233;
        } else if (tempNode[0].equals("TMod")) {
            return 234;
        } else if (tempNode[0].equals("TCmpeq")) {
            return 235;
        } else if (tempNode[0].equals("TCmpne")) {
            return 236;
        } else if (tempNode[0].equals("TCmpgt")) {
            return 237;
        } else if (tempNode[0].equals("TCmpge")) {
            return 238;
        } else if (tempNode[0].equals("TCmplt")) {
            return 239;
        } else if (tempNode[0].equals("TCmple")) {
            return 240;
        } else if (tempNode[0].equals("TShl")) {
            return 241;
        } else if (tempNode[0].equals("TShr")) {
            return 242;
        } else if (tempNode[0].equals("TUshr")) {
            return 243;
        } else if (tempNode[0].equals("TPlus")) {
            return 244;
        } else if (tempNode[0].equals("TMinus")) {
            return 245;
        } else if (tempNode[0].equals("TMult")) {
            return 246;
        } else if (tempNode[0].equals("TDiv")) {
            return 247;
        } else if (tempNode[0].equals("TQuotedName")) {
            return 248;
        } else if (tempNode[0].equals("TFullIdentifier")) {
            return 249;
        } else if (tempNode[0].equals("TIdentifier")) {
            return 250;
        } else if (tempNode[0].equals("TAtIdentifier")) {
            return 251;
        } else if (tempNode[0].equals("TBoolConstant")) {
            return 252;
        } else if (tempNode[0].equals("TIntegerConstan")) {{
            return 253;
        } else if (tempNode[0].equals("TFloatConstant")) {
            return 254;
        } else if (tempNode[0].equals("TStringConstant")) {
            return 255;
        } else {
            System.err.println("CPG2vec: Invalid node " + tempNode.getClass().getSimpleName() + ", exiting...");
            System.exit(0);
        }
        return -1;
    }*/