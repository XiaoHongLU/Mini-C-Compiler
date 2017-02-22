package ast;

public class IntLiteral extends Expr{
	public final int i;
	
	public IntLiteral(int i)
	{
		this.i = i;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		// TODO Auto-generated method stub
		return v.visitIntLiteral(this);
	}
	
}