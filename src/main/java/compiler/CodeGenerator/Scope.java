package compiler.CodeGenerator;

import compiler.Exceptions.CodeGeneratorException.UnexpectedError;
import compiler.Exceptions.CodeGeneratorException.WrongType;
import compiler.Nodes.*;

import java.text.ParseException;
import java.util.HashMap;

public class Scope {
    public final String className;
    final BlockNode origin; // TODO remove if unused
    final Scope parent;
    public final HashMap<String, CVVDeclaration> cvvDeclarations = new HashMap<>();
    final HashMap<String, RecordDeclaration> recordDeclarations = new HashMap<>();
    final HashMap<String, ProcDeclaration> procDeclarations = new HashMap<>();
    public int varCounter = 0;

    public Scope(String className) throws ParseException {
        this(null, null, className);

        // TODO add default functions to scope ? No
        // ArrayList<ParameterNode> params = new ArrayList<>();
        // params.add(new ParameterNode(new IdentifierNode("var"), new TypeNode.Base(Token.STRINGTYPE)));
        // declareProc("writeln", new ProcedureNode(new IdentifierNode("writeln"), params, new TypeNode.Void(), new BlockNode(new ArrayList<>())));
    }

    public Scope(BlockNode origin, Scope parent, String className) {
        this.origin = origin;
        this.parent = parent;
        this.className = className;
        // TODO add ass child to the parent if parent != null?
    }

    public String getClassName() {
        return className;
    }

    public CVVDeclaration declareCVV(String identifier, CVVNode cvv, boolean isGlobal)
            throws WrongType, UnexpectedError {
        CVVDeclaration var;
        if (isGlobal)
            var = new CVVDeclaration(this, cvv, -1, isGlobal);
        else {
            var = new CVVDeclaration(this, cvv, varCounter, isGlobal);
            varCounter += CodeGenerator.nodeToASMType(cvv.type).getSize();
        }
        cvvDeclarations.put(identifier, var);
        return var;
    }

    public void delete(String identifier) {
        // we just can delete a variable that is a record or an array
        //TODO check also if var.declaration.type is a record
        CVVDeclaration var = cvvLookup(identifier);

        if (var.declaration.type instanceof TypeNode.Array)
            cvvDeclarations.remove(identifier);
        else
            throw new RuntimeException("Can't delete " + identifier + " because it's not a record or array");
    }

    public RecordDeclaration declareRec(RecordDeclarationNode rec) {
        RecordDeclaration rd = new RecordDeclaration(this, rec, varCounter);
        // TODO temp change to tree structure instead of single rec at the top
        if (parent != null)
            return parent.recordDeclarations.put(rec.identifier.name, rd);

        recordDeclarations.put(rec.identifier.name, rd);
        varCounter++;
        return rd;
    }

    public ProcDeclaration declareProc(String identifier, ProcedureNode proc) throws WrongType, UnexpectedError {
        ProcDeclaration var = new ProcDeclaration(this, proc, varCounter);
        varCounter += CodeGenerator.nodeToASMType(proc.returnType).getSize();
        procDeclarations.put(identifier, var);
        return var;
    }

    public CVVDeclaration cvvLookup(String name) {
        CVVDeclaration declaration = cvvLocalLookup(name);

        if (declaration == null)
            return parent == null ? null : parent.cvvLookup(name);

        return declaration;
    }

    public CVVDeclaration cvvLocalLookup(String name) { // TODO remove if unused
        return cvvDeclarations.get(name);
    }

    public RecordDeclaration recordLookup(String name) {
        RecordDeclaration declaration = recordLocalLookup(name);

        if (declaration == null)
            return parent == null ? null : parent.recordLookup(name);

        return declaration;
    }

    public RecordDeclaration recordLocalLookup(String name) { // TODO remove if unused
        return recordDeclarations.get(name);
    }

    public ProcDeclaration procLookup(String name) {
        ProcDeclaration declaration = procLocalLookup(name);

        if (declaration == null)
            return parent == null ? null : parent.procLookup(name);

        return declaration;
    }

    public ProcDeclaration procLocalLookup(String name) { // TODO remove if unused
        return procDeclarations.get(name);
    }

    public static class CVVDeclaration {
        public Scope scope;
        public CVVNode declaration;
        public int index;
        public boolean isGlobal;

        public CVVDeclaration(Scope scope, CVVNode declaration, int index, boolean isGlobal) {
            this.scope = scope;
            this.declaration = declaration;
            this.index = index;
            this.isGlobal = isGlobal;
        }
    }

    public static class RecordDeclaration {
        public Scope scope;
        public RecordDeclarationNode declaration;
        public int index;

        public RecordDeclaration(Scope scope, RecordDeclarationNode declaration, int index) {
            this.scope = scope;
            this.declaration = declaration;
            this.index = index;
        }
    }

    public static class ProcDeclaration {
        public Scope scope;
        public ProcedureNode declaration;
        public int index;

        public ProcDeclaration(Scope scope, ProcedureNode declaration, int index) {
            this.scope = scope;
            this.declaration = declaration;
            this.index = index;
        }
    }
}
