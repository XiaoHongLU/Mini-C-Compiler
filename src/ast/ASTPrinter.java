package ast;

import java.io.PrintWriter;
import java.util.*;

public class ASTPrinter implements ASTVisitor<Void> {

    private PrintWriter writer;


    public ASTPrinter(PrintWriter writer) {
            this.writer = writer;
    }

    @Override
    public Void visitBlock(Block b) {
        writer.print("Block(");
        String delimiter = "";
        // to complete
        if(b.vd!=null){
        for (VarDecl vd : b.vd) {
        	writer.print(delimiter);
            delimiter = ",";
            vd.accept(this);
        }}
        if(b.st!=null){
        for (Stmt st : b.st) {
        	writer.print(delimiter);
            delimiter = ",";
            st.accept(this);
        }}
        writer.print(")");
        return null;
    }

    @Override
    public Void visitFunDecl(FunDecl fd) {
        writer.print("FunDecl(");
        fd.type.accept(this);
        writer.print(","+fd.name);
        String delimiter = "";
        if(fd.params!=null)
        {
        	writer.print(",");
        for (VarDecl vd : fd.params) {
        	writer.print(delimiter);
            delimiter = ",";
            vd.accept(this);
        }
        	writer.print(",");

    	}
        else
        {
        	writer.print(",");
        }
        fd.block.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitProgram(Program p) {
        writer.print("Program(");
        String delimiter = "";
        if(p.structTypes!=null){
        for (StructType st : p.structTypes) {
            writer.print(delimiter);
            delimiter = ",";
            st.accept(this);
        }}
        if(p.varDecls!=null){
        for (VarDecl vd : p.varDecls) {
            writer.print(delimiter);
            delimiter = ",";
            vd.accept(this);
        }}
        if(p.funDecls!=null){
        for (FunDecl fd : p.funDecls) {
            writer.print(delimiter);
            delimiter = ",";
            fd.accept(this);
        }}
        writer.print(")");
	    writer.flush();
        return null;
    }

    @Override
    public Void visitVarDecl(VarDecl vd){
        writer.print("VarDecl(");
        vd.type.accept(this);
        writer.print(","+vd.varName);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitVarExpr(VarExpr v) {
        writer.print("VarExpr(");
        writer.print(v.name);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitBaseType(BaseType bt) {
        // to complete ...
    	writer.print(bt.name());
        return null;
    }

    @Override
    public Void visitStructType(StructType st) {
    	writer.print("StructType(");
    	writer.print(st.structName);
    	String delimiter = "";
    	if(st.varDecls!=null){
    		writer.print(",");
    	for (VarDecl vd : st.varDecls) {
    		writer.print(delimiter);
            delimiter = ",";
            vd.accept(this);
        }}
        // to complete ...
    	writer.print(")");
        return null;
    }

	@Override
	public Void visitPointerType(PointerType pt) {
		writer.print("PointerType(");
		pt.t.accept(this);
		// TODO Auto-generated method stub
		writer.print(")");
		return null;
	}


	@Override
	public Void visitIntLiteral(IntLiteral il) {
		writer.print("IntLiteral(");
		writer.print(il.i);
		// TODO Auto-generated method stub
		writer.print(")");
		return null;
	}

	@Override
	public Void visitStrLiteral(StrLiteral sl) {
		writer.print("StrLiteral(");
		writer.print(sl.sl);
		// TODO Auto-generated method stub
		writer.print(")");
		return null;
	}

	@Override
	public Void visitChrLiteral(ChrLiteral cl) {
		writer.print("ChrLiteral(");
		writer.print(cl.cl);
		// TODO Auto-generated method stub
		writer.print(")");
		return null;
	}

	@Override
	public Void visitFunCallExpr(FunCallExpr fce) {
		writer.print("FunCallExpr(");
		writer.print(fce.name);
		String delimiter = "";
		if(fce.e!=null){
			writer.print(",");
		for(Expr e : fce.e)
		{
			writer.print(delimiter);
            delimiter = ",";
			e.accept(this);
		}}
		// TODO Auto-generated method stub
		writer.print(")");
		return null;
	}

	@Override
	public Void visitBinOp(BinOp bo) {
		writer.print("BinOp(");
		if(bo.e1!=null){
		bo.e1.accept(this);
		writer.print(",");}
		if(bo.o!=null){
		bo.o.accept(this);
		writer.print(",");}
		bo.e2.accept(this);
		// TODO Auto-generated method stub
		writer.print(")");
		return null;
	}

	@Override
	public Void visitOp(Op o) {
		writer.print(o.name());
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitArrayAccessExpr(ArrayAccessExpr aae) {
		writer.print("ArrayAccessExpr(");
		// TODO Auto-generated method stub
		aae.e1.accept(this);
		writer.print(",");
		aae.e2.accept(this);
		writer.print(")");
		return null;
	}

	@Override
	public Void visitFieldAccessExpr(FieldAccessExpr fae) {
		writer.print("FieldAccessExpr(");
		fae.e.accept(this);
		writer.print(",");
		writer.print(fae.name);
		// TODO Auto-generated method stub
		writer.print(")");
		return null;
	}

	@Override
	public Void visitValueAtExpr(ValueAtExpr vae) {
		writer.print("ValueAtExpr(");
		// TODO Auto-generated method stub
		vae.e.accept(this);
		writer.print(")");
		return null;
	}

	@Override
	public Void visitSizeOfExpr(SizeOfExpr soe) {
		writer.print("SizeOfExpr(");
		// TODO Auto-generated method stub
		soe.t.accept(this);
		writer.print(")");
		return null;
	}

	@Override
	public Void visitTypecastExpr(TypecastExpr te) {
		writer.print("TypecastExpr(");
		// TODO Auto-generated method stub
		te.t.accept(this);
		writer.print(",");
		te.e.accept(this);
		writer.print(")");
		return null;
	}


	@Override
	public Void visitExprStmt(ExprStmt es) {
		// TODO Auto-generated method stub
		writer.print("ExprStmt(");
		es.e.accept(this);
		writer.print(")");
		return null;
	}

	@Override
	public Void visitWhile(While w) {
		writer.print("While(");
		// TODO Auto-generated method stub
		w.e.accept(this);
		writer.print(",");
		w.s.accept(this);
		writer.print(")");
		return null;
	}

	@Override
	public Void visitIf(If i) {
		writer.print("If(");
		// TODO Auto-generated method stub
		i.e.accept(this);
		writer.print(",");
		i.s1.accept(this);
		if(i.s2!=null)
		{
			writer.print(",");
			i.s2.accept(this);
		}
		writer.print(")");
		return null;
	}

	@Override
	public Void visitAssign(Assign a) {
		writer.print("Assign(");
		// TODO Auto-generated method stub
		a.e1.accept(this);
		writer.print(",");
		a.e2.accept(this);
		writer.print(")");
		return null;
	}

	@Override
	public Void visitReturn(Return r) {
		writer.print("Return(");
		// TODO Auto-generated method stub
		if(r.e!=null)
		{
			r.e.accept(this);
		}
		writer.print(")");
		return null;
	}

	@Override
	public Void visitArrayType(ArrayType at) {
		// TODO Auto-generated method stub
		writer.print("ArrayType(");
		at.t.accept(this);
		writer.print(",");
		writer.print(at.i);
		writer.print(")");
		return null;
	}

    // to complete ...
    
}
