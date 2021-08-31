package ntou.soselab.swagger.neo4j.graph;

import ntou.soselab.swagger.neo4j.domain.relationship.Own;
import ntou.soselab.swagger.neo4j.domain.service.SecurityData;

public class SecurityDataGraph {

    SecurityData securityData;
    Own own;

    public SecurityDataGraph(){ super(); }
    public SecurityData getSecurityData() {
        return securityData;
    }

    public void setSecurityData(SecurityData securityData) {
        this.securityData = securityData;
    }

    public Own getOwn() {
        return own;
    }

    public void setOwn(Own own) {
        this.own = own;
    }
}
