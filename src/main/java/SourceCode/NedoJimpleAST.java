package SourceCode;

import soot.jimple.parser.JimpleAST;
import soot.jimple.parser.Walker;
import soot.jimple.parser.lexer.Lexer;
import soot.jimple.parser.lexer.LexerException;
import soot.jimple.parser.node.Start;
import soot.jimple.parser.parser.Parser;
import soot.jimple.parser.parser.ParserException;

import java.io.*;

/**
 * This class encapsulates a NedoJimpleAST instance and provides methods to act on it.
 */
public class NedoJimpleAST{
    private Start mTree = null;

    /**
     * Constructs a JimpleAST and generates its parse tree from the given InputStream.
     *
     * @param aJIS
     *          The InputStream to parse.
     */
    public NedoJimpleAST(InputStream aJIS) throws ParserException, LexerException, IOException {
        Parser p = new Parser(new Lexer(new PushbackReader(new BufferedReader(new InputStreamReader(aJIS)), 1024)));
        this.mTree = p.parse();
    }

    public void applyWalker(NedoWalker w){
        this.mTree.apply(w);
    }
}
