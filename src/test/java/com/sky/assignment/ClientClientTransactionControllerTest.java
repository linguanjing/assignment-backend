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


import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClientClientTransactionControllerTest {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientTransactionService clientTransactionService;

    private static final String BASE_URL = "/api/v1/transaction";

    @BeforeAll
    public void init(){
        Client alice = new Client("alice","password", "Alice");
        clientService.createNewClient(alice);

        Client bob = new Client("bob","password", "Bob");
        clientService.createNewClient(bob);


        Client sky = new Client("sky","password", "Sky");
        clientService.createNewClient(sky);

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

        // Sky tot op up 50
        ClientTransaction transaction2 = new ClientTransaction(sky.getLoginId(), sky.getLoginId(),50);
        transaction2.setSenderBalance(0);
        transaction2.setReceiverBalance(0);
        clientTransactionService.insertTransaction(transaction2);

        // Alice pay Bob 100
//        ClientTransaction transaction3 = new ClientTransaction(alice.getLoginId(), bob.getLoginId(), Double.valueOf(100));
//        transaction3.setSenderBalance(0);
//        transaction3.setReceiverBalance(180);
//        clientTransactionService.insertTransaction(transaction3);
//
//        // Bob pay Sky 20
//        ClientTransaction transaction4 = new ClientTransaction(bob.getLoginId(), sky.getLoginId(), Double.valueOf(20));
//        transaction4.setSenderBalance(160);
//        transaction4.setReceiverBalance(70);
//        clientTransactionService.insertTransaction(transaction4);
//
//        // Alice pay Bob 20
//        ClientTransaction transaction5 = new ClientTransaction(alice.getLoginId(), bob.getLoginId(), Double.valueOf(20));
//        transaction5.setSenderBalance(-20);
//        transaction5.setReceiverBalance(140);
//        clientTransactionService.insertTransaction(transaction5);
//
//        // Alice pay Sky 80
//        ClientTransaction transaction6 = new ClientTransaction(alice.getLoginId(), sky.getLoginId(), Double.valueOf(80));
//        transaction6.setSenderBalance(-100);
//        transaction6.setReceiverBalance(150);
//        clientTransactionService.insertTransaction(transaction6);
    }

    @Test
    public void newTransactionComplex() {
        Client alice = clientService.getClientByLoginId("alice").get();
        Client bob = clientService.getClientByLoginId("bob").get();
        Client sky = clientService.getClientByLoginId("sky").get();

        // Alice pay Bob 120
        // expected Alice - 20 & Bob = 200
        ClientTransaction transaction = new ClientTransaction(alice.getLoginId(), bob.getLoginId(), 120);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-COM-PERSIST", "true");

        HttpEntity<ClientTransaction> request = new HttpEntity<>(transaction, headers);

        ResponseEntity<BalanceJsonResponse> response = template.postForEntity(new StringBuffer(BASE_URL).append("/new").toString(), request, BalanceJsonResponse.class);

        assertThat( response.getBody().getLoginUserId()).isEqualTo(alice.getLoginId());
        assertThat( response.getBody().getTransactionMessageList().get(1)).isEqualTo("Owing 20 to Bob.");


        // Bob pay Alice 250
        // expected Alice 230 & Bob = -50
        ClientTransaction transaction2 = new ClientTransaction(bob.getLoginId(),alice.getLoginId(),  250);

        HttpEntity<ClientTransaction> request2 = new HttpEntity<>(transaction2, headers);

        ResponseEntity<BalanceJsonResponse> response2 = template.postForEntity(new StringBuffer(BASE_URL).append("/new").toString(), request2, BalanceJsonResponse.class);


        assertThat( response2.getBody().getLoginUserId()).isEqualTo(bob.getLoginId());
        assertThat( response2.getBody().getTransactionMessageList().get(1)).isEqualTo("Owing 50 to Alice.");


        // Bob pay Sky 30
        // expected Bob -80 & Sky = 80
        ClientTransaction transaction3 = new ClientTransaction(bob.getLoginId(),sky.getLoginId(),  30);

        HttpEntity<ClientTransaction> request3 = new HttpEntity<>(transaction3, headers);

        ResponseEntity<BalanceJsonResponse> response3 = template.postForEntity(new StringBuffer(BASE_URL).append("/new").toString(), request3, BalanceJsonResponse.class);


        assertThat( response3.getBody().getLoginUserId()).isEqualTo(bob.getLoginId());
        assertThat( response3.getBody().getTransactionMessageList().get(1)).isEqualTo("Owing 30 to Sky.");


        // Bob pay Sky 50
        // expected Bob -130 & Sky = 130
        ClientTransaction transaction4 = new ClientTransaction(bob.getLoginId(),sky.getLoginId(),  50);

        HttpEntity<ClientTransaction> request4 = new HttpEntity<>(transaction4, headers);

        ResponseEntity<BalanceJsonResponse> response4 = template.postForEntity(new StringBuffer(BASE_URL).append("/new").toString(), request4, BalanceJsonResponse.class);


        assertThat( response4.getBody().getLoginUserId()).isEqualTo(bob.getLoginId());
        assertThat( response4.getBody().getTransactionMessageList().get(1)).isEqualTo("Owing 50 to Sky.");
    }

}
