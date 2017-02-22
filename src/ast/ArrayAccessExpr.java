package ast;

public class ArrayAccessExpr extends Expr{
	
	public final Expr e1;
	public final Expr e2;
	
	public ArrayAccessExpr(Expr e1, Expr e2)
	{
		this.e1 = e1;
		this.e2 = e2;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		// TODO Auto-generated method stub
		return v.visitArrayAccessExpr(this);
	}
	
}