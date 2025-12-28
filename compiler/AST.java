package compiler;

import java.util.*;
import compiler.lib.*;

public class AST {
	
	public static class ProgLetInNode extends Node {
		final List<DecNode> declist;
		final Node exp;
		ProgLetInNode(List<DecNode> d, Node e) {
			declist = Collections.unmodifiableList(d); 
			exp = e;
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	// Nodo per la dichiarazione di una Classe
	public static class ClassNode extends DecNode {
		final String id;
		//final String superId; // ID della superclasse (o null se non c'è extends)
		final List<FieldNode> fields;
		final List<MethodNode> methods;

		public STentry entry;

		// Costruttore
		ClassNode(String i, List<FieldNode> f, List<MethodNode> m) {
			id = i;
			//superId = si;
			fields = Collections.unmodifiableList(f);
			methods = Collections.unmodifiableList(m);
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	// Nodo per la dichiarazione di un Metodo (simile a FunNode)
	public static class MethodNode extends DecNode {
		final String id;
		final TypeNode retType;
		final List<ParNode> parlist;
		final List<DecNode> declist;
		final Node exp;
		String label; // Etichetta per la generazione codice (aggiunta futura)
		int offset;   // Offset nella Virtual Table

		MethodNode(String i, TypeNode rt, List<ParNode> pl, List<DecNode> dl, Node e) {
			id=i;
			retType=rt;
			parlist=Collections.unmodifiableList(pl);
			declist=Collections.unmodifiableList(dl);
			exp=e;
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	// Nodo per un Campo (Field) - Simile a ParNode
	public static class FieldNode extends DecNode {
		final String id;

		FieldNode(String i, TypeNode t) {
			id = i;
			type = t;
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	// Nodo per l'istanziazione (NEW)
	public static class NewNode extends Node {
		final String id; // Nome della classe da istanziare
		final List<Node> arglist;
		STentry entry; // Riferimento alla classe nella Symbol Table

		NewNode(String i, List<Node> a) {
			id = i;
			arglist = Collections.unmodifiableList(a);
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	// Nodo per la chiamata di metodo (obj.metodo(...))
	public static class ClassCallNode extends Node {
		final String objId;   // Nome dell'oggetto
		final String methodId; // Nome del metodo
		final List<Node> arglist;
		STentry entry;      // Entry dell'oggetto
		STentry methodEntry;// Entry del metodo (nella VTable)
		int nl; // Nesting Level

		ClassCallNode(String oi, String mi, List<Node> a) {
			objId = oi;
			methodId = mi;
			arglist = Collections.unmodifiableList(a);
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	// Nodo per il tipo "Riferimento a Classe"
	public static class RefTypeNode extends TypeNode {
		final String id; // Nome della classe referenziata

		RefTypeNode(String i) {
			id = i;
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	// Nodo per 'null'
	public static class EmptyNode extends Node {
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	// Rappresenta il tipo "NULL"
	public static class EmptyTypeNode extends TypeNode {
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	// Rappresenta la struttura di tipo di una Classe (nella Symbol Table)
	public static class ClassTypeNode extends TypeNode {
		// Lista dei tipi di tutti i campi (propri + ereditati)
		final ArrayList<TypeNode> allFields;
		// Lista dei tipi funzionali di tutti i metodi (propri + ereditati)
		final ArrayList<ArrowTypeNode> allMethods;

		public ClassTypeNode(ArrayList<TypeNode> f, ArrayList<ArrowTypeNode> m) {
			allFields = f;
			allMethods = m;
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {
			return visitor.visitNode(this);
		}
	}

	public static class ProgNode extends Node {
		final Node exp;
		ProgNode(Node e) {exp = e;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class FunNode extends DecNode {
		final String id;
		final TypeNode retType;
		final List<ParNode> parlist;
		final List<DecNode> declist; 
		final Node exp;
		FunNode(String i, TypeNode rt, List<ParNode> pl, List<DecNode> dl, Node e) {
	    	id=i; 
	    	retType=rt; 
	    	parlist=Collections.unmodifiableList(pl); 
	    	declist=Collections.unmodifiableList(dl); 
	    	exp=e;
	    }
		
		//void setType(TypeNode t) {type = t;}
		
		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class ParNode extends DecNode {
		final String id;
		ParNode(String i, TypeNode t) {id = i; type = t;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class VarNode extends DecNode {
		final String id;
		final Node exp;
		VarNode(String i, TypeNode t, Node v) {id = i; type = t; exp = v;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
		
	public static class PrintNode extends Node {
		final Node exp;
		PrintNode(Node e) {exp = e;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class IfNode extends Node {
		final Node cond;
		final Node th;
		final Node el;
		IfNode(Node c, Node t, Node e) {cond = c; th = t; el = e;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class EqualNode extends Node {
		final Node left;
		final Node right;
		EqualNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class LessEqualNode extends Node {
		final Node left;
		final Node right;
		LessEqualNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class GreaterEqualNode extends Node {
		final Node left;
		final Node right;
		GreaterEqualNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class NotNode extends Node {
		final Node exp;
		NotNode(Node r) {exp = r;}

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class OrNode extends Node {
		final Node left;
		final Node right;

		OrNode(Node l, Node r) {
			left =l; right = r;}

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class AndNode extends Node {
		final Node left;
		final Node right;

		AndNode(Node l, Node r) {
			left =l; right = r;}

		@Override
		public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class TimesNode extends Node {
		final Node left;
		final Node right;
		TimesNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class DivNode extends Node {
		final Node left;
		final Node right;
		DivNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class PlusNode extends Node {
		final Node left;
		final Node right;
		PlusNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class MinusNode extends Node {
		final Node left;
		final Node right;
		MinusNode(Node l, Node r) {left = l; right = r;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class CallNode extends Node {
		final String id;
		final List<Node> arglist;
		STentry entry;
		int nl;
		CallNode(String i, List<Node> p) {
			id = i; 
			arglist = Collections.unmodifiableList(p);
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class IdNode extends Node {
		final String id;
		STentry entry;
		int nl;
		IdNode(String i) {id = i;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class BoolNode extends Node {
		final Boolean val;
		BoolNode(boolean n) {val = n;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class IntNode extends Node {
		final Integer val;
		IntNode(Integer n) {val = n;}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class ArrowTypeNode extends TypeNode {
		final List<TypeNode> parlist;
		final TypeNode ret;
		ArrowTypeNode(List<TypeNode> p, TypeNode r) {
			parlist = Collections.unmodifiableList(p); 
			ret = r;
		}

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}
	
	public static class BoolTypeNode extends TypeNode {

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

	public static class IntTypeNode extends TypeNode {

		@Override
		public <S,E extends Exception> S accept(BaseASTVisitor<S,E> visitor) throws E {return visitor.visitNode(this);}
	}

}