package sem;


import java.util.ArrayList;
import java.util.List;

import ast.*;

public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {

	Scope scope;
	NameAnalysisVisitor(Scope scope){this.scope = scope;}
	
	@Override
	public Void visitBaseType(BaseType bt) {
		// To be completed...
		return null;
	}

	@Override
	public Void visitStructType(StructType st) {
		Symbol s = scope.lookupCurrent(st.structName);
		if(s != null)
		{
			error("Exiting Struct Name " + st.structName);
		}
		if(st.varDecls != null)
		{
			int i = 0;
			for(;i<st.varDecls.size();i++)
			{
				st.varDecls.get(i).accept(this);
			}
		}
		scope.put(new StructSymbol(st));
		// To be completed...
		return null;
	}

	@Override
	public Void visitBlock(Block b) {
		Scope oldScope = scope;
		scope = new Scope(scope);
		int i = 0;
		if(b.vd != null)
		{
			for(;i<b.vd.size();i++)
			{
				b.vd.get(i).accept(this);
			}
		}
		if(b.st != null)
		{
			for(i=0;i<b.st.size();i++)
			{
				b.st.get(i).accept(this);
			}
		}
		scope = oldScope;
		// To be completed...
		return null;
	}

	@Override
	public Void visitFunDecl(FunDecl p) {
		Symbol s = scope.lookup(p.name);
		if(s != null)
		{
			error("Existing Function " + p.name);
		}
		else
		{
			scope.put(new ProcSymbol(p));
		}
		if(p.params != null)
		{
			if(p.block.vd == null)
			{
				p.block.vd = p.params;
			}
			else
			{
				List<VarDecl> vd = new ArrayList<VarDecl>();
				vd.addAll(p.params);
				vd.addAll(p.block.vd);
				p.block.vd = vd;
				//p.block.vd.addAll(p.params);
			}
		}
		visitBlock(p.block);
		// To be completed...
		return null;
	}


	@Override
	public Void visitProgram(Program p) {


		List<VarDecl> vd_t1 = new ArrayList<VarDecl>();
		vd_t1.add(new VarDecl(BaseType.INT,"i"));
		visitFunDecl(new FunDecl(BaseType.VOID,"print_i",vd_t1,new Block(null,null)));

		List<VarDecl> vd_t2 = new ArrayList<VarDecl>();
		vd_t2.add(new VarDecl(BaseType.CHAR,"c"));
		visitFunDecl(new FunDecl(BaseType.VOID,"print_c",vd_t2,new Block(null,null)));

		visitFunDecl(new FunDecl(BaseType.CHAR,"read_c",null,new Block(null,null)));

		visitFunDecl(new FunDecl(BaseType.INT,"read_i",null,new Block(null,null)));

		List<VarDecl> vd_t3 = new ArrayList<VarDecl>();
		vd_t3.add(new VarDecl(BaseType.INT,"size"));
		visitFunDecl(new FunDecl(new PointerType(BaseType.VOID),"mcmalloc",vd_t3,new Block(null,null)));
		
		List<VarDecl> vd_t = new ArrayList<VarDecl>();
		PointerType pt = new PointerType(BaseType.CHAR);
		VarDecl vd_pt = new VarDecl(pt,"s");
		vd_t.add(vd_pt);
		visitFunDecl(new FunDecl(BaseType.VOID,"print_s",vd_t,new Block(null,null)));

		if(p.structTypes != null)
		{
			for(int i=0;i<p.structTypes.size();i++)
			{
				p.structTypes.get(i).accept(this);
			}
		}
		if(p.varDecls != null)
		{
			for(int i=0;i<p.varDecls.size();i++)
			{
				p.varDecls.get(i).accept(this);
			}
		}
		if(p.funDecls != null)
		{
			for(int i=0;i<p.funDecls.size();i++)
			{
				p.funDecls.get(i).accept(this);
			}
		}
		// To be completed...
		return null;
	}

	@Override
	public Void visitVarDecl(VarDecl vd) {
		Symbol s  = scope.lookupCurrent(vd.varName);
		if(s != null)
		{
			error("Repeated Variable " + vd.varName);
		}
		else
		{
			scope.put(new VarSymbol(vd));
		}
		// To be completed...
		return null;
	}

	@Override
	public Void visitVarExpr(VarExpr v) {
		Symbol vs = scope.lookup(v.name);
		if(vs == null)
		{
			error("No Such Variable " + v.name);
		}
		else
		{
			if(!(vs instanceof VarSymbol))
			{
				error("Not a Variable" + v.name);
			}
			else
			{
				v.vd = ((VarSymbol) vs).vd;
			}
		}
		// To be completed...
		return null;
	}

	@Override
	public Void visitPointerType(PointerType pt) {
		// TODO Auto-generated method stub
		pt.t.accept(this);
		return null;
	}


	@Override
	public Void visitIntLiteral(IntLiteral il) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitStrLiteral(StrLiteral sl) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitChrLiteral(ChrLiteral cl) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitFunCallExpr(FunCallExpr fce) {
		Symbol fs = scope.lookup(fce.name);
		if(fs == null)
		{
			error("No Such Function " + fce.name);
		}		
		else
		{
			if(!(fs instanceof ProcSymbol))
			{
				error("Not a Function "+ fce.name);
			}
			else
			{
				fce.fd = ((ProcSymbol) fs).p;
			}
		}
		if(fce.e != null)
		{
			int i = 0;
			for(;i<fce.e.size();i++)
			{
				fce.e.get(i).accept(this);
			}
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitBinOp(BinOp bo) {
		// TODO Auto-generated method stub
		bo.e1.accept(this);
		bo.e2.accept(this);
		return null;
	}

	@Override
	public Void visitOp(Op o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitArrayAccessExpr(ArrayAccessExpr aae) {
		// TODO Auto-generated method stub
		aae.e1.accept(this);
		aae.e2.accept(this);
		return null;
	}

	@Override
	public Void visitFieldAccessExpr(FieldAccessExpr fae) {
		// TODO Auto-generated method stub
		fae.e.accept(this);
		Symbol fa = scope.lookup(fae.name);
		if(fa != null)
		{
			if(fa instanceof VarSymbol)
			{
				VarDecl vd = ((VarSymbol)fa).vd;
				List<VarDecl> vd1 = new ArrayList<VarDecl>();
				vd1.add(vd);
				fae.st = new StructType(null,vd1);
			}
		}
		else
		{
			error("No this Filed");
		}
		//visitVarExpr(new VarExpr(fae.name));
		return null;
	}

	@Override
	public Void visitValueAtExpr(ValueAtExpr vae) {
		// TODO Auto-generated method stub
		vae.e.accept(this);
		return null;
	}

	@Override
	public Void visitSizeOfExpr(SizeOfExpr soe) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitTypecastExpr(TypecastExpr te) {
		// TODO Auto-generated method stub
		te.e.accept(this);
		return null;
	}


	@Override
	public Void visitExprStmt(ExprStmt es) {
		// TODO Auto-generated method stub
		es.e.accept(this);
		return null;
	}

	@Override
	public Void visitWhile(While w) {
		// TODO Auto-generated method stub
		w.e.accept(this);
		w.s.accept(this);
		return null;
	}

	@Override
	public Void visitIf(If i) {
		// TODO Auto-generated method stub
		i.e.accept(this);
		i.s1.accept(this);
		if(i.s2 != null)
		{
			i.s2.accept(this);
		}
		return null;
	}

	@Override
	public Void visitAssign(Assign a) {
		// TODO Auto-generated method stub
		a.e1.accept(this);
		a.e2.accept(this);
		return null;
	}

	@Override
	public Void visitReturn(Return r) {
		// TODO Auto-generated method stub
		if(r.e != null)
		{
			r.e.accept(this);
		}
		return null;
	}

	@Override
	public Void visitArrayType(ArrayType at) {
		// TODO Auto-generated method stub
		return null;
	}

	// To be completed...
}
