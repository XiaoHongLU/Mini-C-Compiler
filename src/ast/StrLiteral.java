package ast;

public class StrLiteral extends Expr{
	public final String sl;
	public String name;
	
	public StrLiteral(String sl)
	{
		this.sl = sl;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		// TODO Auto-generated method stub
		return v.visitStrLiteral(this);
	}
	
}