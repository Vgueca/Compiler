package compiler.SemanticAnalyzer;

import compiler.Nodes.TypeNode;
import compiler.Visitors.PrintVisitor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Objects;

public abstract class SType {
    public abstract java.lang.String getDescriptor();

    public abstract java.lang.String toString();

    public static class Int extends SType {
        public Int() {
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof SType.Int;
        }

        @Override
        public java.lang.String getDescriptor() {
            return "I";
        }

        @Override
        public java.lang.String toString() {
            return "int_t";
        }
    }

    public static class Real extends SType {
        public Real() {
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof SType.Real;
        }

        @Override
        public java.lang.String getDescriptor() {
            return "D";
        }

        @Override
        public java.lang.String toString() {
            return "real_t";
        }
    }

    public static class String extends SType {
        public String() {
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof SType.String;
        }

        @Override
        public java.lang.String getDescriptor() {
            return "Ljava/lang/String;";
        }

        @Override
        public java.lang.String toString() {
            return "string_t";
        }
    }

    public static class Bool extends SType {
        public Bool() {
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof SType.Bool;
        }

        @Override
        public java.lang.String getDescriptor() {
            return "Z";
        }

        @Override
        public java.lang.String toString() {
            return "bool_t";
        }
    }

    public static class Void extends SType {
        public Void() {
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof SType.Void;
        }

        @Override
        public java.lang.String getDescriptor() {
            return "V";
        }

        @Override
        public java.lang.String toString() {
            return "void_t";
        }
    }

    public static class Id extends SType {
        java.lang.String identifier;

        public Id(java.lang.String identifier) {
            this.identifier = identifier;
        }

        //TODO return correct checking of both types (getting types from identifier)

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Id id = (Id) o;
            return Objects.equals(identifier, id.identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier);
        }

        @Override
        public java.lang.String getDescriptor() {
            return null; // TODO should be implemented or not?
        }

        @Override
        public java.lang.String toString() {
            return identifier + "_id_t";
        }
    }

    public static class Array extends SType {
        SType type;

        public Array(SType type) {
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Array array = (Array) o;
            return Objects.equals(type, array.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type);
        }

        @Override
        public java.lang.String getDescriptor() {
            return "[" + type.getDescriptor();
        }

        @Override
        public java.lang.String toString() {
            return type + "[]";
        }
    }

    public static class Function extends SType {
        public SType returnType;
        public ArrayList<SType> params;

        public Function(SType returnType, ArrayList<SType> params) {
            this.returnType = returnType;
            this.params = params;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Function function = (Function) o;
            return Objects.equals(returnType, function.returnType) && Objects.equals(params, function.params);
        }

        @Override
        public int hashCode() {
            return Objects.hash(returnType, params);
        }

        @Override
        public java.lang.String getDescriptor() {
            return null; // TODO should be implemented or not?
        }

        @Override
        public java.lang.String toString() {
            return params + " -> " + returnType;
        }
    }

    public static class Record extends SType {
        public ArrayList<RecField> fields;

        public Record(ArrayList<RecField> fields) {
            this.fields = fields;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Record record = (Record) o;
            return Objects.equals(fields, record.fields);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fields);
        }

        @Override
        public java.lang.String getDescriptor() {
            return null; // TODO should be implemented or not?
        }

        @Override
        public java.lang.String toString() {
            StringBuilder ret = new StringBuilder("{");
            for (int i = 0; i < fields.size(); i++) {
                if (i > 0)
                    ret.append(", ");
                ret.append(fields.get(i));
            }
            return ret + "}";
        }

        public static class RecField {
            public java.lang.String id;
            public SType type;

            public RecField(java.lang.String id, SType type) {
                this.id = id;
                this.type = type;
            }

            @Override
            public java.lang.String toString() {
                return "<" + id + ": " + type + ">";
            }
        }
    }

    /**
     * Convert a TypeNode.Base to a SType.(Int | Real | String | Bool).
     * @param baseType a base type.
     * @return a new SType matching the baseType.
     */
    public static SType getSType(TypeNode.Base baseType) {
        return switch (baseType.token) {
            case INTTYPE -> new Int();
            case REALTYPE -> new Real();
            case STRINGTYPE -> new String();
            case BOOLTYPE -> new Bool();
            default -> throw new IllegalStateException("Unexpected value: " + baseType.token);
        };
    }

    public static SType getSType(TypeNode.Array array) {
        return switch (array.baseType.token) {
            case INTTYPE -> new Array(new Int());
            case REALTYPE -> new Array(new Real());
            case STRINGTYPE -> new Array(new String());
            case BOOLTYPE -> new Array(new Bool());
            default -> throw new IllegalStateException("Unexpected value: " + array.baseType.token);
        };
    }

    public static SType getSType(TypeNode tn, SymbolTable st) throws ParseException {
        if (tn instanceof TypeNode.Base)
            return getSType((TypeNode.Base) tn);
        else if (tn instanceof TypeNode.Void)
            return new Void();
        else if (tn instanceof TypeNode.Array)
            return getSType((TypeNode.Array) tn);
        else if (tn instanceof TypeNode.Identifier id_t)
            return st.get(id_t.identifier);

        tn.accept(new PrintVisitor(), 0);
        throw new ParseException("No matching type.", 0);
    }
}
