package compiler;

import java.util.*;
import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void,VoidException> {

	// Mappa Nomi Classi -> Virtual Table (per l'accesso statico ai membri)
	private Map<String, Map<String, STentry>> classTable = new HashMap<>();
	private List<Map<String, STentry>> symTable = new ArrayList<>();
	private int nestingLevel=0; // current nesting level
	private int decOffset=-2; // counter for offset of local declarations at current nesting level 
	int stErrors=0;

	SymbolTableASTVisitor() {}
	SymbolTableASTVisitor(boolean debug) {super(debug);} // enables print for debugging

	private STentry stLookup(String id) {
		int j = nestingLevel;
		STentry entry = null;
		while (j >= 0 && entry == null) 
			entry = symTable.get(j--).get(id);	
		return entry;
	}

	@Override
	public Void visitNode(ProgLetInNode n) {
		if (print) printNode(n);
		Map<String, STentry> hm = new HashMap<>();
		symTable.add(hm);
	    for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		symTable.remove(0);
		return null;
	}

	@Override
	public Void visitNode(ProgNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}
	
	@Override
	public Void visitNode(FunNode n) {
		if (print) printNode(n);
		Map<String, STentry> hm = symTable.get(nestingLevel);
		List<TypeNode> parTypes = new ArrayList<>();  
		for (ParNode par : n.parlist) parTypes.add(par.getType()); 
		STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes,n.retType),decOffset--);
		//inserimento di ID nella symtable
		if (hm.put(n.id, entry) != null) {
			System.out.println("Fun id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		} 
		//creare una nuova hashmap per la symTable
		nestingLevel++;
		Map<String, STentry> hmn = new HashMap<>();
		symTable.add(hmn);
		int prevNLDecOffset=decOffset; // stores counter for offset of declarations at previous nesting level 
		decOffset=-2;
		
		int parOffset=1;
		for (ParNode par : n.parlist)
			if (hmn.put(par.id, new STentry(nestingLevel,par.getType(),parOffset++)) != null) {
				System.out.println("Par id " + par.id + " at line "+ n.getLine() +" already declared");
				stErrors++;
			}
		for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		//rimuovere la hashmap corrente poiche' esco dallo scope               
		symTable.remove(nestingLevel--);
		decOffset=prevNLDecOffset; // restores counter for offset of declarations at previous nesting level 
		return null;
	}
	
	@Override
	public Void visitNode(VarNode n) {
		if (print) printNode(n);
		visit(n.exp);
		Map<String, STentry> hm = symTable.get(nestingLevel);
		STentry entry = new STentry(nestingLevel,n.getType(),decOffset--);
		//inserimento di ID nella symtable
		if (hm.put(n.id, entry) != null) {
			System.out.println("Var id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		}
		return null;
	}

	@Override
	public Void visitNode(PrintNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(IfNode n) {
		if (print) printNode(n);
		visit(n.cond);
		visit(n.th);
		visit(n.el);
		return null;
	}
	
	@Override
	public Void visitNode(EqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(LessEqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(GreaterEqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(NotNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(OrNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(AndNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(TimesNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(DivNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}
	
	@Override
	public Void visitNode(PlusNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(MinusNode n)  {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(CallNode n) {
		if (print) printNode(n);
		STentry entry = stLookup(n.id);
		if (entry == null) {
			System.out.println("Fun id " + n.id + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else {
			n.entry = entry;
			n.nl = nestingLevel;
		}
		for (Node arg : n.arglist) visit(arg);
		return null;
	}

	@Override
	public Void visitNode(IdNode n) {
		if (print) printNode(n);
		STentry entry = stLookup(n.id);
		if (entry == null) {
			System.out.println("Var or Par id " + n.id + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else {
			n.entry = entry;
			n.nl = nestingLevel;
		}
		return null;
	}

	@Override
	public Void visitNode(BoolNode n) {
		if (print) printNode(n, n.val.toString());
		return null;
	}

	@Override
	public Void visitNode(IntNode n) {
		if (print) printNode(n, n.val.toString());
		return null;
	}

	// --- GESTIONE CLASSI (Estensione Object Oriented) ---
	@Override
	public Void visitNode(ClassNode n) {
		if (print) printNode(n);

		// 1. Gestione Ereditarietà e Creazione Virtual Table
		Map<String, STentry> virtualTable = new HashMap<>();
		/*ClassTypeNode parentClassType = null;

		// Se eredita, copia la Virtual Table del padre e recupera il suo tipo
		if (n.superId != null) {
			if (classTable.containsKey(n.superId)) {
				// Copia profonda (logica) della mappa del padre
				virtualTable.putAll(classTable.get(n.superId));

				// Recupera il ClassTypeNode del padre dalla Symbol Table globale (livello 0)
				STentry parentEntry = symTable.get(0).get(n.superId);
				parentClassType = (ClassTypeNode) parentEntry.type;
			} else {
				System.out.println("Superclass " + n.superId + " at line " + n.getLine() + " not declared");
				stErrors++;
			}
		}
		*/
		// 2. Registrazione della Classe nella Symbol Table Globale (Livello 0)
		// Inizialmente creo liste vuote o copiate dal padre per fields e methods
		ArrayList<TypeNode> allFields =  new ArrayList<>();
		ArrayList<ArrowTypeNode> allMethods = new ArrayList<>();

		ClassTypeNode classType = new ClassTypeNode(allFields, allMethods);
		STentry classEntry = new STentry(0, classType, decOffset--);

		n.entry = classEntry;

		if (symTable.get(0).put(n.id, classEntry) != null) {
			System.out.println("Class id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		}

		// Registrazione nella Class Table
		classTable.put(n.id, virtualTable);

		// Entrata nello Scope della Classe
		// La Virtual Table diventa lo scope corrente (livello 1)
		symTable.add(virtualTable);
		nestingLevel++;
		int prevNLDecOffset = decOffset; // Salvo offset globale

		// Gestione Campi (Fields)
		// Offset Campi: partono da -1 e decrescono. Se eredito, parto da dove ha finito il padre.
		int fieldOffset = -1;
		/*if (parentClassType != null) {
			fieldOffset = -parentClassType.allFields.size() - 1;
		}*/

		for (FieldNode field : n.fields) {
			if (virtualTable.put(field.id, new STentry(nestingLevel, field.getType(), fieldOffset--)) != null) {
				System.out.println("Field id " + field.id + " at line "+ field.getLine() +" already declared");
				stErrors++;
			}
			// Aggiorno la lista globale dei tipi dei campi
			allFields.add(field.getType());
		}

		// Gestione Metodi - Prima passata per registrare le firme
		// Offset Metodi: partono da 0 e crescono.
		int methodOffset = 0;
		/*if (parentClassType != null) {
			methodOffset = parentClassType.allMethods.size();
		}*/

		for (MethodNode method : n.methods) {
			/*
			// Controllo Overriding
			STentry existingMethod = virtualTable.get(method.id);

			if (existingMethod != null && existingMethod.type instanceof ArrowTypeNode) {
				// OVERRIDING: Mantengo l'offset del padre!
				method.offset = existingMethod.offset;

				// Aggiorno l'entry nella VTable con il nuovo tipo ma vecchio offset
				virtualTable.put(method.id, new STentry(nestingLevel, method.retType  nota: qui andrebbe ArrowType completo , method.offset));

				// Aggiorno la lista globale dei metodi (ArrowTypeNode)
				// Nota: In un compilatore reale dovremmo costruire l'ArrowTypeNode corretto qui
				// Per semplicità assumiamo che il controllo tipi avvenga dopo.
				// Qui dobbiamo aggiornare allMethods alla posizione dell'offset.
				// Purtroppo MethodNode ha parametri sparsi, costruiamo l'ArrowTypeNode al volo
				List<TypeNode> parTypes = new ArrayList<>();
				for(ParNode p : method.parlist) parTypes.add(p.getType());
				allMethods.set(method.offset, new ArrowTypeNode(parTypes, method.retType));

			} else {
			*/
				// NUOVO METODO
				method.offset = methodOffset++;
				List<TypeNode> parTypes = new ArrayList<>();
				for(ParNode p : method.parlist) parTypes.add(p.getType());
				ArrowTypeNode arrowType = new ArrowTypeNode(parTypes, method.retType);

				if (virtualTable.put(method.id, new STentry(nestingLevel, arrowType, method.offset)) != null) {
					System.out.println("Method id " + method.id + " at line "+ method.getLine() +" already declared (as field?)");
					stErrors++;
				}
				allMethods.add(arrowType);
			//}
		}

		// Visita dei Corpi dei Metodi
		// Qui decOffset viene resettato per le variabili locali del metodo
		decOffset = -2;
		for (MethodNode method : n.methods) {
			visit(method); // Visita il corpo del metodo
		}

		// Uscita dallo Scope
		symTable.remove(nestingLevel--);
		decOffset = prevNLDecOffset; // Ripristino offset globale
		return null;
	}

	@Override
	public Void visitNode(MethodNode n) {
		if (print) printNode(n);
		Map<String, STentry> hm = symTable.get(nestingLevel);

		// Nota: L'entry del metodo è già stata inserita nella VTable dal padre (ClassNode)
		// Qui gestiamo solo lo scope interno (parametri e locali)

		nestingLevel++;
		Map<String, STentry> hmn = new HashMap<>();
		symTable.add(hmn);

		int prevNLDecOffset = decOffset;
		decOffset = -2;

		int parOffset = 1;
		for (ParNode par : n.parlist)
			if (hmn.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) != null) {
				System.out.println("Par id " + par.id + " at line " + n.getLine() + " already declared");
				stErrors++;
			}

		for (Node dec : n.declist) visit(dec);
		visit(n.exp);

		symTable.remove(nestingLevel--);
		decOffset = prevNLDecOffset;
		return null;
	}

	@Override
	public Void visitNode(NewNode n) {
		if (print) printNode(n);

		// Cerco la classe nella symbol table globale
		STentry entry = symTable.get(0).get(n.id);

		if (entry == null || !(entry.type instanceof ClassTypeNode)) {
			System.out.println("Class " + n.id + " at line " + n.getLine() + " not declared");
			stErrors++;
		}

		n.entry = entry;
		for (Node arg : n.arglist) visit(arg);
		return null;
	}

	@Override
	public Void visitNode(ClassCallNode n) {
		if (print) printNode(n);

		// 1. Cerco l'oggetto su cui chiamo il metodo (es. 'fido' in fido.abbaia())
		STentry objEntry = stLookup(n.objId);
		if (objEntry == null) {
			System.out.println("Object id " + n.objId + " at line " + n.getLine() + " not declared");
			stErrors++;
		} else {
			n.entry = objEntry; // Entry dell'oggetto
			n.nl = nestingLevel;

			// 2. Controllo se è un RefType (cioè un oggetto)
			if (objEntry.type instanceof RefTypeNode) {
				String classId = ((RefTypeNode) objEntry.type).id;

				// 3. Recupero la Virtual Table della classe
				Map<String, STentry> virtualTable = classTable.get(classId);

				if (virtualTable == null) {
					System.out.println("Class " + classId + " not found in ClassTable (internal error?)");
					stErrors++;
				} else {
					// 4. Cerco il metodo nella Virtual Table
					STentry methodEntry = virtualTable.get(n.methodId);
					if (methodEntry == null) {
						System.out.println("Method " + n.methodId + " not defined for class " + classId + " at line " + n.getLine());
						stErrors++;
					} else {
						n.methodEntry = methodEntry; // Salvo l'entry del metodo (servirà per l'offset a CodeGen)
					}
				}
			} else {
				System.out.println("Variable " + n.objId + " at line " + n.getLine() + " is not an object ref");
				stErrors++;
			}
		}

		for (Node arg : n.arglist) visit(arg);
		return null;
	}

	@Override
	public Void visitNode(RefTypeNode n) {
		if (print) printNode(n);
		// Opzionale: controllare se n.id esiste come classe dichiarata
		return null;
	}

	@Override
	public Void visitNode(EmptyNode n) {
		if (print) printNode(n);
		return null;
	}
}
