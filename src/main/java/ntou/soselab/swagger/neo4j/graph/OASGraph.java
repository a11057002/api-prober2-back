package ntou.soselab.swagger.neo4j.graph;

import ntou.soselab.swagger.neo4j.domain.relationship.Possess;
import ntou.soselab.swagger.neo4j.domain.service.OAS;

public class OASGraph {
    OAS oas;
    Possess possess;


    public OASGraph() {
    }

    public OASGraph(OAS oaa) {
        this.oas = oaa;
    }

    public OAS getOas() {
        return oas;
    }

    public void setOas(OAS oas) {
        this.oas = oas;
    }

    public Possess getPossess() {
        return possess;
    }

    public void setPossess(Possess possess) {
        this.possess = possess;
    }
}
