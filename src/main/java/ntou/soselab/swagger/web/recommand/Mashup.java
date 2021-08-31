package ntou.soselab.swagger.web.recommand;

public class Mashup {
    double score;
    String category;
    String targetEndpoint;
    String targetOperation;
    Long compareOASId;
    String compareOASName;
    String compareEndpoint;
    String compareOperation;

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTargetEndpoint() {
        return targetEndpoint;
    }

    public void setTargetEndpoint(String targetEndpoint) {
        this.targetEndpoint = targetEndpoint;
    }

    public String getTargetOperation() {
        return targetOperation;
    }

    public void setTargetOperation(String targetOperation) {
        this.targetOperation = targetOperation;
    }

    public Long getCompareOASId() {
        return compareOASId;
    }

    public void setCompareOASId(Long compareOASId) {
        this.compareOASId = compareOASId;
    }

    public String getCompareOASName() {
        return compareOASName;
    }

    public void setCompareOASName(String compareOASName) {
        this.compareOASName = compareOASName;
    }

    public String getCompareEndpoint() {
        return compareEndpoint;
    }

    public void setCompareEndpoint(String compareEndpoint) {
        this.compareEndpoint = compareEndpoint;
    }

    public String getCompareOperation() {
        return compareOperation;
    }

    public void setCompareOperation(String compareOperation) {
        this.compareOperation = compareOperation;
    }
}
