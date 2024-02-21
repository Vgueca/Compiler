package compiler.SemanticAnalyzer;

import compiler.Exceptions.SemanticException.SemanticException;
import compiler.Nodes.IdentifierNode;
import compiler.Nodes.TypeNode;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    String origin;
    SymbolTable previousTable;
    HashMap<String, SType> entries;
    ArrayList<SymbolTable> nextTables;

    public SymbolTable() throws ParseException, SemanticException {
        previousTable = new SymbolTable(null, "default");

        // default functions
        previousTable.add(new IdentifierNode("readInt"), new SType.Function(new SType.Int(), new ArrayList<>()));
        previousTable.add(new IdentifierNode("readReal"), new SType.Function(new SType.Real(), new ArrayList<>()));
        previousTable.add(new IdentifierNode("readString"), new SType.Function(new SType.String(), new ArrayList<>()));

        ArrayList<SType> writeIntParam = new ArrayList<>();
        writeIntParam.add(new SType.Int());
        previousTable.add(new IdentifierNode("writeInt"), new SType.Function(new SType.Void(), writeIntParam));

        ArrayList<SType> writeRealParam = new ArrayList<>();
        writeRealParam.add(new SType.Real());
        previousTable.add(new IdentifierNode("writeReal"), new SType.Function(new SType.Void(), writeRealParam));

        ArrayList<SType> writeBoolParam = new ArrayList<>();
        writeBoolParam.add(new SType.Bool());
        previousTable.add(new IdentifierNode("writeBool"), new SType.Function(new SType.Void(), writeBoolParam));

        ArrayList<SType> writeParam = new ArrayList<>();
        writeParam.add(new SType.String());
        previousTable.add(new IdentifierNode("write"), new SType.Function(new SType.Void(), writeParam));
        previousTable.add(new IdentifierNode("writeln"), new SType.Function(new SType.Void(), writeParam));

        ArrayList<SType> notBoolParam = new ArrayList<>();
        notBoolParam.add(new SType.Bool());
        previousTable.add(new IdentifierNode("not"), new SType.Function(new SType.Bool(), notBoolParam));

        // init
        previousTable.nextTables.add(this);
        entries = new HashMap<>();
        nextTables = new ArrayList<>();
        origin = "root";
    }

    public SymbolTable(SymbolTable prev, String origin) {
        previousTable = prev;
        if (prev != null)
            prev.nextTables.add(this);
        entries = new HashMap<>();
        nextTables = new ArrayList<>();
        this.origin = origin;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int depth) {
        StringBuilder ret = new StringBuilder();
        if (depth == 0)
            ret.append("(Symbol table) {");
        else
            ret.append("\t".repeat(depth)).append("(").append(origin).append(") {");

        for (Map.Entry<String, SType> s : entries.entrySet())
            ret.append("\n\t").append("\t".repeat(depth)).append(s.getKey()).append(" : ").append(s.getValue());
        if (!nextTables.isEmpty())
            for (SymbolTable st : nextTables)
                ret.append("\n").append(st.toString(depth + 1));
        return ret + "\n" + "\t".repeat(depth) + "}";
    }

    public SymbolTable add(IdentifierNode id, SType type) throws SemanticException {
        if (entries.containsKey(id.name))
            throw new SemanticException("Already declared.",
                    "You can't declare '" + id + "' multiple times in the same scope (but shadowing is allowed).");
        entries.put(id.name, type);
        return this;
    }

    public SymbolTable add(IdentifierNode id, TypeNode.Base baseType) throws ParseException, SemanticException {
        return add(id, SType.getSType(baseType));
    }

    public SymbolTable add(IdentifierNode id, TypeNode type) throws ParseException, SemanticException {
        if (type instanceof TypeNode.Base) {
            return add(id, ((TypeNode.Base) type));
        } else if (type instanceof TypeNode.Array) {
            return add(id, new SType.Array(SType.getSType(((TypeNode.Array) type).baseType)));
        } else if (type instanceof TypeNode.Void) {
            return add(id, new SType.Void());
        } // instanceof identifier?
        throw new ParseException("Unknown type." + type, 0);
    }

    public SType get(IdentifierNode id) throws ParseException {
        if (entries.containsKey(id.name))
            return entries.get(id.name);
        else if (previousTable == null)
            throw new ParseException("The id '" + id
                    + "' doesn't exist in the SymbolTable. You are trying to use an undefined variable/array/function.",
                    0);
        else
            return previousTable.get(id);
    }

    public SymbolTable delete(IdentifierNode id) throws ParseException {
        if (entries.containsKey(id.name)) {
            if (get(id) instanceof SType.Array || get(id) instanceof SType.Record)
                entries.remove(id.name);
            else
                throw new ParseException("You can only delete Arrays and Records.", 0);
        } else if (previousTable == null)
            throw new ParseException("The id '" + id + "' doesn't exist in the SymbolTable.", 0);
        else
            previousTable.delete(id);
        return this;
    }

    //TODO check that variables and procedures with the same name/identifier cant exist (make a test?)

    public SType getReturnType() throws SemanticException {
        if (this instanceof SymbolTable.ProcST procST)
            return procST.returnType;
        else if (previousTable == null)
            throw new SemanticException("Return statement outside of a procedure",
                    "You can only use the return statement inside the code block of a procedure.");
        else
            return previousTable.getReturnType();
    }

    public static class ProcST extends SymbolTable {
        public SType returnType;

        public ProcST(SymbolTable prev, String origin, SType returnType) {
            super(prev, origin);
            this.returnType = returnType;
        }
    }
}
