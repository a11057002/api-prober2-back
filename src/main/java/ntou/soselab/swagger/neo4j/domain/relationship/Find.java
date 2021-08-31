package ntou.soselab.swagger.neo4j.domain.relationship;

import ntou.soselab.swagger.neo4j.domain.service.GitHub;
import ntou.soselab.swagger.neo4j.domain.service.Path;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type = "FIND")
public class Find {

    @GraphId
    Long graphId;

    @StartNode
    Path path;

    @EndNode
    GitHub gitHub;

    public Find() {}

    public Find(Path path, GitHub gitHub) {
        this.path = path;
        this.gitHub = gitHub;
    }
}
