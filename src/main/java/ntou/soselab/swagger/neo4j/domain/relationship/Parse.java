package ntou.soselab.swagger.neo4j.domain.relationship;

import ntou.soselab.swagger.neo4j.domain.service.JavaRepo;
import ntou.soselab.swagger.neo4j.domain.service.Path;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type = "parse")
public class Parse {

    @GraphId
    Long graphId;

    @StartNode
    Path path;

    @EndNode
    JavaRepo javaRepo;

    public Parse() {}

    public Parse(Path path, JavaRepo javaRepo) {
        this.path = path;
        this.javaRepo = javaRepo;
    }
}
