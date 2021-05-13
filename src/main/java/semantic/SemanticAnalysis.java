package semantic;

import ast.*;
import norswap.uranium.SemanticError;
import types.Type;
import scopes.*;
import static ast.BinaryOperator.*;
import static types.Type.*;

import norswap.uranium.Attribute;
import norswap.uranium.Reactor;
import norswap.uranium.Rule;
import norswap.utils.visitors.ReflectiveFieldWalker;
import norswap.utils.visitors.Walker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import static java.lang.String.format;

import static norswap.utils.Util.cast;
import static norswap.utils.Vanilla.forEachIndexed;
import static norswap.utils.Vanilla.list;
import static norswap.utils.visitors.WalkVisitType.POST_VISIT;
import static norswap.utils.visitors.WalkVisitType.PRE_VISIT;


// TODO read and correct
/**
 * Holds the logic implementing semantic analyzis for the language, including typing and name
 * resolution.
 *
 * <p>The entry point into this class is {@link #createWalker(Reactor)}.
 *
 * <h2>Big Principles
 * <ul>
 *     <li>Every {@link DeclarationNode} instance must have its {@code type} attribute to an
 *     instance of {@link Type} which is the type of the value declared (note that for struct
 *     declaration, this is always {@link TypeType}.</li>
 *
 *     <li>Additionally, {@link StructDeclarationNode} (and default
 *     {@link SyntheticDeclarationNode} for types) must have their {@code declared} attribute set to
 *     an instance of the type being declared.</li>
 *
 *     <li>Every {@link ExpressionNode} instance must have its {@code type} attribute similarly
 *     set.</li>
 *
 *     <li>Every {@link ReferenceNode} instance must have its {@code decl} attribute set to the the
 *     declaration it references and its {@code scope} attribute set to the {@link Scope} in which
 *     the declaration it references lives. This speeds up lookups in the interpreter and simplifies the compiler.</li>
 *
 *     <li>For the same reasons, {@link VarDeclarationNode} and {@link ParameterNode} should have
 *     their {@code scope} attribute set to the scope in which they appear (this also speeds up the
 *     interpreter).</li>
 *
 *     <li>All statements introducing a new scope must have their {@code scope} attribute set to the
 *     corresponding {@link Scope} (only {@link RootNode}, {@link BlockNode} and {@link
 *     FunDeclarationNode} (for parameters)). These nodes must also update the {@code scope}
 *     field to track the current scope during the walk.</li>
 *
 *     <li>Every {@link TypeNode} instance must have its {@code value} set to the {@link Type} it
 *     denotes.</li>
 *
 *     <li>Every {@link ReturnNode}, {@link BlockNode} and {@link IfNode} must have its {@code
 *     returns} attribute set to a boolean to indicate whether its execution causes
 *     unconditional exit from the surrounding function or main script.</li>
 *
 *     <li>The rules check typing constraints: assignment of values to variables, of arguments to
 *     parameters, checking that if/while conditions are booleans, and array indices are
 *     integers.</li>
 *
 *     <li>The rules also check a number of other constraints: that accessed struct fields exist,
 *     that variables are declared before being used, etc...</li>
 * </ul>
 */
public final class SemanticAnalysis
{
    // =============================================================================================
    // region [Initialization]
    // =============================================================================================

    private final Reactor R;

    /** Current scope. */
    private Scope scope;
    private ArrayList<String> args;

    // ---------------------------------------------------------------------------------------------

    private SemanticAnalysis(Reactor reactor) {
        this.R = reactor;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Call this method to create a tree walker that will instantiate the typing rules defined
     * in this class when used on an AST, using the given {@code reactor}.
     */
    public static Walker<KneghelNode> createWalker (Reactor reactor) {
        return createWalker(reactor, null);
    }

    public static Walker<KneghelNode> createWalker (Reactor reactor, ArrayList<String> args)
    {
        ReflectiveFieldWalker<KneghelNode> walker = new ReflectiveFieldWalker<>(
                KneghelNode.class, PRE_VISIT, POST_VISIT);

        SemanticAnalysis analysis = new SemanticAnalysis(reactor);
        analysis.args = args;

        // expressions
        walker.register(IntLiteralNode.class,           PRE_VISIT,  analysis::intLiteral);
        walker.register(FloatLiteralNode.class,         PRE_VISIT,  analysis::floatLiteral);
        walker.register(BoolLiteralNode.class,          PRE_VISIT,  analysis::boolLiteral);
        walker.register(NullLiteralNode.class,          PRE_VISIT,  analysis::nullLiteral);
        walker.register(StringLiteralNode.class,        PRE_VISIT,  analysis::stringLiteral);
        walker.register(ReferenceNode.class,            PRE_VISIT,  analysis::reference);
        walker.register(ArrayLiteralNode.class,         PRE_VISIT,  analysis::arrayLiteral);
        walker.register(ParenthesizedNode.class,        PRE_VISIT,  analysis::parenthesized);
        walker.register(ArrayAccessNode.class,          PRE_VISIT,  analysis::arrayAccess);
        walker.register(FunCallNode.class,              PRE_VISIT,  analysis::funCall);
        walker.register(UnaryExpressionNode.class,      PRE_VISIT,  analysis::unaryExpression);
        walker.register(BinaryExpressionNode.class,     PRE_VISIT,  analysis::binaryExpression);
        walker.register(AssignmentNode.class,           PRE_VISIT,  analysis::assignment);

        // declarations & scopes
        walker.register(ClassNode.class,                PRE_VISIT,  analysis::root);
        walker.register(BlockNode.class,                PRE_VISIT,  analysis::block);
        walker.register(ParameterNode.class,            PRE_VISIT,  analysis::parameter);
        walker.register(FunDeclarationNode.class,       PRE_VISIT,  analysis::funDecl);

        walker.register(ClassNode.class,                POST_VISIT, analysis::popScope);
        walker.register(BlockNode.class,                POST_VISIT, analysis::popScope);
        walker.register(FunDeclarationNode.class,       POST_VISIT, analysis::popScope);

        // statements
        walker.register(ExpressionStatementNode.class,  PRE_VISIT,  node -> {});
        walker.register(IfNode.class,                   PRE_VISIT,  analysis::ifStmt);
        walker.register(WhileNode.class,                PRE_VISIT,  analysis::whileStmt);
        walker.register(ReturnNode.class,               PRE_VISIT,  analysis::returnStmt);

        walker.registerFallback(POST_VISIT, node -> {});

        return walker;
    }

    // endregion
    // =============================================================================================
    // region [Expressions]
    // =============================================================================================

    private void intLiteral (IntLiteralNode node) {
        R.set(node, "type", INTEGER);
    }

    // ---------------------------------------------------------------------------------------------

    private void floatLiteral (FloatLiteralNode node) {
        R.set(node, "type", FLOAT);
    }

    // ---------------------------------------------------------------------------------------------

    private void boolLiteral (BoolLiteralNode node) {
        R.set(node, "type", BOOLEAN);
    }

    // ---------------------------------------------------------------------------------------------

    private void nullLiteral (NullLiteralNode node) {
        R.set(node, "type", NULL);
    }

    // ---------------------------------------------------------------------------------------------

    private void stringLiteral (StringLiteralNode node) {
        R.set(node, "type", STRING);
    }

    // ---------------------------------------------------------------------------------------------

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

            R.set(node, "type", UNKNOWN_TYPE);
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

                        r.errorFor("Variable used before declaration: " + node.name,
                                node, node.attr("type"));
                    }
                });
    }

    // ---------------------------------------------------------------------------------------------

    private void arrayLiteral (ArrayLiteralNode node)
    {
        Attribute[] dependencies =
                node.components.stream().map(it -> it.attr("type")).toArray(Attribute[]::new);

        R.rule(node, "type")
                .using(dependencies)
                .by(r -> {
                    Type[] types = IntStream.range(0, dependencies.length).<Type>mapToObj(r::get)
                            .distinct().toArray(Type[]::new);
                    for (Type type: types) {
                        if (!(type == INTEGER || type == UNKNOWN_TYPE)) {
                            r.error("Indexing an array using a non-Int-valued expression", node);
                        }
                    }
                    r.set(node, "type", INTEGER);
                });
    }

    // ---------------------------------------------------------------------------------------------

    private void parenthesized (ParenthesizedNode node)
    {
        R.rule(node, "type")
                .using(node.expression, "type")
                .by(Rule::copyFirst);
    }

    // ---------------------------------------------------------------------------------------------

    private void arrayAccess (ArrayAccessNode node)
    {
        R.rule()
                .using(node.index, "type")
                .by(r -> {
                    Type type = r.get(0);
                    if (!(type == INTEGER || type == UNKNOWN_TYPE))
                        r.error("Indexing an array using a non-Int-valued expression", node.index);
                });

        R.rule(node, "type")
                .using(node.array, "type")
                .by(r -> {
                    Type type = r.get(0);
                    if (type == ARRAY || type == UNKNOWN_TYPE)
                        r.set(node, "type", UNKNOWN_TYPE);
                    else
                        r.error("Trying to index a non-array expression of type " + type, node);
                });
    }

    // ---------------------------------------------------------------------------------------------

    private void funCall (FunCallNode node)
    {
        final Scope scope = this.scope;

        R.rule(node, "type")
                .using()
                .by(r -> {
                    r.set(0, UNKNOWN_TYPE);
                });
    }

    // ---------------------------------------------------------------------------------------------

    private void unaryExpression (UnaryExpressionNode node)
    {
        switch (node.operator) {
            case NOT:
                R.set(node, "type", BOOLEAN);
                R.rule()
                        .using(node.operand, "type")
                        .by(r -> {
                            Type opType = r.get(0);
                            if (!(opType == BOOLEAN || opType == UNKNOWN_TYPE))
                                r.error("Trying to negate a non-boolean type: " + opType, node);
                        });
                break;
            case NEG:
                R.rule(node.attr("type"))
                        .using(node.operand.attr("type"))
                        .by(r -> {
                            Type opType = r.get(0);
                            if (opType == INTEGER)
                                r.set(node, "type", INTEGER);
                            else if (opType == FLOAT)
                                r.set(node, "type", FLOAT);
                            else if (opType == UNKNOWN_TYPE)
                                r.set(node, "type", UNKNOWN_TYPE);
                            else
                                r.error("Trying to negate a non-number type: " + opType, node);
                        });
                break;
            default:
                break;
        }
    }

    // endregion
    // =============================================================================================
    // region [Binary Expressions]
    // =============================================================================================

    private void binaryExpression (BinaryExpressionNode node)
    {
        R.rule(node, "type")
                .using(node.left.attr("type"), node.right.attr("type"))
                .by(r -> {
                    Type left  = r.get(0);
                    Type right = r.get(1);

                    // TODO adding strings
//                    if (node.operator == ADD && (left instanceof StringType || right instanceof StringType))
//                        r.set(0, StringType.INSTANCE);
                    /*else*/ if (isArithmetic(node.operator))
                        binaryArithmetic(r, node, left, right);
                    else if (isComparison(node.operator))
                        binaryComparison(r, node, left, right);
                    else if (isLogic(node.operator))
                        binaryLogic(r, node, left, right);
                    else if (isEquality(node.operator))
                        binaryEquality(r, node, left, right);
                });
    }

    // ---------------------------------------------------------------------------------------------

    private boolean isArithmetic (BinaryOperator op) {
        return op == ADD || op == MULTIPLY || op == SUBTRACT || op == DIVIDE || op == REMAINDER;
    }

    private boolean isComparison (BinaryOperator op) {
        return op == GREATER || op == GREATER_EQUAL || op == LOWER || op == LOWER_EQUAL;
    }

    private boolean isLogic (BinaryOperator op) {
        return op == OR || op == AND;
    }

    private boolean isEquality (BinaryOperator op) {
        return op == EQUALITY || op == NOT_EQUALS;
    }

    // ---------------------------------------------------------------------------------------------

    private void binaryArithmetic (Rule r, BinaryExpressionNode node, Type left, Type right)
    {
        if (right == BOOLEAN || left == BOOLEAN)
            r.errorFor("Trying to perform arithmetic operation on boolean", node);

        if (node.operator == MULTIPLY && (right == STRING || left == STRING))
            r.errorFor("Trying to perform a multiplication on a string", node);

        if (left == UNKNOWN_TYPE)
            r.set(0, right);
        else if (right == UNKNOWN_TYPE)
            r.set(0, left);
        else if (left == INTEGER)
            if (right == INTEGER)
                r.set(0, INTEGER);
            else if (right == FLOAT)
                r.set(0, FLOAT);
            else
                r.error(arithmeticError(node, "Int", right), node);
        else if (left == FLOAT)
            if (right == INTEGER || right == FLOAT)
                r.set(0, FLOAT);
            else
                r.error(arithmeticError(node, "Float", right), node);
        else
            r.error(arithmeticError(node, left, right), node);
    }

    // ---------------------------------------------------------------------------------------------

    private static String arithmeticError (BinaryExpressionNode node, Object left, Object right) {
        return format("Trying to %s %s with %s", node.operator.name().toLowerCase(), left, right);
    }

    // ---------------------------------------------------------------------------------------------

    private void binaryComparison (Rule r, BinaryExpressionNode node, Type left, Type right)
    {
        r.set(0, BOOLEAN);

        if (!isValidBinaryComparison(left, right))
            r.errorFor("Attempting to perform arithmetic comparison on invalid types",node);
    }

    // ---------------------------------------------------------------------------------------------

    private void binaryEquality (Rule r, BinaryExpressionNode node, Type left, Type right)
    {
        r.set(0, BOOLEAN);

        if (!isComparableTo(left, right))
            r.errorFor(format("Trying to compare incomparable types %s and %s", left, right),
                    node);
    }

    // ---------------------------------------------------------------------------------------------

    private void binaryLogic (Rule r, BinaryExpressionNode node, Type left, Type right)
    {
        r.set(0, BOOLEAN);

        if (!(left == BOOLEAN || left == UNKNOWN_TYPE))
            r.errorFor("Attempting to perform binary logic on non-boolean type: " + left,
                    node.left);
        if (!(right == BOOLEAN || right == UNKNOWN_TYPE))
            r.errorFor("Attempting to perform binary logic on non-boolean type: " + right,
                    node.right);
    }

    // ---------------------------------------------------------------------------------------------

    private void assignment (AssignmentNode node)
    {
        if (scope.lookup(node.name()) == null) {
            scope.declare(node.name(), node);
            R.set(node, "scope", scope);
        }

        R.rule(node, "type")
                .using(node.left.attr("type"), node.right.attr("type"))
                .by(r -> {
                    Type left  = r.get(0);
                    Type right = r.get(1);

                    r.set(0, r.get(0)); // the type of the assignment is the left-side type

                    if (node.left instanceof ReferenceNode
                            ||  node.left instanceof ArrayAccessNode) {
                        if (!isAssignableTo(right, left))
                            r.errorFor("Trying to assign a value to a non-compatible lvalue.", node);
                    }
                    else
                        r.errorFor("Trying to assign to an non-lvalue expression.", node.left);
                });
    }

    // endregion
    // =============================================================================================
    // region [Types & Typing Utilities]
    // =============================================================================================

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates whether a value of type {@code right} can be assigned to a location (variable,
     * parameter, ...) of type {@code left}.
     */
    private static boolean isAssignableTo (Type right, Type left)
    {
        // TODO to determine what may be accepted
//        if (right instanceof VoidType || left instanceof VoidType)
//            return false;
//
//        if (right == INTEGER && left == FLOAT)
//            return true;
//
//        if (right instanceof ArrayType)
//            return left instanceof ArrayType
//                    && isAssignableTo(((ArrayType)right).componentType, ((ArrayType)left).componentType);
//
        return right == NULL /*&& left.isReference()*/
                || right == UNKNOWN_TYPE
                || left == UNKNOWN_TYPE
                || right.equals(left);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicate whether the two types are comparable.
     */
    private static boolean isValidBinaryComparison (Type a, Type b)
    {
        return (a.equals(b) && a != BOOLEAN)
                || a == INTEGER && b == FLOAT
                || a == FLOAT && b == INTEGER
                || a == UNKNOWN_TYPE && b != BOOLEAN
                || b == UNKNOWN_TYPE && a != BOOLEAN;
    }
    private static boolean isComparableTo (Type a, Type b)
    {
        // TODO to determine what may be accepted

        return a.equals(b)
                || a == INTEGER && b == FLOAT
                || a == FLOAT && b == INTEGER
                || a == BOOLEAN && b == INTEGER
                || a == INTEGER && b == BOOLEAN
                || a == UNKNOWN_TYPE || b == UNKNOWN_TYPE;
    }

    // endregion
    // =============================================================================================
    // region [Scopes & Declarations]
    // =============================================================================================

    private void popScope (KneghelNode node) {
        scope = scope.parent;
    }

    // ---------------------------------------------------------------------------------------------

    private void root (ClassNode node) {
        assert scope == null;
        scope = new RootScope(node, R);
        R.set(node, "scope", scope);
    }

    // ---------------------------------------------------------------------------------------------

    private void block (BlockNode node) {
        scope = new Scope(node, scope);
        R.set(node, "scope", scope);

        Attribute[] deps = getReturnsDependencies(node.statements);
        R.rule(node, "returns")
                .using(deps)
                .by(r -> r.set(0, deps.length != 0 && Arrays.stream(deps).anyMatch(r::get)));
    }

    // ---------------------------------------------------------------------------------------------

    private void parameter (ParameterNode node)
    {
        R.set(node, "scope", scope);
        scope.declare(node.name, node); // scope pushed by FunDeclarationNode

        R.set(node, "type", UNKNOWN_TYPE);
    }

    // ---------------------------------------------------------------------------------------------

    private void funDecl (FunDeclarationNode node)
    {
        if (scope.lookup(node.name) == null) {
            scope.declare(node.name, node);
        } else {
            R.error(new SemanticError("Function already implemented, cannot have same name.", null, node.name));
        }
        scope = new Scope(node, scope);
        R.set(node, "scope", scope);
        R.set(node, "type", UNKNOWN_TYPE);
    }

    // endregion
    // =============================================================================================
    // region [Other Statements]
    // =============================================================================================

    private void ifStmt (IfNode node) {
        R.rule()
                .using(node.condition, "type")
                .by(r -> {
                    Type type = r.get(0);
                    if (!(type == BOOLEAN || type == UNKNOWN_TYPE)) {
                        r.error("If statement with a non-boolean condition of type: " + type,
                                node.condition);
                    }
                });

        Attribute[] deps = getReturnsDependencies(list(node.trueStatement, node.falseStatement));
        R.rule(node, "returns")
                .using(deps)
                .by(r -> r.set(0, deps.length == 2 && Arrays.stream(deps).allMatch(r::get)));
    }

    // ---------------------------------------------------------------------------------------------

    private void whileStmt (WhileNode node) {
        R.rule()
                .using(node.condition, "type")
                .by(r -> {
                    Type type = r.get(0);
                    if (!(type == BOOLEAN)) {
                        r.error("While statement with a non-boolean condition of type: " + type,
                                node.condition);
                    }
                });
    }

    // ---------------------------------------------------------------------------------------------

    private void returnStmt (ReturnNode node)
    {
        R.set(node, "returns", true);

        FunDeclarationNode function = currentFunction();
        if (function == null) // top-level return
            return;

        if (node.expression == null) {
            R.set(node, "value", null);
        } else {
            R.set(node, "value", UNKNOWN_TYPE);
        }
    }

    // ---------------------------------------------------------------------------------------------

    private FunDeclarationNode currentFunction()
    {
        Scope scope = this.scope;
        while (scope != null) {
            KneghelNode node = scope.node;
            if (node instanceof FunDeclarationNode)
                return (FunDeclarationNode) node;
            scope = scope.parent;
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private boolean isReturnContainer (KneghelNode node) {
        return node instanceof BlockNode
                || node instanceof IfNode
                || node instanceof ReturnNode;
    }

    // ---------------------------------------------------------------------------------------------

    /** Get the depedencies necessary to compute the "returns" attribute of the parent. */
    private Attribute[] getReturnsDependencies (List<? extends KneghelNode> children) {
        return children.stream()
                .filter(Objects::nonNull)
                .filter(this::isReturnContainer)
                .map(it -> it.attr("returns"))
                .toArray(Attribute[]::new);
    }

    // endregion
    // =============================================================================================
}
