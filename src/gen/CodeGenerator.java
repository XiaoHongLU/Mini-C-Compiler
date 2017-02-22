package gen;

import ast.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class CodeGenerator implements ASTVisitor<Register> {

    /*
     * Simple register allocator.
     */

    // contains all the free temporary registers
    private Stack<Register> freeRegs = new Stack<Register>();

    public CodeGenerator() {
        freeRegs.addAll(Register.tmpRegs);
    }

    private class RegisterAllocationError extends Error {}

    private Register getRegister() {
        try {
            Register reg = freeRegs.pop();
            usedRegister.add(reg);
            return reg;
        } catch (EmptyStackException ese) {
            throw new RegisterAllocationError(); // no more free registers, bad luck!
        }
    }

    private void freeRegister(Register reg) {
        usedRegister.remove(reg);
        freeRegs.push(reg);
    }
    public int ifCounter = 0;
    public int whileCounter = 0;
    private int checkReturn = 0;
    private int numPLocal = 0;
    public HashMap<String,List<VarDecl>> structList = new HashMap<String,List<VarDecl>>();
    public int checkMainBlock = 0;
    public int funOffset = 0;
    public List<Register> usedRegister = new ArrayList<Register>();

    private PrintWriter writer; // use this writer to output the assembly instructions


    public void emitProgram(Program program, File outputFile) throws FileNotFoundException {
        GlobalDecl glodecl = new GlobalDecl();
            try {
               writer =  glodecl.emitProgram(program, outputFile);
            } catch (FileNotFoundException e) {
                System.out.println("File "+outputFile.toString()+" does not exist.");
                System.exit(2);
            }
        structList = glodecl.structList;
        funOffset = glodecl.funcOffset;
        //writer = new PrintWriter(outputFile);
        //ifCounter = mainDecl.getIfCounter();
        //whileCounter = mainDecl.getWhileCounter();
        visitProgram(program);
        writer.close();
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
    				if(b.vd.get(i).pValue == 1)
    				{
                        Register exp = getRegister();
    					if(b.vd.get(i).type == BaseType.INT || b.vd.get(i).type instanceof PointerType)
    					{
                            writer.println("lw "+ exp.toString()+","+((b.vd.get(i).aValue+numP)*4)+"($sp)");
    						writer.println("sw "+exp.toString()+","+b.vd.get(i).offset+"($sp)");
    					}
    					else
    					{
    						if(b.vd.get(i).type == BaseType.CHAR)
    						{
    							writer.println("lb "+ exp.toString()+","+((b.vd.get(i).aValue+numP)*4)+"($sp)");
                                writer.println("sb "+exp.toString()+","+b.vd.get(i).offset+"($sp)");
    						}
    						else
    						{
    							if(b.vd.get(i).type instanceof ArrayType)
                                {
                                        int offset = currentOffset * 4;
                                        for(int j =0;j<((ArrayType)b.vd.get(i).type).i;j++)
                                        {
                                            if(((ArrayType)b.vd.get(i).type).t == BaseType.CHAR)
                                            {
                                                writer.println("lb "+ exp.toString()+","+((b.vd.get(i).aValue+numP+j)*4)+"($sp)");
                                                writer.println("sb "+exp.toString()+","+offset+"($sp)");
                                                //writer.println("sb "+((b.vd.get(i).aValue+i+numP)*4)+"($sp),"+offset+"($sp)");
                                            }
                                            else
                                            {
                                                writer.println("lw "+ exp.toString()+","+((b.vd.get(i).aValue+numP+j)*4)+"($sp)");
                                                writer.println("sw "+exp.toString()+","+offset+"($sp)");
                                                //writer.println("sw "+((b.vd.get(i).aValue+i+numP)*4)+"($sp),"+offset+"($sp)");
                                            }
                                        }
                                        currentOffset = currentOffset + ((ArrayType)b.vd.get(i).type).i;
                                }
                                else
                                {
                                    if(b.vd.get(i).type instanceof StructType)
                                    {
                                        int offset = currentOffset * 4;
                                        int n = countStructSize(((StructType)b.vd.get(i).type));
                                        for(int j =0;j<n;j++)
                                        {
                                            writer.println("lw "+ exp.toString()+","+((b.vd.get(i).aValue+numP+j)*4)+"($sp)");
                                            writer.println("sw "+exp.toString()+","+offset+"($sp)");
                                                //writer.println("sw "+((b.vd.get(i).aValue+i+numP)*4)+"($sp),"+offset+"($sp)");
                                        }
                                        //writer.println("lw "+ exp.toString()+","+((b.vd.get(i).aValue+i+numP)*4)+"($sp)");
                                        //writer.println("sw "+exp.toString()+","+offset+"($sp)");
                                        //writer.println("sw "+((b.vd.get(i).aValue+i+numP)*4)+"($sp),"+offset+"($sp)");
                                        currentOffset = currentOffset + n;
                                    }
                                }
    						}
    					}
                        freeRegister(exp);
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
                    if(b.st.get(i) instanceof If)
                    {
                        ((If)b.st.get(i)).numP = numP;
                    }
                    if(b.st.get(i) instanceof While)
                    {
                        ((While)b.st.get(i)).numP = numP;
                    }
    				b.st.get(i).accept(this);
    			}
    		}
    		if(checkReturn == 0 && b.vd == null && b.mainReturn == 0)
    		{
    			writer.println("jr $ra");
    		}
    		else
    		{
                if(checkReturn == 0 && b.vd == null && b.mainReturn == 1)
                {

                }
                else
                {
                    if(checkReturn == 0)
                    {
                        writer.println("move $sp,$fp");
                        writer.println("lw $fp,0($sp)");
                        writer.println("lw $ra,4($sp)");
                        writer.println("addi $sp,$sp,"+4*numP);
                        if(b.mainReturn == 0)
                        {
                            writer.println("jr $ra");
                        }
                    }
                    else
                    {
                        checkReturn = 0;
                    }
                }
    		}
    	}
    	else
    	{
            int currentOffset = 2 + numPLocal;
    		if(b.vd != null)
    		{
    			numP = b.blockStack+numP;
    			writer.println("addi $sp,$sp,"+-4*numP);
                currentOffset = currentOffset + 2;
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
    					((Return)b.st.get(i)).numP = currentOffset;
    				}
                    if(b.st.get(i) instanceof If)
                    {
                        ((If)b.st.get(i)).numP = numP;
                    }
                    if(b.st.get(i) instanceof While)
                    {
                        ((While)b.st.get(i)).numP = numP;
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
                    if(checkMainBlock == 1)
                    {
                        writer.println("li $v0 10");
                        writer.println("syscall");
                    }
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
        int m = 2;
        if(p.params != null)
        {
        	for(int i=0;i<p.params.size();i++)
        	{
        		p.params.get(i).pValue=1;
        		p.params.get(i).aValue=m;
                if(p.params.get(i).type == BaseType.INT || p.params.get(i).type == BaseType.CHAR || p.params.get(i).type instanceof PointerType)
                {
                    m = m+1;
                }
                if(p.params.get(i).type instanceof ArrayType)
                {
                    m = m + ((ArrayType)p.params.get(i).type).i;
                }
                if(p.params.get(i).type instanceof StructType)
                {
                    m = m + countStructSize(((StructType)p.params.get(i).type)) ;
                }
        	}
        }
        if(p.name.equals("main")) 
        {
            p.block.mainReturn =1;
            checkMainBlock = 1;
        }
        p.block.funcBlock = 1;
        p.block.accept(this);
        checkMainBlock = 0;
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
                else
                {
                }
            }
			for(i=0;i<p.funDecls.size();i++)
			{
				if(p.funDecls.get(i).name.equals("main"))
				{

				}
				else
				{
					writer.println(".globl "+p.funDecls.get(i).name);
					p.funDecls.get(i).accept(this);
				}
			}
		}
		writer.flush();
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
    		if(v.vd.type == BaseType.INT || v.vd.type instanceof PointerType)
    		{
                writer.println("la "+addrReg.toString()+","+v.name);
    			writer.println("lw "+result.toString()+",("+addrReg.toString()+")");
    		}
    		else
    		{
    			if(v.vd.type == BaseType.CHAR)
    			{
                    writer.println("la "+addrReg.toString()+","+v.name);
    				writer.println("lb "+result.toString()+",("+addrReg.toString()+")");
    			}
    			else
    			{
    				if(v.vd.type instanceof ArrayType)
    				{
                        writer.println("la "+addrReg.toString()+","+v.name);
    					freeRegister(result);
    					return addrReg;
    				}
    				else
    				{
    					if(v.vd.type instanceof StructType)
    					{
                            writer.println("la "+addrReg.toString()+","+v.name+"_"+((StructType)v.vd.type).structName+"_"+v.fieldName);
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
                writer.println("lw "+exp.toString()+",("+exp.toString()+")");
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
                writer.println("lb "+exp.toString()+",("+exp.toString()+")");
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


        if(!usedRegister.isEmpty())
        {
            fce.fd.block.usedRegister = usedRegister.size();
            writer.println("addi $sp,$sp,"+(usedRegister.size())*-4);
            for(int i=0;i<usedRegister.size();i++)
            {
                writer.println("sw "+usedRegister.get(i).toString()+","+i*4+"($sp)");
            }
        }


		if(fce.e != null)
		{
            int m = 0;
            writer.println("addi $sp,$sp,"+(fce.e.size()+2)*-4);
			for(int i=0;i<fce.e.size();i++)
			{
				Register exp = fce.e.get(i).accept(this);
                if(fce.e.get(i) instanceof VarExpr)
                {
                    if(((VarExpr)fce.e.get(i)).vd.type == BaseType.INT || ((VarExpr)fce.e.get(i)).vd.type instanceof PointerType)
                    {
                        writer.println("sw "+exp.toString()+","+(i+2)*4+"($sp)");
                        m = m +1;
                    }
                    if(((VarExpr)fce.e.get(i)).vd.type == BaseType.CHAR)
                    {
                        writer.println("sb "+exp.toString()+","+(i+2)*4+"($sp)");
                        m = m +1;
                    }
                    if(((VarExpr)fce.e.get(i)).vd.type instanceof ArrayType)
                    {
                        for(int j = 0; j<((ArrayType)fce.e.get(i).type).i;j++)
                        {
                            if(((ArrayType)fce.e.get(i).type).t == BaseType.INT)writer.println("sw "+exp.toString()+","+(i+2+j)*4+"($sp)");
                            if(((ArrayType)fce.e.get(i).type).t == BaseType.CHAR)writer.println("sb "+exp.toString()+","+(i+2+j)*4+"($sp)");
                            writer.println("addi "+exp.toString()+","+exp.toString()+", 4");
                        }
                        m = m + ((ArrayType)fce.e.get(i).type).i;
                    }
                    if(((VarExpr)fce.e.get(i)).vd.type instanceof StructType)
                    {
                        int n = countStructSize(((StructType)fce.e.get(i).type));
                        for(int j = 0; j<n;j++)
                        {
                            writer.println("sw "+exp.toString()+","+(i+2+j)*4+"($sp)");
                            writer.println("addi "+exp.toString()+","+exp.toString()+", 4");
                        }
                        m = m + n;
                    }
                }
                else
                {
                    if(fce.e.get(i) instanceof IntLiteral)
                    {
                         if(((IntLiteral)fce.e.get(i)).type == BaseType.INT)
                        {
                            writer.println("sw "+exp.toString()+","+(i+2)*4+"($sp)");
                            m = m +1;
                        }
                    }
                    else
                    {
                        if(((ChrLiteral)fce.e.get(i)).type == BaseType.CHAR)
                        {
                            writer.println("sb "+exp.toString()+","+(i+2)*4+"($sp)");
                            m = m +1;
                        }
                        else
                        {
                            if(((StrLiteral)fce.e.get(i)).type instanceof PointerType)
                            {
                                writer.println("sw "+exp.toString()+","+(i+2)*4+"($sp)");
                                m = m +1;
                            }
                        }
                    }
                }
				freeRegister(exp);
			}
		}
        /*writer.println("addi $sp,$sp,-12");
        writer.println("sw $fp,0($sp)");
        writer.println("sw $ra,4($sp)");
        writer.println("move $fp,$sp");*/
		writer.println("jal "+fce.name);
        if(fce.e != null)
        {
            writer.println("addi $sp,$sp,"+(fce.e.size()+2)*4);
        }

        if(!usedRegister.isEmpty())
        {
            for(int i=0;i<usedRegister.size();i++)
            {
                writer.println("lw "+usedRegister.get(i).toString()+","+i*4+"($sp)");
            }
            writer.println("addi $sp,$sp,"+(usedRegister.size())*4);
        }

		writer.println("add "+result.toString()+",$v0,$zero");


        /*writer.println("move $sp,$fp");
        writer.println("lw $fp,0($sp)");
        writer.println("lw $ra,4($sp)");
        writer.println("addi $sp,$sp,12");*/
		return result;
	}

	@Override
	public Register visitBinOp(BinOp bo) {
		// TODO Auto-generated method stub
		Register lhsReg = bo.e1.accept(this);
		Register rhsReg = bo.e2.accept(this);
        if(bo.e1 instanceof ArrayAccessExpr || bo.e1 instanceof FieldAccessExpr)
        {
            writer.println("lw "+lhsReg.toString()+",("+lhsReg.toString()+")");
        }
        if(bo.e2 instanceof ArrayAccessExpr || bo.e2 instanceof FieldAccessExpr)
        {
            writer.println("lw "+rhsReg.toString()+",("+rhsReg.toString()+")");
        }
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
		writer.println("la "+result.toString()+","+"0("+exp1.toString()+")");
		freeRegister(exp1);
		freeRegister(exp2);
		return result;
	}

	@Override
	public Register visitFieldAccessExpr(FieldAccessExpr fae) {
		// TODO Auto-generated method stub
		Register result = getRegister();
        ((VarExpr)fae.e).fieldName = fae.name;
        Register exp = fae.e.accept(this);
        if(((VarExpr)fae.e).vd.global == 1)
        {
            writer.println("la "+result.toString()+","+"("+exp.toString()+")");
            freeRegister(exp);
            return result;
        }


        int j = 0;
        List<VarDecl> vdst = structList.get(((StructType)fae.e.type).structName);
        for(int i=0; i<vdst.size();i++)
        {
            if(vdst.get(i).varName.equals(fae.name))
            {
                j = i;
                break;
            }
        }

        writer.println("la "+result.toString()+","+j*4+"("+exp.toString()+")");
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
			 int numP = countStructSize(((StructType)soe.t)) * 4;
             writer.println("li "+result.toString()+", "+numP);
		}
		return result;
	}

    public int countStructSize(StructType soe)
    {
        List<VarDecl> stvd = structList.get(soe.structName);
        int numP = 0;
        if(stvd != null)
        {
            for(int i=0;i<stvd.size();i++)
            {
                if(stvd.get(i).type == BaseType.INT || stvd.get(i).type == BaseType.CHAR || stvd.get(i).type instanceof PointerType)
                {
                    numP = numP +1;
                }
                else
                {
                    if(stvd.get(i).type instanceof ArrayType)
                   {
                        numP = numP + ((ArrayType)stvd.get(i).type).i;
                    }
                    else
                    {
                        if(stvd.get(i).type instanceof StructType)
                        {
                            numP = numP + countStructSize(((StructType)stvd.get(i).type));
                        }
                    }
                }
            }
        }
        return numP;
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
        if(w.s instanceof Return)
        {
            ((Return)w.s).numP = w.numP;
        }
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
        if(i.s1 instanceof Return)
        {
            ((Return)i.s1).numP = i.numP;
        }
		i.s1.accept(this);
		writer.println("j "+ifStr);
		writer.println(elseStr+":");
		if(i.s2!=null)
		{
            if(i.s2 instanceof Return)
            {
                ((Return)i.s2).numP = i.numP;
            }
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
                writer.println("sw "+expr.toString()+",0("+expl.toString()+")");
            }
            else
            {
                if(a.e1 instanceof FieldAccessExpr)
                {
                    if(((VarExpr)(((FieldAccessExpr)a.e1).e)).vd.global == 0)
                    {
                        /*int j = 0;
                        List<VarDecl> vdst = structList.get(((StructType)((FieldAccessExpr)a.e1).e.type).structName);
                        for(int i=0; i<vdst.size();i++)
                        {
                            if(vdst.get(i).varName.equals(((FieldAccessExpr)a.e1).name))
                            {
                                j = i;
                            }
                        }*/
                        //writer.println("sw "+expr.toString()+","+((StructType)((FieldAccessExpr)a.e1).e.type).structName+"."+((FieldAccessExpr)a.e1).name+"("+expl.toString()+")");
                        writer.println("sw "+expr.toString()+","+"("+expl.toString()+")");
                    }
                    else
                    {
                        writer.println("sw "+expr.toString()+",("+expl.toString()+")");
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
			writer.println("lw $fp,0($sp)");
    		writer.println("lw $ra,4($sp)");
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
