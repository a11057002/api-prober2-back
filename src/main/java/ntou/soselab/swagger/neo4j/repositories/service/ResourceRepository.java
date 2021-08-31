package ntou.soselab.swagger.neo4j.repositories.service;

import ntou.soselab.swagger.neo4j.domain.service.Resource;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResourceRepository extends GraphRepository<Resource> {

    @Query("MATCH (n:Resource) RETURN count(n)")
    int totalResource();

    @Query("MATCH (n:Resource) RETURN ID(n)")
    List<Long> getResourceNodeId();


    @Query("MATCH (n:Resource) WHERE id(n)= {id} RETURN n")
    Resource findResourceById(@Param("id") Long id);

    @Query("MATCH (n:Resource)-[a:endpoint]-(m:Path) WHERE id(m)= {id} RETURN n")
    Resource findResourceByPathId(@Param("id") Long id);

    @Query("MATCH (n:Resource {title : {title}}) RETURN n")
    List<Resource> findResourcesByTitle(@Param("title") String title);

    @Query("MATCH (n:Resource {clusterGroup:{clusterGroup}}) RETURN n")
    List<Resource> findResourcesBySameCluster(@Param("clusterGroup") String clusterGroup);

    @Query("MATCH (n:Resource {clusterGroup:{clusterGroup}}) RETURN count(n)")
    int findCountBySameCluster(@Param("clusterGroup") String clusterGroup);
}
