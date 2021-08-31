package ntou.soselab.swagger.web.oaspage;

import java.util.ArrayList;

public class JavaRepoInfo {

    String repoName;
    String repoUrl;
    String javaDocHtml;
    ArrayList<String> method;
    int score;

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

    public String getJavaDocHtml() {
        return javaDocHtml;
    }

    public void setJavaDocHtml(String javaDocHtml) {
        this.javaDocHtml = javaDocHtml;
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
