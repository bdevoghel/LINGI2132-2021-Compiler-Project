package interpreter;

import AST.*;
import scopes.DeclarationKind;
import scopes.ClassScope;
import scopes.Scope;
import scopes.SyntheticDeclarationNode;
import types.DoubleType;
import types.IntType;
import types.StringType;
import types.Type;
import norswap.uranium.Reactor;
import norswap.utils.Util;
import norswap.utils.exceptions.Exceptions;
import norswap.utils.exceptions.NoStackException;
import norswap.utils.visitors.ValuedVisitor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static norswap.utils.Util.cast;
import static norswap.utils.Vanilla.coIterate;
import static norswap.utils.Vanilla.map;

/**
 * Implements a simple but inefficient interpreter for Kneghel.
 *
 * <p>Runtime value representation:
 * <ul>
 *     <li>{@code Int}, {@code Float}, {@code Bool}: {@link Long}, {@link Double}, {@link Boolean}</li>
 *     <li>{@code String}: {@link String}</li>
 *     <li>{@code null}: {@link Null#INSTANCE}</li>
 *     <li>Arrays: {@code Object[]}</li>
 *     <li>Structs: {@code HashMap<String, Object>}</li>
 *     <li>Functions: the corresponding {@link DeclarationNode} ({@link FunDeclarationNode} or
 *     {@link SyntheticDeclarationNode}), excepted structure constructors, which are
 *     represented by {@link Constructor}</li>
 *     <li>Types: the corresponding {@link StructDeclarationNode}</li>
 * </ul>
 */
public final class Interpreter
{
    // ---------------------------------------------------------------------------------------------

    private final ValuedVisitor<ASTNode, Object> visitor = new ValuedVisitor<>();
    private final Reactor reactor;
    private ScopeStorage storage = null;
    private ClassScope rootScope;
    private ScopeStorage rootStorage;

    private StringNode[] args;

    // ---------------------------------------------------------------------------------------------

    public Interpreter (Reactor reactor, String[] args) {
        this.reactor = reactor;
        this.args = map(args, new StringNode[0], (string) -> new StringNode(null, string));

        // expressions
        visitor.register(IntegerNode.class,           this::intLiteral);
        visitor.register(DoubleNode.class,         this::doubleLiteral);
        visitor.register(StringNode.class,        this::stringLiteral);
        visitor.register(IdentifierNode.class,            this::identifier);
//        visitor.register(ConstructorNode.class,          this::constructor);
//        visitor.register(ArrayLiteralNode.class,         this::arrayLiteral);
//        visitor.register(ParenthesizedNode.class,        this::parenthesized);
//        visitor.register(FieldAccessNode.class,          this::fieldAccess);
//        visitor.register(ArrayAccessNode.class,          this::arrayAccess);
//        visitor.register(FunCallNode.class,              this::funCall);
//        visitor.register(UnaryExpressionNode.class,      this::unaryExpression);
//        visitor.register(BinaryExpressionNode.class,     this::binaryExpression);
//        visitor.register(AssignmentNode.class,           this::assignment);
//
        // statement groups & declarations
        visitor.register(ClassStatementNode.class,                 this::root);
//        visitor.register(BlockNode.class,                this::block);
//        visitor.register(VarDeclarationNode.class,       this::varDecl);
//        // no need to visitor other declarations! (use fallback)
//
//        // statements
//        visitor.register(ExpressionStatementNode.class,  this::expressionStmt);
//        visitor.register(IfNode.class,                   this::ifStmt);
//        visitor.register(WhileNode.class,                this::whileStmt);
//        visitor.register(ReturnNode.class,               this::returnStmt);
//
        visitor.registerFallback(node -> null);
    }

    // ---------------------------------------------------------------------------------------------

    public Object interpret (ASTNode root) {
        try {
            return run(root);
        } catch (PassthroughException e) {
            throw Exceptions.runtime(e.getCause());
        }
    }

    // ---------------------------------------------------------------------------------------------

    private Object run (ASTNode node) {
        try {
            return visitor.apply(node);
        } catch (InterpreterException | Return | PassthroughException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new InterpreterException("exception while executing " + node, e);
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Used to implement the control flow of the return statement.
     */
    private static class Return extends NoStackException {
        final Object value;
        private Return (Object value) {
            this.value = value;
        }
    }

    // ---------------------------------------------------------------------------------------------

    private <T> T get(ASTNode node) {
        return cast(run(node));
    }

    // ---------------------------------------------------------------------------------------------

    private int intLiteral (IntegerNode node) {
        return node.value;
    }

    private Double doubleLiteral (DoubleNode node) {
        return node.value;
    }

    private String stringLiteral (StringNode node) {
        return node.value;
    }

    // ---------------------------------------------------------------------------------------------

//    private Object parenthesized (ParenthesizedNode node) {
//        return get(node.expression);
//    }
//
//    // ---------------------------------------------------------------------------------------------
//
//    private Object[] arrayLiteral (ArrayLiteralNode node) {
//        return map(node.components, new Object[0], visitor);
//    }
//
//    // ---------------------------------------------------------------------------------------------
//
//    private Object binaryExpression (BinaryExpressionNode node)
//    {
//        Type leftType  = reactor.get(node.left, "type");
//        Type rightType = reactor.get(node.right, "type");
//
//        // Cases where both operands should not be evaluated.
//        switch (node.operator) {
//            case OR:  return booleanOp(node, false);
//            case AND: return booleanOp(node, true);
//        }
//
//        Object left  = get(node.left);
//        Object right = get(node.right);
//
//        if (node.operator == BinaryOperator.ADD
//                && (leftType instanceof StringType || rightType instanceof StringType))
//            return convertToString(left) + convertToString(right);
//
//        boolean floating = leftType instanceof FloatType || rightType instanceof FloatType;
//        boolean numeric  = floating || leftType instanceof IntType;
//
//        if (numeric)
//            return numericOp(node, floating, (Number) left, (Number) right);
//
//        switch (node.operator) {
//            case EQUALITY:
//                return  leftType.isPrimitive() ? left.equals(right) : left == right;
//            case NOT_EQUALS:
//                return  leftType.isPrimitive() ? !left.equals(right) : left != right;
//        }
//
//        throw new Error("should not reach here");
//    }
//
//    // ---------------------------------------------------------------------------------------------
//
//    private boolean booleanOp (BinaryExpressionNode node, boolean isAnd)
//    {
//        boolean left = get(node.left);
//        return isAnd
//                ? left && (boolean) get(node.right)
//                : left || (boolean) get(node.right);
//    }
//
//    // ---------------------------------------------------------------------------------------------
//
//    private Object numericOp
//            (BinaryExpressionNode node, boolean floating, Number left, Number right)
//    {
//        long ileft, iright;
//        double fleft, fright;
//
//        if (floating) {
//            fleft  = left.doubleValue();
//            fright = right.doubleValue();
//            ileft = iright = 0;
//        } else {
//            ileft  = left.longValue();
//            iright = right.longValue();
//            fleft = fright = 0;
//        }
//
//        Object result;
//        if (floating)
//            switch (node.operator) {
//                case MULTIPLY:      return fleft *  fright;
//                case DIVIDE:        return fleft /  fright;
//                case REMAINDER:     return fleft %  fright;
//                case ADD:           return fleft +  fright;
//                case SUBTRACT:      return fleft -  fright;
//                case GREATER:       return fleft >  fright;
//                case LOWER:         return fleft <  fright;
//                case GREATER_EQUAL: return fleft >= fright;
//                case LOWER_EQUAL:   return fleft <= fright;
//                case EQUALITY:      return fleft == fright;
//                case NOT_EQUALS:    return fleft != fright;
//                default:
//                    throw new Error("should not reach here");
//            }
//        else
//            switch (node.operator) {
//                case MULTIPLY:      return ileft *  iright;
//                case DIVIDE:        return ileft /  iright;
//                case REMAINDER:     return ileft %  iright;
//                case ADD:           return ileft +  iright;
//                case SUBTRACT:      return ileft -  iright;
//                case GREATER:       return ileft >  iright;
//                case LOWER:         return ileft <  iright;
//                case GREATER_EQUAL: return ileft >= iright;
//                case LOWER_EQUAL:   return ileft <= iright;
//                case EQUALITY:      return ileft == iright;
//                case NOT_EQUALS:    return ileft != iright;
//                default:
//                    throw new Error("should not reach here");
//            }
//    }
//
//    // ---------------------------------------------------------------------------------------------
//
//    public Object assignment (AssignmentNode node)
//    {
//        if (node.left instanceof ReferenceNode) {
//            Scope scope = reactor.get(node.left, "scope");
//            String name = ((ReferenceNode) node.left).name;
//            Object rvalue = get(node.right);
//            assign(scope, name, rvalue, reactor.get(node, "type"));
//            return rvalue;
//        }
//
//        if (node.left instanceof ArrayAccessNode) {
//            ArrayAccessNode arrayAccess = (ArrayAccessNode) node.left;
//            Object[] array = getNonNullArray(arrayAccess.array);
//            int index = getIndex(arrayAccess.index);
//            try {
//                return array[index] = get(node.right);
//            } catch (ArrayIndexOutOfBoundsException e) {
//                throw new PassthroughException(e);
//            }
//        }
//
//        if (node.left instanceof FieldAccessNode) {
//            FieldAccessNode fieldAccess = (FieldAccessNode) node.left;
//            Object object = get(fieldAccess.stem);
//            if (object == Null.INSTANCE)
//                throw new PassthroughException(
//                        new NullPointerException("accessing field of null object"));
//            Map<String, Object> struct = cast(object);
//            Object right = get(node.right);
//            struct.put(fieldAccess.fieldName, right);
//            return right;
//        }
//
//        throw new Error("should not reach here");
//    }
//
//    // ---------------------------------------------------------------------------------------------
//
//    private int getIndex (ExpressionNode node)
//    {
//        long index = get(node);
//        if (index < 0)
//            throw new ArrayIndexOutOfBoundsException("Negative index: " + index);
//        if (index >= Integer.MAX_VALUE - 1)
//            throw new ArrayIndexOutOfBoundsException("Index exceeds max array index (2ˆ31 - 2): " + index);
//        return (int) index;
//    }
//
//    // ---------------------------------------------------------------------------------------------
//
//    private Object[] getNonNullArray (ExpressionNode node)
//    {
//        Object object = get(node);
//        if (object == Null.INSTANCE)
//            throw new PassthroughException(new NullPointerException("indexing null array"));
//        return (Object[]) object;
//    }
//
//    // ---------------------------------------------------------------------------------------------
//
//    private Object unaryExpression (UnaryExpressionNode node)
//    {
//        // there is only NOT
//        assert node.operator == UnaryOperator.NOT;
//        return ! (boolean) get(node.operand);
//    }
//
//    // ---------------------------------------------------------------------------------------------
//
//    private Object arrayAccess (ArrayAccessNode node)
//    {
//        Object[] array = getNonNullArray(node.array);
//        try {
//            return array[getIndex(node.index)];
//        } catch (ArrayIndexOutOfBoundsException e) {
//            throw new PassthroughException(e);
//        }
//    }
//
    // ---------------------------------------------------------------------------------------------

    private Object root (ClassStatementNode node)
    {
        assert storage == null;
        rootScope = reactor.get(node, "scope");
        storage = rootStorage = new ScopeStorage(rootScope, null);
        storage.initRoot(rootScope);

        for (FunctionStatementNode fsn:node.functions) {
            if (fsn.identifier.value == "main") {
                storage.set(rootScope, fsn.identifier.value, fsn.identifier.value);
                Object decl = get(fsn.identifier);
                Object[] args = map(this.args, new Object[0], visitor);

                if (decl == Null.INSTANCE)
                    throw new PassthroughException(new NullPointerException("calling a null function"));

                if (decl instanceof SyntheticDeclarationNode) {
                    SyntheticDeclarationNode declNode = (SyntheticDeclarationNode) decl;
                    return builtin(declNode.name(), args);
                }

                Scope scope = reactor.get(decl, "scope");
                storage = new ScopeStorage(scope, storage);

                FunctionStatementNode funDecl = (FunctionStatementNode) decl;

                coIterate(args, funDecl.arguments,
                        (arg, param) -> storage.set(scope, param.param.value, arg));

                try {
                    funDecl.statements.forEach(this::get);
                } catch (Return r) {
                    return r.value;
                } finally {
                    storage = null;
                }
            } else { // not main
                // TODO ??
            }
        }
        return null;
    }

//    // ---------------------------------------------------------------------------------------------
//
//    private Void block (BlockNode node) {
//        Scope scope = reactor.get(node, "scope");
//        storage = new ScopeStorage(scope, storage);
//        node.statements.forEach(this::run);
//        storage = storage.parent;
//        return null;
//    }
//
//    // ---------------------------------------------------------------------------------------------
//
//    private Constructor constructor (ConstructorNode node) {
//        // guaranteed safe by semantic analysis
//        return new Constructor(get(node.ref));
//    }
//
//    // ---------------------------------------------------------------------------------------------
//
//    private Object expressionStmt (ExpressionStatementNode node) {
//        get(node.expression);
//        return null;  // discard value
//    }
//
//    // ---------------------------------------------------------------------------------------------
//
//    private Object fieldAccess (FieldAccessNode node)
//    {
//        Object stem = get(node.stem);
//        if (stem == Null.INSTANCE)
//            throw new PassthroughException(
//                    new NullPointerException("accessing field of null object"));
//        return stem instanceof Map
//                ? Util.<Map<String, Object>>cast(stem).get(node.fieldName)
//                : (long) ((Object[]) stem).length; // only field on arrays
//    }
//
//    // ---------------------------------------------------------------------------------------------
//
//    private Object funCall (FunCallNode node)
//    {
//        Object decl = get(node.function);
//        node.arguments.forEach(this::run);
//        Object[] args = map(node.arguments, new Object[0], visitor);
//
//        if (decl == Null.INSTANCE)
//            throw new PassthroughException(new NullPointerException("calling a null function"));
//
//        if (decl instanceof SyntheticDeclarationNode)
//            return builtin(((SyntheticDeclarationNode) decl).name(), args);
//
//        if (decl instanceof Constructor)
//            return buildStruct(((Constructor) decl).declaration, args);
//
//        ScopeStorage oldStorage = storage;
//        Scope scope = reactor.get(decl, "scope");
//        storage = new ScopeStorage(scope, storage);
//
//        FunDeclarationNode funDecl = (FunDeclarationNode) decl;
//        coIterate(args, funDecl.parameters,
//                (arg, param) -> storage.set(scope, param.name, arg));
//
//        try {
//            get(funDecl.block);
//        } catch (Return r) {
//            return r.value;
//        } finally {
//            storage = oldStorage;
//        }
//        return null;
//    }
//
    // ---------------------------------------------------------------------------------------------

    private Object builtin (String name, Object[] args)
    {
        assert name.equals("print"); // TODO only one at the moment
        String out = convertToString(args[0]);
        System.out.println(out);
        return out;
    }

    // ---------------------------------------------------------------------------------------------

    private String convertToString (Object arg)
    {
        if (arg == Null.INSTANCE)
            return "null";
        else if (arg instanceof Object[])
            return Arrays.deepToString((Object[]) arg);
        // TODO
//        else if (arg instanceof FunDeclarationNode)
//            return ((FunDeclarationNode) arg).name;
//        else if (arg instanceof StructDeclarationNode)
//            return ((StructDeclarationNode) arg).name;
//        else if (arg instanceof Constructor)
//            return "$" + ((Constructor) arg).declaration.name;
        else
            return arg.toString();
    }

//    // ---------------------------------------------------------------------------------------------
//
//    private HashMap<String, Object> buildStruct (StructDeclarationNode node, Object[] args)
//    {
//        HashMap<String, Object> struct = new HashMap<>();
//        for (int i = 0; i < node.fields.size(); ++i)
//            struct.put(node.fields.get(i).name, args[i]);
//        return struct;
//    }
//
//    // ---------------------------------------------------------------------------------------------
//
//    private Void ifStmt (IfNode node)
//    {
//        if (get(node.condition))
//            get(node.trueStatement);
//        else if (node.falseStatement != null)
//            get(node.falseStatement);
//        return null;
//    }
//
//    // ---------------------------------------------------------------------------------------------
//
//    private Void whileStmt (WhileNode node)
//    {
//        while (get(node.condition))
//            get(node.body);
//        return null;
//    }
//
    // ---------------------------------------------------------------------------------------------

    private Object identifier (IdentifierNode node)
    {
        Scope scope = reactor.get(node, "scope");
        DeclarationNode decl = reactor.get(node, "decl");

        if (decl instanceof AssignmentNode
                || decl instanceof ClassStatementNode
                || decl instanceof FunctionStatementNode
                || decl instanceof FunctionParameterNode
                || decl instanceof FunctionArgumentsNode
                || decl instanceof SyntheticDeclarationNode
                && ((SyntheticDeclarationNode) decl).kind() == DeclarationKind.VARIABLE)
            return scope == rootScope
                    ? rootStorage.get(scope, node.value)
                    : storage.get(scope, node.value);

        return decl; // structure or function
    }

//    // ---------------------------------------------------------------------------------------------
//
//    private Void returnStmt (ReturnNode node) {
//        throw new Return(node.expression == null ? null : get(node.expression));
//    }
//
//    // ---------------------------------------------------------------------------------------------
//
//    private Void varDecl (VarDeclarationNode node)
//    {
//        Scope scope = reactor.get(node, "scope");
//        assign(scope, node.name, get(node.initializer), reactor.get(node, "type"));
//        return null;
//    }
//
//    // ---------------------------------------------------------------------------------------------
//
//    private void assign (Scope scope, String name, Object value, Type targetType)
//    {
//        if (value instanceof Long && targetType instanceof FloatType)
//            value = ((Long) value).doubleValue();
//        storage.set(scope, name, value);
//    }
//
//    // ---------------------------------------------------------------------------------------------
}
