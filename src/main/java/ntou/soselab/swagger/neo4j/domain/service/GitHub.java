package ntou.soselab.swagger.neo4j.domain.service;

public class GitHub extends ConcreteService {

    private String repoFullName;

    private String repoName;

    private String repoUrl;

    private String javaDocumentName;

    private String javaDocumentUrl;

    private String javaDocumentPath;

    private String javaDocumentHtmlUrl;

    public GitHub() {
    }

    public GitHub(String repoFullName, String repoName, String repoUrl, String javaDocumentName, String javaDocumentUrl, String javaDocumentPath, String javaDocumentHtmlUrl) {
        this.repoFullName = repoFullName;
        this.repoName = repoName;
        this.repoUrl = repoUrl;
        this.javaDocumentName = javaDocumentName;
        this.javaDocumentUrl = javaDocumentUrl;
        this.javaDocumentPath = javaDocumentPath;
        this.javaDocumentHtmlUrl = javaDocumentHtmlUrl;
    }

    public String getRepoFullName() {
        return repoFullName;
    }

    public void setRepoFullName(String repoFullName) {
        this.repoFullName = repoFullName;
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

    public String getJavaDocumentUrl() {
        return javaDocumentUrl;
    }

    public void setJavaDocumentUrl(String javaDocumentUrl) {
        this.javaDocumentUrl = javaDocumentUrl;
    }

    public String getJavaDocumentPath() {
        return javaDocumentPath;
    }

    public void setJavaDocumentPath(String javaDocumentPath) {
        this.javaDocumentPath = javaDocumentPath;
    }

    public String getJavaDocumentHtmlUrl() {
        return javaDocumentHtmlUrl;
    }

    public void setJavaDocumentHtmlUrl(String javaDocumentHtmlUrl) {
        this.javaDocumentHtmlUrl = javaDocumentHtmlUrl;
    }
}
