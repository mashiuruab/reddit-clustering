package bdp.reddit.KMeans.tfidf;

public class TfDocCountContainer {
    private double aggregatedTF;
    private long totalDoc;

    public double getAggregatedTF() {
        return aggregatedTF;
    }

    public void setAggregatedTF(double aggregatedTF) {
        this.aggregatedTF = aggregatedTF;
    }

    public long getTotalDoc() {
        return totalDoc;
    }

    public void setTotalDoc(long totalDoc) {
        this.totalDoc = totalDoc;
    }
}
