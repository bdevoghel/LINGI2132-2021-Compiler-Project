package scopes;

import AST.ASTNode;
import AST.IdentifierNode;
import norswap.uranium.Reactor;
import types.*;

import static scopes.DeclarationKind.*;

public final class ClassScope extends Scope
{
    // ---------------------------------------------------------------------------------------------

    private SyntheticDeclarationNode decl (String name, DeclarationKind kind) {
        SyntheticDeclarationNode decl = new SyntheticDeclarationNode(name, kind);
        declare(name,  decl);
        return decl;
    }

    // ---------------------------------------------------------------------------------------------

    // root scope types
    public final SyntheticDeclarationNode Bool   = decl("Bool",   TYPE);
    public final SyntheticDeclarationNode Int    = decl("Int",    TYPE);
    public final SyntheticDeclarationNode Doub  = decl("Doub",  TYPE);
    public final SyntheticDeclarationNode String = decl("String", TYPE);
    public final SyntheticDeclarationNode Void   = decl("Void",   TYPE);
    public final SyntheticDeclarationNode Type   = decl("Type",   TYPE);

    // root scope variables
    public final SyntheticDeclarationNode _true  = decl("true",  VARIABLE);
    public final SyntheticDeclarationNode _false = decl("false", VARIABLE);
    public final SyntheticDeclarationNode _null  = decl("null",  VARIABLE);

    // root scope functions
    public final SyntheticDeclarationNode _print = decl("print", FUNCTION);
    public final SyntheticDeclarationNode _int = decl("int", FUNCTION);
    public final SyntheticDeclarationNode _str = decl("str", FUNCTION);
    public final SyntheticDeclarationNode _len = decl("len", FUNCTION);
    public final SyntheticDeclarationNode _makeArray = decl("makeArray", FUNCTION);
    public final SyntheticDeclarationNode _makeDict = decl("makeDict", FUNCTION);

    // ---------------------------------------------------------------------------------------------

    public ClassScope(ASTNode node, Reactor reactor) {
        super(node, null);

        reactor.set(Bool,   "type",       TypeType.INSTANCE);
        reactor.set(Int,    "type",       TypeType.INSTANCE);
        reactor.set(Doub,   "type",       TypeType.INSTANCE);
        reactor.set(String, "type",       TypeType.INSTANCE);
        reactor.set(Void,   "type",       TypeType.INSTANCE);
        reactor.set(Type,   "type",       TypeType.INSTANCE);

        reactor.set(Bool,   "declared",   BoolType.INSTANCE);
        reactor.set(Int,    "declared",   IntType.INSTANCE);
        reactor.set(Type,   "declared",   TypeType.INSTANCE);

        reactor.set(_true,  "type",       BoolType.INSTANCE);
        reactor.set(_false, "type",       BoolType.INSTANCE);

        reactor.set(_print, "type",       new FunType(UnknownType.INSTANCE));
        reactor.set(_int, "type",         new FunType(StringType.INSTANCE));
        reactor.set(_str, "type",         new FunType(IntType.INSTANCE));
        reactor.set(_len, "type",         new FunType(ArrayType.INSTANCE));
        reactor.set(_makeArray, "type",   new FunType());
        reactor.set(_makeDict, "type",    new FunType());
    }

    // ---------------------------------------------------------------------------------------------
}
