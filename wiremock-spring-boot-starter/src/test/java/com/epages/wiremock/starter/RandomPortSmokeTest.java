package com.epages.wiremock.starter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import com.github.tomakehurst.wiremock.WireMockServer;

@WireMockTest
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Configuration
class RandomPortSmokeTest {

	@Autowired
	public WireMockServer server;

	@Test
	void check_server_availability() {
		assertThat(server.isRunning()).isTrue();
		ResponseEntity<String> result = new RestTemplate().exchange("http://localhost:" + server.port() + "/__admin", HttpMethod.GET, null, String.class);
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

}
