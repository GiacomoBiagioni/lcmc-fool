package compiler;

import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;
import static compiler.TypeRels.*;

//visitNode(n) fa il type checking di un Node n e ritorna:
//- per una espressione, il suo tipo (oggetto BoolTypeNode o IntTypeNode)
//- per una dichiarazione, "null"; controlla la correttezza interna della dichiarazione
//(- per un tipo: "null"; controlla che il tipo non sia incompleto) 
//
//visitSTentry(s) ritorna, per una STentry s, il tipo contenuto al suo interno
public class TypeCheckEASTVisitor extends BaseEASTVisitor<TypeNode,TypeException> {

	TypeCheckEASTVisitor() { super(true); } // enables incomplete tree exceptions 
	TypeCheckEASTVisitor(boolean debug) { super(true,debug); } // enables print for debugging

	//checks that a type object is visitable (not incomplete) 
	private TypeNode ckvisit(TypeNode t) throws TypeException {
		visit(t);
		return t;
	} 
	
	@Override
	public TypeNode visitNode(ProgLetInNode n) throws TypeException {
		if (print) printNode(n);
		for (Node dec : n.declist)
			try {
				visit(dec);
			} catch (IncomplException e) { 
			} catch (TypeException e) {
				System.out.println("Type checking error in a declaration: " + e.text);
			}
		return visit(n.exp);
	}

	@Override
	public TypeNode visitNode(ProgNode n) throws TypeException {
		if (print) printNode(n);
		return visit(n.exp);
	}

	@Override
	public TypeNode visitNode(FunNode n) throws TypeException {
		if (print) printNode(n,n.id);
		for (Node dec : n.declist)
			try {
				visit(dec);
			} catch (IncomplException e) { 
			} catch (TypeException e) {
				System.out.println("Type checking error in a declaration: " + e.text);
			}
		if ( !isSubtype(visit(n.exp),ckvisit(n.retType)) ) 
			throw new TypeException("Wrong return type for function " + n.id,n.getLine());
		return null;
	}

	@Override
	public TypeNode visitNode(VarNode n) throws TypeException {
		if (print) printNode(n,n.id);
		if ( !isSubtype(visit(n.exp),ckvisit(n.getType())) )
			throw new TypeException("Incompatible value for variable " + n.id,n.getLine());
		return null;
	}

	@Override
	public TypeNode visitNode(PrintNode n) throws TypeException {
		if (print) printNode(n);
		return visit(n.exp);
	}

	@Override
	public TypeNode visitNode(IfNode n) throws TypeException {
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.cond), new BoolTypeNode())) )
			throw new TypeException("Non boolean condition in if",n.getLine());

		TypeNode t = visit(n.th);
		TypeNode e = visit(n.el);

		// MODIFICA OO: Usa Lowest Common Ancestor per trovare il tipo comune
		TypeNode lca = lowestCommonAncestor(t, e);
		if (lca != null) return lca;

		throw new TypeException("Incompatible types in then-else branches",n.getLine());
	}

	@Override
	public TypeNode visitNode(EqualNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode l = visit(n.left);
		TypeNode r = visit(n.right);

		// MODIFICA OO: Permetti confronto tra oggetti o con null
		if (l instanceof RefTypeNode || r instanceof RefTypeNode || l instanceof EmptyTypeNode || r instanceof EmptyTypeNode) {
			// Controlla se sono compatibili (es. Cane == Cane oppure Cane == null)
			if (lowestCommonAncestor(l, r) == null)
				throw new TypeException("Incompatible types in equal (objects must be comparable)", n.getLine());
			return new BoolTypeNode();
		}

		// Vecchia logica per int/bool
		if ( !(isSubtype(l, r) || isSubtype(r, l)) )
			throw new TypeException("Incompatible types in equal",n.getLine());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(LessEqualNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode l = visit(n.left);
		TypeNode r = visit(n.right);
		if ( !(isSubtype(l, r) || isSubtype(r, l)) )
			throw new TypeException("Incompatible types in less equal",n.getLine());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(GreaterEqualNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode l = visit(n.left);
		TypeNode r = visit(n.right);
		if ( !(isSubtype(l, r) || isSubtype(r, l)) )
			throw new TypeException("Incompatible types in greater equal",n.getLine());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(NotNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode r = visit(n.exp);
		if ( !isSubtype(r, new BoolTypeNode()))
			throw new TypeException("Incompatible types in NOT", n.getLine());

		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(OrNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode l = visit(n.left);
		TypeNode r = visit(n.right);
		if ( !(isSubtype(l, new BoolTypeNode()) || isSubtype(r, new BoolTypeNode())) )
			throw new TypeException("Incompatible types in OR",n.getLine());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(AndNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode l = visit(n.left);
		TypeNode r = visit(n.right);
		if ( !(isSubtype(l, new BoolTypeNode()) || isSubtype(r, new BoolTypeNode())) )
			throw new TypeException("Incompatible types in AND",n.getLine());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(TimesNode n) throws TypeException {
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.left), new IntTypeNode())
				&& isSubtype(visit(n.right), new IntTypeNode())) )
			throw new TypeException("Non integers in multiplication",n.getLine());
		return new IntTypeNode();
	}

	@Override
	public TypeNode visitNode(DivNode n) throws TypeException {
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.left), new IntTypeNode())
				&& isSubtype(visit(n.right), new IntTypeNode())) )
			throw new TypeException("Non integers in division",n.getLine());
		return new IntTypeNode();
	}

	@Override
	public TypeNode visitNode(PlusNode n) throws TypeException {
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.left), new IntTypeNode())
				&& isSubtype(visit(n.right), new IntTypeNode())) )
			throw new TypeException("Non integers in sum",n.getLine());
		return new IntTypeNode();
	}

	@Override
	public TypeNode visitNode(MinusNode n) throws TypeException {
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.left), new IntTypeNode())
				&& isSubtype(visit(n.right), new IntTypeNode())) )
			throw new TypeException("Non integers in difference",n.getLine());
		return new IntTypeNode();
	}

	@Override
	public TypeNode visitNode(CallNode n) throws TypeException {
		if (print) printNode(n,n.id);
		TypeNode t = visit(n.entry); 
		if ( !(t instanceof ArrowTypeNode) )
			throw new TypeException("Invocation of a non-function "+n.id,n.getLine());
		ArrowTypeNode at = (ArrowTypeNode) t;
		if ( !(at.parlist.size() == n.arglist.size()) )
			throw new TypeException("Wrong number of parameters in the invocation of "+n.id,n.getLine());
		for (int i = 0; i < n.arglist.size(); i++)
			if ( !(isSubtype(visit(n.arglist.get(i)),at.parlist.get(i))) )
				throw new TypeException("Wrong type for "+(i+1)+"-th parameter in the invocation of "+n.id,n.getLine());
		return at.ret;
	}

	@Override
	public TypeNode visitNode(IdNode n) throws TypeException {
		if (print) printNode(n,n.id);
		TypeNode t = visit(n.entry);

		// MODIFICA OO: Rimuovere il blocco se t è ArrowTypeNode se si vuole permettere funzioni come valori,
		//		// ma per ora assicuriamoci solo che non usiamo una CLASSE come variabile.
		if (t instanceof ClassTypeNode)
			throw new TypeException("Wrong usage of class identifier " + n.id, n.getLine());

		return t;
	}

	@Override
	public TypeNode visitNode(BoolNode n) {
		if (print) printNode(n,n.val.toString());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(IntNode n) {
		if (print) printNode(n,n.val.toString());
		return new IntTypeNode();
	}

// gestione tipi incompleti	(se lo sono lancia eccezione)
	
	@Override
	public TypeNode visitNode(ArrowTypeNode n) throws TypeException {
		if (print) printNode(n);
		for (Node par: n.parlist) visit(par);
		visit(n.ret,"->"); //marks return type
		return null;
	}

	@Override
	public TypeNode visitNode(BoolTypeNode n) {
		if (print) printNode(n);
		return null;
	}

	@Override
	public TypeNode visitNode(IntTypeNode n) {
		if (print) printNode(n);
		return null;
	}

// STentry (ritorna campo type)

	@Override
	public TypeNode visitSTentry(STentry entry) throws TypeException {
		if (print) printSTentry("type");
		return ckvisit(entry.type); 
	}

	// --- GESTIONE OBJECT ORIENTED ---

	@Override
	public TypeNode visitNode(ClassNode n) throws TypeException {
		if (print) printNode(n, n.id);
		// Visita i metodi per controllare il loro corpo
		for (MethodNode method : n.methods) {
			try {
				visit(method);
			} catch (IncomplException e) {
			} catch (TypeException e) {
				System.out.println("Type checking error in method " + method.id + ": " + e.text);
			}
		}
		// Nota: Senza ereditarietà, non controlliamo superId o overriding qui.
		return null;
	}

	@Override
	public TypeNode visitNode(MethodNode n) throws TypeException {
		if (print) printNode(n, n.id);
		// Controlla le dichiarazioni locali (var/let)
		for (Node dec : n.declist)
			try {
				visit(dec);
			} catch (IncomplException e) {
			} catch (TypeException e) {
				System.out.println("Type checking error in declaration: " + e.text);
			}
		// Controlla che il corpo ritorni il tipo dichiarato
		if (!isSubtype(visit(n.exp), ckvisit(n.retType)))
			throw new TypeException("Wrong return type for method " + n.id, n.getLine());
		return null;
	}

	@Override
	public TypeNode visitNode(NewNode n) throws TypeException {
		if (print) printNode(n, n.id);

		// Recupera il tipo della classe (ClassTypeNode) dalla Symbol Table
		TypeNode t = visit(n.entry);
		if (!(t instanceof ClassTypeNode))
			throw new TypeException("Invocation of new on non-class " + n.id, n.getLine());

		ClassTypeNode ct = (ClassTypeNode) t;

		// Controlla numero parametri costruttore
		if (ct.allFields.size() != n.arglist.size())
			throw new TypeException("Wrong number of parameters for constructor of " + n.id, n.getLine());

		// Controlla tipi parametri costruttore
		for (int i = 0; i < n.arglist.size(); i++) {
			if (!isSubtype(visit(n.arglist.get(i)), ct.allFields.get(i)))
				throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in constructor of " + n.id, n.getLine());
		}

		return new RefTypeNode(n.id); // Ritorna il tipo "Puntatore a Classe ID"
	}

	@Override
	public TypeNode visitNode(ClassCallNode n) throws TypeException {
		if (print) printNode(n, n.objId + "." + n.methodId);

		// 1. Controllo l'oggetto ricevitore
		TypeNode objType = visit(n.entry);
		if (!(objType instanceof RefTypeNode))
			throw new TypeException("Method call on non-object " + n.objId, n.getLine());

		// 2. Controllo il metodo (usando la entry specifica salvata nel SymbolTableVisitor)
		TypeNode methodType = visit(n.methodEntry);
		if (!(methodType instanceof ArrowTypeNode))
			throw new TypeException("Invocation of non-method " + n.methodId, n.getLine());

		ArrowTypeNode at = (ArrowTypeNode) methodType;

		// 3. Controllo Argomenti
		if (at.parlist.size() != n.arglist.size())
			throw new TypeException("Wrong number of parameters in invocation of " + n.methodId, n.getLine());

		for (int i = 0; i < n.arglist.size(); i++)
			if (!isSubtype(visit(n.arglist.get(i)), at.parlist.get(i)))
				throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in invocation of " + n.methodId, n.getLine());

		return at.ret;
	}

	@Override
	public TypeNode visitNode(RefTypeNode n) {
		if (print) printNode(n, n.id);
		return null;
	}

	@Override
	public TypeNode visitNode(EmptyNode n) {
		if (print) printNode(n);
		return new EmptyTypeNode();
	}

	// Serve per evitare errori quando si visita l'entry di una classe
	@Override
	public TypeNode visitNode(ClassTypeNode n) {
		if (print) printNode(n);
		return null;
	}

}