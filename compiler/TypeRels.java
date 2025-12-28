package compiler;

import compiler.AST.*;
import compiler.lib.*;

public class TypeRels {

	// Valuta se il tipo "a" <= "b"
	public static boolean isSubtype(TypeNode a, TypeNode b) {
		// 1. Stesso tipo Java (Int vs Int, Bool vs Bool, RefType vs RefType)
		if (a.getClass().equals(b.getClass())) {
			// Per gli oggetti, senza ereditarietà, i nomi devono essere identici
			if (a instanceof RefTypeNode) {
				return ((RefTypeNode)a).id.equals(((RefTypeNode)b).id);
			}
			return true;
		}

		// 2. Bool <= Int
		if ((a instanceof BoolTypeNode) && (b instanceof IntTypeNode)) return true;

		// 3. Null (EmptyType) <= Qualsiasi Classe (RefType)
		if ((a instanceof EmptyTypeNode) && (b instanceof RefTypeNode)) return true;

		return false;
	}

	// Calcola il tipo comune più basso (usato nell'IF)
	public static TypeNode lowestCommonAncestor(TypeNode a, TypeNode b) {
		// Se a è sottotipo di b, b è il tipo comune (es. Bool e Int -> Int)
		if (isSubtype(a, b)) return b;

		// Se b è sottotipo di a, a è il tipo comune
		if (isSubtype(b, a)) return a;

		// Senza ereditarietà, se non sono uno sottotipo dell'altro, non c'è LCA
		return null;
	}

}
