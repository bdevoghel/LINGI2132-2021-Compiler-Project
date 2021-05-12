package scopes_new;

import ast_new.ClassNode;
import types_new.Type;
import norswap.uranium.Reactor;

import static scopes_new.DeclarationKind.*;

/**
 * The lexical scope of a file in Sigh. It is notably responsible for introducing the default
 * declarations made by the language.
 */
public final class RootScope extends Scope
{
    // ---------------------------------------------------------------------------------------------

    private SyntheticDeclarationNode decl (String name, DeclarationKind kind) {
        SyntheticDeclarationNode decl = new SyntheticDeclarationNode(name, kind);
        declare(name, decl);
        return decl;
    }

    // ---------------------------------------------------------------------------------------------

    // TODO delete : dynamically typed
//    // root scope types
//    public final SyntheticDeclarationNode Bool   = decl("Bool",   TYPE);
//    public final SyntheticDeclarationNode Int    = decl("Int",    TYPE);
//    public final SyntheticDeclarationNode Float  = decl("Float",  TYPE);
//    public final SyntheticDeclarationNode String = decl("String", TYPE);
//    public final SyntheticDeclarationNode Void   = decl("Void",   TYPE);
//    public final SyntheticDeclarationNode Type   = decl("Type",   TYPE);

    // root scope variables
    public final SyntheticDeclarationNode _true  = decl("true",  VARIABLE);
    public final SyntheticDeclarationNode _false = decl("false", VARIABLE);
    public final SyntheticDeclarationNode _null  = decl("null",  VARIABLE);

    // root scope functions
    public final SyntheticDeclarationNode print = decl("print", FUNCTION);
    // TODO select ours
//    public final SyntheticDeclarationNode println = decl("println", FUNCTION);
//    public final SyntheticDeclarationNode sort    = decl("sort",    FUNCTION);
//    public final SyntheticDeclarationNode range   = decl("range",   FUNCTION);
//    public final SyntheticDeclarationNode indexer = decl("indexer", FUNCTION);
//    public final SyntheticDeclarationNode open    = decl("open",    FUNCTION);
//    public final SyntheticDeclarationNode close   = decl("close",   FUNCTION);
//    public final SyntheticDeclarationNode read    = decl("read",    FUNCTION);
//    public final SyntheticDeclarationNode write   = decl("write",   FUNCTION);

    // ---------------------------------------------------------------------------------------------

    public RootScope (ClassNode node, Reactor reactor) {
        super(node, null);

        // TODO delete not ours
//        reactor.set(Bool,   "type",       TypeType.INSTANCE);
//        reactor.set(Int,    "type",       TypeType.INSTANCE);
//        reactor.set(Float,  "type",       TypeType.INSTANCE);
//        reactor.set(String, "type",       TypeType.INSTANCE);
//        reactor.set(Void,   "type",       TypeType.INSTANCE);
//        reactor.set(Type,   "type",       TypeType.INSTANCE);

//        reactor.set(Bool,   "declared",   BoolType.INSTANCE);
//        reactor.set(Int,    "declared",    IntType.INSTANCE);
//        reactor.set(Float,  "declared",  FloatType.INSTANCE);
//        reactor.set(String, "declared", StringType.INSTANCE);
//        reactor.set(Void,   "declared",   VoidType.INSTANCE);
//        reactor.set(Type,   "declared",   TypeType.INSTANCE);

        reactor.set(_true,  "type",       Type.BOOLEAN);
        reactor.set(_false, "type",       Type.BOOLEAN);
        reactor.set(_null,  "type",       Type.NULL);

        reactor.set(print,  "type",       Type.FUNCTION);
    }

    // ---------------------------------------------------------------------------------------------
}