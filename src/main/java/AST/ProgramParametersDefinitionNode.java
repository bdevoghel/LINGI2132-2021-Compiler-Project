package AST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProgramParametersDefinitionNode implements StatementNode{
    public List parameters;

    public ProgramParametersDefinitionNode(String parameters){
        String[] paramList = parameters.split(" ");
        ArrayList p = new ArrayList();
        for (int i = 0; i < paramList.length; i++){
            p.add(
                    new AssignmentNode(
                        new ArrayMapAccessNode(new IdentifierNode("args"), new IntegerNode(i)),
                        new StringNode(paramList[i])));
        }
        this.parameters = Arrays.asList(p);
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        ProgramParametersDefinitionNode other = (ProgramParametersDefinitionNode) obj;
        return this.parameters.equals(other.parameters);
    }

    @Override
    public String toString() {
        return "ProgramParametersDefinitionNode:" + parameters;
    }
}
