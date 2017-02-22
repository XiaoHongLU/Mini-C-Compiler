package ast;

public class ValueAtExpr extends Expr{
	
	public final Expr e;
	
	public ValueAtExpr(Expr e)
	{
		this.e = e;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		// TODO Auto-generated method stub
		return v.visitValueAtExpr(this);
	}
	
}