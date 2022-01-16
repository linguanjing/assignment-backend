package com.sky.assignment;

import com.sky.assignment.datamodel.BalanceJsonResponse;
import com.sky.assignment.datamodel.Client;
import com.sky.assignment.datamodel.ClientTransaction;
import com.sky.assignment.service.ClientService;
import com.sky.assignment.service.ClientTransactionService;
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

    @Autowired
    private ClientTransactionService clientTransactionService;

    private static final String BASE_URL = "/api/v1/client";

    @BeforeAll
    public void init(){
        Client alice = new Client("alice","password", "Alice");
        clientService.createNewClient(alice);

        Client bob = new Client("bob","password", "Bob");
        clientService.createNewClient(bob);

        // Alice tot op up 100
        ClientTransaction transaction = new ClientTransaction(alice.getLoginId(), alice.getLoginId(), 100);
        transaction.setSenderBalance(0);
        transaction.setReceiverBalance(0);
        clientTransactionService.insertTransaction(transaction);

        // Bob tot op up 80
        ClientTransaction transaction1 = new ClientTransaction(bob.getLoginId(), bob.getLoginId(), 80);
        transaction1.setSenderBalance(0);
        transaction1.setReceiverBalance(0);
        clientTransactionService.insertTransaction(transaction1);

        // Bob pay Alice 50
        ClientTransaction transaction2 = new ClientTransaction(bob.getLoginId(), alice.getLoginId(), 50);
        transaction2.setSenderBalance(80);
        transaction2.setReceiverBalance(100);
        clientTransactionService.insertTransaction(transaction2);

        // Bob pay Alice 100
        ClientTransaction transaction3 = new ClientTransaction(bob.getLoginId(), alice.getLoginId(), 100);
        transaction3.setSenderBalance(30);
        transaction3.setReceiverBalance(150);
        clientTransactionService.insertTransaction(transaction3);
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

        ResponseEntity<BalanceJsonResponse> response = template.postForEntity(new StringBuffer(BASE_URL).append("/login").toString(), request, BalanceJsonResponse.class);

        assertThat( response.getBody().getLoginUserId()).isEqualTo(alice.getLoginId());
        assertThat( response.getBody().getTransactionMessageList().get(1)).isEqualTo("Your balance is 250.");
    }


    @Test
    public void getReceiverList() throws Exception {
        Client alice = new Client("alice","password", "Alice");

        ResponseEntity<List> response =
                template.getForEntity(new StringBuffer(BASE_URL).append("/").append(alice.getLoginId()).append("/receivers").toString(), List.class);

        assertThat( response.getBody()).isNotNull();
    }

}
