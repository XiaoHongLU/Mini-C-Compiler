package gen;

import ast.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class MainDecl implements ASTVisitor<Register> {

    /*
     * Simple register allocator.
     */

    // contains all the free temporary registers
    private Stack<Register> freeRegs = new Stack<Register>();

    public MainDecl() {
        freeRegs.addAll(Register.tmpRegs);
    }

    private class RegisterAllocationError extends Error {}

    private Register getRegister() {
        try {
            return freeRegs.pop();
        } catch (EmptyStackException ese) {
            throw new RegisterAllocationError(); // no more free registers, bad luck!
        }
    }

    private void freeRegister(Register reg) {
        freeRegs.push(reg);
    }
    public int ifCounter = 0;
    public int whileCounter = 0;
    private int checkReturn = 0;
    private int numPLocal = 0;

    public int getIfCounter()
    {
    	return this.ifCounter;
    }
    public int getWhileCounter()
    {
    	return this.whileCounter;
    }

    private PrintWriter writer; // use this writer to output the assembly instructions


    public PrintWriter emitProgram(Program program, File outputFile) throws FileNotFoundException {
        GlobalDecl glodecl = new GlobalDecl();
            try {
               writer =  glodecl.emitProgram(program, outputFile);
            } catch (FileNotFoundException e) {
                System.out.println("File "+outputFile.toString()+" does not exist.");
                System.exit(2);
            }
        //writer = new PrintWriter(outputFile);

        visitProgram(program);
        return writer;
    }

    @Override
    public Register visitBaseType(BaseType bt) {
        return null;
    }

    @Override
    public Register visitStructType(StructType st) {
        return null;
    }

    @Override
    public Register visitBlock(Block b) {
        // TODO: to complete
        int numP = 2;
        if(b.funcBlock == 1)
        {
    		if(b.vd != null)
    		{
    			numP = b.blockStack + numP;
    			writer.println("addi $sp,$sp,"+-4*numP);
    			int currentOffset = 2;
    			for(int i =0;i<b.vd.size();i++)
    			{
    				b.vd.get(i).offset = 4 * currentOffset;
    				b.vd.get(i).global = 0;
    				if(b.vd.get(i).pValue == 1 && i < 4)
    				{
    					if(b.vd.get(i).type == BaseType.INT || b.vd.get(i).type instanceof PointerType)
    					{
    						writer.println("sw $a"+b.vd.get(i).aValue+","+b.vd.get(i).offset+"($sp)");
    					}
    					else
    					{
    						if(b.vd.get(i).type == BaseType.CHAR)
    						{
    							writer.println("sb $a"+b.vd.get(i).aValue+","+b.vd.get(i).offset+"($sp)");
    						}
    						else
    						{
    							if(b.vd.get(i).type instanceof ArrayType)
    							{
    									int offset = currentOffset * 4;
    									if(((ArrayType)b.vd.get(i).type).t == BaseType.CHAR)
    									{
    										writer.println("sb $a"+b.vd.get(i).aValue+","+offset+"($sp)");
    									}
    									else
    									{
    										writer.println("sw $a"+b.vd.get(i).aValue+","+offset+"($sp)");
    									}
    									currentOffset = currentOffset + ((ArrayType)b.vd.get(i).type).i-1;
    							}
    							else
    							{
    								if(b.vd.get(i).type instanceof StructType)
    								{
    									int offset = currentOffset * 4;
    									writer.println("sw $a"+b.vd.get(i).aValue+","+offset+"($sp)");
    									currentOffset = currentOffset + ((StructType)b.vd.get(i).type).offset-1;
    								}
    							}
    						}
    					}
    				}
    				else
    				{
    					if(b.vd.get(i).type instanceof ArrayType)
    					{
    						currentOffset = currentOffset + ((ArrayType)b.vd.get(i).type).i - 1;
    					}
    					else
    					{
    						if(b.vd.get(i).type instanceof StructType)
    						{
    							currentOffset = currentOffset + ((StructType)b.vd.get(i).type).offset - 1;		
    						}
    					}
    				}
    				currentOffset = currentOffset + 1;
    			}
    			numPLocal = b.blockStack;
    			writer.println("sw $fp,0($sp)");
    			writer.println("sw $ra,4($sp)");
    			writer.println("move $fp,$sp");
    		}
    		if(b.st != null)
    		{
    			for(int i =0;i<b.st.size();i++)
    			{
    				if(b.st.get(i) instanceof Return)
    				{
    					checkReturn = 1;
    					((Return)b.st.get(i)).numP = numP;
    					((Return)b.st.get(i)).funcReturn = 1;
    				}
    				b.st.get(i).accept(this);
    			}
    		}
    		if(checkReturn == 0 && b.vd == null)
    		{
    			//writer.println("jr $ra");
    		}
    		else
    		{
    			if(checkReturn == 0)
    			{
    				writer.println("move $sp,$fp");
    				writer.println("lw $fp,0($sp)");
    				writer.println("lw $ra,4($sp)");
    				writer.println("addi $sp,$sp,"+4*numP);
    			}
    			else
    			{
    				checkReturn = 0;
    			}
    		}
    	}
    	else
    	{
    		if(b.vd != null)
    		{
    			numP = b.blockStack+numP;
    			writer.println("addi $sp,$sp,"+-4*numP);
    			int currentOffset = 4 + numPLocal;
    			for(int i =0;i<b.vd.size();i++)
    			{
    				b.vd.get(i).offset = 4 * currentOffset;
    				b.vd.get(i).global = 0;
    				if(b.vd.get(i).type instanceof ArrayType)
    				{
    					currentOffset = currentOffset + ((ArrayType)b.vd.get(i).type).i - 1;
    				}
    				else
    				{
   						if(b.vd.get(i).type instanceof StructType)
  						{
  							currentOffset = currentOffset + ((StructType)b.vd.get(i).type).offset - 1;
    					}
    				}
    				currentOffset = currentOffset + 1;
    			}
    			//writer.println("sw $fp,"+(numPLocal+2)*4+"($sp)");
    			//writer.println("sw $ra,"+(numPLocal+3)*4+"($sp)");
    		}
    		if(b.st != null)
    		{
    			for(int i =0;i<b.st.size();i++)
    			{
    				if(b.st.get(i) instanceof Return)
    				{
    					checkReturn = 1;
    					((Return)b.st.get(i)).numP = numP;
    				}
    				b.st.get(i).accept(this);
    			}
    		}
    		if(checkReturn == 0 && b.vd == null)
    		{
    		}
    		else
    		{
    			if(checkReturn == 0)
    			{
    				//writer.println("lw $fp,"+(numPLocal+2)*4+"($sp)");
    				//writer.println("lw $ra,"+(numPLocal+3)*4+"($sp)");
    				writer.println("addi $sp,$sp,"+4*numP);
    			}
    			else
    			{
    				checkReturn = 0;
    			}
    		}
    	}
        return null;
    }

    @Override
    public Register visitFunDecl(FunDecl p) {
        // TODO: to complete
        writer.println(p.name+":");
        if(p.params != null)
        {
        	for(int i=0;i<p.params.size();i++)
        	{
        		p.params.get(i).pValue=1;
        		p.params.get(i).aValue=i;
        	}
        }
        p.block.funcBlock = 1;
        p.block.accept(this);
        if(p.name.equals("main"))
        {
        	writer.println("li $v0 10");
			writer.println("syscall");
        }
        numPLocal = 0;
        return null;
    }

    @Override
    public Register visitProgram(Program p) {
        // TODO: to complete
    	int i=0;
		writer.println(".text");
		if(p.funDecls != null)
		{
			for(i=0;i<p.funDecls.size();i++)
			{
				if(p.funDecls.get(i).name.equals("main"))
				{
					writer.println(".globl "+p.funDecls.get(i).name);
					p.funDecls.get(i).accept(this);
				}
			}
		}
        return null;
    }

    @Override
    public Register visitVarDecl(VarDecl vd) {
        // TODO: to complete
        return null;
    }

    @Override
    public Register visitVarExpr(VarExpr v) {
        // TODO: to complete
    	Register addrReg = getRegister();
    	Register result = getRegister();
    	//Register vAddress = v.vd.accept(this);
    	//writer.print("la "+addrReg.toString()+","+vAddress.toString());
    	if(v.vd.global == 0)
    	{
    		/*if(v.vd.pValue == 1)
    		{
    			writer.println("add "+result.toString()+",$zero,"+"$a"+v.vd.aValue+"");
    			freeRegister(addrReg);
    			return result;
    		}
    		else
    		{*/
    			if(v.vd.type == BaseType.INT || v.vd.type instanceof PointerType)
    			{
    				writer.println("lw "+result.toString()+","+v.vd.offset+"($fp)");
    			}
    			else
    			{
    				if(v.vd.type == BaseType.CHAR)
    				{
    					writer.println("lb "+result.toString()+","+v.vd.offset+"($fp)");
    				}
    				else
    				{
    					if(v.vd.type instanceof ArrayType)
    					{
    						writer.println("la "+result.toString()+","+v.vd.offset+"($fp)");	
    					}
    					else
    					{
    						if(v.vd.type instanceof StructType)
    						{
    							writer.println("la "+result.toString()+","+v.vd.offset+"($fp)");
    						}
    					}
    				}
    			}
    			freeRegister(addrReg);
    			return result;
    		//}
    	}
    	else
    	{
    		writer.println("la "+addrReg.toString()+","+v.name);
    		if(v.vd.type == BaseType.INT || v.vd.type instanceof PointerType)
    		{
    			writer.println("lw "+result.toString()+",("+addrReg.toString()+")");
    		}
    		else
    		{
    			if(v.vd.type == BaseType.CHAR)
    			{
    				writer.println("lb "+result.toString()+",("+addrReg.toString()+")");
    			}
    			else
    			{
    				if(v.vd.type instanceof ArrayType)
    				{
    					freeRegister(result);
    					return addrReg;
    				}
    				else
    				{
    					if(v.vd.type instanceof StructType)
    					{
    						freeRegister(result);
    						return addrReg;
    					}
    				}
    			}
    		}
    		freeRegister(addrReg);
    		//freeRegister(vAddress);
        	return result;
    	}
    	//writer.println("lw "+result.toString()+",0("+addrReg.toString()+")");
    	//freeRegister(addrReg);
    	//freeRegister(vAddress);
        //return result;
    }

	@Override
	public Register visitPointerType(PointerType pt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Register visitIntLiteral(IntLiteral il) {
		// TODO Auto-generated method stub
		Register result = getRegister();
		writer.println("li "+ result.toString()+","+il.i);
		return result;
	}

	@Override
	public Register visitStrLiteral(StrLiteral sl) {
		// TODO Auto-generated method stub
		Register result = getRegister();
		writer.println("la "+result.toString()+","+sl.name);
		return result;
	}

	@Override
	public Register visitChrLiteral(ChrLiteral cl) {
		// TODO Auto-generated method stub
		Register result = getRegister();
		writer.println("li "+result.toString()+", '"+cl.cl+"'");
		return result;
	}

	@Override
	public Register visitFunCallExpr(FunCallExpr fce) {
		// TODO Auto-generated method stub
		Register result = getRegister();
		if(fce.name.equals("print_i"))
		{
			writer.println("li $v0, 1");
			Register exp = fce.e.get(0).accept(this);
			if(fce.e.get(0) instanceof ArrayAccessExpr)
			{
				writer.println("lw "+exp.toString()+",("+exp.toString()+")");
			}
			if(fce.e.get(0) instanceof FieldAccessExpr)
			{
				writer.println("lw "+exp.toString()+","+((StructType)((FieldAccessExpr)fce.e.get(0)).type).structName+"."+((FieldAccessExpr)fce.e.get(0)).name+"("+exp.toString()+")");
			}
			writer.println("add $a0, $zero,"+exp.toString());
			writer.println("syscall");
			freeRegister(exp);
			freeRegister(result);
			return null;
		}
		if(fce.name.equals("print_c"))
		{
			writer.println("li $v0, 11");
			Register exp = fce.e.get(0).accept(this);
			if(fce.e.get(0) instanceof ArrayAccessExpr)
			{
				writer.println("lb "+exp.toString()+",("+exp.toString()+")");
			}
			if(fce.e.get(0) instanceof FieldAccessExpr)
			{
				writer.println("lb "+exp.toString()+","+((StructType)((FieldAccessExpr)fce.e.get(0)).type).structName+"."+((FieldAccessExpr)fce.e.get(0)).name+"("+exp.toString()+")");
			}
			writer.println("add $a0, $zero,"+exp.toString());
			writer.println("syscall");
			freeRegister(exp);
			freeRegister(result);
			return null;
		}
		if(fce.name.equals("print_s"))
		{
			writer.println("li $v0, 4");
			Register exp = fce.e.get(0).accept(this);
			writer.println("la $a0,("+exp.toString()+")");
			writer.println("syscall");
			freeRegister(exp);
			freeRegister(result);
			return null;
		}
		if(fce.name.equals("read_i"))
		{
			writer.println("li $v0, 5");
			writer.println("syscall");
			writer.println("move "+result.toString()+",$v0");
			return result;
		}
		if(fce.name.equals("read_c"))
		{
			writer.println("li $v0, 12");
			writer.println("syscall");
			writer.println("move "+result.toString()+",$v0");
			return result;
		}
		if(fce.name.equals("mcmalloc"))
		{
			Register exp = fce.e.get(0).accept(this);
			writer.println("add $a0,$zero "+ exp.toString());
			writer.println("li $v0, 9");
			writer.println("syscall");
			writer.println("move "+result.toString()+",$v0");
			freeRegister(exp);
			return result;
		}
		if(fce.e != null)
		{
			for(int i=0;i<fce.e.size();i++)
			{
				Register exp = fce.e.get(i).accept(this);
				if(i < 4)
				{
					writer.println("add $a"+i+","+exp.toString()+",$zero");
				}
				else
				{
					writer.println("sw "+exp.toString()+","+(i+2)*4+"($sp)");
				}
				freeRegister(exp);
			}
		}
		writer.println("jal "+fce.name);
		writer.println("add "+result.toString()+",$v0,$zero");
		return result;
	}

	@Override
	public Register visitBinOp(BinOp bo) {
		// TODO Auto-generated method stub
		Register lhsReg = bo.e1.accept(this);
		Register rhsReg = bo.e2.accept(this);
		Register result = getRegister();
		switch(bo.o)
		{
		case ADD:
			writer.println("add "+result.toString()+","+lhsReg.toString()+","+rhsReg.toString());
			break;
		case SUB:
			writer.println("sub "+result.toString()+","+lhsReg.toString()+","+rhsReg.toString());
			break;
		case MUL:
			writer.println("mul "+result.toString()+","+lhsReg.toString()+","+rhsReg.toString());
			break;
		case DIV:
			writer.println("div "+lhsReg.toString()+","+rhsReg.toString());
			writer.println("mflo "+result.toString());
			break;
		case MOD:
			writer.println("div "+lhsReg.toString()+","+rhsReg.toString());
			writer.println("mfhi "+result.toString());
			break;
		case GT:
			writer.println("slt "+result.toString()+","+rhsReg.toString()+","+lhsReg.toString());
			break;
		case LT:
			writer.println("slt "+result.toString()+","+lhsReg.toString()+","+rhsReg.toString());
			break;
		case GE:
			writer.println("slt "+result.toString()+","+lhsReg.toString()+","+rhsReg.toString());
			writer.println("xori "+result.toString()+","+result.toString()+",1");
			break;
		case LE:
			writer.println("slt "+result.toString()+","+rhsReg.toString()+","+lhsReg.toString());
			writer.println("xori "+result.toString()+","+result.toString()+",1");
			break;
		case NE:
			Register result4 = getRegister();
			Register result5 = getRegister();
			writer.println("slt "+result4.toString()+","+lhsReg.toString()+","+rhsReg.toString());
			writer.println("slt "+result5.toString()+","+rhsReg.toString()+","+lhsReg.toString());
			writer.println("or "+result.toString()+","+result4.toString()+","+result5.toString());
			freeRegister(result4);
			freeRegister(result5);
			break;
		case EQ:
			Register result2 = getRegister();
			Register result3 = getRegister();
			writer.println("slt "+result2.toString()+","+lhsReg.toString()+","+rhsReg.toString());
			writer.println("slt "+result3.toString()+","+rhsReg.toString()+","+lhsReg.toString());
			writer.println("xor "+result.toString()+","+result2.toString()+","+result3.toString());
			writer.println("xori "+result.toString()+","+result.toString()+", 1");
			freeRegister(result2);
			freeRegister(result3);
			break;
		case OR:
			writer.println("or "+result.toString()+","+lhsReg.toString()+","+rhsReg.toString());
			break;
		case AND:
			writer.println("and "+result.toString()+","+lhsReg.toString()+","+rhsReg.toString());
			break;
		}
		freeRegister(lhsReg);
		freeRegister(rhsReg);
		return result;
	}

	@Override
	public Register visitOp(Op o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Register visitArrayAccessExpr(ArrayAccessExpr aae) {
		// TODO Auto-generated method stub
		Register result = getRegister();
		Register exp1 = aae.e1.accept(this);
		Register exp2 = aae.e2.accept(this);
		writer.println("add "+exp2.toString()+","+exp2.toString()+","+exp2.toString());
		writer.println("add "+exp2.toString()+","+exp2.toString()+","+exp2.toString()); //4 * the offset
		writer.println("add "+exp1.toString()+","+exp2.toString()+","+exp1.toString());
		if(((ArrayType)aae.e1.type).t == BaseType.INT)
		{
			writer.println("la "+result.toString()+","+"0("+exp1.toString()+")");
		}
		else
		{
			writer.println("la "+result.toString()+","+"0("+exp1.toString()+")");
		}
		freeRegister(exp1);
		freeRegister(exp2);
		return result;
	}

	@Override
	public Register visitFieldAccessExpr(FieldAccessExpr fae) {
		// TODO Auto-generated method stub
		Register result = getRegister();
		Register exp = fae.e.accept(this);
		writer.println("la "+result.toString()+",("+exp.toString()+")");
		freeRegister(exp);
		return result;
	}

	@Override
	public Register visitValueAtExpr(ValueAtExpr vae) {
		// TODO Auto-generated method stub
		Register result = getRegister();
		Register exp = vae.e.accept(this);
		writer.println("move "+result.toString()+","+exp.toString());
		freeRegister(exp);
		return result;
	}

	@Override
	public Register visitSizeOfExpr(SizeOfExpr soe) {
		// TODO Auto-generated method stub
		Register result = getRegister();
		if(soe.t == BaseType.INT)
		{
			writer.println("li "+result.toString()+", 4");
		}
		if(soe.t == BaseType.CHAR)
		{
			writer.println("li "+result.toString()+", 1");
		}
		if(soe.t instanceof PointerType)
		{
			writer.println("li "+result.toString()+", 4");
		}
		if(soe.t instanceof ArrayType)
		{
			int i = 4 * ((ArrayType)soe.t).i;
			writer.println("li "+result.toString()+", "+i);
		}
		if(soe.t instanceof StructType)
		{
			writer.println("li "+result.toString()+", "+((StructType)soe.t).structName+".sizeof");
		}
		return result;
	}

	@Override
	public Register visitTypecastExpr(TypecastExpr te) {
		// TODO Auto-generated method stub
		Register result = te.e.accept(this);
		return result;
	}

	@Override
	public Register visitExprStmt(ExprStmt es) {
		// TODO Auto-generated method stub
		es.e.accept(this);
		return null;
	}

	@Override
	public Register visitWhile(While w) {
		// TODO Auto-generated method stub
		String str = "DONE" + whileCounter;
		String doneStr = "L" + whileCounter;
		whileCounter++;
		writer.print(doneStr+":");
		Register exp = w.e.accept(this);
		writer.println("beq "+exp.toString()+","+"$zero"+","+str);
		freeRegister(exp);
		w.s.accept(this);
		writer.println("j "+doneStr);
		writer.println(str+":");
		return null;
	}

	@Override
	public Register visitIf(If i) {
		// TODO Auto-generated method stub
		Register exp = i.e.accept(this);
		String ifStr = "END"+ifCounter;
		String elseStr = "ELSE"+ifCounter;
		ifCounter++;
		writer.println("beq "+exp.toString()+","+"$zero"+", "+elseStr);
		freeRegister(exp);
		i.s1.accept(this);
		writer.println("j "+ifStr);
		writer.println(elseStr+":");
		if(i.s2!=null)
		{
			i.s2.accept(this);
		}
		writer.println(ifStr+":");
		return null;
	}

	@Override
	public Register visitAssign(Assign a) {
		// TODO Auto-generated method stub
		Register expl = a.e1.accept(this);
		Register expr = a.e2.accept(this);
		if(a.e1 instanceof VarExpr)
		{
			writer.println("move "+expl.toString()+","+expr.toString());
			if(((VarExpr)a.e1).vd.global == 0)
			{
				if(((VarExpr)a.e1).vd.type == BaseType.INT || ((VarExpr)a.e1).vd.type instanceof PointerType)
				{
					writer.println("sw "+expl.toString()+","+((VarExpr)a.e1).vd.offset+"($fp)");
				}
				else
				{
					if(((VarExpr)a.e1).vd.type == BaseType.CHAR)
					{
						writer.println("sb "+expl.toString()+","+((VarExpr)a.e1).vd.offset+"($fp)");
					}
				}
			}
			else
			{
				if(((VarExpr)a.e1).vd.type == BaseType.INT || ((VarExpr)a.e1).vd.type instanceof PointerType)
				{
					writer.println("sw "+expl.toString()+","+((VarExpr)a.e1).vd.varName);
				}
				else
				{
					if(((VarExpr)a.e1).vd.type == BaseType.CHAR)
					{
						writer.println("sb "+expl.toString()+","+((VarExpr)a.e1).vd.varName);
					}
				}
			}
		}
		else
		{
			if(a.e1 instanceof ArrayAccessExpr)
			{
				if(((VarExpr)(((ArrayAccessExpr)a.e1).e1)).vd.global == 0)
				{
					writer.println("sw "+expr.toString()+",0("+expl.toString()+")");
				}
				else
				{
					writer.println("sw "+expr.toString()+",("+expl.toString()+")");
				}
			}
			else
			{
				if(a.e1 instanceof FieldAccessExpr)
				{
					if(((VarExpr)(((FieldAccessExpr)a.e1).e)).vd.global == 0)
					{
						writer.println("sw "+expr.toString()+","+((StructType)((FieldAccessExpr)a.e1).type).structName+"."+((FieldAccessExpr)a.e1).name+"("+expl.toString()+")");
					}
					else
					{
						writer.println("sw "+expr.toString()+","+((StructType)((FieldAccessExpr)a.e1).type).structName+"."+((FieldAccessExpr)a.e1).name+"("+((VarExpr)(((FieldAccessExpr)a.e1).e)).vd.varName+")");
					}
				}
			}
		}
		freeRegister(expl);
		freeRegister(expr);
		return null;
	}

	@Override
	public Register visitReturn(Return r) {
		// TODO Auto-generated method stub
		if(r.e != null)
		{
			Register result = r.e.accept(this);
			writer.println("add $v0,$zero,"+result.toString());
			freeRegister(result);
		}
		if(r.funcReturn == 1)
		{
			writer.println("move $sp,$fp");
			writer.println("lw $fp,0($sp)");
    		writer.println("lw $ra,4($sp)");
			writer.println("addi $sp,$sp,"+4*r.numP);
		}
		else
		{
			writer.println("move $sp,$fp");
			writer.println("lw $fp,"+"0($sp)");
    		writer.println("lw $ra,"+"4($sp)");
			writer.println("addi $sp,$sp,"+4*r.numP);
		}
		if(r.e != null)
		{
			writer.println("jr $ra");
		}
		return null;
	}

	@Override
	public Register visitArrayType(ArrayType at) {
		// TODO Auto-generated method stub
		return null;
	}

}
