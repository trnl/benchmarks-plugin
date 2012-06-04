package hudson.plugins.benchmarks.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Report implements Serializable {

    private String key = null;
    private Map<String, Benchmark> benchmarks = new HashMap<String, Benchmark>();

    public Report(String key, Collection<Benchmark> benchmarks) {
        this.key = key;
        for (Benchmark b : benchmarks) {
            addBenchmark(b);
        }
    }

    public void addBenchmark(Benchmark benchmark) {
        benchmarks.put(benchmark.getTitle(), benchmark);
    }

    public String getKey() {
        return key;
    }

    public Collection<Benchmark> getBenchmarks() {
        return benchmarks.values();
    }

    public Benchmark get(String name) {
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
