package interpreter;

import ast.*;
import scopes.DeclarationKind;
import scopes.RootScope;
import scopes.Scope;
import scopes.SyntheticDeclarationNode;
import types.Type;
import static types.Type.*;

import norswap.uranium.Reactor;
import norswap.utils.Util;
import norswap.utils.exceptions.Exceptions;
import norswap.utils.exceptions.NoStackException;
import norswap.utils.visitors.ValuedVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static norswap.utils.Util.cast;
import static norswap.utils.Vanilla.coIterate;
import static norswap.utils.Vanilla.map;


// TODO reread and correct
/**
 * Implements a simple interpreter for Kneghel.
 *
 * <h2>Limitations</h2>
 * <ul>
 *     <li>The compiled code currently doesn't support closures (using variables in functions that
 *     are declared in some surroudning scopes outside the function). The top scope is supported.
 *     </li>
 * </ul>
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

    private final ValuedVisitor<KneghelNode, Object> visitor = new ValuedVisitor<>();
    private final Reactor reactor;
    private ScopeStorage storage = null;
    private RootScope rootScope;
    private ScopeStorage rootStorage;

    private ArrayList<String> args;

    // ---------------------------------------------------------------------------------------------

    public Interpreter (Reactor reactor) {
        this(reactor, new ArrayList<String>());
    }

    public Interpreter (Reactor reactor, ArrayList<String> args) {
        this.reactor = reactor;
        this.args = args;

        // expressions
        visitor.register(IntLiteralNode.class,           this::intLiteral);
        visitor.register(FloatLiteralNode.class,         this::floatLiteral);
        visitor.register(StringLiteralNode.class,        this::stringLiteral);
        visitor.register(BoolLiteralNode.class,          this::boolLiteral);
        visitor.register(NullLiteralNode.class,          this::nullLiteral);
        visitor.register(ArrayLiteralNode.class,         this::arrayLiteral);
        visitor.register(ReferenceNode.class,            this::reference);
        visitor.register(ParenthesizedNode.class,        this::parenthesized);
        visitor.register(ArrayAccessNode.class,          this::arrayAccess);
        visitor.register(FunCallNode.class,              this::funCall);
        visitor.register(UnaryExpressionNode.class,      this::unaryExpression);
        visitor.register(BinaryExpressionNode.class,     this::binaryExpression);
        visitor.register(AssignmentNode.class,           this::assignment);

        // statement groups & declarations
        visitor.register(ClassNode.class,                this::root);
        visitor.register(BlockNode.class,                this::block);

        // statements
        visitor.register(ExpressionStatementNode.class,  this::expressionStmt);
        visitor.register(IfNode.class,                   this::ifStmt);
        visitor.register(WhileNode.class,                this::whileStmt);
        visitor.register(ReturnNode.class,               this::returnStmt);

        visitor.registerFallback(node -> null);
    }

    // ---------------------------------------------------------------------------------------------

    public Object interpret (KneghelNode root) {
        try {
            return run(root);
        } catch (PassthroughException e) {
            throw Exceptions.runtime(e.getCause());
        }
    }

    // ---------------------------------------------------------------------------------------------

    private Object run (KneghelNode node) {
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

    private <T> T get(KneghelNode node) {
        return cast(run(node));
    }

    // ---------------------------------------------------------------------------------------------

    private boolean isPrimitive(Object o) {
        return o instanceof String || o instanceof Long || o instanceof Boolean;
    }

    // ---------------------------------------------------------------------------------------------

    private Long intLiteral (IntLiteralNode node) {
        return node.value;
    }

    private Double floatLiteral (FloatLiteralNode node) {
        return node.value;
    }

    private String stringLiteral (StringLiteralNode node) {
        return node.value;
    }

    private Boolean boolLiteral (BoolLiteralNode node) { return node.value;}

    private Object nullLiteral (NullLiteralNode node) {return Null.INSTANCE;}

    // ---------------------------------------------------------------------------------------------

    private Object parenthesized (ParenthesizedNode node) {
        return get(node.expression);
    }

    // ---------------------------------------------------------------------------------------------

    private Object[] arrayLiteral (ArrayLiteralNode node) {
        return map(node.components, new Object[0], visitor);
    }

    // ---------------------------------------------------------------------------------------------

    private Object binaryExpression (BinaryExpressionNode node)
    {
        Type leftType  = reactor.get(node.left, "type");
        Type rightType = reactor.get(node.right, "type");

        // Cases where both operands should not be evaluated.
        switch (node.operator) {
            case OR:  return booleanOp(node, false);
            case AND: return booleanOp(node, true);
        }

        Object left  = get(node.left);
        Object right = get(node.right);

        if (node.operator == BinaryOperator.ADD
                && (leftType == STRING || rightType == STRING))
            return convertToString(left) + convertToString(right);

        boolean floating = leftType == FLOAT || rightType == FLOAT;
        boolean numeric  = floating || leftType == INTEGER || (leftType == UNKNOWN_TYPE && rightType != STRING && rightType != BOOLEAN && rightType != NULL);

        if (numeric)
            return numericOp(node, floating, (Number) left, (Number) right);

        switch (node.operator) {
            case EQUALITY:
                return  isPrimitive(leftType) ? left.equals(right) : left == right;
            case NOT_EQUALS:
                return  isPrimitive(leftType) ? !left.equals(right) : left != right;
            default:
                if (node.operator instanceof BinaryOperator)
                    return inequalityComparison(left, right, node.operator);
        }

        throw new Error("should not reach here");
    }

    private Object inequalityComparison(Object leftObject, Object rightObject, BinaryOperator op) {
        int comparison;

        if (leftObject instanceof Long && rightObject instanceof Long) {
            Long left = (Long) leftObject;
            Long right = (Long) rightObject;

            comparison = left.compareTo(right);

        } else if (leftObject instanceof String && rightObject instanceof String) {
            String left = (String) leftObject;
            String right = (String) rightObject;

            comparison = left.compareTo(right);

        } else {
            throw new PassthroughException(new ClassCastException("Cannot do inequality comparisons on " + leftObject + " and " + rightObject));
        }

        switch (op) {
            case LOWER_EQUAL:
                return comparison <= 0;
            case GREATER_EQUAL:
                return comparison >= 0;
            case LOWER:
                return comparison < 0;
            case GREATER:
                return comparison > 0;
        }

        throw new Error("Should not get here");
    }

    // ---------------------------------------------------------------------------------------------

    private boolean booleanOp (BinaryExpressionNode node, boolean isAnd)
    {
        boolean left = get(node.left);
        return isAnd
                ? left && (boolean) get(node.right)
                : left || (boolean) get(node.right);
    }

    // ---------------------------------------------------------------------------------------------

    private Object numericOp
            (BinaryExpressionNode node, boolean floating, Number left, Number right)
    {
        long ileft, iright;
        double fleft, fright;

        if (floating) {
            fleft  = left.doubleValue();
            fright = right.doubleValue();
            ileft = iright = 0;
        } else {
            ileft  = left.longValue();
            iright = right.longValue();
            fleft = fright = 0;
        }

        Object result;
        if (floating)
            switch (node.operator) {
                case MULTIPLY:      return fleft *  fright;
                case DIVIDE:        return fleft /  fright;
                case REMAINDER:     return fleft %  fright;
                case ADD:           return fleft +  fright;
                case SUBTRACT:      return fleft -  fright;
                case GREATER:       return fleft >  fright;
                case LOWER:         return fleft <  fright;
                case GREATER_EQUAL: return fleft >= fright;
                case LOWER_EQUAL:   return fleft <= fright;
                case EQUALITY:      return fleft == fright;
                case NOT_EQUALS:    return fleft != fright;
                default:
                    throw new Error("should not reach here");
            }
        else
            switch (node.operator) {
                case MULTIPLY:      return ileft *  iright;
                case DIVIDE:        return ileft /  iright;
                case REMAINDER:     return ileft %  iright;
                case ADD:           return ileft +  iright;
                case SUBTRACT:      return ileft -  iright;
                case GREATER:       return ileft >  iright;
                case LOWER:         return ileft <  iright;
                case GREATER_EQUAL: return ileft >= iright;
                case LOWER_EQUAL:   return ileft <= iright;
                case EQUALITY:      return ileft == iright;
                case NOT_EQUALS:    return ileft != iright;
                default:
                    throw new Error("should not reach here");
            }
    }

    // ---------------------------------------------------------------------------------------------

    public Object assignment (AssignmentNode node)
    {
        if (node.left instanceof ReferenceNode) {
            Scope scope = reactor.get(node.left, "scope");
            String name = ((ReferenceNode) node.left).name;

            Object rvalue = get(node.right);
            assign(scope, name, rvalue, reactor.get(node, "type"));

            return rvalue;
        }

        if (node.left instanceof ArrayAccessNode) {
            ArrayAccessNode arrayAccess = (ArrayAccessNode) node.left;
            ArrayList<Object> array = getNonNullArray(arrayAccess.array);
            int index = getIndex(arrayAccess.index);
            try {
                int size = array.size();
                if (index >= size) {
                    for(int i=size ; i<=index ; i++)
                        array.add(i, null);
                }
                Object right = get(node.right);
                array.set(index, right);
                return right;
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new PassthroughException(e);
            }
        }
        throw new Error("should not reach here");
    }

    // ---------------------------------------------------------------------------------------------

    private int getIndex (ExpressionNode node)
    {
        long index;
        if (node instanceof ArrayLiteralNode)
            index = (long) ((Object[]) get(node))[0];
        else if (node instanceof IntLiteralNode || node instanceof ReferenceNode)
            index = get(node);
        else
            throw new PassthroughException(new IllegalArgumentException("Wrong type of index: " + node));
        if (index < 0)
            throw new ArrayIndexOutOfBoundsException("Negative index: " + index);
        if (index >= Integer.MAX_VALUE - 1)
            throw new ArrayIndexOutOfBoundsException("Index exceeds max array index (2ˆ31 - 2): " + index);
        return (int) index;
    }

    // ---------------------------------------------------------------------------------------------

    private ArrayList<Object> getNonNullArray (ExpressionNode node)
    {
        Object object = get(node);
        if (object == Null.INSTANCE)
            throw new PassthroughException(new NullPointerException("indexing null array"));
        return (ArrayList<Object>) object;
    }

    // ---------------------------------------------------------------------------------------------

    private Object unaryExpression (UnaryExpressionNode node)
    {
        switch (node.operator) {
            case NOT:
                return ! (boolean) get(node.operand);
            case NEG:
                Type type  = reactor.get(node.operand, "type");
                if (type == INTEGER)
                    return - (int) get(node.operand);
                else if (type == FLOAT)
                    return - (float) get(node.operand);
            default:
                break;
        }
        throw new Error("should not reach here");
    }

    // ---------------------------------------------------------------------------------------------

    private Object arrayAccess (ArrayAccessNode node)
    {
        ArrayList<Object> array = getNonNullArray(node.array);
        try {
            return array.get(getIndex(node.index));
        } catch (IndexOutOfBoundsException e) {
            throw new PassthroughException(e);
        }
    }

    // ---------------------------------------------------------------------------------------------

    private Object root (ClassNode node)
    {
        assert storage == null;
        rootScope = reactor.get(node, "scope");
        storage = rootStorage = new ScopeStorage(rootScope, null);
        storage.initRoot(rootScope, args);

        try {
            node.functions.forEach(func -> {
                if (func.name.equals("main")) {
                    run(func.block);
                }
            });
        } catch (Return r) {
            return r.value;
            // allow returning from the main script
        } finally {
            storage = null;
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Void block (BlockNode node) {
        Scope scope = reactor.get(node, "scope");
        storage = new ScopeStorage(scope, storage);
        node.statements.forEach(this::run);
        storage = storage.parent;
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object expressionStmt (ExpressionStatementNode node) {
        get(node.expression);
        return null;  // discard value
    }

    // ---------------------------------------------------------------------------------------------

    private Object funCall (FunCallNode node)
    {
        Object decl = get(node.function);
        node.arguments.forEach(this::run);
        Object[] args = map(node.arguments, new Object[0], visitor);

        if (decl == Null.INSTANCE)
            throw new PassthroughException(new NullPointerException("calling a null function"));

        if (decl instanceof SyntheticDeclarationNode)
            return builtin(((SyntheticDeclarationNode) decl).name(), args);

        ScopeStorage oldStorage = storage;
        Scope scope = reactor.get(decl, "scope");
        storage = new ScopeStorage(scope, storage);

        FunDeclarationNode funDecl = (FunDeclarationNode) decl;
        coIterate(args, funDecl.parameters,
                (arg, param) -> storage.set(scope, param.name, arg));

        try {
            get(funDecl.block);
        } catch (Return r) {
            return r.value;
        } finally {
            storage = oldStorage;
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object builtin (String name, Object[] args)
    {
        // TODO complete with reserved methods
        switch (name) {
            case "print":
                String out = convertToString(args[0]);
                System.out.println(out);
                return out;
            case "makeArray":
                return new ArrayList<>();
            case "makeDict":
                return new HashMap<>();
            case "dictAdd":
                HashMap dicA = (HashMap) args[0];
                Object keyA = args[1];
                Object val = args[2];
                dicA.put(keyA, val);
                return dicA;
            case "dictGet":
                HashMap dicG = (HashMap) args[0];
                Object keyG = args[1];
                Object dicReturn = dicG.get(keyG);
                if (dicReturn == null) {
                    return Null.INSTANCE;
                }
                return dicReturn;
            case "len":
                if (args[0] instanceof ArrayList)
                    return ((ArrayList<?>) args[0]).size();
                if (args[0] instanceof HashMap)
                    return ((HashMap<?, ?>) args[0]).size();
                if (args[0] instanceof String)
                    return ((String) args[0]).length();
                if (args[0] instanceof Null)
                    throw new PassthroughException(new NullPointerException());
                throw new PassthroughException(new IllegalCallerException("Could not get length of object" + args[0]));
            case "int":
                return Integer.parseInt(convertToString(args[0]));
            default:
                break;
        }
        throw new Error("should not reach here");
    }

    // ---------------------------------------------------------------------------------------------

    private String convertToString (Object arg)
    {
        if (arg == Null.INSTANCE)
            return "null";
        else if (arg instanceof Object[])
            return Arrays.deepToString((Object[]) arg);
        else if (arg instanceof FunDeclarationNode)
            return ((FunDeclarationNode) arg).name;
        else
            return arg.toString();
    }

    // ---------------------------------------------------------------------------------------------

    private Void ifStmt (IfNode node)
    {
        if (get(node.condition))
            get(node.trueStatement);
        else if (node.falseStatement != null)
            get(node.falseStatement);
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Void whileStmt (WhileNode node)
    {
        while (get(node.condition))
            get(node.body);
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object reference (ReferenceNode node)
    {
        Scope scope = reactor.get(node, "scope");
        DeclarationNode decl = reactor.get(node, "decl");

        if (decl instanceof AssignmentNode
                || decl instanceof ParameterNode
                || decl instanceof SyntheticDeclarationNode
                && ((SyntheticDeclarationNode) decl).kind() == DeclarationKind.VARIABLE)
            return scope == rootScope
                    ? rootStorage.get(scope, node.name)
                    : storage.get(scope, node.name);

        return decl; // structure or function
    }

    // ---------------------------------------------------------------------------------------------

    private Void returnStmt (ReturnNode node) {
        throw new Return(node.expression == null ? null : get(node.expression));
    }

    // ---------------------------------------------------------------------------------------------

    private void assign (Scope scope, String name, Object value, Type targetType)
    {
        if (value instanceof Long && targetType == FLOAT)
            value = ((Long) value).doubleValue();
        storage.set(scope, name, value);
    }
}
