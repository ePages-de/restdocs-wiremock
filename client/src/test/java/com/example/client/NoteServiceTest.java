package com.example.client;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;

import com.epages.wiremock.starter.WireMockTest;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ClientApplication.class })
@ActiveProfiles("test")
@WireMockTest(stubPath = "wiremock/restdocs-server")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NoteServiceTest {

	@Autowired
	private NoteService noteService;

	@Autowired
	private WireMockServer server;

	@Test
	@WireMockTest(stubPath = "mappings/note-get-example")
	public void should_1_use_dedicated_wiremock_stub_returning_path_id_in_response_body() {
		UUID id = UUID.randomUUID();
		final Note note = noteService.getNote(id.toString());
		assertEquals("REST maturity model", note.getTitle());
		assertEquals(id, note.getId());
	}

	@Test(expected = HttpClientErrorException.class)
	@WireMockTest(stubPath = "mappings/note-badrequest-example")
	public void should_2_use_different_wiremock_stub() {
		noteService.getNote("xy");
	}

	@Test
	public void should_3_use_default_wiremock_stubs() {
		assertEquals("REST maturity model", noteService.getNote(UUID.randomUUID().toString()).getTitle());
	}

	@Test(expected = HttpClientErrorException.class)
	@WireMockTest(stubPath = "mappings/note-badrequest-example")
	public void should_4_not_find_excluded_wiremock_stub() {
		noteService.getNote("1");
	}

	@Test
	public void should_print_wiremock_mappings() {
		List<StubMapping> mappings = server.listAllStubMappings().getMappings();
		System.out.println("\n\nRegistered WireMock Mappings:\n" + mappings);
		assertEquals(11, mappings.size());
	}

}
