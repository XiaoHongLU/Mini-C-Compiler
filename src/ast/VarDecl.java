package ast;

public class VarDecl implements ASTNode {
    public final Type type;
    public final String varName;
    public int offset;
    public int global = 1;
    public int pValue = 0;
    public int aValue;

    public VarDecl(Type type, String varName) {
	    this.type = type;
	    this.varName = varName;
    }

     public <T> T accept(ASTVisitor<T> v) {
	return v.visitVarDecl(this);
    }
}
