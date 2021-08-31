package ntou.soselab.swagger.neo4j.graph;

import ntou.soselab.swagger.neo4j.domain.relationship.Have;
import ntou.soselab.swagger.neo4j.domain.relationship.Output;
import ntou.soselab.swagger.neo4j.domain.service.StatusCode;

import java.util.ArrayList;

public class StatusCodeGraph {
    StatusCode statusCode;
    ArrayList<ResponseGraph> responseGraphs;
    Output output;

    public StatusCodeGraph(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    //    public ArrayList<ResponseGraph> getResponseGraphs() {
//        return responseGraphs;
//    }
    public ArrayList<ResponseGraph> getResponseGraphs() {
        if(this.responseGraphs == null){
            this.responseGraphs=new ArrayList<ResponseGraph>();
        }
        return responseGraphs;
    }

    public void setResponseGraphs(ArrayList<ResponseGraph> responseGraphs) {
        this.responseGraphs = responseGraphs;
    }

    public void setResponseGraph(ResponseGraph responseGraph) {
        if(this.responseGraphs == null){
            this.responseGraphs = new ArrayList<ResponseGraph>();
        }
        this.responseGraphs.add(responseGraph);
    }

    public Output getOutput() {
        return output;
    }
    public void setOutput(Output output) {
        this.output = output;
    }


}
