package ast;

public class Return extends Stmt{
	
	public final Expr e;
	public int numP;
	public int funcReturn = 0;
	
	public Return(Expr e)
	{
		this.e = e;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		// TODO Auto-generated method stub
		return v.visitReturn(this);
	}
	
}