package hudson.plugins.benchmarks.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author Uladzimir Mihura
 *         Date: 3/4/12
 *         Time: 1:01 PM
 */
public class Benchmark implements Serializable{
    private double executionTime;
    private int invocations;
    private double max;
    private double min;

    private double average;
    private String title;
    private double median;
    @SerializedName("90percentile") private double percentile90;

    public double getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(double executionTime) {
        this.executionTime = executionTime;
    }

    public int getInvocations() {
        return invocations;
    }

    public void setInvocations(int invocations) {
        this.invocations = invocations;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public String getTitle() {
        return title;
    }

    public double getPercentile90() {
        return percentile90;
    }

    public void setPercentile90(double percentile90) {
        this.percentile90 = percentile90;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getMedian() {
        return median;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Benchmark)) return false;

        Benchmark benchmark = (Benchmark) o;

        if (!title.equals(benchmark.title)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    public void setMedian(double median) {
        this.median = median;

    }

    @Override
    public String toString() {
        return "Benchmark{" +
                "executionTime=" + executionTime +
                ", invocations=" + invocations +
                ", max=" + max +
                ", average=" + average +
                ", title='" + title + '\'' +
                ", median=" + median +
                ", percentile90=" + percentile90 +
                '}';
    }


}
