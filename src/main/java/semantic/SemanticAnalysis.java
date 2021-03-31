/*
 * In part inspired by https://github.com/norswap/sigh/blob/775535d6aa6c85dd8b1881718e5e2a34efc6d5dd/src/norswap/sigh/SemanticAnalysis.java
 */

package semantic;
import AST.*;
import norswap.uranium.Attribute;
import norswap.uranium.Reactor;
import norswap.uranium.Rule;
import norswap.utils.visitors.ReflectiveFieldWalker;
import norswap.utils.visitors.Walker;
import scopes.*;
import types.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static norswap.utils.Vanilla.list;
import static norswap.utils.Util.cast;

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

//        walker.register(ArrayMapAccessNode.class,     PRE_VISIT, analysis::??);
//        walker.register(AssignmentNode.class,         PRE_VISIT, analysis::??);
//        walker.register(ClassStatementNode.class,     PRE_VISIT, analysis::??);
//        walker.register(FunctionArgumentsNode.class,     PRE_VISIT, analysis::??);
//        walker.register(FunctionCallNode.class,     PRE_VISIT, analysis::??);
//        walker.register(FunctionStatementNode.class,     PRE_VISIT, analysis::??);
//        walker.register(IfStatementNode.class,     PRE_VISIT, analysis::??);
//        walker.register(ReturnStatementNode.class,     PRE_VISIT, analysis::??);
//        walker.register(RootNode.class,     PRE_VISIT, analysis::??); // TODO needed ?

        // Fallback rules
        walker.registerFallback(PRE_VISIT, node -> {});
        walker.registerFallback(POST_VISIT, node -> {});


        return walker;
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

        DeclarationContext maybeCtx = scope.lookup(node.getValue());

        if (maybeCtx != null) {
            R.set(node, "decl", maybeCtx.declaration);
            R.set(node, "scope", maybeCtx.scope);

            R.rule(node, "type").using(maybeCtx.declaration, "type")
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

    private void binaryExpression (BinaryExpressionNode node)
    {
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
                    }
                });
    }

    //TODO accept double : do we accept operation between doubles and integers?
    private void binaryArithmetic (Rule r, BinaryExpressionNode node, Type left, Type right)
    {
        if (left instanceof IntType)
            if (right instanceof IntType)
                r.set(0, IntType.INSTANCE);
            else
                r.error(arithmeticError(node, "Int", right), node);
        else
            r.error(arithmeticError(node, left, right), node);
    }

    private static String arithmeticError (BinaryExpressionNode node, Object left, Object right) {
        return format("Trying to %s %s with %s", node.operator.name().toLowerCase(), left, right);
    }

    private void binaryEquality (Rule r, BinaryExpressionNode node, Type left, Type right)
    {
        r.set(0, BoolType.INSTANCE);

        if (!isComparableTo(left, right))
            r.errorFor(format("Trying to compare incomparable types %s and %s", left, right),
                    node);
    }

    private static boolean isComparableTo (Type a, Type b)
    {
        return a.isReference() && b.isReference()
                || a.equals(b);
    }

    private void binaryComparison (Rule r, BinaryExpressionNode node, Type left, Type right)
    {
        r.set(0, BoolType.INSTANCE);

        if (!(left instanceof IntType) && !(left instanceof DoubleType))
            r.errorFor("Attempting to perform arithmetic comparison on non-numeric type: " + left,
                    node.leftChild);
        if (!(right instanceof IntType) && !(right instanceof DoubleType))
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

    /**private void simpleType (SimpleTypeNode node)
    {
        final Scope scope = this.scope;

        R.rule()
                .by(r -> {
                    // type declarations may occur after use
                    DeclarationContext ctx = scope.lookup(node.name);
                    DeclarationNode decl = ctx == null ? null : ctx.declaration;

                    if (ctx == null)
                        r.errorFor("could not resolve: " + node.name,
                                node,
                                node.attr("value"));

                    else if (!isTypeDecl(decl))
                        r.errorFor(format(
                                "%s did not resolve to a type declaration but to a %s declaration",
                                node.name, decl.declaredThing()),
                                node,
                                node.attr("value"));

                    else
                        R.rule(node, "value")
                                .using(decl, "declared")
                                .by(Rule::copyFirst);
                });
    }
    private static boolean isTypeDecl (DeclarationNode decl)
    {
        //if (decl instanceof StructDeclarationNode) return true;
        if (!(decl instanceof SyntheticDeclarationNode)) return false;
        SyntheticDeclarationNode synthetic = cast(decl);
        return synthetic.kind() == DeclarationKind.TYPE;
    }

    private void popScope (ASTNode node) {
        scope = scope.parent;
    }

    private void root (RootNode node) {
        assert scope == null;
        scope = new RootScope(node, R);
        R.set(node, "scope", scope);
    }

    private void intLiteral (IntLiteralNode node) {
        R.set(node, "type", IntType.INSTANCE);
    }

    private void reference (ReferenceNode node)
    {
        final Scope scope = this.scope;

        // Try to lookup immediately. This must succeed for variables, but not necessarily for
        // functions or types. By looking up now, we can report looked up variables later
        // as being used before being defined.
        DeclarationContext maybeCtx = scope.lookup(node.name);

        if (maybeCtx != null) {
            R.set(node, "decl",  maybeCtx.declaration);
            R.set(node, "scope", maybeCtx.scope);

            R.rule(node, "type")
                    .using(maybeCtx.declaration, "type")
                    .by(Rule::copyFirst);
            return;
        }

        // Re-lookup after the scopes have been built.
        R.rule(node.attr("decl"), node.attr("scope"))
                .by(r -> {
                    DeclarationContext ctx = scope.lookup(node.name);
                    DeclarationNode decl = ctx == null ? null : ctx.declaration;

                    if (ctx == null) {
                        r.errorFor("Could not resolve: " + node.name,
                                node, node.attr("decl"), node.attr("scope"), node.attr("type"));
                    }
                    else {
                        r.set(node, "scope", ctx.scope);
                        r.set(node, "decl", decl);

                        if (decl instanceof VarDeclarationNode)
                            r.errorFor("Variable used before declaration: " + node.name,
                                    node, node.attr("type"));
                        else
                            R.rule(node, "type")
                                    .using(decl, "type")
                                    .by(Rule::copyFirst);
                    }
                });
    }

    private void parenthesized (ParenthesizedNode node)
    {
        R.rule(node, "type")
                .using(node.expression, "type")
                .by(Rule::copyFirst);
    }

    private void binaryExpression (BinaryExpressionNode node)
    {
        R.rule(node, "type")
                .using(node.left.attr("type"), node.right.attr("type"))
                .by(r -> {
                    Type left  = r.get(0);
                    Type right = r.get(1);

                    if (node.operator == ADD){
                        binaryArithmetic(r, node, left, right);}
                    else if (node.operator == EQUALITY){
                        binaryEquality(r, node, left, right);}
                });
    }

    private void binaryArithmetic (Rule r, BinaryExpressionNode node, Type left, Type right)
    {
        if (left instanceof IntType)
            if (right instanceof IntType)
                r.set(0, IntType.INSTANCE);
            else
                r.error(arithmeticError(node, "Int", right), node);
        else
            r.error(arithmeticError(node, left, right), node);
    }

    private static String arithmeticError (BinaryExpressionNode node, Object left, Object right) {
        return format("Trying to %s %s with %s", node.operator.name().toLowerCase(), left, right);
    }

    private void binaryEquality (Rule r, BinaryExpressionNode node, Type left, Type right)
    {
        r.set(0, BoolType.INSTANCE);

        if (!isComparableTo(left, right))
            r.errorFor(format("Trying to compare incomparable types %s and %s", left, right),
                    node);
    }

    private static boolean isComparableTo (Type a, Type b)
    {
        return a.isReference() && b.isReference()
                || a.equals(b);
    }

    private void ifStmt (IfNode node) {
        R.rule()
                .using(node.condition, "type")
                .by(r -> {
                    Type type = r.get(0);
                    if (!(type instanceof BoolType)) {
                        r.error("If statement with a non-boolean condition of type: " + type,
                                node.condition);
                    }
                });

        Attribute[] deps = getReturnsDependencies(list(node.trueStatement, node.falseStatement));
        R.rule(node, "returns")
                .using(deps)
                .by(r -> r.set(0, deps.length == 2 && Arrays.stream(deps).allMatch(r::get)));
    }

    private boolean isReturnContainer (SighNode node) {
        return node instanceof IfNode;
    }

     Get the depedencies necessary to compute the "returns" attribute of the parent.
    private Attribute[] getReturnsDependencies (List<? extends SighNode> children) {
        return children.stream()
                .filter(Objects::nonNull)
                .filter(this::isReturnContainer)
                .map(it -> it.attr("returns"))
                .toArray(Attribute[]::new);
    }
    **/

}
