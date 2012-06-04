package hudson.plugins.benchmarks.action;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.plugins.benchmarks.model.Report;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BuildAction implements Action {

    private final AbstractBuild<?, ?> build;
    private Map<String, Report> map;

    public BuildAction(AbstractBuild<?, ?> build, Collection<Report> reports) {
        this.build = build;
        this.map = new HashMap<String, Report>();
        for (Report r : reports)
            map.put(r.getKey(), r);
    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    public String getDisplayName() {
        return "Benchmarks";
    }

    public String getIconFileName() {
        return null;
    }

    public String getUrlName() {
        return "benchmarks";
    }

    public Collection<Report> getReports() {
        return map.values();
    }

    public Report getReport(String key){
        return map.get(key);
    }
}
