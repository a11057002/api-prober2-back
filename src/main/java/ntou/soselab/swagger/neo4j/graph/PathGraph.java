package ntou.soselab.swagger.neo4j.graph;

import ntou.soselab.swagger.neo4j.domain.relationship.Endpoint;
import ntou.soselab.swagger.neo4j.domain.service.Path;

import java.util.ArrayList;

public class PathGraph {
    Path path;
    Endpoint endpoint;
    ArrayList<OperationGraph> operationGraphs;

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public ArrayList<OperationGraph> getOperationGraphs() {
        return operationGraphs;
    }

    public void setOperationGraphs(ArrayList<OperationGraph> operationGraphs) {
        this.operationGraphs = operationGraphs;
    }

    public void setOperationGraph(OperationGraph operationGraph) {
        if(this.operationGraphs == null){
            this.operationGraphs = new ArrayList<OperationGraph>();
        }
        this.operationGraphs.add(operationGraph);
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }
}
