package hudson.plugins.benchmarks.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Report implements Serializable {

    private String key = null;
    private Map<String, BenchmarkResult> benchmarks = new HashMap<String, BenchmarkResult>();

    public Report(String key, Collection<BenchmarkResult> results) {
        this.key = key;
        for (BenchmarkResult b : results) {
            addBenchmarkResult(b);
        }
    }

    public void addBenchmarkResult(BenchmarkResult result) {
        benchmarks.put(result.get("title").toString(), result);
    }

    public String getKey() {
        return key;
    }

    public Collection<BenchmarkResult> getBenchmarkResults() {
        return benchmarks.values();
    }

    public BenchmarkResult get(String name) {
        return benchmarks.get(name);
    }

    public Collection<String> getNames() {
        return benchmarks.keySet();
    }

    public boolean contains(String name) {
        return benchmarks.containsKey(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Report)) return false;
        Report report = (Report) o;
        if (key != null ? !key.equals(report.key) : report.key != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Report{" +
                "key='" + key + '\'' +
                ", benchmarks=" + benchmarks +
                '}';
    }
}
