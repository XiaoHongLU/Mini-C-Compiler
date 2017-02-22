package ast;

import java.util.List;

public class FunCallExpr extends Expr{
	public final List<Expr> e;
	public final String name;
	public FunDecl fd;
	public int offset;
	
	public FunCallExpr(String name, List<Expr> e)
	{
		this.name = name;
		this.e = e;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		// TODO Auto-generated method stub
		return v.visitFunCallExpr(this);
	}
	
}