package com.epages.wiremock.starter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

@AutoConfiguration
@ConditionalOnClass({WireMockServer.class})
@ConditionalOnProperty(name="wiremock.enabled", havingValue="true")
@EnableConfigurationProperties(WireMockProperties.class)
public class WireMockAutoConfiguration {

	private static final Log log = LogFactory.getLog(WireMockAutoConfiguration.class);

	@Bean
	@ConditionalOnMissingBean
	public WireMockConfiguration wireMockConfiguration(WireMockProperties properties) {
		WireMockConfiguration config = WireMockConfiguration.options();
		if(properties.getPort() > 0) {
			log.info("Starting WireMock on port " + properties.getPort());
			config.port(properties.getPort());
		} else {
			log.info("Starting WireMock on dynamic port");
			config.dynamicPort();
		}
		if(properties.getStubPath() != null) {
			config.fileSource(new ClasspathFileSource(properties.getStubPath()));
		}
		return config;
	}

	@Bean
	public WireMockServer wireMockServer(WireMockConfiguration config) {
		return new WireMockServer(config);
	}

}
