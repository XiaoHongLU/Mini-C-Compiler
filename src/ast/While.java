package ast;

public class While extends Stmt{
	
	public final Expr e;
	public final Stmt s;
	public int numP;

	public While(Expr e, Stmt s)
	{
		this.e = e;
		this.s = s;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		// TODO Auto-generated method stub
		return v.visitWhile(this);
	}
	
}