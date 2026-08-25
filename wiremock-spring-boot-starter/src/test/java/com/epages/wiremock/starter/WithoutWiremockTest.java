package com.epages.wiremock.starter;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.tomakehurst.wiremock.WireMockServer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=TestApp.class)
@ActiveProfiles("test")
class WithoutWiremockTest {

	@Autowired(required = false)
	private WireMockServer server;

	@Test
	void should_not_have_wiremock_server() {
		assertThat(server).isNull();
	}
	
}
