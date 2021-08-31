package ntou.soselab.swagger.neo4j.domain.service;

import ntou.soselab.swagger.neo4j.domain.relationship.Annotate;
import ntou.soselab.swagger.neo4j.domain.relationship.Endpoint;
import ntou.soselab.swagger.neo4j.domain.relationship.Own;
import ntou.soselab.swagger.neo4j.domain.relationship.Possess;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class Resource extends ConcreteService{

    ArrayList<String> schemes;
    String host;
    String basePath;

    String id;
    String title;
    String description;
    String logo;
    String provider;
    String version;
    String swaggerUrl;
    String clusterGroup;
    ArrayList<String> authentications;
    ArrayList<String> consumes;
    ArrayList<String> produces;
    ArrayList<String> originalWord;
    ArrayList<String> wordnetWord;
    ArrayList<String> features;
    String testCaseUrl;


    //SecurityScheme securityScheme;


    public Resource() {
        super();
    }

    public Resource(String id, String title, String description, String logo, String provider, String version, String swaggerUrl, String basePath,String clusterGroup, ArrayList<String> authentications, ArrayList<String> consumes, ArrayList<String> produces, ArrayList<String> originalWord, ArrayList<String> wordnetWord, ArrayList<String> features) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.logo = logo;
        this.provider = provider;
        this.version = version;
        this.swaggerUrl = swaggerUrl;
        this.clusterGroup = clusterGroup;
        this.authentications = authentications;
        this.consumes = consumes;
        this.produces = produces;
        this.originalWord = originalWord;
        this.wordnetWord = wordnetWord;
        this.features = features;
        this.basePath = basePath;
        //this.securityScheme = securityScheme;
    }

    public ArrayList<String> getSchemes() {
        return schemes;
    }

    public void setSchemes(ArrayList<String> schemes) {
        this.schemes = schemes;
    }

    public void setScheme(String scheme) {
        if(schemes == null){
            schemes = new ArrayList<String>();
        }
        schemes.add(scheme);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSwaggerUrl() {
        return swaggerUrl;
    }

    public void setSwaggerUrl(String swaggerUrl) {
        this.swaggerUrl = swaggerUrl;
    }

    public String getClusterGroup() {
        return clusterGroup;
    }

    public void setClusterGroup(String clusterGroup) {
        this.clusterGroup = clusterGroup;
    }

    public ArrayList<String> getAuthentications() {
        return authentications;
    }

    public void setAuthentications(ArrayList<String> authentications) {
        this.authentications = authentications;
    }

    public void setAuthentication(String authentication) {
        if(authentications == null){
            authentications = new ArrayList<String>();
        }
        authentications.add(authentication);
    }

    public ArrayList<String> getConsumes() {
        return consumes;
    }

    public void setConsumes(ArrayList<String> consumes) {
        this.consumes = consumes;
    }

    public void setConsume(String consume) {
        if(consumes == null){
            consumes = new ArrayList<String>();
        }
        consumes.add(consume);
    }

    public ArrayList<String> getProduces() {
        return produces;
    }

    public void setProduces(ArrayList<String> produces) {
        this.produces = produces;
    }

    public void setProduce(String produce) {
        if(produces == null){
            produces = new ArrayList<String>();
        }
        produces.add(produce);
    }

    public ArrayList<String> getOriginalWord() {
        return originalWord;
    }

    public void setOriginalWord(ArrayList<String> originalWord) {
        this.originalWord = originalWord;
    }

    public ArrayList<String> getWordnetWord() {
        return wordnetWord;
    }

    public void setWordnetWord(ArrayList<String> wordnetWord) {
        this.wordnetWord = wordnetWord;
    }

    public ArrayList<String> getFeature() {
        return features;
    }

    public void setFeatures(ArrayList<String> features) {
        this.features = features;
    }

    public void setFeature(String feature) {

        if(features == null){
            features = new ArrayList<String>();
        }
        features.add(feature);
    }

    public String getTestCaseUrl() {
        return testCaseUrl;
    }

    public void setTestCaseUrl(String testCaseUrl) {
        this.testCaseUrl = testCaseUrl;
    }

    @Override
    public String toString() {
        return "Resource [title=" + title + ", provider=" + provider + "]";
    }

    @Relationship(type = "endpoint", direction = Relationship.OUTGOING)
    Set<Endpoint> endpoints = new HashSet<>();

    public Set<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Endpoint endpoint) {
        this.endpoints.add(endpoint);
    }


    @Relationship(type = "annotate", direction = Relationship.OUTGOING)
    Set<Annotate> securityAnnotate = new HashSet<>();

    public Set<Annotate> getAnnotate() {
        return securityAnnotate;
    }
    public void setAnnotate(Annotate securityAnnotate) { this.securityAnnotate.add(securityAnnotate); }

    @Relationship(type = "own", direction = Relationship.OUTGOING)
    Set<Own> securityDataOwn = new HashSet<>();

    public Set<Own> getOwn() {
        return securityDataOwn;
    }
    public void setOwn(Own securityData) { this.securityDataOwn.add(securityData); }


    @Relationship(type = "possess", direction = Relationship.OUTGOING)
    Set<Possess> oas = new HashSet<>();

    public Set<Possess> getOas() {
        return oas;
    }

    public void setOas(Set<Possess> oas) {
        this.oas = oas;
    }
}
