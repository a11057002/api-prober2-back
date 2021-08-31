package ntou.soselab.swagger.neo4j.repositories.service;

import ntou.soselab.swagger.neo4j.domain.service.GitHub;
import ntou.soselab.swagger.neo4j.domain.service.Path;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PathRepository extends GraphRepository<Path> {

    @Query("MATCH (n:Path) RETURN count(n)")
    int totalPath();

    @Query("MATCH (n:Resource)-[a:endpoint]-(m:Path) WHERE id(n)= {id} RETURN m")
    List<Path> findPathsByResource(@Param("id") Long id);

    @Query("MATCH (n:Path)-[a:FIND]-(m:GitHub) WHERE id(n)= {id} RETURN m")
    List<GitHub> findGitHubsByPathId(@Param("id") Long id);

    @Query("MATCH (n:Path) WHERE id(n)= {id} RETURN n")
    Path findPathById(@Param("id") Long id);
}
