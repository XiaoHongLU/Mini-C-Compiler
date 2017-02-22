package parser;

import lexer.Token;
import lexer.Tokeniser;
import lexer.Token.TokenClass;
 
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import ast.*;

 
 
/**
 * @author cdubach
 */
public class Parser {
 
    private Token token;
 
    // use for backtracking (useful for distinguishing decls from procs when parsing a program for instance)
    private Queue<Token> buffer = new LinkedList<Token>();
 
    private final Tokeniser tokeniser;
 
 
 
    public Parser(Tokeniser tokeniser) {
        this.tokeniser = tokeniser;
    }
 
    public Program parse() {
        // get the first token
        nextToken();
 
        return parseProgram();
    }
 
    public int getErrorCount() {
        return error;
    }
 
    private int error = 0;
    private Token lastErrorToken;
 
    private void error(TokenClass... expected) {
 
        if (lastErrorToken == token) {
            // skip this error, same token causing trouble
            return;
        }
 
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (TokenClass e : expected) {
            sb.append(sep);
            sb.append(e);
            sep = "|";
        }
        System.out.println("Parsing error: expected ("+sb+") found ("+token+") at "+token.position);
 
        error++;
        lastErrorToken = token;
    }
 
    /*
     * Look ahead the i^th element from the stream of token.
     * i should be >= 1
     */
    private Token lookAhead(int i) {
        // ensures the buffer has the element we want to look ahead
        while (buffer.size() < i)
            buffer.add(tokeniser.nextToken());
        assert buffer.size() >= i;
 
        int cnt=1;
        for (Token t : buffer) {
            if (cnt == i)
                return t;
            cnt++;
        }
 
        assert false; // should never reach this
        return null;
    }
 
 
    /*
     * Consumes the next token from the tokeniser or the buffer if not empty.
     */
    private void nextToken() {
        if (!buffer.isEmpty())
            token = buffer.remove();
        else
            token = tokeniser.nextToken();
    }
 
    /*
     * If the current token is equals to the expected one, then skip it, otherwise report an error.
     * Returns the expected token or null if an error occurred.
     */
    private Token expect(TokenClass... expected) {
        for (TokenClass e : expected) {
            if (e == token.tokenClass) {
                Token cur = token;
                nextToken();
                return cur;
            }
        }
 
        error(expected);
        return null;
    }
 
    /*
    * Returns true if the current token is equals to any of the expected ones.
    */
    private boolean accept(TokenClass... expected) {
        boolean result = false;
        for (TokenClass e : expected)
            result |= (e == token.tokenClass);
        return result;
    }
    private boolean acceptType()
    {
        return accept(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID,TokenClass.STRUCT);
    }
    private boolean acceptLexp()
    {
        return acceptExp();
    }
    private boolean acceptExp()
    {
        return accept(TokenClass.LPAR,TokenClass.MINUS,TokenClass.IDENTIFIER,TokenClass.INT_LITERAL,TokenClass.CHAR_LITERAL,TokenClass.SIZEOF,TokenClass.ASTERIX,TokenClass.STRING_LITERAL);
    }
    private boolean acceptStmt()
    {
        if(acceptExp()||accept(TokenClass.LBRA,TokenClass.WHILE,TokenClass.IF,TokenClass.RETURN))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    private void errorExp()
    {
        error(TokenClass.LPAR,TokenClass.MINUS,TokenClass.IDENTIFIER,TokenClass.INT_LITERAL,TokenClass.CHAR_LITERAL,TokenClass.SIZEOF,TokenClass.ASTERIX,TokenClass.STRING_LITERAL);
    }
 
    private Program parseProgram() {
        parseIncludes();
        List<StructType> st = parseStructDecls();
        List<VarDecl> vd = parseVarDecls();
        List<FunDecl> fd = parseFunDecls();
        expect(TokenClass.EOF);
        return new Program(st,vd,fd);
    }
 
    // includes are ignored, so does not need to return an AST node
    private void parseIncludes() {
        if (accept(TokenClass.INCLUDE)) {
            nextToken();
            expect(TokenClass.STRING_LITERAL);
            parseIncludes();
        }
    }
 
    private List<StructType> parseStructDecls() {
        if(accept(TokenClass.STRUCT))
        {
            if(lookAhead(2).tokenClass==TokenClass.LBRA)
            {
            String s = parseStructType();
            expect(TokenClass.LBRA);
            List<VarDecl> vd = structDecl_a();
            expect(TokenClass.RBRA);
            expect(TokenClass.SC);
            StructType st = new StructType(s,vd);
            List<StructType> st1 = new ArrayList<StructType>();
            if(st!=null)st1.add(st);
            List<StructType> st2 = parseStructDecls();
            if(st2!=null)st1.addAll(st2);
            return st1;
            }
            else
            {
                return null;
            }
        }
        return null;
            // to be completed ...
    }
    private List<VarDecl> structDecl_a()
    {
        List<VarDecl> vd = parseVarDecls_2();
        if(acceptType())
        {
            List<VarDecl> vd1 = structDecl_a();
            if(vd1!=null)vd.addAll(vd1);
        }
        else
        {
            if(accept(TokenClass.RBRA))
            {
                return vd;
            }
            else
            {
                error(TokenClass.RBRA,TokenClass.INT,TokenClass.CHAR,TokenClass.VOID,TokenClass.STRUCT);
                return null;
            }
        }
        return vd;
    }
 
    private List<VarDecl> parseVarDecls() {
        if(acceptType())
        {
            if(lookAhead(1).tokenClass==TokenClass.IDENTIFIER||lookAhead(2).tokenClass==TokenClass.IDENTIFIER||lookAhead(3).tokenClass==TokenClass.IDENTIFIER)
            {
            	List<VarDecl> vd2 = new ArrayList<VarDecl>();
            if(lookAhead(2).tokenClass==TokenClass.SC||lookAhead(3).tokenClass==TokenClass.SC||lookAhead(4).tokenClass==TokenClass.SC)
            {
                VarDecl vd = varDecls_normal();
                if(vd!=null)vd2.add(vd);
                List<VarDecl> vd1 = parseVarDecls();
                if(vd1!=null)vd2.addAll(vd1);
                return vd2;
            }
            else
            {
                if(lookAhead(2).tokenClass==TokenClass.LSBR||lookAhead(3).tokenClass==TokenClass.LSBR||lookAhead(4).tokenClass==TokenClass.LSBR)
                {
                    VarDecl vd = varDecls_array();
                    if(vd!=null)vd2.add(vd);
                    List<VarDecl> vd1 = parseVarDecls();
                    if(vd1!=null)vd2.addAll(vd1);
                    return vd2;
                }
                else
                {
                	if(lookAhead(2).tokenClass==TokenClass.LPAR||lookAhead(3).tokenClass==TokenClass.LPAR||lookAhead(4).tokenClass==TokenClass.LPAR)
                	{
                        return vd2;
                	}
                	else
                	{
                		error();
                		return null;
                	}
                }
            }
          
        }
        else
        {
            error(TokenClass.IDENTIFIER);
            return null;
        }
    }
        return null;

        // to be completed ...
    }
    private VarDecl varDecls_normal()
    {
        Type t = parseType();
        String s = null;
        if(accept(TokenClass.IDENTIFIER))
        {
        	s = token.data;
        	nextToken();
        }
        else
        {
        	error(TokenClass.IDENTIFIER);
        }
        expect(TokenClass.SC);
        return new VarDecl(t,s);
        //parseVarDecls();
    }
    private VarDecl varDecls_array()
    {
        Type t = parseType();
        String s = null;
        if(accept(TokenClass.IDENTIFIER))
        {
        	s = token.data;
        	nextToken();
        }
        else
        {
        	error(TokenClass.IDENTIFIER);
        }
        expect(TokenClass.LSBR);
        int i = 0;
        if(accept(TokenClass.INT_LITERAL))
        {
        	i = Integer.valueOf(token.data);
        	nextToken();
        }
        else
        {
        	error(TokenClass.INT_LITERAL);
        }
        //IntLiteral il = new IntLiteral(i);
        expect(TokenClass.RSBR);
        expect(TokenClass.SC);
        return new VarDecl(new ArrayType(t,i),s);
        //parseVarDecls();
    }
 
    private List<FunDecl> parseFunDecls() {
        if(acceptType())
        {
            Type t = parseType();
            String s = null;
            if(accept(TokenClass.IDENTIFIER))
            {
            	s = token.data;
            	nextToken();
            }
            else
            {
            	error(TokenClass.IDENTIFIER);
            }
            expect(TokenClass.LPAR);
            List<VarDecl> vd = parseParams();
            expect(TokenClass.RPAR);
            Block b = parseBlock();
            FunDecl fd = new FunDecl(t,s,vd,b);
            List<FunDecl> fd1 = new ArrayList<FunDecl>();
            if(fd!=null)fd1.add(fd);
            List<FunDecl> fd2 = parseFunDecls();
            if(fd2!=null)fd1.addAll(fd2);
            return fd1;
        }
        return null;
        // to be completed ...
    }
 
    private Type parseType()
    {
        if(acceptType())
        {
        	Type t = null;
            if(accept(TokenClass.STRUCT))
            {
               String s = parseStructType();
               t = new StructType(s,null);
               //StructType 
            }
            else
            {
            	if(token.tokenClass == TokenClass.INT)
            	{
            		t = BaseType.INT;
            	}
            	else
            	{
            		if(token.tokenClass == TokenClass.VOID)
            		{
            			t = BaseType.VOID;
            		}
            		else
            		{
            			if(token.tokenClass == TokenClass.CHAR)
            			{
            				t = BaseType.CHAR;
            			}
            		}
            	}
                nextToken();
            }
            boolean check = parseType_a();
            if(check==true)
            {
            	return new PointerType(t);
            }
            return t;
        }
        else
        {
            error(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID,TokenClass.STRUCT);
            return null;
        }
 
    }
    private boolean parseType_a()
    {
        if(accept(TokenClass.ASTERIX))
        {
            nextToken();
            return true;
        }
        return false;
    }
 
    private String parseStructType()
    {
        expect(TokenClass.STRUCT);
        String s = null;
        if(accept(TokenClass.IDENTIFIER))
        {
        	s = token.data;
        	nextToken();
        }
        else
        {
        	error(TokenClass.IDENTIFIER);
        }
        return s;
    }
 
    private List<VarDecl> parseParams()
    {
        if(acceptType())
        {
            Type t = parseType();
            String s = null;
            if(accept(TokenClass.IDENTIFIER))
            {
            	s = token.data;
            	nextToken();
            }
            else
            {
            	error(TokenClass.IDENTIFIER);
            }
            VarDecl vd = new VarDecl(t,s);
            List<VarDecl> vd1 = new ArrayList<VarDecl>();
            if(vd!=null)vd1.add(vd);
            List<VarDecl> vd2 = params_a();
            if(vd2!=null)vd1.addAll(vd2);
            return vd1;
        }
        return null;
    }
    private List<VarDecl> params_a()
    {
        if(accept(TokenClass.COMMA))
        {
            nextToken();
            Type t = parseType();
            String s = null;
            if(accept(TokenClass.IDENTIFIER))
            {
            	s = token.data;
            	nextToken();
            }
            else
            {
            	error(TokenClass.IDENTIFIER);
            }
            VarDecl vd = new VarDecl(t,s);
            List<VarDecl> vd1 = new ArrayList<VarDecl>();
            if(vd!=null)vd1.add(vd);
            List<VarDecl> vd2 = params_a();
            if(vd2!=null)vd1.addAll(vd2);
            return vd1;
        }
        return null;
    }
 
    private Stmt parseStmt()
    {
        switch(token.tokenClass)
        {
            case LBRA:
            Block b = parseBlock();
            return b;
		case WHILE:
            expect(TokenClass.WHILE);
            expect(TokenClass.LPAR);
            Expr e = parseExp();
            expect(TokenClass.RPAR);
            Stmt s = parseStmt();
            return new While(e,s);
		case IF:
            expect(TokenClass.IF);
            expect(TokenClass.LPAR);
            Expr e1 = parseExp();
            expect(TokenClass.RPAR);
            Stmt s1 = parseStmt();
            Stmt s2 = stmt_a();
            return new If(e1,s1,s2);
		case RETURN:
            expect(TokenClass.RETURN);
            Expr e2 = stmt_b();
            expect(TokenClass.SC);
            return new Return(e2);
		default:
            if(acceptLexp())
            {
                Expr e3 = parseExp();
                if(accept(TokenClass.ASSIGN))
                {
                    nextToken();
                    Expr e4 = parseExp();
                    expect(TokenClass.SC);
                    return new Assign(e3,e4);
                }
                else
                {
                    if(accept(TokenClass.SC))
                    {
                        nextToken();
                        return new ExprStmt(e3);
                    }
                    else
                    {
                        error(TokenClass.SC,TokenClass.ASSIGN);
                        return null;
                    }
                }
            }
            else
            {
                    error(TokenClass.LPAR,TokenClass.MINUS,TokenClass.IDENTIFIER,TokenClass.INT_LITERAL,TokenClass.CHAR_LITERAL,TokenClass.SIZEOF,TokenClass.ASTERIX,TokenClass.LBRA,TokenClass.WHILE,TokenClass.IF,TokenClass.RETURN);
                    return null;
            }
        }
               
    }
    private Stmt stmt_a()
    {
        if(accept(TokenClass.ELSE))
        {
            nextToken();
            Stmt s = parseStmt();
            return s;
        }
        return null;
    }
    private Expr stmt_b()
    {
        if(acceptExp())
        {
            Expr e = parseExp();
            return e;
        }
        return null;
    }
 
    private Block parseBlock()
    {
        if(accept(TokenClass.LBRA))
        {
            nextToken();
            List<VarDecl> vd = blcok_a();
            List<Stmt> s = block_b();
            expect(TokenClass.RBRA);
            return new Block(vd,s);
        }
        else
        {
            error(TokenClass.LBRA);
            return null;
        }
    }
    private List<VarDecl> blcok_a()
    {
        if(acceptType())
        {
        	/*parseType();
        	expect(TokenClass.IDENTIFIER);
            if(accept(TokenClass.SC))
            {
            	nextToken();
            }
            else
            {
            	if(accept(TokenClass.LSBR))
            	{
            		//expect(TokenClass.LSBR);
            		nextToken();
        			expect(TokenClass.INT_LITERAL);
        			expect(TokenClass.RSBR);
        			expect(TokenClass.SC);
        		}
        		else
        		{
        			error(TokenClass.SC,TokenClass.LSBR);
        		}
            }*/
            List<VarDecl> vd = parseVarDecls_2();
            List<VarDecl> vd1 = new ArrayList<VarDecl>();
            if(vd!=null)vd1.addAll(vd);
            //parseVarDecls();
            //System.out.println("test");
            List<VarDecl> vd2 = blcok_a();
            if(vd2!=null)vd1.addAll(vd2);
            return vd1;
        }
        return null;
    }
    private List<Stmt> block_b()
    {
        if(acceptStmt())
        {
            Stmt s = parseStmt();
            List<Stmt> s1 = new ArrayList<Stmt>();
            if(s!=null)s1.add(s);
            List<Stmt> s2 = block_b();
            if(s2!=null)s1.addAll(s2);
            return s1;
        }
        return null;
    }
 
    private Expr parseExp()
    {
        Expr e1 = exp_a();
        if(accept(TokenClass.AND,TokenClass.OR))
        {
        	Op o = null;
        	if(token.tokenClass == TokenClass.AND)
        	{
        		o = Op.AND;
        	}
        	else
        	{
        		if(token.tokenClass == TokenClass.OR)
        		{
        			o = Op.OR;
        		}
        	}
            nextToken();
            Expr e2 = exp_a();
            return new BinOp(e1,o,e2);
        }
        return e1;
    }
    private Expr exp_a()
    {
        Expr e1 = exp_b();
        BinOp bo = exp_f(e1);
        if(bo!=null)
        {
            return bo;
        }
        return e1;
    }
    private Expr exp_b()
    {
        Expr e1 = exp_d();
        BinOp bo = exp_g(e1);
        if(bo!=null)
        {
            return bo;
        }
        return e1;
    }
    private Expr exp_d()
    {
        Expr e1 = exp_e();
        BinOp bo = exp_h(e1);
        if(bo!=null)
        {
            return bo;
        }
        return e1;
    }
    private Expr exp_e()
    {
    	Expr e_total = null;
        switch(token.tokenClass)
        {
            case MINUS:
            nextToken();
            if(token.tokenClass == TokenClass.IDENTIFIER)
            {
                String s = token.data;
                e_total = new BinOp(new IntLiteral(0),Op.SUB,new StrLiteral(s));
            }
            else
            {
                if (token.tokenClass == TokenClass.INT_LITERAL)
                	{
                		int i = Integer.valueOf(token.data);
                		e_total = new BinOp(new IntLiteral(0),Op.SUB,new IntLiteral(i));
                	}
                else
                {
                	error(TokenClass.IDENTIFIER,TokenClass.INT_LITERAL);
                }
            }
            nextToken();
            break;
            case CHAR_LITERAL:
            char c = token.data.charAt(0);
            e_total = new ChrLiteral(c);
            nextToken();
            break;
            case STRING_LITERAL:
            String s = token.data;
            e_total = new StrLiteral(s);
            nextToken();
            break;
            case INT_LITERAL:
            int i = Integer.valueOf(token.data);
            e_total = new IntLiteral(i);
            nextToken();
            break;
            case ASTERIX:
            e_total = parseValueAt();
            break;
            case SIZEOF:
            e_total = parseSizeOf();
            break;
            case LPAR:
            if(lookAhead(1).tokenClass==TokenClass.INT || lookAhead(1).tokenClass==TokenClass.CHAR ||lookAhead(1).tokenClass==TokenClass.VOID ||lookAhead(1).tokenClass==TokenClass.STRUCT)
            {
            	e_total = parseTypeCast();
            }
            else
            {
                if(lookAhead(1).tokenClass==TokenClass.LPAR || lookAhead(1).tokenClass==TokenClass.MINUS ||lookAhead(1).tokenClass==TokenClass.IDENTIFIER ||lookAhead(1).tokenClass==TokenClass.INT_LITERAL || lookAhead(1).tokenClass==TokenClass.CHAR_LITERAL || lookAhead(1).tokenClass==TokenClass.STRING_LITERAL ||lookAhead(1).tokenClass==TokenClass.ASTERIX ||lookAhead(1).tokenClass==TokenClass.SIZEOF)
                {
                    nextToken();
                    e_total = parseExp();
                    expect(TokenClass.RPAR);
                }
                else
                {
                    nextToken();
                    error(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID,TokenClass.STRUCT,TokenClass.LPAR,TokenClass.MINUS,TokenClass.IDENTIFIER,TokenClass.INT_LITERAL,TokenClass.CHAR_LITERAL,TokenClass.STRING_LITERAL,TokenClass.ASTERIX,TokenClass.SIZEOF);
                    return null;
                }
            }
            break;
            case IDENTIFIER:
            if(lookAhead(1).tokenClass == TokenClass.LPAR)
            {
            	e_total = parseFunCall();
            }
            else
            {
                String s2 = token.data;
                nextToken();
                e_total = new VarExpr(s2);
            }
            break;
            default:
            errorExp();
            return null;
        }
        if(accept(TokenClass.DOT,TokenClass.LSBR))
        {
        	if(token.tokenClass == TokenClass.DOT)
        	{
        		String s = parseFieldAccess();
        		return new FieldAccessExpr(e_total,s);
        	}
        	else
        	{
        		if(token.tokenClass == TokenClass.LSBR)
        		{
        			Expr e = parseArrayAccess();
        			return new ArrayAccessExpr(e_total,e);
        		}
        	}
        }
        return e_total;
    }
    private BinOp exp_f(Expr e2)
    {
        if(accept(TokenClass.EQ,TokenClass.NE,TokenClass.LT,TokenClass.GT,TokenClass.LE,TokenClass.GE))
        {
        	Op o = null;
        	if(token.tokenClass==TokenClass.EQ)
        	{
        		o = Op.EQ;
        	}
        	else
        	{
        		if(token.tokenClass==TokenClass.NE)
        		{
        			o = Op.NE;
        		}
        		else
        		{
        			if(token.tokenClass==TokenClass.GT)
        			{
        				o = Op.GT;
        			}
        			else
        			{
        				if(token.tokenClass==TokenClass.LE)
        				{
        					o = Op.LE;
        				}
        				else
        				{
        					if(token.tokenClass==TokenClass.GE)
        					{
        						o = Op.GE;
        					}
                            else
                            {
                                if(token.tokenClass==TokenClass.LT)
                                {
                                    o = Op.LT;
                                }
                            }
        				}
        			}
        		}
        	}
            nextToken();
            Expr e1 = exp_b();
            BinOp bo = new BinOp(e2,o,e1);
            BinOp bo1 = exp_f(bo);
            if(bo1!=null)
            {
                return bo1;
            }
            return bo;
        }
        return null;
    }
    private BinOp exp_g(Expr e2)
    {
	   if(accept(TokenClass.PLUS,TokenClass.MINUS))
        {
		   Op o = null;
		   if(token.tokenClass==TokenClass.PLUS)
		   {
			   o = Op.ADD;
		   }
		   else
		   {
			   if(token.tokenClass==TokenClass.MINUS)
			   {
				   o = Op.SUB;
			   }
		   }
            nextToken();
            Expr e1 = exp_d();
            BinOp bo = new BinOp(e2,o,e1);
            BinOp bo1 = exp_g(bo);
            if(bo1 != null)
            {
                return bo1;
            }
            return bo;
        }
	   return null;
    }
    private BinOp exp_h(Expr e2)
    {
        if(accept(TokenClass.ASTERIX,TokenClass.DIV,TokenClass.REM))
        {
        	Op o = null;
        	if(token.tokenClass==TokenClass.ASTERIX)
        	{
        		o = Op.MUL;
        	}
        	else
        	{
        		if(token.tokenClass==TokenClass.DIV)
        		{
        			o = Op.DIV;
        		}
        		else
        		{
        			if(token.tokenClass==TokenClass.REM)
        			{
        				o = Op.MOD;
        			}
        		}
        	}
            nextToken();
            Expr e1 = exp_e();
            BinOp bo = new BinOp(e2,o,e1);
            BinOp bo1 = exp_h(bo);
            if(bo1!=null)
            {
                return bo1;
            }
            return bo;
        }
        return null;
    }
 
 
    private FunCallExpr parseFunCall()
    {
    	String s = null;
    	if(accept(TokenClass.IDENTIFIER))
        {
        	s = token.data;
        	nextToken();
        }
        else
        {
        	error(TokenClass.IDENTIFIER);
        }
        expect(TokenClass.LPAR);
        List<Expr> e = funCall_a();
        expect(TokenClass.RPAR);
        return new FunCallExpr(s,e);
    }
    private List<Expr> funCall_a()
    {
        if(acceptExp())
        {
            Expr e = parseExp();
            List<Expr> e1 = new ArrayList<Expr>();
            if(e!=null)e1.add(e);
            List<Expr> e2 = funCall_b();
            if(e2!=null)e1.addAll(e2);
            return e1;
        }
        return null;
 
    }
    private List<Expr> funCall_b()
    {
        if(accept(TokenClass.COMMA))
        {
            nextToken();
            Expr e = parseExp();
            List<Expr> e1 = new ArrayList<Expr>();
            if(e!=null)e1.add(e);
            List<Expr> e2 = funCall_b();
            if(e2!=null)e1.addAll(e2);
            return e1;
        }
        return null;
    }
 
    private Expr parseArrayAccess()
    {
        //parseExp();
        expect(TokenClass.LSBR);
        Expr e = parseExp();
        expect(TokenClass.RSBR);
        if(accept(TokenClass.DOT))
        {
            String s = parseFieldAccess();
            return new FieldAccessExpr(e,s);
        }
        else
        {
            if(accept(TokenClass.LSBR))
            {
                Expr e2 = parseArrayAccess();
                return new ArrayAccessExpr(e,e2);
            }
        }
        return e;
    }
 
    private String parseFieldAccess()
    {
        //parseExp();
        expect(TokenClass.DOT);
        //parseExp();
        String s = null;
        if(accept(TokenClass.IDENTIFIER))
        {
        	s = token.data;
        	nextToken();
            if(accept(TokenClass.DOT))
            {
                parseFieldAccess();
                //new FieldAccessExpr(s,s2);
            }
            else
            {
                if(accept(TokenClass.LSBR))
                {
                    parseArrayAccess();
                }
            }
        }
        else
        {
        	error(TokenClass.IDENTIFIER);
        }
        return s;
    }
 
    private ValueAtExpr parseValueAt()
    {
        expect(TokenClass.ASTERIX);
        Expr e = parseExp();
        return new ValueAtExpr(e);
    }
 
    private SizeOfExpr parseSizeOf()
    {
        expect(TokenClass.SIZEOF);
        expect(TokenClass.LPAR);
        Type t = parseType();
        expect(TokenClass.RPAR);
        return new SizeOfExpr(t);
    }
 
    private TypecastExpr parseTypeCast()
    {
        expect(TokenClass.LPAR);
        Type t = parseType();
        expect(TokenClass.RPAR);
        Expr e = parseExp();
        return new TypecastExpr(t,e);
    }
    private List<VarDecl> parseVarDecls_2()
    {
        if(acceptType())
        {
            Type t = parseType();
            String s = null;
            List<VarDecl> vd1 = new ArrayList<VarDecl>();
            if(accept(TokenClass.IDENTIFIER))
            {
            	s = token.data;
            	nextToken();
            }
            else
            {
            	error(TokenClass.IDENTIFIER);
            }
            if(accept(TokenClass.SC))
            {
            	VarDecl vd = new VarDecl(t,s);
                if(vd!=null)vd1.add(vd);
                nextToken();
            }
            else
            {
                if(accept(TokenClass.LSBR))
                {
                    nextToken();
                    int i = 0;
                    if(accept(TokenClass.INT_LITERAL))
                    {
                    	i = Integer.valueOf(token.data);
                    	nextToken();
                    }
                    else
                    {
                    	error(TokenClass.INT_LITERAL);
                    }
                    VarDecl vd = new VarDecl(new ArrayType(t,i),s);
                    if(vd!=null)vd1.add(vd);
                    expect(TokenClass.RSBR);
                    expect(TokenClass.SC);
                }
                else
                {
                    error(TokenClass.SC,TokenClass.LSBR);
                }
            }
            List<VarDecl> vd2 = parseVarDecls_2();
            if(vd2!=null)vd1.addAll(vd2);
            return vd1;
        }
        return null;
    }
 
    // to be completed ...        
}
