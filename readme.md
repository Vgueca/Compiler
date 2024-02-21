Compiler implementation project for a custom language (see [code_example.lang](code_example.lang)).

## Project structure

```text
LINFO2132-compiler-project
├───src/main/java/compiler
│   ├───CodeGenerator           > [4th part] produces bytecode from the AST runnable with JVM
│   ├───Exceptions              > custom exceptions
│   │   └───SemanticException   > semantic exceptions
│   ├───Lexer                   > [1st part] reads text input and creates a stream of tokens
│   │   └───Tokens              > more complex tokens than simple strings
│   ├───Nodes                   > all nodes used to build the AST
│   ├───Parser                  > [2nd part] gets every token and creates an AST using Nodes
│   ├───SemanticAnalyzer        > [3rd part] traverses the AST to check for semantic errors
│   └───Visitors                > visitors of the AST
└───test                        > tests for the 4 parts of the compiler
```

## Backus-Naur form of the custom language

### Program

```go
<program> ::= <const>* <record-declaration>* <val-var>* <procedure>*
```

### Declarations

```go
<const> ::= "const" <IDENTIFIER> <base-type> "=" <expression> ";"
<value> ::= "val" <IDENTIFIER> <base-type> "=" <expression> ";"
<variable> ::= "var" <IDENTIFIER> <type> "=" <expression> ";"
<val-var> ::= <value> | <variable>
<record-declaration> ::= "record" <IDENTIFIER> "{" <field-declaration>* "}"
<field-declaration> ::= <IDENTIFIER> <type> ";"
```

### Types

```go
<type> ::= <base-type> | <IDENTIFIER> | <array-type>
<base-type> ::= "int" | "real" | "bool" | "string"
<array-type> ::= <base-type> "[]"
```

### Expressions

```go
<expression> ::= <bool-term> ( ( "or" | "and" ) <expression> )?
<bool-term> ::= <bool-factor> ( ( "<" | "<=" | ">" | ">=" | "==" | "<>" ) <bool-term> )?
<bool-factor> ::= <arith-term> ( ( "+" | "-" ) <bool-factor> )?
<arith-term> ::= <arith-factor> ( ( "*" | "/" | "%" ) <arith-term> )?
<arith-factor> ::= ( "+" | "-" )? <primary>
<primary> ::= <literal> | <IDENTIFIER> | <function-call> | "(" <expression> ")" | <array-init> | <array-access> | <record-access>
```

### Primaries

```go
<literal> ::= <INT> | <REAL> | <BOOL> | <STRING>
<function-call> ::= <IDENTIFIER> "(" (<expression> ("," <expression>)*)? ")"
<array-init> ::= <array-type> "(" <expression> ")"
<array-access> ::= <IDENTIFIER> "[" <expression> "]"
<record-access> ::= (<IDENTIFIER> | <array-access>) "." <IDENTIFIER>
```

### Procedures

```go
<procedure> ::= "proc" <IDENTIFIER> "(" (<parameter> ("," <parameter>)*)? ")" <return-type> <block>
<parameter> ::= <IDENTIFIER> <type>
<return-type> ::= <type> | "void"
<block> ::= "{" <statement>* "}"
<statement> ::= <val-var> | <if> | <while> | <for> | <assignment> | <function-call> | <return> | <delete>
```

### Statements

```go
<if> ::= "if" <expression> <block> ("else" <block>)?
<while> ::= "while" <expression> <block>
<for> ::= "for" <IDENTIFIER> "=" <INT> "to" <INT> ("by" <INT>)? <block>
<assignment> ::= (<IDENTIFIER> | <array-access> | <record-access>) "=" <expression> ";"
<return> ::= "return" (<expression>)? ";"
<delete> ::= "delete" <IDENTIFIER> ";"
```
