package com.sky.assignment;

import com.sky.assignment.datamodel.Client;
import com.sky.assignment.service.ClientService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClientControllerTest {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ClientService clientService;


    private static final String BASE_URL = "/api/v1/client";

    @BeforeAll
    public void init(){
        Client alice = new Client("alice","password", "Alice");
        clientService.createNewClient(alice);

        Client bob = new Client("bob","password", "Bob");
        clientService.createNewClient(bob);

    }


    @Test
    public void createNewClient() throws Exception {

        Client client = new Client("sky_new","password", "Sky Lin");

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-COM-PERSIST", "true");

        HttpEntity<Client> request = new HttpEntity<>(client, headers);

        ResponseEntity<Client> response = template.postForEntity(new StringBuffer(BASE_URL).append("/signup").toString(), request, Client.class);

        assertThat(response.getBody()).isNotNull().isEqualTo(client);
    }


    @Test
    public void userLogin() throws Exception {
        Client alice = new Client("alice","password", "Alice");

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-COM-PERSIST", "true");

        HttpEntity<Client> request = new HttpEntity<>(alice, headers);

        ResponseEntity<Client> response = template.postForEntity(new StringBuffer(BASE_URL).append("/login").toString(), request, Client.class);

        assertThat( response.getBody().getLoginId()).isEqualTo(alice.getLoginId());
    }


    @Test
    public void getReceiverList() throws Exception {
        Client alice = new Client("alice","password", "Alice");

        ResponseEntity<List> response =
                template.getForEntity(new StringBuffer(BASE_URL).append("/").append(alice.getLoginId()).append("/receivers").toString(), List.class);

        assertThat( response.getBody()).isNotNull();
    }

}
