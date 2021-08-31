package ntou.soselab.swagger.neo4j.graph;

import ntou.soselab.swagger.neo4j.domain.relationship.Input;
import ntou.soselab.swagger.neo4j.domain.service.Parameter;

public class ParameterGraph {
    Parameter parameter;
    Input input;


    public ParameterGraph(Parameter parameter) {
        this.parameter = parameter;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public Input getInput() {
        return input;
    }
    public void setInput(Input input) {
        this.input = input;
    }
}
