package compiler;

import java.sql.Time;
import java.util.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import compiler.AST.*;
import compiler.FOOLParser.*;
import compiler.lib.*;
import org.antlr.v4.runtime.tree.TerminalNode;

import static compiler.lib.FOOLlib.*;

public class ASTGenerationSTVisitor extends FOOLBaseVisitor<Node> {

	String indent;
    public boolean print;
	
    ASTGenerationSTVisitor() {}    
    ASTGenerationSTVisitor(boolean debug) { print=debug; }
        
    private void printVarAndProdName(ParserRuleContext ctx) {
        String prefix="";        
    	Class<?> ctxClass=ctx.getClass(), parentClass=ctxClass.getSuperclass();
        if (!parentClass.equals(ParserRuleContext.class)) // parentClass is the var context (and not ctxClass itself)
        	prefix=lowerizeFirstChar(extractCtxName(parentClass.getName()))+": production #";
    	System.out.println(indent+prefix+lowerizeFirstChar(extractCtxName(ctxClass.getName())));                               	
    }
        
    @Override
	public Node visit(ParseTree t) {
    	if (t==null) return null;
        String temp=indent;
        indent=(indent==null)?"":indent+"  ";
        Node result = super.visit(t);
        indent=temp;
        return result; 
	}

	@Override
	public Node visitProg(ProgContext c) {
		if (print) printVarAndProdName(c);
		return visit(c.progbody());
	}


	@Override
	public Node visitLetInProg(LetInProgContext c) {
		if (print) printVarAndProdName(c);
		List<DecNode> declist = new ArrayList<>();

		// Visita le classi (se ce ne sono)
		for (CldecContext dec : c.cldec()) {
			declist.add((DecNode) visit(dec));
		}

		// Visita le dichiarazioni standard (var/fun)
		for (DecContext dec : c.dec()){
			declist.add((DecNode) visit(dec));
		}
		return new ProgLetInNode(declist, visit(c.exp()));
	}

	@Override
	public Node visitCldec(CldecContext c) {
		if (print) printVarAndProdName(c);

		// Recupera il nome della classe
		String classId = c.ID(0).getText();

		/*
		// Recupera la superclasse (se c'è "extends")
		String superId = null;
		int fieldStartIndex = 1; // Di base i campi iniziano dall'ID all'indice 1

		if (c.EXTENDS() != null) {
			superId = c.ID(1).getText();
			fieldStartIndex = 2; // Se c'è extends, i campi iniziano dall'ID all'indice 2
		}*/

		int fieldStartIndex = 1;

		// Recupera i Campi (Fields) - Trattati come ParNode o FieldNode
		List<FieldNode> fields = new ArrayList<>();

		// I campi sono pari al numero totale di ID meno quelli usati per nome classe e super
		int numFields = c.ID().size() - fieldStartIndex;

		for (int i = 0; i < numFields; i++) {
			String fieldName = c.ID(fieldStartIndex + i).getText();
			TypeNode fieldType = (TypeNode) visit(c.type(i));

			FieldNode f = new FieldNode(fieldName, fieldType);
			f.setLine(c.ID(fieldStartIndex + i).getSymbol().getLine());
			fields.add(f);
		}

		// Recupera i Metodi
		List<MethodNode> methods = new ArrayList<>();
		for (MethdecContext mc : c.methdec()) {
			methods.add((MethodNode) visit(mc));
		}

		Node n = new ClassNode(classId, fields, methods);
		n.setLine(c.CLASS().getSymbol().getLine());
		return n;
	}

	@Override
	public Node visitMethdec(MethdecContext c) {
		if (print) printVarAndProdName(c);

		List<ParNode> parList = new ArrayList<>();
		for (int i = 1; i < c.ID().size(); i++) {
			ParNode p = new ParNode(c.ID(i).getText(), (TypeNode) visit(c.type(i)));
			p.setLine(c.ID(i).getSymbol().getLine());
			parList.add(p);
		}

		List<DecNode> decList = new ArrayList<>();
		for (DecContext dec : c.dec()) decList.add((DecNode) visit(dec));

		Node n = new MethodNode(c.ID(0).getText(), (TypeNode) visit(c.type(0)), parList, decList, visit(c.exp()));
		n.setLine(c.FUN().getSymbol().getLine());
		return n;
	}

	@Override
	public Node visitNew(NewContext c) {
		if (print) printVarAndProdName(c);

		List<Node> args = new ArrayList<>();
		for (ExpContext e : c.exp()) {
			args.add(visit(e));
		}

		Node n = new NewNode(c.ID().getText(), args);
		n.setLine(c.NEW().getSymbol().getLine());
		return n;
	}

	@Override
	public Node visitDotCall(DotCallContext c) {
		if (print) printVarAndProdName(c);

		List<Node> args = new ArrayList<>();
		for (ExpContext e : c.exp()) {
			args.add(visit(e));
		}

		// ID(0) è l'oggetto, ID(1) è il metodo
		Node n = new ClassCallNode(c.ID(0).getText(), c.ID(1).getText(), args);
		n.setLine(c.ID(0).getSymbol().getLine());
		return n;
	}

	@Override
	public Node visitNull(NullContext c) {
		if (print) printVarAndProdName(c);
		return new EmptyNode(); // Rappresenta 'null'
	}

	@Override
	public Node visitIdType(IdTypeContext c) {
		if (print) printVarAndProdName(c);
		// Quando trovo un ID come tipo (es: "Cane"), creo un RefTypeNode
		return new RefTypeNode(c.ID().getText());
	}


	@Override
	public Node visitNoDecProg(NoDecProgContext c) {
		if (print) printVarAndProdName(c);
		return new ProgNode(visit(c.exp()));
	}

	@Override
	public Node visitTimesDiv(TimesDivContext c) {
		if (print) printVarAndProdName(c);
		TerminalNode TimesNode = c.TIMES();

		if (TimesNode != null) {
			Node n = new TimesNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(TimesNode.getSymbol().getLine());
			return n;
		}

		Node n = new DivNode(visit(c.exp(0)), visit(c.exp(1)));
		n.setLine(c.DIV().getSymbol().getLine());		// setLine added
        return n;		
	}

	@Override
	public Node visitPlusMinus(PlusMinusContext c) {
		if (print) printVarAndProdName(c);

		TerminalNode PlusNode = c.PLUS();
		if (PlusNode != null) {
			Node n = new PlusNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(PlusNode.getSymbol().getLine());
			return n;
		}

		Node n = new MinusNode(visit(c.exp(0)), visit(c.exp(1)));
		n.setLine(c.MINUS().getSymbol().getLine());
        return n;		
	}

	@Override
	public Node visitComp(CompContext c) {
		if (print) printVarAndProdName(c);

		TerminalNode eqNode = c.EQ();
		if (eqNode != null) {
			Node n = new EqualNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(eqNode.getSymbol().getLine());
			return n;
		}

		TerminalNode leNode = c.LE();
		if (leNode != null) {
			Node n = new LessEqualNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(leNode.getSymbol().getLine());
			return n;
		}

		Node n = new GreaterEqualNode(visit(c.exp(0)), visit(c.exp(1)));
		n.setLine(c.GE().getSymbol().getLine());
        return n;		
	}

	@Override
	public Node visitNot(NotContext c) {
		if (print) printVarAndProdName(c);

		Node n = new NotNode(visit(c.exp()));
		n.setLine(c.NOT().getSymbol().getLine());
		return n;
	}

	@Override
	public Node visitAndOr(AndOrContext c) {
		if (print) printVarAndProdName(c);

		TerminalNode orNode = c.OR();
		if (orNode != null) {
			Node n = new OrNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(orNode.getSymbol().getLine());
			return n;
		}

		Node n = new AndNode(visit(c.exp(0)), visit(c.exp(1)));
		n.setLine(c.AND().getSymbol().getLine());
		return n;
	}

	@Override
	public Node visitVardec(VardecContext c) {
		if (print) printVarAndProdName(c);
		Node n = null;
		if (c.ID()!=null) { //non-incomplete ST
			n = new VarNode(c.ID().getText(), (TypeNode) visit(c.type()), visit(c.exp()));
			n.setLine(c.VAR().getSymbol().getLine());
		}
        return n;
	}

	@Override
	public Node visitFundec(FundecContext c) {
		if (print) printVarAndProdName(c);
		List<ParNode> parList = new ArrayList<>();
		for (int i = 1; i < c.ID().size(); i++) { 
			ParNode p = new ParNode(c.ID(i).getText(),(TypeNode) visit(c.type(i)));
			p.setLine(c.ID(i).getSymbol().getLine());
			parList.add(p);
		}
		List<DecNode> decList = new ArrayList<>();
		for (DecContext dec : c.dec()) decList.add((DecNode) visit(dec));
		Node n = null;
		if (c.ID().size()>0) { //non-incomplete ST
			n = new FunNode(c.ID(0).getText(),(TypeNode)visit(c.type(0)),parList,decList,visit(c.exp()));
			n.setLine(c.FUN().getSymbol().getLine());
		}
        return n;
	}

	@Override
	public Node visitIntType(IntTypeContext c) {
		if (print) printVarAndProdName(c);
		return new IntTypeNode();
	}

	@Override
	public Node visitBoolType(BoolTypeContext c) {
		if (print) printVarAndProdName(c);
		return new BoolTypeNode();
	}

	@Override
	public Node visitInteger(IntegerContext c) {
		if (print) printVarAndProdName(c);
		int v = Integer.parseInt(c.NUM().getText());
		return new IntNode(c.MINUS()==null?v:-v);
	}

	@Override
	public Node visitTrue(TrueContext c) {
		if (print) printVarAndProdName(c);
		return new BoolNode(true);
	}

	@Override
	public Node visitFalse(FalseContext c) {
		if (print) printVarAndProdName(c);
		return new BoolNode(false);
	}

	@Override
	public Node visitIf(IfContext c) {
		if (print) printVarAndProdName(c);
		Node ifNode = visit(c.exp(0));
		Node thenNode = visit(c.exp(1));
		Node elseNode = visit(c.exp(2));
		Node n = new IfNode(ifNode, thenNode, elseNode);
		n.setLine(c.IF().getSymbol().getLine());			
        return n;		
	}

	@Override
	public Node visitPrint(PrintContext c) {
		if (print) printVarAndProdName(c);
		return new PrintNode(visit(c.exp()));
	}

	@Override
	public Node visitPars(ParsContext c) {
		if (print) printVarAndProdName(c);
		return visit(c.exp());
	}

	@Override
	public Node visitId(IdContext c) {
		if (print) printVarAndProdName(c);
		Node n = new IdNode(c.ID().getText());
		n.setLine(c.ID().getSymbol().getLine());
		return n;
	}

	@Override
	public Node visitCall(CallContext c) {
		if (print) printVarAndProdName(c);		
		List<Node> arglist = new ArrayList<>();
		for (ExpContext arg : c.exp()) arglist.add(visit(arg));
		Node n = new CallNode(c.ID().getText(), arglist);
		n.setLine(c.ID().getSymbol().getLine());
		return n;
	}
}
