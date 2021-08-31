package ntou.soselab.swagger.neo4j.domain.service;

import ntou.soselab.swagger.neo4j.domain.relationship.Have;
import ntou.soselab.swagger.neo4j.domain.relationship.Input;
import ntou.soselab.swagger.neo4j.domain.relationship.Output;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class StatusCode extends ConcreteService{
    String statusCode;
    String description;

    public StatusCode() {
        super();
    }

    public StatusCode(String statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Relationship(type = "output", direction = Relationship.INCOMING)
    Set<Output> outputs = new HashSet<>();

    public Set<Output> getOutputs() {
        return outputs;
    }
    public void setOutput(Output output) {
        this.outputs.add(output);
    }

    @Relationship(type = "have", direction = Relationship.OUTGOING)
    Set<Have> haves = new HashSet<>();

    public Set<Have> getHaves() {
        return haves;
    }
    public void setHave(Have have) {
        this.haves.add(have);
    }
}
