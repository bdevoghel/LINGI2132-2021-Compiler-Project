package AST;

public class ValueNode implements ASTNode {

    public ValueNode (String value){ new ValueNode (value, false); }

    public ValueNode (String value, boolean isNegative){
        try{
            int intValue = Integer.parseInt(value);
            if (isNegative) {
                new IntegerNode(-intValue);
            } else {
                new IntegerNode(intValue);
            }
        }
        catch (NumberFormatException e){
            if (value.equals("true") || value.equals("false")){
                if (isNegative) {
                    new BooleanNode(! Boolean.parseBoolean(value));
                } else {
                    new BooleanNode(Boolean.parseBoolean(value));
                }
            }
            new VariableNode(value, isNegative);
        }
    }

}
