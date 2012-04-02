package hudson.plugins.benchmarks;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.util.IOException2;
import org.kohsuke.stapler.DataBoundConstructor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Parser for JUnit.
 *
 * @author Manuel Carrasco
 */
public class JUnitParser extends ReportParser {

    @Extension
    public static class DescriptorImpl extends BenchmarksReportParserDescriptor {
        @Override
        public String getDisplayName() {
            return "JUnit";
        }
    }

    @DataBoundConstructor
    public JUnitParser(String glob) {
        super(glob);
    }

    @Override
    public String getDefaultGlobPattern() {
        return "**/TEST-*.xml";
    }

    @Override
    public Collection<BenchmarksReport> parse(AbstractBuild<?, ?> build,
                                              Collection<File> reports, TaskListener listener) throws IOException {
        List<BenchmarksReport> result = new ArrayList<BenchmarksReport>();

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        PrintStream logger = listener.getLogger();

        for (File f : reports) {
            try {
                SAXParser parser = factory.newSAXParser();
                final BenchmarksReport r = new BenchmarksReport();
                r.setReportFileName(f.getName());
                logger.println("Performance: Parsing JUnit report file " + f.getName());
                parser.parse(f, new DefaultHandler() {
                    private Benchmark benchmark;
                    private int status;

                    @Override
                    public void endElement(String uri, String localName, String qName)
                            throws SAXException {
                        if (("testsuite".equalsIgnoreCase(qName) || "testcase".equalsIgnoreCase(qName))
                                && status != 0) {
                            r.addBenchmark(benchmark);
                            status = 0;
                        }
                    }

                    /**
                     * JUnit XML format is: tag "testcase" with attributes: "name" and "time".
                     * If there is one error, there is an other tag, "failure" inside testcase tag.
                     * SOAPUI uses JUnit format
                     */
                    @Override
                    public void startElement(String uri, String localName, String qName,
                                             Attributes attributes) throws SAXException {
                        if ("testcase".equalsIgnoreCase(qName)) {
                            if (status != 0) {
                                r.addBenchmark(benchmark);
                            }
                            status = 1;
                            benchmark = new Benchmark();
                            String time = attributes.getValue("time");
                            if (time != null) time = time.replaceAll("[^\\d\\.]+","");
                            double duration = Double.parseDouble(time);
                            benchmark.setDuration((long) (duration * 1000));
                            benchmark.setSuccessful(true);
                            benchmark.setName(attributes.getValue("name"));
                            benchmark.setGroup(attributes.getValue("classname"));
                        } else if ("failure".equalsIgnoreCase(qName) && status != 0) {
                            benchmark.setSuccessful(false);
                            r.addBenchmark(benchmark);
                            status = 0;
                        }
                    }
                });
                result.add(r);
            } catch (ParserConfigurationException e) {
                throw new IOException2("Failed to create parser ", e);
            } catch (SAXException e) {
                logger.println("Performance: Failed to parse " + f + ": "
                        + e.getMessage());
            }
        }

        return result;
    }
}
