package ast;

public class ArrayType implements Type{
	public final Type t;
	public final int i;

	public ArrayType(Type t, int i)
	{
		this.t = t;
		this.i = i;
	}

	public <T> T accept(ASTVisitor<T> v){
		return v.visitArrayType(this);
	}
}