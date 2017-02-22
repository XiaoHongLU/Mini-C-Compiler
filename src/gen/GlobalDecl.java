package gen;

import ast.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.HashMap;
import java.util.List;

public class GlobalDecl implements ASTVisitor<Void> {


	public int strCounter = 0;
    public int funcOffset = 0;
    public HashMap<String,List<VarDecl>> structList = new HashMap<String,List<VarDecl>>();

    private PrintWriter writer; // use this writer to output the assembly instructions


    public PrintWriter emitProgram(Program program, File outputFile) throws FileNotFoundException {
        writer = new PrintWriter(outputFile);
        visitProgram(program);
        return writer;
    }

    @Override
    public Void visitBaseType(BaseType bt) {
        return null;
    }

    @Override
    public Void visitStructType(StructType st) {
    	writer.println(st.structName+":");
    	if(st.varDecls!=null)
    	{
            structList.put(st.structName,st.varDecls);
    		for(int i=0;i<st.varDecls.size();i++)
    		{
    			writer.print(st.structName+".");
    			st.varDecls.get(i).accept(this);
    			if(st.varDecls.get(i).type == BaseType.CHAR)
    			{
    				writer.println(".align 4");
    			}
    			if(st.varDecls.get(i).type == BaseType.CHAR || st.varDecls.get(i).type == BaseType.INT || st.varDecls.get(i).type instanceof PointerType)
    			{
    				st.offset = st.offset + 1;
    			}
    			else
    			{
    				if(st.varDecls.get(i).type instanceof ArrayType)
    				{
    					st.offset = st.offset + ((ArrayType)st.varDecls.get(i).type).i;
    				}
    				else
    				{
    					if(st.varDecls.get(i).type instanceof StructType)
    					{
    						st.offset = st.offset + ((StructType)st.varDecls.get(i).type).offset;
    					}
    				}
    			}
    		}
    	}
    	else
    	{
            structList.put(st.structName,null);
    		st.offset = 0;
    	}
        return null;
    }

    @Override
    public Void visitBlock(Block b) {
        // TODO: to complete
        if(b.funcBlock == 1)
        {
        	if(b.vd !=null)
        	{
        		for(int i=0;i<b.vd.size();i++)
        		{
        			if(b.vd.get(i).type == BaseType.CHAR || b.vd.get(i).type == BaseType.INT || b.vd.get(i).type instanceof PointerType)
        			{
        				b.blockStack = b.blockStack + 1;
        			}
        			else
        			{
        				if(b.vd.get(i).type instanceof ArrayType)
        				{
        					b.blockStack = b.blockStack + ((ArrayType)b.vd.get(i).type).i;
        				}
        				else
        				{
        					if(b.vd.get(i).type instanceof StructType)
        					{
        						b.blockStack = b.blockStack + ((StructType)b.vd.get(i).type).offset;
        					}
        				}
        			}
        		}
        	}
        	else
        	{
        		b.blockStack = 0;
        	}
        	if(b.st != null)
    		{
    			for(int i =0;i<b.st.size();i++)
    			{
    				b.st.get(i).accept(this);
    			}
    		}
        }
        else
        {
        	if(b.vd !=null)
        	{
        		for(int i=0;i<b.vd.size();i++)
        		{
        			if(b.vd.get(i).type == BaseType.CHAR || b.vd.get(i).type == BaseType.INT || b.vd.get(i).type instanceof PointerType)
        			{
        				b.blockStack = b.blockStack + 1;
        			}
        			else
        			{
        				if(b.vd.get(i).type instanceof ArrayType)
        				{
        					b.blockStack = b.blockStack + ((ArrayType)b.vd.get(i).type).i;
        				}
        				else
        				{
        					if(b.vd.get(i).type instanceof StructType)
        					{
                                b.blockStack = b.blockStack + ((StructType)b.vd.get(i).type).offset;
        					}
        				}
        			}
        		}
        	}
        	else
        	{
        		b.blockStack = 0;
        	}
        	if(b.st != null)
    		{
    			for(int i =0;i<b.st.size();i++)
    			{
    				b.st.get(i).accept(this);
    			}
    		}
        }
        return null;
    }

    @Override
    public Void visitFunDecl(FunDecl p) {
        // TODO: to complete
        int i=0;
        p.block.funcBlock = 1;
        p.block.accept(this);
        return null;
    }

    @Override
    public Void visitProgram(Program p) {
        // TODO: to complete
    	writer.println(".data");
    	int i = 0;
		if(p.structTypes != null)
		{
			for(;i<p.structTypes.size();i++)
			{
				p.structTypes.get(i).accept(this);
			}
		}
		if(p.varDecls != null)
		{
			for(i=0;i<p.varDecls.size();i++)
			{
				p.varDecls.get(i).accept(this);
			}
		}
		if(p.funDecls != null)
		{
			for(i=0;i<p.funDecls.size();i++)
			{
				p.funDecls.get(i).accept(this);
			}
		}
        return null;
    }

    @Override
    public Void visitVarDecl(VarDecl vd) {
        // TODO: to complete
    	if(vd.type == BaseType.INT)writer.println(vd.varName+":"+".space 4");
    	if(vd.type == BaseType.CHAR)writer.println(vd.varName+":"+".space 1");
    	if(vd.type instanceof PointerType)
    	{
    		if(((PointerType)vd.type).t == BaseType.INT)
    		{
    			writer.println(vd.varName+":"+".space 4");
    		}
    		else
    		{
    			if(((PointerType)vd.type).t == BaseType.CHAR)
    			{
    				writer.println(vd.varName+":"+".space 1");
    			}
    		}
    	}
    	if(vd.type instanceof ArrayType)
    	{
    		if(((ArrayType)vd.type).t == BaseType.INT)
    		{
    			int i = 4 * ((ArrayType)vd.type).i;
    			writer.println(vd.varName+":"+".space "+i);
    		}
    		else
    		{
    			if(((ArrayType)vd.type).t == BaseType.CHAR)
    			{
    				writer.println(vd.varName+":"+".space "+((ArrayType)vd.type).i*4);
    			}
    		}
    	}
    	if(vd.type instanceof StructType)
    	{
            List<VarDecl> vds = structList.get(((StructType)vd.type).structName);
    		for(int i = 0;i<vds.size();i++)
            {
                writer.println(vd.varName+"_"+((StructType)vd.type).structName+"_"+vds.get(i).varName+":");
                if(vds.get(i).type == BaseType.INT || vds.get(i).type == BaseType.CHAR || vds.get(i).type instanceof PointerType)
                {
                    writer.println(".space "+4);
                }
                else
                {
                    if(vds.get(i).type instanceof ArrayType)
                    {
                        int j = ((ArrayType)vds.get(i).type).i * 4;
                        writer.println(".space "+j);
                    }
                }
            }
    	}
        return null;
    }

    @Override
    public Void visitVarExpr(VarExpr v) {
        // TODO: to complete
        return null;
    }

	@Override
	public Void visitPointerType(PointerType pt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitIntLiteral(IntLiteral il) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitStrLiteral(StrLiteral sl) {
		// TODO Auto-generated method stub
		String name = "Str"+strCounter;
		sl.name = name;
		writer.println(name+": .asciiz \""+sl.sl+"\"");
		this.strCounter++;
		return null;
	}

	@Override
	public Void visitChrLiteral(ChrLiteral cl) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitFunCallExpr(FunCallExpr fce) {
		// TODO Auto-generated method stub
        if(fce.e != null)
        {
            for(int i=0;i<fce.e.size();i++)
            {
                fce.e.get(i).accept(this);
            }
        }
        fce.offset = funcOffset;
        funcOffset = funcOffset + 1;
		return null;
	}

	@Override
	public Void visitBinOp(BinOp bo) {
		// TODO Auto-generated method stub
        bo.e1.accept(this);
        bo.e2.accept(this);
		return null;
	}

	@Override
	public Void visitOp(Op o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitArrayAccessExpr(ArrayAccessExpr aae) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitFieldAccessExpr(FieldAccessExpr fae) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitValueAtExpr(ValueAtExpr vae) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitSizeOfExpr(SizeOfExpr soe) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitTypecastExpr(TypecastExpr te) {
		// TODO Auto-generated method stub
        te.e.accept(this);
		return null;
	}

	@Override
	public Void visitExprStmt(ExprStmt es) {
		// TODO Auto-generated method stub
        es.e.accept(this);
		return null;
	}

	@Override
	public Void visitWhile(While w) {
		// TODO Auto-generated method stub
        w.s.accept(this);
		return null;
	}

	@Override
	public Void visitIf(If i) {
		// TODO Auto-generated method stub
        i.s1.accept(this);
        if(i.s2 != null)
        {
            i.s2.accept(this);
        }
		return null;
	}

	@Override
	public Void visitAssign(Assign a) {
		// TODO Auto-generated method stub
        a.e2.accept(this);
		return null;
	}

	@Override
	public Void visitReturn(Return r) {
		// TODO Auto-generated method stub
        if(r.e != null)r.e.accept(this);
		return null;
	}

	@Override
	public Void visitArrayType(ArrayType at) {
		// TODO Auto-generated method stub
		return null;
	}
}