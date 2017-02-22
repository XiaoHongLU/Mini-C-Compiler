package ast;

public class ExprStmt extends Stmt{
	
	public final Expr e;
	
	public ExprStmt(Expr e)
	{
		this.e = e;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		// TODO Auto-generated method stub
		return v.visitExprStmt(this);
	}
	
}