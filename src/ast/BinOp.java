package ast;

public class BinOp extends Expr{

	public Expr e1;
	public final Expr e2;
	public final Op o;
	//public Type t;
	
	public BinOp(Expr e1, Op o, Expr e2)
	{
		this.e1 = e1;
		this.o = o;
		this.e2 = e2;
	}
	@Override
	public <T> T accept(ASTVisitor<T> v) {
		// TODO Auto-generated method stub
		return v.visitBinOp(this);
	}
	
}