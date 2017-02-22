package ast;

public class FieldAccessExpr extends Expr{
	
	public final Expr e;
	public final String name;
	public StructType st;
	
	public FieldAccessExpr(Expr e, String name)
	{
		this.e = e;
		this.name = name;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		// TODO Auto-generated method stub
		return v.visitFieldAccessExpr(this);
	}
	
}