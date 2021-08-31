package ntou.soselab.swagger.neo4j.domain.concept;

import org.neo4j.ogm.annotation.GraphId;

import java.util.ArrayList;

public class Concept {
    @GraphId
    Long nodeId;

    ArrayList<String> originalConcepts = new ArrayList<>();
    ArrayList<String> extensiveConcepts = new ArrayList<>();


    public ArrayList<String> getOriginalConcepts() {
        return originalConcepts;
    }
    public void setOriginalConcepts(ArrayList<String> originalConcepts) {
        this.originalConcepts = originalConcepts;
    }
    public ArrayList<String> getExtensiveConcepts() {
        return extensiveConcepts;
    }
    public void setExtensiveConcepts(ArrayList<String> extensiveConcepts) {
        this.extensiveConcepts = extensiveConcepts;
    }

}
