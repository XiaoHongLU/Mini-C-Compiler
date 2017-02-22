package sem;

import ast.FunDecl;

public class ProcSymbol extends Symbol {
	
	FunDecl p;

	public ProcSymbol(FunDecl p) {
		super(p.name);
		this.p = p;
	}

}
