package ntou.soselab.swagger.neo4j.graph;

import ntou.soselab.swagger.neo4j.domain.relationship.Action;
import ntou.soselab.swagger.neo4j.domain.service.Operation;

import java.util.ArrayList;

public class OperationGraph {

    Operation operation;
    Action action;
    ArrayList<ParameterGraph> parameterGraphs;
    ArrayList<StatusCodeGraph> statusCodeGraphs;
    ArrayList<TestCaseGraph> testCaseGraphs;

    public OperationGraph(){

    }
    public OperationGraph(Operation operation) {
        this.operation = operation;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    //    public ArrayList<ParameterGraph> getParameterGraphs() {
//        return parameterGraphs;
//    }

    // 避免該 path 沒有參數
    public ArrayList<ParameterGraph> getParameterGraphs() {
        if(parameterGraphs == null){
            parameterGraphs = new ArrayList<ParameterGraph>();
        }
        return parameterGraphs;
    }

    public void setParameterGraphs(ArrayList<ParameterGraph> parameterGraphs) {
        this.parameterGraphs = parameterGraphs;
    }

    public void setParameterGraph(ParameterGraph parameterGraph) {
        if(this.parameterGraphs == null){
            this.parameterGraphs = new ArrayList<ParameterGraph>();
        }
        this.parameterGraphs.add(parameterGraph);
    }

    public ArrayList<StatusCodeGraph> getStatusCodeGraphs() {
        return statusCodeGraphs;
    }

    public void setStatusCodeGraphs(ArrayList<StatusCodeGraph> statusCodeGraphs) {
        this.statusCodeGraphs = statusCodeGraphs;
    }

    public void setStatusCodeGraphs(StatusCodeGraph statusCodeGraph) {
        if(this.statusCodeGraphs == null){
            this.statusCodeGraphs = new ArrayList<StatusCodeGraph>();
        }
        this.statusCodeGraphs.add(statusCodeGraph);
    }

    /*public TestCaseGraph getTestCaseGraph() {
        return testCaseGraph;
    }

    public void setTestCaseGraph(TestCaseGraph testCaseGraph) {
        this.testCaseGraph = testCaseGraph;
    }*/
    public ArrayList<TestCaseGraph> getTestCaseGraphs() {
        return testCaseGraphs;
    }

    public void setTestCaseGraphs(TestCaseGraph testCaseGraph) {
        if(testCaseGraphs == null){
            this.testCaseGraphs = new ArrayList<TestCaseGraph>();
        }

        this.testCaseGraphs.add(testCaseGraph);
    }
}
