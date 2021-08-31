package ntou.soselab.swagger.neo4j.domain.service;

import ntou.soselab.swagger.neo4j.domain.relationship.Possess;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class OAS extends ConcreteService {
    String proberVersionOAS;

    public OAS() {
    }

    public OAS(String proberVersionOAS) {
        this.proberVersionOAS = proberVersionOAS;
    }

    public String getProberVersionOAS() {
        return proberVersionOAS;
    }

    public void setProberVersionOAS(String proberVersionOAS) {
        this.proberVersionOAS = proberVersionOAS;
    }


    @Relationship(type = "possess", direction = Relationship.INCOMING)
    Set<Possess> oas = new HashSet<>();

    public Set<Possess> getOas() {
        return oas;
    }

    public void setOas(Set<Possess> oas) {
        this.oas = oas;
    }
}
