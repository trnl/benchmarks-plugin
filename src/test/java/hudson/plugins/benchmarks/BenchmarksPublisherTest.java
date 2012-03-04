package hudson.plugins.benchmarks;

import hudson.model.FreeStyleProject;
import org.jvnet.hudson.test.HudsonTestCase;

import static java.util.Arrays.asList;

/**
 * @author Kohsuke Kawaguchi
 */
public class BenchmarksPublisherTest extends HudsonTestCase {
	public void testConfigRoundtrip() throws Exception {
		BenchmarksPublisher before = new BenchmarksPublisher(10, 20,
				asList(new JUnitParser("**/*.jtl")));

		FreeStyleProject p = createFreeStyleProject();
		p.getPublishersList().add(before);
		submit(createWebClient().getPage(p, "configure")
				.getFormByName("config"));

		BenchmarksPublisher after = p.getPublishersList().get(
				BenchmarksPublisher.class);
		assertEqualBeans(before, after,
				"errorFailedThreshold,errorUnstableThreshold");
		assertEquals(before.getParsers().size(), after.getParsers().size());
		assertEqualBeans(before.getParsers().get(0), after.getParsers().get(0),
				"glob");
		assertEquals(before.getParsers().get(0).getClass(), after.getParsers()
				.get(0).getClass());
	}

//	public void testBuild() throws Exception {
//		FreeStyleProject p = createFreeStyleProject();
//		p.getBuildersList().add(new TestBuilder() {
//			@Override
//			public boolean perform(AbstractBuild<?, ?> build,
//					Launcher launcher, BuildListener listener)
//					throws InterruptedException, IOException {
//				build.getWorkspace().child("test.jtl").copyFrom(
//						getClass().getResource("/JMeterResults.jtl"));
//				return true;
//			}
//		});
//		p.getPublishersList().add(
//				new BenchmarksPublisher(0, 0, asList(new JMeterParser(
//						"**/*.jtl"))));
//
//		FreeStyleBuild b = assertBuildStatusSuccess(p.scheduleBuild2(0).get());
//
//		BenchmarksBuildAction a = b.getAction(BenchmarksBuildAction.class);
//		assertNotNull(a);
//
//		// poke a few random pages to verify rendering
//		WebClient wc = createWebClient();
//		wc.getPage(b, "benchmarks");
//		wc
//				.getPage(b,
//						"benchmarks/uriReport/test.jtl;Home.endperformanceparameter/");
//	}
}
