package sem;

import java.util.Map;
import java.util.*;

public class Scope {
	private Scope outer;
	private Map<String, Symbol> symbolTable = new HashMap<String, Symbol>();
	
	public Scope(Scope outer) { 
		this.outer = outer; 
	}
	
	public Scope() { this(null); }
	
	public Symbol lookup(String name) {
		Symbol sm;
		if(lookupCurrent(name) != null)
		{
			sm = lookupCurrent(name);
			return sm;
		}
		else
		{
			if(outer != null)
			{
				if(outer.lookup(name) !=null)
				{
					sm = outer.lookup(name);
					return sm;
				}
			}
		}
		// To be completed...
		return null;
	}
	
	public Symbol lookupCurrent(String name) {
		
		if(symbolTable.containsKey(name))
		{
			Symbol sm = symbolTable.get(name);
			return sm;
		}
		// To be completed...
		return null;
	}
	
	public void put(Symbol sym) {
		symbolTable.put(sym.name, sym);
	}
}
