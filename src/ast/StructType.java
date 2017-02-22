package ast;

import java.util.List;

/**
 * @author cdubach
 */
public class StructType implements Type {


    // to be completed
	public final List<VarDecl> varDecls;
	public final String structName;
	public int offset;
	
	public StructType(String strcutName, List<VarDecl> varDecls)
	{
		this.structName = strcutName;
		this.varDecls = varDecls;
	}

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStructType(this);
    }

}
