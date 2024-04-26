package com.epages.wiremock.starter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSource;

class WireMockListener extends AbstractTestExecutionListener implements Ordered {

	private WireMockTest wireMockAnnotation;

	@Override
	public void prepareTestInstance(TestContext testContext) {
		if(wireMockAnnotation == null) {
			return;
		}

		ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) testContext
				.getApplicationContext();

		applicationContext.getBean(WireMockServer.class).start();
	}

	@Override
	public void beforeTestClass(TestContext testContext) {
		wireMockAnnotation = testContext.getTestClass().getAnnotation(WireMockTest.class);
		if(wireMockAnnotation == null) {
			return;
		}

		int port = wireMockAnnotation.port() > 0 ? wireMockAnnotation.port() : InetUtils.getFreeServerPort();
		ArrayList<String> properties = new ArrayList<>();
		properties.add("wiremock.port=" + port);
		properties.add("wiremock.enabled=true");
		properties.add("wiremock.stubPath=" + wireMockAnnotation.stubPath());
		properties.add("ribbon.eureka.enabled=false");
		for (String service : wireMockAnnotation.ribbonServices()) {
			properties.add(service + ".ribbon.listOfServers=localhost:" + port);
		}

		addPropertySourceProperties(testContext, properties.toArray(new String[0]));
	}

	@Override
	public void beforeTestMethod(TestContext testContext) {
		if(wireMockAnnotation == null) {
			return;
		}
		WireMockTest methodAnnotation = testContext.getTestMethod().getAnnotation(WireMockTest.class);
		
		String stubPath = "";
		if(this.wireMockAnnotation.stubPath() != null) {
			stubPath = this.wireMockAnnotation.stubPath();
		}
		if (methodAnnotation != null && methodAnnotation.stubPath() != null) {
			stubPath += "/" + methodAnnotation.stubPath();
		}

		ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) testContext
				.getApplicationContext();

		WireMockServer server = applicationContext.getBean(WireMockServer.class);
		server.resetMappings();
		if(! stubPath.isEmpty()) {
			server.loadMappingsUsing(new JsonFileMappingsSource(new ClasspathFileSource(stubPath), null));
		}
	}

	@Override
	public void afterTestMethod(TestContext testContext) {
		if(wireMockAnnotation == null) {
			return;
		}
		ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) testContext
				.getApplicationContext();

		applicationContext.getBean(WireMockServer.class).resetToDefaultMappings();
	}

	@Override
	public void afterTestClass(TestContext testContext) {
		if(wireMockAnnotation == null) {
			return;
		}
		ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) testContext
				.getApplicationContext();

		applicationContext.getBean(WireMockServer.class).stop();
	}

	private void addPropertySourceProperties(TestContext testContext, String[] properties) {
		try {
			MergedContextConfiguration configuration = (MergedContextConfiguration) ReflectionTestUtils
					.getField(testContext, "mergedContextConfiguration");
			new MergedContextConfigurationProperties(configuration).add(properties);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	static class MergedContextConfigurationProperties {

		private final MergedContextConfiguration configuration;

		MergedContextConfigurationProperties(MergedContextConfiguration configuration) {
			this.configuration = configuration;
		}

		void add(String[] properties, String... additional) {
			Set<String> merged = new LinkedHashSet<>(
					Arrays.asList(this.configuration.getPropertySourceProperties()));
			merged.addAll(Arrays.asList(properties));
			merged.addAll(Arrays.asList(additional));
			ReflectionTestUtils.setField(this.configuration, "propertySourceProperties",
					merged.toArray(new String[merged.size()]));
		}

	}

}
