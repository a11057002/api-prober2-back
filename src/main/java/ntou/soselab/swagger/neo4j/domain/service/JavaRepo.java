package ntou.soselab.swagger.neo4j.domain.service;

import java.util.ArrayList;

public class JavaRepo extends ConcreteService {

    private String repoName;

    private String repoUrl;

    private String javaDocumentName;

    private String javaDocumentHtmlUrl;

    private String documentId;

    private ArrayList<String> method;

    private int score;

    public JavaRepo() {

    }

    public JavaRepo(String repoName, String repoUrl, String javaDocumentName, String javaDocumentHtmlUrl, String documentId, ArrayList<String> method, int score) {
        this.repoName = repoName;
        this.repoUrl = repoUrl;
        this.javaDocumentName = javaDocumentName;
        this.javaDocumentHtmlUrl = javaDocumentHtmlUrl;
        this.documentId = documentId;
        this.method = method;
        this.score = score;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getJavaDocumentName() {
        return javaDocumentName;
    }

    public void setJavaDocumentName(String javaDocumentName) {
        this.javaDocumentName = javaDocumentName;
    }

    public String getJavaDocumentHtmlUrl() {
        return javaDocumentHtmlUrl;
    }

    public void setJavaDocumentHtmlUrl(String javaDocumentHtmlUrl) {
        this.javaDocumentHtmlUrl = javaDocumentHtmlUrl;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public ArrayList<String> getMethod() {
        return method;
    }

    public void setMethod(ArrayList<String> method) {
        this.method = method;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
