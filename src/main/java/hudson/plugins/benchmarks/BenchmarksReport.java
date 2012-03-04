package hudson.plugins.benchmarks;

import hudson.model.AbstractBuild;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BenchmarksReport implements
        Comparable<BenchmarksReport> {

    private BenchmarksBuildAction buildAction;

    private String reportFileName = null;

    private final Map<String, Benchmark> benchmarks = new HashMap<String, Benchmark>();


    public void addBenchmark(Benchmark benchmark) {
        benchmarks.put(benchmark.getName(), benchmark);
    }

    public int compareTo(BenchmarksReport report) {
        if (this == report) {
            return 0;
        }
        return getReportFileName().compareTo(report.getReportFileName());
    }

    public AbstractBuild<?, ?> getBuild() {
        return buildAction.getBuild();
    }

    public String getDisplayName() {
        return Messages.Report_DisplayName();
    }

    public String getReportFileName() {
        return reportFileName;
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


    public void setReportFileName(String reportFileName) {
        this.reportFileName = reportFileName;
    }

    public void setBuildAction(BenchmarksBuildAction buildAction) {
        this.buildAction = buildAction;
    }

    BenchmarksBuildAction getBuildAction() {
        return buildAction;
    }
}
