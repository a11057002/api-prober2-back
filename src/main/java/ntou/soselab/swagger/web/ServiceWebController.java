package ntou.soselab.swagger.web;

import ntou.soselab.swagger.cluster.ClusterMashup;
import ntou.soselab.swagger.neo4j.repositories.service.OASRepository;
import ntou.soselab.swagger.neo4j.repositories.service.ResourceRepository;
import ntou.soselab.swagger.neo4j.repositories.service.SecurityRepository;
import ntou.soselab.swagger.web.recommand.ServiceRecommender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ServiceWebController {
    @Autowired
    ServiceManager serviceManager;

    @Autowired
    ServiceRecommender serviceRecommender;
    @Autowired
    ClusterMashup clusterMashup;
    @Autowired
    ResourceRepository resourceRepository;
    @Autowired
    SecurityRepository securityRepository;
    @Autowired
    OASRepository oasRepository;

    Logger log = LoggerFactory.getLogger(ServiceWebController.class);

    @CrossOrigin
    @RequestMapping(value = "/getSearchEngineResult", method = RequestMethod.GET)
    public String getSearchEngineResult(@RequestParam String query) {
        return serviceManager.runSearchEngine(query);
    }

    @CrossOrigin
    @RequestMapping(value = "/getServiceLevel", method = RequestMethod.GET)
    public String getServiceLevel() {
        return serviceManager.countServiceLevel();
    }

    @CrossOrigin
    @RequestMapping(value = "/getEndpointLevel", method = RequestMethod.GET)
    public String getEndpointLevel() {
        return serviceManager.countEndpointLevel();
    }

    @CrossOrigin
    @RequestMapping(value = "/getPopularStandardOAS", method = RequestMethod.GET)
    public String getPopularStandardOAS() {
        return serviceManager.countPopularStandardOAS();
    }

    @CrossOrigin
    @RequestMapping(value = "/getOASInformation/{id}", method = RequestMethod.GET)
    public String getOASBasicInformation(@PathVariable("id")Long resourceId) {
        return serviceManager.runOASBasicInformation(resourceId);
    }

    @CrossOrigin
    @RequestMapping(value = "/getOASClusterInformation/{id}", method = RequestMethod.GET)
    public String getOASClusterInformation(@PathVariable("id")Long resourceId, @RequestParam String threshold) {
        return clusterMashup.compareTagetClusterAndOtherCluster(resourceId, Double.valueOf(threshold));
    }

    @CrossOrigin
    @RequestMapping(value = "/getOASRecommendation/{id}", method = RequestMethod.GET)
    public String getRecommendationResult(@PathVariable("id")Long resourceId) {
        return serviceRecommender.runRecommendService(resourceId);
    }



}
