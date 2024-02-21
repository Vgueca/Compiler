package compiler.Nodes;

import compiler.CodeGenerator.Scope;
import compiler.Exceptions.CodeGeneratorException.CodeGeneratorException;
import compiler.Exceptions.SemanticException.SemanticException;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.Visitors.PrintVisitor;
import compiler.Visitors.SemanticVisitor;
import org.objectweb.asm.ClassWriter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Objects;

public class ProgramNode extends ASTNode {
    ArrayList<CVVNode.Const> constants;
    ArrayList<RecordDeclarationNode> records;
    ArrayList<CVVNode> valVar;
    ArrayList<ProcedureNode> procedures;

    public ProgramNode(ArrayList<CVVNode.Const> constants, ArrayList<RecordDeclarationNode> records,
            ArrayList<CVVNode> valVar,
            ArrayList<ProcedureNode> procedures) {
        this.constants = constants == null ? new ArrayList<>() : constants;
        this.records = records == null ? new ArrayList<>() : records;
        this.valVar = valVar == null ? new ArrayList<>() : valVar;
        this.procedures = procedures == null ? new ArrayList<>() : procedures;
    }

    public ArrayList<CVVNode.Const> getConstants() {
        return constants;
    }

    public ArrayList<RecordDeclarationNode> getRecords() {
        return records;
    }

    public ArrayList<CVVNode> getValVar() {
        return valVar;
    }

    public ArrayList<ProcedureNode> getProcedures() {
        return procedures;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("// constants");
        for (ASTNode c : constants)
            ret.append("\n").append(c);
        ret.append("\n// records");
        for (ASTNode r : records)
            ret.append("\n").append(r);
        ret.append("\n// val/var");
        for (CVVNode v : valVar)
            ret.append("\n").append(v);
        ret.append("\n// procedures");
        for (ProcedureNode p : procedures)
            ret.append("\n").append(p);
        return ret.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProgramNode that = (ProgramNode) o;
        return Objects.equals(constants, that.constants) && Objects.equals(records, that.records)
                && Objects.equals(valVar, that.valVar) && Objects.equals(procedures, that.procedures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(constants, records, valVar, procedures);
    }

    // -------------------------------------------------------------------------

    @Override
    public void accept(PrintVisitor visitor, int depth) {
        visitor.visit(this, depth);
        depth++;
        for (ASTNode c : constants)
            c.accept(visitor, depth);
        for (ASTNode r : records)
            r.accept(visitor, depth);
        for (CVVNode v : valVar)
            v.accept(visitor, depth);
        for (ProcedureNode p : procedures)
            p.accept(visitor, depth);
    }

    @Override
    public void accept(SemanticVisitor visitor, SymbolTable st) throws ParseException, SemanticException {
        visitor.visit(this, st);
        for (ASTNode c : constants)
            c.accept(visitor, st);
        for (ASTNode r : records)
            r.accept(visitor, st);
        for (CVVNode v : valVar)
            v.accept(visitor, st);
        for (ProcedureNode p : procedures)
            p.accept(visitor, st);

    }

    @Override
    public void accept(Object o, Scope scope) throws CodeGeneratorException {
        if (!(o instanceof ClassWriter cw))
            return; // todo throw error

        for (CVVNode.Const c : constants)
            c.accept(cw, scope);
        for (RecordDeclarationNode r : records)
            r.accept(cw, scope);
        for (CVVNode v : valVar)
            v.accept(cw, scope);
        for (ProcedureNode p : procedures)
            p.accept(cw, scope);
    }

    // -------------------------------------------------------------------------
}