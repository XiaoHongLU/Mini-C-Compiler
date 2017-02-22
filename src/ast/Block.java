package ast;

import java.util.List;

public class Block extends Stmt {
	
	public List<VarDecl> vd;
	public final List<Stmt> st;
	public int funcBlock = 0;
	public int blockStack;
	public int mainReturn = 0;
	public int usedRegister;
	
	public Block(List<VarDecl> vd, List<Stmt> st)
	{
		this.vd = vd;
		this.st = st;
	}

    // to complete ...

    public <T> T accept(ASTVisitor<T> v) {
	    return v.visitBlock(this);
    }
}
