package ntou.soselab.swagger.neo4j.repositories.service;

import ntou.soselab.swagger.neo4j.domain.service.GitHub;
import ntou.soselab.swagger.neo4j.domain.service.Path;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

public interface GitHubRepository extends GraphRepository<GitHub> {

    @Query("MATCH (n:GitHub) WHERE id(n)= {id} RETURN n")
    GitHub findGitHubById(@Param("id") Long id);

    @Query("MATCH (n:GitHub)-[r:FIND]-(m:Path) WHERE id(n)= {id} RETURN m")
    Path findPathByGitHub(@Param("id") Long id);

}
