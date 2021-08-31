package ntou.soselab.swagger.neo4j.graph;

import ntou.soselab.swagger.neo4j.domain.service.Resource;

import java.util.ArrayList;

public class ResourceGraph {

    Resource resource;
    ArrayList<PathGraph> pathGraphs;
    ArrayList<SecurityGraph> securitySchemeGraphs;
    ArrayList<SecurityDataGraph> securityDataGraphs;
    OASGraph oasGraph;

    public ResourceGraph(){}
    public ResourceGraph(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public ArrayList<PathGraph> getPathGraphs() {
        return pathGraphs;
    }

    public void setPathGraphs(ArrayList<PathGraph> pathGraphs) {
        this.pathGraphs = pathGraphs;
    }

    public void setPathGraph(PathGraph pathGraph) {
        if(this.pathGraphs == null){
            this.pathGraphs = new ArrayList<PathGraph>();
        }
        this.pathGraphs.add(pathGraph);
    }

    // SecuritySchemeGraph
    public ArrayList<SecurityGraph> getSecuritySchemeGraphs() {
        return securitySchemeGraphs;
    }

    public void setSecuritySchemeGraphs(ArrayList<SecurityGraph> securitySchemeGraphs) {
        this.securitySchemeGraphs = securitySchemeGraphs;
    }
    public void setSecuritySchemeGraph(SecurityGraph securitySchemeGraph) {
        if(this.securitySchemeGraphs == null) {
            this.securitySchemeGraphs = new ArrayList<SecurityGraph>();
        }
        this.securitySchemeGraphs.add(securitySchemeGraph);
    }


    // SecurityDataGraph
    public ArrayList<SecurityDataGraph> getSecurityDataGraphs() {
        return securityDataGraphs;
    }

    public void setSecurityDataGraphs(ArrayList<SecurityDataGraph> securityDataGraphs) {
        this.securityDataGraphs = securityDataGraphs;
    }
    public void setSecurityDataGraph(SecurityDataGraph securityDataGraph) {
        if(this.securityDataGraphs == null) {
            this.securityDataGraphs = new ArrayList<SecurityDataGraph>();
        }
        this.securityDataGraphs.add(securityDataGraph);
    }

    public OASGraph getOasGraph() {
        return oasGraph;
    }

    public void setOasGraph(OASGraph oasGraph) {
        this.oasGraph = oasGraph;
    }
}
