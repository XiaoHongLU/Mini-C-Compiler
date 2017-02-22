package ast;

public class If extends Stmt{
	
	public final Expr e;
	public final Stmt s1;
	public final Stmt s2;
	public int numP;
	
	public If(Expr e, Stmt s1, Stmt s2)
	{
		this.e = e;
		this.s1 = s1;
		this.s2 = s2;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		// TODO Auto-generated method stub
		return v.visitIf(this);
	}
	
}