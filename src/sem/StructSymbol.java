package sem;

import ast.StructType;

public class StructSymbol extends Symbol{
	public StructType st;

	public StructSymbol(StructType st) {
		super(st.structName);
		this.st = st;
		// TODO Auto-generated constructor stub
	}

}
