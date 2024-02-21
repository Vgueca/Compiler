package compiler.Visitors;

import compiler.Exceptions.SemanticException.SemanticException;
import compiler.Exceptions.SemanticException.UndeclaredVariable;
import compiler.Nodes.*;
import compiler.SemanticAnalyzer.SType;
import compiler.SemanticAnalyzer.SemanticAnalyzer;
import compiler.SemanticAnalyzer.SymbolTable;

import java.text.ParseException;
import java.util.ArrayList;

public class SemanticVisitor {

    public void visit(ASTNode node, SymbolTable st) {
        // do nothing
    }

    //    public void visit(ArithFactorNode node, SymbolTable st) {}
    //    public void visit(ArithTermNode node, SymbolTable st) {}
    //    public void visit(ArrayAccessNode node, SymbolTable st) {}

    //TODO  MAYBE NOT NECESSARY
    public void visit(AssignmentNode node, SymbolTable st) throws SemanticException, ParseException {
        SType left = SemanticAnalyzer.getType(node.left, st);
        SType right = SemanticAnalyzer.getType(node.right, st);

        if (!(left.equals(right))) {
            throw new SemanticException("Types in assignment does not match ",
                    " types given: " + left + " and " + right + ".");
        }
    }

    //    public void visit(BlockNode node, SymbolTable st) {}
    //    public void visit(BoolFactorNode node, SymbolTable st) {}
    //    public void visit(BoolTermNode node, SymbolTable st) {}

    public void visit(CVVNode node, SymbolTable st) throws ParseException, SemanticException {
        // const/val/var declaration so we have to add it to the st

        SType type = SemanticAnalyzer.getType(node.type, st); // int, real, string, bool, arrays, or Identifier (record)
        SType expression = SemanticAnalyzer.getType(node.expression, st); // expression or init

        if (node.type instanceof TypeNode.Identifier id_type
                && st.get(id_type.identifier) instanceof SType.Record rec) {
            st.add(node.identifier, new SType.Id(id_type.identifier.name));
            for (SType.Record.RecField field : rec.fields)
                st.add(new IdentifierNode(node.identifier.name + "." + field.id), field.type);
        } else
            st.add(node.identifier, node.type);
        if (!(type.equals(expression)) && !(type.equals(new SType.Real()) && expression.equals(new SType.Int())))
            throw new SemanticException("Types don't match",
                    "When doing an assignment make sure the types match " + type + " and " + expression + ".");
    }

    public void visit(DeleteNode node, SymbolTable st) throws ParseException {
        st.delete(node.deleted);
    }

    //    public void visit(ExpressionNode node, SymbolTable st) {}
    //    public void visit(FieldDeclarationNode node, SymbolTable st) {}

    public void visit(ForNode node, SymbolTable st) throws ParseException, SemanticException {
        SType idx = st.get(node.i);
        SType upperbound = SemanticAnalyzer.getType(node.to, st);

        if (!(idx.equals(SemanticAnalyzer.getType(node.from, st))) || !(idx.equals(new SType.Int()))) {
            throw new SemanticException("Error in the index of the for statement",
                    "The index should be an integer and got " + st.get(node.i) + ".");
        }
        if (!(upperbound.equals(new SType.Int()))) {
            throw new SemanticException("Error in the upperbound of the statement",
                    "The upperbound should be an integer and got " + upperbound + ".");
        }
        if (node instanceof ForNode.By node_by) {
            SType by = SemanticAnalyzer.getType(node_by.by, st);
            if (!by.equals(new SType.Int())) {
                throw new SemanticException("Error in the step of the statement",
                        "The step should be an integer and got " + by + ".");
            }
        }

    }

    public void visit(FunctionCallNode node, SymbolTable st) throws ParseException, SemanticException {
        SType ftype = st.get(node.identifier);
        if (ftype instanceof SType.Function fun) {
            if (node.args.size() != fun.params.size())
                throw new SemanticException("Wrong number of arguments",
                        "Not the right amount of function call arguments.");

            for (int i = 0; i < node.args.size(); i++) {
                ASTNode arg = node.args.get(i);

                SType arg_type = SemanticAnalyzer.getType(arg, st);
                SType param = fun.params.get(i);

                if (!(arg_type.equals(param)))
                    throw new SemanticException("Wrong argument type",
                            "Check the type of argument " + (i + 1) + " that should be a " + param + " (not: " + arg_type + ").");
            }
        } else if (ftype instanceof SType.Record rec) { // record init

            if (node.args.size() != rec.fields.size())
                throw new SemanticException("Wrong number of arguments",
                        "Not the right amount of record call arguments.");

            for (int i = 0; i < rec.fields.size(); i++) {
                ASTNode arg = node.args.get(i);
                SType arg_type = SemanticAnalyzer.getType(arg, st);

                if (!(rec.fields.get(i).type.equals(arg_type)))
                    throw new SemanticException("Argument types don't match.",
                            "Argument number " + i + " should be a " + rec.fields.get(i) + " (not: " + arg_type + ").");
            }
        } else
            throw new SemanticException("Function call not on a function or a record",
                    "You called  (" + ftype + ").");
    }

    //    public void visit(IdentifierNode node, SymbolTable st) {}
    public void visit(IfNode node, SymbolTable st) throws ParseException, SemanticException {
        SType condition = SemanticAnalyzer.getType(node.condition, st);

        if (!condition.equals(new SType.Bool()) && !condition.equals(new SType.Int())
                && !condition.equals(new SType.Real())) {
            throw new SemanticException("Incorrect type for condition in the if statement.",
                    "The type used as condition is not a bool, is + " + condition + ".");
        }
    }

    //    public void visit(LiteralNode node, SymbolTable st) {}

    public void visit(ParameterNode node, SymbolTable st) throws ParseException, UndeclaredVariable {
        // TODO a parameter can be certain types but not all? (Parameter node just can be a Base Type, array, record, but not procedure )
        if (node.type instanceof TypeNode.Base.Identifier) {
            try {
                st.get(node.identifier);
            } catch (ParseException e) {
                throw new UndeclaredVariable(node.identifier.name, node.type.toString());
            }
        }
    }

    //    public void visit(PrimaryNode node, SymbolTable st) {}

    public void visit(ProcedureNode node, SymbolTable st) throws ParseException, SemanticException {
        ArrayList<SType> params = new ArrayList<>();
        for (ParameterNode p : node.params)
            params.add(SType.getSType(p.type, st));
        st.add(node.identifier, new SType.Function(SType.getSType(node.returnType, st), params));
    }

    //    public void visit(ProgramNode node, SymbolTable st) {}

    public void visit(RecordAccessNode node, SymbolTable st) {
        // TODO check fields
    }

    public void visit(RecordDeclarationNode node, SymbolTable st) throws ParseException, SemanticException {
        ArrayList<SType.Record.RecField> fields = new ArrayList<>();

        for (FieldDeclarationNode fdn : node.fields)
            fields.add(new SType.Record.RecField(fdn.identifier.name, SType.getSType(fdn.type, st)));

        st.add(node.identifier, new SType.Record(fields));
    }

    public void visit(ReturnNode node, SymbolTable st) throws SemanticException, ParseException {
        SType returnType = st.getReturnType();
        if (returnType instanceof SType.Void) {
            if (node.returned != null)
                throw new SemanticException("Can't return in void procedure",
                        "This procedure is supposed to return nothing because it's void (not: " + node.returned + ").");
        } else {
            SType actualReturnType = SemanticAnalyzer.getType(node.returned, st);

            if (!returnType.equals(actualReturnType))
                throw new SemanticException("Bad return type",
                        "You should return a " + returnType + " but actually returned " + actualReturnType + ".");
        }
    }

    //    public void visit(TypeNode node, SymbolTable st) {}

    public void visit(WhileNode node, SymbolTable st) throws ParseException, SemanticException {
        SType type = SemanticAnalyzer.getType(node.condition, st);
        if (!type.equals(new SType.Bool()) && !type.equals(new SType.Int()) && !type.equals(new SType.Real())) {
            throw new SemanticException("Wrong while condition type.",
                    "The type used as condition should be a bool (not: " + type + ").");
        }
    }
}
