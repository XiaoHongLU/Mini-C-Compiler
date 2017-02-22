package sem;

import ast.*;

public class TypeCheckVisitor extends BaseSemanticVisitor<Type> {

	TypeCheckVisitor(){}

	@Override
	public Type visitBaseType(BaseType bt) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitStructType(StructType st) {
		// To be completed...
		int i = 0;
		if(st.varDecls != null)
		{
			for(;i<st.varDecls.size();i++)
			{
				st.varDecls.get(i).accept(this);
			}
		}
		return st;
	}

	@Override
	public Type visitBlock(Block b) {
		// To be completed...
		Type rt = null;
		Type brt = null;
		if(b.vd != null)
		{
			for(int i=0;i<b.vd.size();i++)
			{
				b.vd.get(i).accept(this);
			}
		}
		if(b.st != null)
		{
			for(int i=0;i<b.st.size();i++)
			{
				if(b.st.get(i) instanceof Return)
				{
					rt = b.st.get(i).accept(this);
				}
				else
				{
					if(b.st.get(i) instanceof If || b.st.get(i) instanceof While)
					{
						brt = b.st.get(i).accept(this);
					}
					else
					{
						b.st.get(i).accept(this);
					}
				}
			}
			if(rt != null && brt != null)
			{
				if(rt instanceof PointerType || rt instanceof ArrayType)
				{
					rt = rt.accept(this);
				}
				if(brt instanceof PointerType || brt instanceof ArrayType)
				{
					brt = brt.accept(this);
				}
				if(rt == brt)
				{
					return rt;
				}
				else
				{
					if(brt == BaseType.VOID)
					{
						return rt;
					}
					else
					{
						error("Wrong Block Type");
					}
				}
			}
			else
			{
				if(rt != null)
				{
					return rt;
				}
				else
				{
					if(brt != null)
					{
						return brt;
					}
					else
					{
						return BaseType.VOID;
					}
				}
			}
		}
		else
		{
			return BaseType.VOID;
		}
		return null;
	}

	@Override
	public Type visitFunDecl(FunDecl p) {
		// To be completed...
		if(p.params != null)
		{
			for(int i=0; i<p.params.size();i++)
			{
				p.params.get(i).accept(this);
			}
		}
		Type bt = p.block.accept(this);
		Type tt;
		if(p.type instanceof PointerType || p.type instanceof ArrayType)
		{
			tt = p.type.accept(this);
		}
		else
		{
			tt = p.type;
		}
		if((bt instanceof PointerType && p.type instanceof PointerType) || bt instanceof ArrayType)
		{
			bt = bt.accept(this);
		}
		if(tt != bt)
		{
			error("Wrong FunDecl Type");
			return null;
		}
		return p.type;
	}


	@Override
	public Type visitProgram(Program p) {
		int i = 0;
		if(p.structTypes != null)
		{
			for(;i<p.structTypes.size();i++)
			{
				p.structTypes.get(i).accept(this);
			}
		}
		if(p.varDecls != null)
		{
			for(i=0;i<p.varDecls.size();i++)
			{
				p.varDecls.get(i).accept(this);
			}
		}
		if(p.funDecls != null)
		{
			for(i=0;i<p.funDecls.size();i++)
			{
				p.funDecls.get(i).accept(this);
			}
		}
		return null;
	}

	@Override
	public Type visitVarDecl(VarDecl vd) {
		// To be completed...
		if(vd.type == BaseType.VOID)
		{
			error("Wrong VarDecl Type");
			return null;
		}
		return vd.type;
	}

	@Override
	public Type visitVarExpr(VarExpr v) {
		// To be completed...
		if(v.vd != null)
		{
			v.type = v.vd.type;
			return v.vd.type;
		}
		error("Wrong VarExpr Type");
		return null;
	}

	@Override
	public Type visitPointerType(PointerType pt) {
		// TODO Auto-generated method stub
		return pt.t;
	}


	@Override
	public Type visitIntLiteral(IntLiteral il) {
		// TODO Auto-generated method stub
		il.type = BaseType.INT;
		return BaseType.INT;
	}

	@Override
	public Type visitStrLiteral(StrLiteral sl) {
		// TODO Auto-generated method stub
		int l = sl.sl.length();
		Type st = new ArrayType(BaseType.CHAR,l+1);
		sl.type = st;
		return st;
	}

	@Override
	public Type visitChrLiteral(ChrLiteral cl) {
		// TODO Auto-generated method stub
		cl.type = BaseType.CHAR;
		return BaseType.CHAR;
	}

	@Override
	public Type visitFunCallExpr(FunCallExpr fce) {
		// TODO Auto-generated method stub
		if(fce.fd != null)
		{
			FunDecl fd = fce.fd;
			if(fce.e != null)
			{
				if(fd.params.size() != fce.e.size())
				{
					error("Not the same number of params");
					return null;
				}
				for(int i=0;i<fce.e.size();i++)
				{
					Type fcet = fce.e.get(i).accept(this);
					Type fdt = fd.params.get(i).accept(this);
					if(fcet instanceof PointerType || fcet instanceof ArrayType)
					{
						fcet = fcet.accept(this);
					}
					if(fdt instanceof PointerType || fdt instanceof ArrayType)
					{
						fdt = fdt.accept(this);
					}
					if(fcet != fdt)
					{
						error("Not the same params Type");
						return null;
					}
					else
					{
						fce.type = fd.type;
						return fd.type;
					}
				}
			}
			else
			{
				if(fd.params != null)
				{
					error("Not the same number of params");
					return null;
				}
				fce.type = fd.type;
				return fd.type;
			}
		}
		error("No such FunCallExpr");
		return null;
	}

	@Override
	public Type visitBinOp(BinOp bo) {
		// TODO Auto-generated method stub
		Type lhsT = bo.e1.accept(this);
		Type rhsT = bo.e2.accept(this);
		if(bo.o == Op.ADD || bo.o == Op.SUB || bo.o == Op.MUL || bo.o == Op.DIV || bo.o == Op.MOD || bo.o == Op.OR || bo.o == Op.AND || bo.o == Op.GT || bo.o == Op.LT || bo.o == Op.GE || bo.o == Op.LE)
		{
			if(lhsT == BaseType.INT && rhsT == BaseType.INT)
			{
				bo.type = BaseType.INT;
				return BaseType.INT;
			}
		}
		if (bo.o == Op.EQ || bo.o == Op.NE) {
			if(lhsT instanceof ArrayType && rhsT instanceof ArrayType)
			{
				if(((ArrayType)lhsT).i != ((ArrayType)rhsT).i)
				{
					error("Wrong BinOp ArrayType");
					return null;
				}
				lhsT = lhsT.accept(this);
				rhsT = rhsT.accept(this);
			}
			if(lhsT instanceof PointerType && rhsT instanceof PointerType)
			{
				lhsT = lhsT.accept(this);
				rhsT = rhsT.accept(this);
			}
			if(lhsT instanceof ArrayType && !(rhsT instanceof ArrayType))
			{
				lhsT = lhsT.accept(this);
			}
			if(!(lhsT instanceof ArrayType) && rhsT instanceof ArrayType)
			{
				rhsT = rhsT.accept(this);
			}
			if(lhsT == rhsT)
			{
				bo.type = BaseType.INT;
				return BaseType.INT;
			}
		}
		error("Wrong BinOp Type");
		return null;
	}

	@Override
	public Type visitOp(Op o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visitArrayAccessExpr(ArrayAccessExpr aae) {
		// TODO Auto-generated method stub
		Type et1 = aae.e1.accept(this);
		Type et2 = aae.e2.accept(this);
		if((aae.e1.type instanceof ArrayType || aae.e1.type instanceof PointerType) && et2 == BaseType.INT)
		{
			aae.type = et1.accept(this);
			return et1.accept(this);
		}
		error("Wrong ArrayAccessExpr Type");
		return null;
	}

	@Override
	public Type visitFieldAccessExpr(FieldAccessExpr fae) {
		// TODO Auto-generated method stub
		Type et = fae.e.accept(this);
		if(fae.e.type instanceof StructType)
		{
			StructType st = fae.st;
			fae.type = st;
			for(int i=0;i<st.varDecls.size();i++)
			{
				if(st.varDecls.get(i).varName.equals(fae.name))
				{
					if(st.varDecls.get(i).type instanceof ArrayType)
					{
						Type lt = st.varDecls.get(i).type.accept(this);
						return lt;
					}
					Type rt = st.varDecls.get(i).accept(this);
					return rt;
				}
			}
			error("Wrong FieldAccessExpr Type");
			//VarExpr ve = new VarExpr(fae.name);
			//Type rt = visitVarExpr(ve);
			//fae.type = rt;
			return null;
		}
		error("Wrong FieldAccessExpr Type");
		return null;
	}

	@Override
	public Type visitValueAtExpr(ValueAtExpr vae) {
		// TODO Auto-generated method stub
		Type et = vae.e.accept(this);
		if(vae.e.type instanceof PointerType)
		{
			vae.type = et.accept(this);
			return et.accept(this);
		}
		error("Wrong ValueAtExpr Type");
		return null;
	}

	@Override
	public Type visitSizeOfExpr(SizeOfExpr soe) {
		// TODO Auto-generated method stub
		soe.type = BaseType.INT;
		return BaseType.INT;
	}

	@Override
	public Type visitTypecastExpr(TypecastExpr te) {
		// TODO Auto-generated method stub
		Type et = te.e.accept(this);
		if(et instanceof ArrayType)
		{
			Type etr = et.accept(this);
			te.type = new PointerType(etr);
			return new PointerType(etr);
		}
		if(et instanceof PointerType)
		{
			Type etr = te.t.accept(this);
			te.type = new PointerType(etr);
			return new PointerType(etr);
		}
		if(et == BaseType.CHAR)
		{
			te.type = BaseType.INT;
			return BaseType.INT;
		}
		error("Wrong TypecastExpr Type");
		return null;
	}


	@Override
	public Type visitExprStmt(ExprStmt es) {
		// TODO Auto-generated method stub
		return es.e.accept(this);
	}

	@Override
	public Type visitWhile(While w) {
		// TODO Auto-generated method stub
		Type et = w.e.accept(this);
		Type wt;
		if(et instanceof PointerType || et instanceof ArrayType)
		{
			et = et.accept(this);
		}
		if(et != BaseType.INT)
		{
			error("Wrong While Type");
			return null;
		}
		if(w.s instanceof Block || w.s instanceof Return)
		{
			wt = w.s.accept(this);
		}
		else
		{
			w.s.accept(this);
			wt = null;
		}
		return wt;
	}

	@Override
	public Type visitIf(If i) {
		// TODO Auto-generated method stub
		Type et = i.e.accept(this);
		Type st1,st2;
		if(et instanceof PointerType || et instanceof ArrayType)
		{
			et = et.accept(this);
		}
		if(et != BaseType.INT)
		{
			error("Wrong If Type");
			return null;
		}
		if(i.s1 instanceof Block || i.s1 instanceof Return)
		{
			st1 = i.s1.accept(this);
		}
		else
		{
			i.s1.accept(this);
			st1 = null;
		}
		if(i.s2 != null)
		{
			if(i.s2 instanceof Block || i.s2 instanceof Return)
			{
				st2 = i.s2.accept(this);
			}
			else
			{
				st2 = null;
			}
			if(st1 != st2)
			{
				if(st1 == null)
				{
					return st2;
				}
				else
				{
					if(st2 == null)
					{
						return st1;
					}
					else
					{
						error("Wrong If S2 Type");
						return null;
					}
				}
			}
			return st2;
		}
		return st1;
	}

	@Override
	public Type visitAssign(Assign a) {
		// TODO Auto-generated method stub
		Type et1 = a.e1.accept(this);
		Type et2 = a.e2.accept(this);
		if((et1 != BaseType.VOID && !(et1 instanceof ArrayType))&& et1 == et2)
		{
			return et1;
		}
		else
		{
			/*if(et1 instanceof PointerType && et2 instanceof PointerType)
			{
				Type et1r = et1.accept(this);
				Type et2r = et2.accept(this);
				if(et1r == et2r)
				{
					return et1r;
				}
			}*/
		}
		error("Wrong Assign e2 Type");
		return null;
	}

	@Override
	public Type visitReturn(Return r) {
		// TODO Auto-generated method stub
		if(r.e == null)
		{
			return BaseType.VOID;
		}
		Type et = r.e.accept(this);
		return et;
	}

	@Override
	public Type visitArrayType(ArrayType at) {
		// TODO Auto-generated method stub
		return at.t;
	}

	// To be completed...


}
