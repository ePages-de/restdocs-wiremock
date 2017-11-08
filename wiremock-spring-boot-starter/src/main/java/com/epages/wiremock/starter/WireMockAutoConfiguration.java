package com.epages.wiremock.starter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

@Configuration
@ConditionalOnClass({WireMockServer.class})
@ConditionalOnProperty(name="wiremock.enabled", havingValue="true")
@EnableConfigurationProperties(WireMockProperties.class)
public class WireMockAutoConfiguration {

	private static final Log log = LogFactory.getLog(WireMockAutoConfiguration.class);

	@Bean
	@ConditionalOnMissingBean
	public WireMockConfiguration wireMockConfiguration(WireMockProperties properties) {
		WireMockConfiguration config = WireMockConfiguration.options();

		if (properties.getPort() > 0) {
			log.info("Starting WireMock on port " + properties.getPort());
			config.port(properties.getPort());
		} else {
			log.info("Starting WireMock on dynamic port");
			config.dynamicPort();
		}
		if (properties.getStubPath() != null) {
			final ClasspathFileSource classpathFileSource = new ClasspathFileSource(properties.getStubPath());
			if (classpathFileSource.exists()) {
				config.fileSource(classpathFileSource);
			} else {
				final SingleRootFileSource fileSource = new SingleRootFileSource(properties.getStubPath());
				fileSource.child("mappings").createIfNecessary();
				config.fileSource(fileSource);
			}
		}
		if (!properties.getExtensions().isEmpty()) {
			config.extensions(properties.getExtensions().toArray(new String[0]));
		}
		return config;
	}

	@Bean
	public WireMockServer wireMockServer(WireMockConfiguration config) {
		return new WireMockServer(config);
	}

}
