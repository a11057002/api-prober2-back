package ntou.soselab.swagger.neo4j.domain.service;

import ntou.soselab.swagger.neo4j.domain.relationship.Action;
import ntou.soselab.swagger.neo4j.domain.relationship.Endpoint;
import ntou.soselab.swagger.neo4j.domain.relationship.Find;
import ntou.soselab.swagger.neo4j.domain.relationship.Parse;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Path extends ConcreteService {

    String path;

    public Path() {}

    public Path(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Relationship(type = "endpoint", direction = Relationship.INCOMING)
    Set<Endpoint> endpoints = new HashSet<>();

    public Set<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Endpoint endpoint) {
        this.endpoints.add(endpoint);
    }

    @Relationship(type = "action", direction = Relationship.OUTGOING)
    Set<Action> actions = new HashSet<>();

    public Set<Action> getActions() {
        return actions;
    }

    public void setActions(Action action) {
        this.actions.add(action);
    }

    @Relationship(type="FIND", direction = Relationship.OUTGOING)
    ArrayList<Find> finds = new ArrayList<Find>();

    public void addFindRelationship(Path path, GitHub gitHub){
        Find find = new Find(path, gitHub);
        finds.add(find);
    }

    @Relationship(type="parse", direction = Relationship.OUTGOING)
    ArrayList<Parse> parses = new ArrayList<Parse>();

    public void addParseRelationship(Path path, JavaRepo javaRepo){
        Parse parse = new Parse(path, javaRepo);
        parses.add(parse);
    }
}
