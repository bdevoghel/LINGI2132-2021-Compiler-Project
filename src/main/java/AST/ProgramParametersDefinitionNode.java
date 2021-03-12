package AST;

import java.util.List;

public class ProgramParametersDefinitionNode implements StatementNode{

    public ProgramParametersDefinitionNode(String args, List<String> parameters){
        for (int i = 0; i<parameters.size(); i++){
            new AssignmentNode(
                    new ArrayMapAccessNode(new IdentifierNode("args"), new IntegerNode(i)),
                    new StringNode(parameters.get(i)));
        }
    }
}
