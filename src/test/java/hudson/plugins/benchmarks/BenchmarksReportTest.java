package hudson.plugins.benchmarks;

import hudson.util.StreamTaskListener;
import org.easymock.classextension.EasyMock;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;

public class BenchmarksReportTest {

    private BenchmarksReport benchmarksReport;

    @Before
    public void setUp() throws Exception {
        BenchmarksBuildAction buildAction = EasyMock
                .createMock(BenchmarksBuildAction.class);
        benchmarksReport = new BenchmarksReport();
        benchmarksReport.setBuildAction(buildAction);
    }
//
//    @org.junit.Test
//    public void testAddBenchmark() throws Exception {
//        PrintStream printStream = EasyMock.createMock(PrintStream.class);
//        EasyMock.expect(
//                benchmarksReport.getBuildAction().getHudsonConsoleWriter())
//                .andReturn(printStream);
//        printStream
//                .println("label cannot be empty, please ensure your jmx file specifies name properly for each http sample: skipping sample");
//        EasyMock.replay(printStream);
//        EasyMock.replay(benchmarksReport.getBuildAction());
//
//        Benchmark b = new Benchmark();
//        benchmarksReport.addBenchmark(b);
//
//        b.setName("invalidCharacter/");
//        benchmarksReport.addBenchmark(b);
//        Benchmark benchmark = benchmarksReport.get(
//                "invalidCharacter_");
//        assertNotNull(benchmark);
//
//        String uri = "uri";
//        b.setName(uri);
//        benchmarksReport.addBenchmark(b);
//        benchmark = benchmarksReport.get(uri);
//        assertNotNull(benchmark);
//    }


//	@Test
//	public void testPerformanceReport() throws IOException, SAXException {
//		BenchmarksReport benchmarksReport = parseOneJMeter(new File(
//				"src/test/resources/JMeterResults.jtl"));
//		Map<String, UriReport> uriReportMap = benchmarksReport
//				.getUriReportMap();
//		assertEquals(2, uriReportMap.size());
//		String loginUri = "Home";
//		UriReport firstUriReport = uriReportMap.get(loginUri);
//		Test firstHttpSample = firstUriReport.getTestList().get(0);
//		assertEquals(loginUri, firstHttpSample.getUri());
//		assertEquals(14720, firstHttpSample.getDuration());
//		assertEquals(new Date(1296846793179L), firstHttpSample.getDate());
//		assertTrue(firstHttpSample.isSuccessful());
//		String logoutUri = "Workgroup";
//		UriReport secondUriReport = uriReportMap.get(logoutUri);
//		Test secondHttpSample = secondUriReport.getTestList()
//				.get(0);
//		assertEquals(logoutUri, secondHttpSample.getUri());
//		assertEquals(278, secondHttpSample.getDuration());
//		assertEquals(new Date(1296846847952L), secondHttpSample.getDate());
//		assertTrue(secondHttpSample.isSuccessful());
//	}

    private BenchmarksReport parseOneJUnit(File f) throws IOException {
        return new JUnitParser("").parse(null, Collections.singleton(f),
                new StreamTaskListener(System.out)).iterator().next();
    }

//	@Test
//	public void testPerformanceNonHTTPSamplesMultiThread() throws IOException,
//			SAXException {
//		BenchmarksReport benchmarksReport = parseOneJMeter(new File(
//				"src/test/resources/JMeterResultsMultiThread.jtl"));
//
//		Map<String, UriReport> uriReportMap = benchmarksReport
//				.getUriReportMap();
//		assertEquals(1, uriReportMap.size());
//
//		String uri = "WebService(SOAP) Request";
//		UriReport report = uriReportMap.get(uri);
//		assertNotNull(report);
//
//		int[] expectedDurations = {894, 1508, 1384, 1581, 996};
//		for (int i = 0; i < expectedDurations.length; i++) {
//			Test sample = report.getTestList().get(i);
//			assertEquals(expectedDurations[i], sample.getDuration());
//		}
//	}

//	@Test
//	public void testPerformanceReportJUnit() throws IOException, SAXException {
//		BenchmarksReport benchmarksReport = parseOneJUnit(new File(
//				"src/test/resources/TEST-JUnitResults.xml"));
//		Map<String, UriReport> uriReportMap = benchmarksReport
//				.getUriReportMap();
//		assertEquals(5, uriReportMap.size());
//		String firstUri = "testGetMin";
//		UriReport firstUriReport = uriReportMap.get(firstUri);
//		Test firstHttpSample = firstUriReport.getTestList().get(0);
//		assertEquals(firstUri, firstHttpSample.getUri());
//		assertEquals(31, firstHttpSample.getDuration());
//		assertEquals(new Date(0L), firstHttpSample.getDate());
//		assertTrue(firstHttpSample.isSuccessful());
//		String lastUri = "testGetMax";
//		UriReport secondUriReport = uriReportMap.get(lastUri);
//		Test secondHttpSample = secondUriReport.getTestList()
//				.get(0);
//		assertEquals(lastUri, secondHttpSample.getUri());
//		assertEquals(26, secondHttpSample.getDuration());
//		assertEquals(new Date(0L), secondHttpSample.getDate());
//		assertFalse(secondHttpSample.isSuccessful());
//	}

//	 @Test
//	public void testIssue5571() throws IOException, SAXException {
//	  BenchmarksReport benchmarksReport = parseOneJUnit(new File(
//	        "src/test/resources/jUnitIssue5571.xml"));
//	    Map<String, UriReport> uriReportMap = benchmarksReport
//	        .getUriReportMap();
//	    assertEquals(1, uriReportMap.size());
//	    String uri = "unknown";
//	    UriReport report = uriReportMap.get(uri);
//	    Test firstHttpSample = report.getTestList().get(0);
//	    assertEquals(uri, firstHttpSample.getUri());
//	    assertEquals(890, firstHttpSample.getDuration());
//	    assertEquals(new Date(0L), firstHttpSample.getDate());
//	    assertTrue(firstHttpSample.isSuccessful());
//
//	    Test secondHttpSample = report.getTestList().get(1);
//      assertEquals(uri, secondHttpSample.getUri());
//      assertEquals(50, secondHttpSample.getDuration());
//      assertEquals(new Date(0L), secondHttpSample.getDate());
//      assertTrue(secondHttpSample.isSuccessful());
//
//      assertEquals(33, report.getMedian());
//	}
}
