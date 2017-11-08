package com.epages.wiremock.starter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.github.tomakehurst.wiremock.WireMockServer;

@WireMockTest
@SpringBootTest
@RunWith(SpringRunner.class)
public class RandomPortSmokeTest {

	@Autowired
	private WireMockServer server;

	@Test
	public void check_server_availability() {
		assertThat(server.isRunning()).isTrue();
		ResponseEntity<String> result = new RestTemplate().exchange("http://localhost:" + server.port() + "/__admin", HttpMethod.GET, null, String.class);
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

}
