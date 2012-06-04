package hudson.plugins.benchmarks;

import hudson.FilePath;
import hudson.model.StreamBuildListener;
import hudson.plugins.benchmarks.model.Report;
import hudson.plugins.benchmarks.parser.JSONParser;
import hudson.plugins.benchmarks.parser.ReportParser;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;

public class ReportTest {

    @Test
    public void testJSONParser() throws IOException {
        ReportParser parser = new JSONParser();
        Collection<Report> reports= parser.parse(Arrays.asList(new FilePath(new File("src/test/resources/com.mongodb.performance.bson-REPORT.json"))),
                new StreamBuildListener(System.out, Charset.defaultCharset()));
        System.out.println(reports);
    }

}
