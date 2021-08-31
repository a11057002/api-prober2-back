package ntou.soselab.swagger.neo4j.domain.service;

import org.neo4j.ogm.annotation.GraphId;

public class ConcreteService {
    @GraphId
    private Long nodeId;

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }
}
