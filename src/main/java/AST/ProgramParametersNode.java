package AST;

import java.util.Arrays;
import java.util.List;

public class ProgramParametersNode implements ExpressionNode {

    private List parameters;

    public ProgramParametersNode(String parameters){
        this.parameters = Arrays.asList(parameters.split(" "));
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        ProgramParametersNode other = (ProgramParametersNode) obj;
        return this.parameters.equals(other.parameters);
    }

    @Override
    public String toString() {
        return "ProgramParametersNode:" + parameters;
    }
}
