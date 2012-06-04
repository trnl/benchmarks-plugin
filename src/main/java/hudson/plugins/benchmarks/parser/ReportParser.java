package hudson.plugins.benchmarks.parser;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.plugins.benchmarks.model.Report;

import java.io.IOException;
import java.util.Collection;

public abstract class ReportParser{
    public abstract Collection<Report> parse(Collection<FilePath> files, TaskListener listener)
            throws IOException;
}
