package ntou.soselab.swagger.neo4j.repositories.service;

import ntou.soselab.swagger.neo4j.domain.service.JavaRepo;
import ntou.soselab.swagger.neo4j.domain.service.Resource;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JavaRepoRepository extends GraphRepository<JavaRepo> {

    @Query("MATCH (n:Resource) WHERE (n)-[:endpoint]-(:Path)-[:parse]-(:JavaRepo) RETURN n")
    List<Resource> findResourceByHaveJavaRepo();

    @Query("MATCH (n:Resource) WHERE (n)-[:endpoint]-(:Path)-[:parse]-(:JavaRepo) RETURN count(n)")
    int findAllJavaRepoCount();

    @Query("MATCH (n:Resource) MATCH (n)-[:endpoint]-(:Path)-[:parse]-(o:JavaRepo) RETURN n, count(o) ORDER BY count(o) DESC")
    List<Resource> findResourceBySortJavaRepo();

    @Query("MATCH (n:Path)-[p:parse]-(j:JavaRepo) WHERE id(n)= {id} RETURN j")
    List<JavaRepo> findJavaReposByPath(@Param("id") Long id);
}