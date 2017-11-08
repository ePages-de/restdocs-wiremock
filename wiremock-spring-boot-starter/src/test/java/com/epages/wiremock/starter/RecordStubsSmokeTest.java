package com.epages.wiremock.starter;

import static com.github.tomakehurst.wiremock.recording.RecordingStatus.Recording;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;

@WireMockTest(port = 8081, record = true, targetBaseUrl = "http://localhost:8082", stubPath = "build/wiremock-stubs")
@SpringBootTest(
		classes = {TestApp.class, RecordStubsSmokeTest.ControllerConfiguration.class},
		webEnvironment = DEFINED_PORT ,
		properties = "server.port=8082")
@RunWith(SpringRunner.class)
public class RecordStubsSmokeTest {

	@Autowired
	private WireMockServer server;

	@Test
	public void should_record_mapping() {

		assertThat(server.isRunning()).isTrue();
		assertThat(server.getRecordingStatus().getStatus()).isEqualTo(Recording);
		assertThat(server.port()).isEqualTo(8081);

		TestData result = new RestTemplate().postForObject("http://localhost:" + server.port() + "/some", new TestData("some"), TestData.class);
		assertThat(result).isNotNull();
		thenMappingRecorded();
	}


	private void thenMappingRecorded() {
		server.stopRecording();
		assertThat(getOutputMappingPath()).exists();
		assertThat(new SingleRootFileSource(getOutputMappingPath().toString()).listFilesRecursively()).hasSize(1);
	}

	private Path getOutputMappingPath() {
		return Paths.get("build", "wiremock-stubs", "mappings");
	}

	@Configuration
	static class ControllerConfiguration {

		@RestController
		static class TestController {

			@PostMapping("/some")
			ResponseEntity<TestData> doSomething(@RequestBody TestData testData) {
				return ResponseEntity.ok(testData);
			}
		}
	}

	static class TestData {
		private String some;

		TestData() { }

		TestData(String some) {
			this.some = some;
		}

		public String getSome() {
			return some;
		}

		public void setSome(String some) {
			this.some = some;
		}
	}

}
