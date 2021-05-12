/*
 * Our code is largely inspired by the implementation of the semantic analysis made by the Professor N. Laurent for his language Sigh.
 * https://github.com/norswap/sigh/blob/775535d6aa6c85dd8b1881718e5e2a34efc6d5dd/src/norswap/sigh/SemanticAnalysis.java
 */

package semantic;
import AST.*;
import norswap.uranium.Attribute;
import norswap.uranium.Reactor;
import norswap.uranium.Rule;
import norswap.uranium.SemanticError;
import norswap.utils.visitors.ReflectiveFieldWalker;
import norswap.utils.visitors.Walker;
import scopes.*;
import types.*;

import java.util.List;

import static java.lang.String.format;
import static norswap.utils.Util.cast;
import static norswap.utils.Vanilla.forEachIndexed;


import static AST.BinaryOperator.*;
import static norswap.utils.visitors.WalkVisitType.POST_VISIT;
import static norswap.utils.visitors.WalkVisitType.PRE_VISIT;

public final class SemanticAnalysis {

    private final Reactor R;

    /** Current scope. */
    private Scope scope;

    /** Current context for type inference (currently only to infer the type of empty arrays). */
    private ASTNode inferenceContext;

    /** Index of the current function argument. */
    private int argumentIndex;

    // ---------------------------------------------------------------------------------------------

    private SemanticAnalysis(Reactor reactor) {
        this.R = reactor;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Call this method to create a tree walker that will instantiate the typing rules defined
     * in this class when used on an AST, using the given {@code reactor}.
     */
    public static Walker<ASTNode> createWalker(Reactor reactor) {
        ReflectiveFieldWalker<ASTNode> walker = new ReflectiveFieldWalker<>(
                ASTNode.class, PRE_VISIT, POST_VISIT);

        SemanticAnalysis analysis = new SemanticAnalysis(reactor);

        //Expression
        walker.register(IntegerNode.class,              PRE_VISIT, analysis::integer);
        walker.register(DoubleNode.class,               PRE_VISIT, analysis::doub);
        walker.register(BooleanNode.class,              PRE_VISIT, analysis::bool);
        walker.register(StringNode.class,               PRE_VISIT, analysis::string);
        walker.register(IdentifierNode.class,           PRE_VISIT, analysis::identifier);

        walker.register(UnaryExpressionNode.class,      PRE_VISIT, analysis::unaryExpression);
        walker.register(BinaryExpressionNode.class,     PRE_VISIT, analysis::binaryExpression);

        walker.register(ClassStatementNode.class,       PRE_VISIT, analysis::classStatement);
        walker.register(FunctionStatementNode.class,    PRE_VISIT, analysis::functionStatement);
        walker.register(FunctionParameterNode.class,    PRE_VISIT, analysis::functionParameter);
        walker.register(FunctionArgumentsNode.class,    PRE_VISIT, analysis::functionArguments);
        walker.register(FunctionCallNode.class,         PRE_VISIT, analysis::functionCall);

        walker.register(ArrayMapAccessNode.class,       PRE_VISIT, analysis::arrayMapAccess);
        walker.register(AssignmentNode.class,           PRE_VISIT, analysis::assignment);
        walker.register(IfStatementNode.class,          PRE_VISIT, analysis::ifStatement);
        walker.register(WhileStatementNode.class,       PRE_VISIT, analysis::whileStatement);
//        walker.register(ReturnStatementNode.class,     PRE_VISIT, analysis::??); //TODO do we actually need it?

        walker.register(ClassStatementNode.class,       POST_VISIT, analysis::popScope);
        walker.register(FunctionStatementNode.class,    POST_VISIT, analysis::popScope);
        walker.register(IfStatementNode.class,          POST_VISIT, analysis::popScope);
        walker.register(WhileStatementNode.class,       POST_VISIT, analysis::popScope);
        walker.register(AssignmentNode.class,           POST_VISIT, analysis::remove_FromScope);

        // Fallback rules
        walker.registerFallback(PRE_VISIT, node -> {});
        walker.registerFallback(POST_VISIT, node -> {});


        return walker;
    }

    private static boolean isComparableTo (Type a, Type b) {
        return a.isReference() && b.isReference()
                || a.equals(b)
                || a instanceof UnknownType
                || b instanceof UnknownType;
    }

    private static String arithmeticError (BinaryExpressionNode node, Object left, Object right) {
        return format("Trying to %s %s with %s", node.operator.name().toLowerCase(), left, right);
    }

    /**
     * Indicates whether a value of type {@code a} can be assigned to a location (variable,
     * parameter, ...) of type {@code b}.
     */
    private static boolean isAssignableTo (Type a, Type b)
    {
//        if (a instanceof VoidType || b instanceof VoidType) // TODO to add ??
//            return false;
        if (a instanceof UnknownType)
            return true;

        if (a instanceof IntType && b instanceof DoubleType)
            return true;

        if (a instanceof ArrayType)
            return b instanceof ArrayType
                    && isAssignableTo(((ArrayType)a).componentType, ((ArrayType)b).componentType);

        return a instanceof NullType && b.isReference() || a.equals(b);
    }

    private void integer (IntegerNode node) {
        R.set(node, "type", IntType.INSTANCE);
    }

    private void doub (DoubleNode node) {
        R.set(node, "type", DoubleType.INSTANCE);
    }

    private void bool (BooleanNode node) {
        R.set(node, "type", BoolType.INSTANCE);
    }

    private void string (StringNode node) {
        R.set(node, "type", StringType.INSTANCE);
    }

    private void identifier (IdentifierNode node) {
        final Scope scope = this.scope;

        // TODO take context in scope into account
        DeclarationContext maybeCtx = scope.lookup(node.getValue());

        // Try to lookup immediately. This must succeed for variables, but not necessarily for functions or types.
        if (maybeCtx != null) {
            R.set(node, "decl", maybeCtx.declaration);
            R.set(node, "scope", maybeCtx.scope);
            R.rule(node, "type")
                    .using(maybeCtx.declaration, "type")
                    .by(Rule::copyFirst);
            return;
        }

        // Re-lookup after the scopes have been built.
        R.rule(node.attr("decl"), node.attr("scope"))
                .by(r -> {
                    DeclarationContext ctx = scope.lookup(node.getValue());
                    DeclarationNode decl = ctx == null ? null : ctx.declaration;

                    if (ctx == null) {
                        r.errorFor("Could not resolve: " + node.getValue(),
                                node, node.attr("decl"), node.attr("scope"), node.attr("type"));
                    }
                    else {
                        r.set(node, "scope", ctx.scope);
                        r.set(node, "decl", decl);

                        if (decl instanceof DeclarationNode)
                            r.errorFor("Variable used before declaration: " + node.getValue(),
                                    node, node.attr("type"));
                        else
                            R.rule(node, "type")
                                    .using(decl, "type")
                                    .by(Rule::copyFirst);
                    }
                });
    }

    private void binaryExpression (BinaryExpressionNode node) {
    // TODO !! allow for fancy arithmetic with string etc
        R.rule(node, "type")
                .using(node.leftChild.attr("type"), node.rightChild.attr("type"))
                .by(r -> {
                    Type left  = r.get(0);
                    Type right = r.get(1);

                    if (node.operator == ADD || node.operator == SUBTRACT || node.operator == MULTIPLY || node.operator == DIVIDE || node.operator == MODULO){
                        binaryArithmetic(r, node, left, right);}
                    else if (node.operator == EQUAL || node.operator == NOT_EQUAL){
                        binaryEquality(r, node, left, right);}
                    else if (node.operator == GREATER_THAN || node.operator == GREATER_OR_EQUAL || node.operator == LESS_OR_EQUAL || node.operator == LESS_THAN){
                        binaryComparison(r, node, left, right);
                    } else if (node.operator == AND || node.operator == OR) {
                        boolBinaryArithmetic(r, node, left, right);
                    }
                });
    }

    private void boolBinaryArithmetic (Rule r, BinaryExpressionNode node, Type left, Type right) {
        if (!(left instanceof BoolType || right instanceof BoolType || right instanceof UnknownType)) {
            r.error(arithmeticError(node, left, right), node);
        } else {
            r.set(0, BoolType.INSTANCE);
        }
    }

    private void binaryArithmetic (Rule r, BinaryExpressionNode node, Type left, Type right) {
        if (left instanceof BoolType || left instanceof StringType || right instanceof BoolType || right instanceof StringType) {
            r.error(arithmeticError(node, left, right), node);
        } else if (left instanceof IntType) {
            if (right instanceof IntType) {
                r.set(0, IntType.INSTANCE);
            } else if (right instanceof DoubleType) {
                r.set(0, DoubleType.INSTANCE);
            } else
                r.error(arithmeticError(node, "Int", right), node);
        } else if (left instanceof DoubleType) {
            if (right instanceof IntType || right instanceof DoubleType) {
                r.set(0, DoubleType.INSTANCE);
            } else
                r.error(arithmeticError(node, "Double", right), node);
        } else if (left instanceof UnknownType) {
            r.set(0, UnknownType.INSTANCE);
        } else
            r.error(arithmeticError(node, left, right), node);
    }

    private void binaryEquality (Rule r, BinaryExpressionNode node, Type left, Type right) {
        if (!isComparableTo(left, right)) {
            r.errorFor(format("Trying to compare incomparable types %s and %s", left, right),
                    node);
        } else {
            r.set(0, BoolType.INSTANCE);
        }
    }

    private void binaryComparison (Rule r, BinaryExpressionNode node, Type left, Type right) {
        r.set(0, BoolType.INSTANCE);

        if (!(left instanceof IntType || left instanceof DoubleType || left instanceof UnknownType))
            r.errorFor("Attempting to perform arithmetic comparison on non-numeric type: " + left,
                    node.leftChild);
        if (!(right instanceof IntType || right instanceof DoubleType || right instanceof UnknownType))
            r.errorFor("Attempting to perform arithmetic comparison on non-numeric type: " + right,
                    node.rightChild);
    }

    private void unaryExpression(UnaryExpressionNode node) {
        if (node.operator == UnaryOperator.NEG) {
            if (node.operand instanceof IntegerNode){
                R.set(node, "type", IntType.INSTANCE);

                R.rule()
                        .using(node.operand, "type")
                        .by(r -> {
                            Type opType = r.get(0);
                            if (!(opType instanceof IntType))
                                r.error("Trying to negate type: " + opType, node);
                        });
            }
            else if (node.operand instanceof DoubleNode){
                R.set(node, "type", DoubleType.INSTANCE);

                R.rule()
                        .using(node.operand, "type")
                        .by(r -> {
                            Type opType = r.get(0);
                            if (!(opType instanceof DoubleType))
                                r.error("Trying to negate type: " + opType, node);
                        });
            }


        }
        else if (node.operator == UnaryOperator.NOT){
            R.set(node, "type", BoolType.INSTANCE);

            R.rule()
                .using(node.operand, "type")
                .by(r -> {
                    Type opType = r.get(0);
                    if (!(opType instanceof BoolType))
                        r.error("Trying to negate type: " + opType, node);
                });
        }

    }

    private void arrayMapAccess(ArrayMapAccessNode node) {
        R.rule()
                .using(node.index, "type")
                .by(r -> {
                    Type type = r.get(0);
                    if (!(type instanceof IntType || type instanceof UnknownType))
                        r.error("Indexing an array using a non-int-valued expression.", node.index);
                });

        R.rule(node, "type")
                .using(node.arrayMap, "type")
                .by(r -> {
                    Type type = r.get(0);
                    if (type instanceof ArrayType)
                        r.set(0, ((ArrayType) type).componentType);
                    else if (type instanceof UnknownType)
                        r.set(0, UnknownType.INSTANCE);
                    else
                        r.error("Trying to index a non-array expression of type " + type, node);
                });
    }

    private void assignment(AssignmentNode node) {
        if (node.variable instanceof IdentifierNode) {
            DeclarationContext maybeCtx = scope.lookup(((IdentifierNode) node.variable).getValue());

            if (maybeCtx == null) {
                scope.declare(((IdentifierNode) node.variable).getValue(), node);
                R.set(node, "scope", scope);
                R.rule(node, "type")
                        .using(node.value.attr("type"))
                        .by(Rule::copyFirst);
                return;
            }
        }

        R.rule(node, "type")
                .using(node.variable.attr("type"), node.value.attr("type"))
                .by(r -> {
                    Type var  = r.get(0);
                    Type val = r.get(1);

                    r.set(0, r.get(1)); // the type of the assignment is the right-side type

                    if (node.variable instanceof IdentifierNode
                            ||  node.variable instanceof ArrayMapAccessNode) {
                        if (!isAssignableTo(var, val))
                            r.errorFor("Trying to assign a value to a non-compatible lvalue.", node);
                    }
                    else
                        r.errorFor("Trying to assign to an non-lvalue expression.", node.variable);
                });
    }

    private void ifStatement(IfStatementNode node) {
        scope = new Scope(node, scope);
        R.set(node, "scope", scope);

        R.rule()
                .using(node.condition, "type")
                .by(r -> {
                    Type type = r.get(0);
                    if (!(type instanceof BoolType || type instanceof UnknownType)) {
                        r.error("If statement with a non-boolean condition of type: " + type,
                                node.condition);
                    }
                });
    }

    private void whileStatement(WhileStatementNode node) {
        scope = new Scope(node, scope);
        R.set(node, "scope", scope);

        R.rule()
                .using(node.condition, "type")
                .by(r -> {
                    Type type = r.get(0);
                    if (!(type instanceof BoolType)) {
                        r.error("While statement with a non-boolean condition of type: " + type,
                                node.condition);
                    }
                });

        Attribute[] dependencies = new Attribute[node.statements.size()];
        forEachIndexed(node.statements, (i, param) ->
                dependencies[i] = param.attr("type"));

        R.rule(node, "type")
                .using(dependencies)
                .by(r -> {
                    Type[] paramTypes = new Type[dependencies.length];
                    for (int i = 0; i < paramTypes.length; ++i)
                        paramTypes[i] = r.get(i);
                    r.set(0, new FunType(paramTypes));
                });

    }

    private void functionStatement(FunctionStatementNode node) {
//        if (scope.lookup(node.identifier.getValue()) == null) {
            scope.declare(node.identifier.getValue(), node);
//        } else {
//            R.error(new SemanticError("Function already implemented, cannot have same name.", null, node.identifier));
//        }
        scope = new Scope(node, scope);
        R.set(node, "scope", scope);

        Attribute[] dependencies = new Attribute[node.arguments.size()];
        forEachIndexed(node.arguments, (i, param) ->
                dependencies[i] = param.attr("type"));

        R.rule(node, "type")
                .using(dependencies)
                .by(r -> {
                    Type[] paramTypes = new Type[dependencies.length];
                    for (int i = 0; i < paramTypes.length; ++i)
                        paramTypes[i] = r.get(i);
                    r.set(0, new FunType(paramTypes));
                });
    }

    public void functionParameter(FunctionParameterNode node) {
        scope.declare(node.param.value, node);
        R.set(node, "type", UnknownType.INSTANCE);
        R.set(node, "scope", scope);
    }

    private void functionArguments(FunctionArgumentsNode node) {
        for (ExpressionNode e: node.elements) {
            if (e instanceof IdentifierNode) {
                scope.declare(((IdentifierNode) e).getValue(), node); // scope pushed by FunDeclarationNode
            }
            else if (e instanceof IntegerNode) {
                scope.declare(String.valueOf(((IntegerNode) e).value), node); // scope pushed by FunDeclarationNode
            }
            R.rule(node, "type")
                    .using(e, "type")
                    .by(Rule::copyFirst);
        }
    }

    private void functionCall(FunctionCallNode node) {
        final Scope scope = this.scope;

        R.rule(node, "type")
                .using(node.function.attr("type"))
                .by(r -> {
                    r.set(0, "type");

                    DeclarationContext maybeCtx = scope.lookup(node.function.getValue());

                    List<FunctionParameterNode> params = ((FunctionStatementNode) maybeCtx.declaration).arguments;
                    List<ExpressionNode> args = node.arguments.elements;

                    if (params.size() != args.size())
                        r.errorFor("Wrong number of arguments, expected "+params.size()+" but got "+args.size(),
                                node);
                });
    }

    private void classStatement(ClassStatementNode node) {
        // Class is root of Kneghel
        assert scope == null;
        scope = new ClassScope(node, R);
        scope.declare(node.identifier.getValue(), node);

        scope.declare("args",
                new AssignmentNode(node.span,
                        new IdentifierNode(node.span, "args"),
                        new StringNode(node.span, node.span.toString())));

        R.set(node, "scope", scope);


        Attribute[] dependencies = new Attribute[node.functions.size()];
        forEachIndexed(node.functions, (i, fun) ->
                dependencies[i] = fun.attr("type"));

        R.rule(node, "type")
                .using(dependencies)
                .by(r -> {
                    Type[] funTypes = new Type[node.functions.size()];
                    for (int i = 0; i < funTypes.length; ++i)
                        funTypes[i] = r.get(i);
                    r.set(0, new ClassType(funTypes));
                });
    }

    private void popScope (ASTNode node) {
        scope = scope.parent;
    }

    private void remove_FromScope (AssignmentNode node) {
        if ( node.variable instanceof IdentifierNode && ((IdentifierNode) node.variable).value.equals("_") )
            scope.declarations.remove("_");
    }

}
