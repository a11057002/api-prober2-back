package ntou.soselab.swagger.web.oaspage;

import ntou.soselab.swagger.neo4j.domain.service.JavaRepo;

import java.util.ArrayList;

public class PathInfo {

    String endpoint;
    ArrayList<OperationInfo> operations;
    ArrayList<JavaRepoInfo> javaRepos;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public ArrayList<OperationInfo> getOperations() {
        return operations;
    }

    public void setOperations(ArrayList<OperationInfo> operations) {
        this.operations = operations;
    }

    public ArrayList<JavaRepoInfo> getJavaRepos() {
        return javaRepos;
    }

    public void setJavaRepos(ArrayList<JavaRepoInfo> javaRepos) {
        this.javaRepos = javaRepos;
    }
}
