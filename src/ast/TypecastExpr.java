package ast;

public class TypecastExpr extends Expr{
	
	public final Type t;
	public final Expr e;
	
	public TypecastExpr(Type t, Expr e)
	{
		this.t = t;
		this.e = e;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		// TODO Auto-generated method stub
		return v.visitTypecastExpr(this);
	}
	
}