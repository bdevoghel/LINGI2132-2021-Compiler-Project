package scopes;

import ast.ClassNode;
import types.Type;
import norswap.uranium.Reactor;

import static scopes.DeclarationKind.*;

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

    // root scope variables
    public final SyntheticDeclarationNode _true  = decl("true",  VARIABLE);
    public final SyntheticDeclarationNode _false = decl("false", VARIABLE);
    public final SyntheticDeclarationNode _null  = decl("null",  VARIABLE);
    public final SyntheticDeclarationNode _args  = decl("args",  VARIABLE);

    // root scope functions
    public final SyntheticDeclarationNode _print     = decl("print",     FUNCTION);
    public final SyntheticDeclarationNode _int       = decl("int",       FUNCTION);
    public final SyntheticDeclarationNode _len       = decl("len",       FUNCTION);
    public final SyntheticDeclarationNode _makeDict  = decl("makeDict",  FUNCTION);
    public final SyntheticDeclarationNode _dictAdd   = decl("dictAdd",   FUNCTION);
    public final SyntheticDeclarationNode _dictGet   = decl("dictGet",   FUNCTION);
    public final SyntheticDeclarationNode _makeArray = decl("makeArray", FUNCTION);
    // TODO select ours
//    public final SyntheticDeclarationNode println    = decl("println", FUNCTION);
//    public final SyntheticDeclarationNode sort       = decl("sort",    FUNCTION);
//    public final SyntheticDeclarationNode range      = decl("range",   FUNCTION);
//    public final SyntheticDeclarationNode indexer    = decl("indexer", FUNCTION);
//    public final SyntheticDeclarationNode open       = decl("open",    FUNCTION);
//    public final SyntheticDeclarationNode close      = decl("close",   FUNCTION);
//    public final SyntheticDeclarationNode read       = decl("read",    FUNCTION);
//    public final SyntheticDeclarationNode write      = decl("write",   FUNCTION);

    // ---------------------------------------------------------------------------------------------

    public RootScope (ClassNode node, Reactor reactor) {
        super(node, null);
        reactor.set(_true,      "type",    Type.BOOLEAN);
        reactor.set(_false,     "type",    Type.BOOLEAN);
        reactor.set(_null,      "type",    Type.NULL);
        reactor.set(_args,      "type",    Type.ARRAY);

        reactor.set(_print,     "type",    Type.FUNCTION);
        reactor.set(_int,       "type",    Type.FUNCTION);
        reactor.set(_len,       "type",    Type.FUNCTION);

        reactor.set(_makeDict,  "type",    Type.MAP);
        reactor.set(_dictAdd,   "type",    Type.MAP);
        reactor.set(_dictGet,   "type",    Type.UNKNOWN_TYPE);
        reactor.set(_makeArray, "type",    Type.ARRAY);
    }
}
