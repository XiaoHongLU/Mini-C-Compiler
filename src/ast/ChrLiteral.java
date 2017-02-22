package ast;

public class ChrLiteral extends Expr{
	
	public final char cl;
	
	public ChrLiteral(char cl)
	{
		this.cl = cl;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		// TODO Auto-generated method stub
		return v.visitChrLiteral(this);
	}
	
}