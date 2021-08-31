package ntou.soselab.swagger.neo4j.graph;

import ntou.soselab.swagger.neo4j.domain.relationship.Have;
import ntou.soselab.swagger.neo4j.domain.relationship.Output;
import ntou.soselab.swagger.neo4j.domain.service.Response;

public class ResponseGraph {
    Response response;
    Have have;

    public ResponseGraph(Response response) {
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Have getHave() {
        return have;
    }

    public void setHave(Have have) {
        this.have = have;
    }
}
