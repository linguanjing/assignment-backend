package com.sky.assignment;

import com.sky.assignment.datamodel.TransferJsonRequest;
import com.sky.assignment.datamodel.Client;
import com.sky.assignment.datamodel.ClientTransaction;
import com.sky.assignment.datamodel.TransferJsonRequest;
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
public class ClientClientTransactionControllerTest {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientTransactionService clientTransactionService;

    private static final String BASE_URL = "/api/v1/transaction";

    @BeforeAll
    public void init() {
        Client alice = new Client("alice", "password", "Alice");
        clientService.createNewClient(alice);

        Client bob = new Client("bob", "password", "Bob");
        clientService.createNewClient(bob);


        Client sky = new Client("sky", "password", "Sky");
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
        ClientTransaction transaction2 = new ClientTransaction(sky.getLoginId(), sky.getLoginId(), 50);
        transaction2.setSenderBalance(0);
        transaction2.setReceiverBalance(0);
        clientTransactionService.insertTransaction(transaction2);

    }

    /**
     * before Alice = 100, bob = 80
     * Alice pay Bob 120
     * after Alice - 20 & Bob = 200
     */
    @Test
    public void newTransaction1() {
        Client alice = clientService.getClientByLoginId("alice").get();
        Client bob = clientService.getClientByLoginId("bob").get();

        // Alice pay Bob 120
        TransferJsonRequest transferJsonRequest = new TransferJsonRequest(alice.getLoginId(), bob.getLoginId(), 120);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-COM-PERSIST", "true");

        HttpEntity<TransferJsonRequest> request = new HttpEntity<>(transferJsonRequest, headers);

        ResponseEntity<TransferJsonRequest> response = template.postForEntity(new StringBuffer(BASE_URL).append("/new").toString(), request, TransferJsonRequest.class);

        assertThat(response.getBody().getSenderId()).isEqualTo(alice.getLoginId());

        // expected Alice - 20 & Bob = 200
        ResponseEntity<List> senderMessage =
                template.getForEntity(new StringBuffer(BASE_URL).append("/").append(alice.getLoginId()).toString(), List.class);

        assertThat(senderMessage.getBody().get(1)).isEqualTo("Your balance is 0.");
        assertThat(senderMessage.getBody().get(2)).isEqualTo("Owing 20 to Bob.");

        ResponseEntity<List> receiverMessage =
                template.getForEntity(new StringBuffer(BASE_URL).append("/").append(bob.getLoginId()).toString(), List.class);

        assertThat(receiverMessage.getBody().get(1)).isEqualTo("Your balance is 200.");
        assertThat(receiverMessage.getBody().get(2)).isEqualTo("Owing 20 from Alice.");

    }


    /**
     * before Alice = -20, bob = 200
     * Bob pay Alice 250
     * after Alice 230 & Bob = -50
     */
    @Test
    public void newTransaction2() {
        Client alice = clientService.getClientByLoginId("alice").get();
        Client bob = clientService.getClientByLoginId("bob").get();

        // Bob pay Alice 250
        TransferJsonRequest transferJsonRequest = new TransferJsonRequest(bob.getLoginId(),alice.getLoginId(),  250);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-COM-PERSIST", "true");

        HttpEntity<TransferJsonRequest> request = new HttpEntity<>(transferJsonRequest, headers);

        ResponseEntity<TransferJsonRequest> response = template.postForEntity(new StringBuffer(BASE_URL).append("/new").toString(), request, TransferJsonRequest.class);

        assertThat(response.getBody().getSenderId()).isEqualTo(bob.getLoginId());


        ResponseEntity<List> senderMessage =
                template.getForEntity(new StringBuffer(BASE_URL).append("/").append(bob.getLoginId()).toString(), List.class);

        assertThat(senderMessage.getBody().get(1)).isEqualTo("Your balance is 0.");
        assertThat(senderMessage.getBody().get(2)).isEqualTo("Owing 50 to Alice.");

        ResponseEntity<List> receiverMessage =
                template.getForEntity(new StringBuffer(BASE_URL).append("/").append(alice.getLoginId()).toString(), List.class);

        assertThat(receiverMessage.getBody().get(1)).isEqualTo("Your balance is 230.");
        assertThat(receiverMessage.getBody().get(2)).isEqualTo("Owing 50 from Bob.");
    }


    /**
     * before Alice 230 & Bob = -50
     * Alice pay Bob 60
     * after Alice 170 & Bob = 10
     * no owning message
     */
    @Test
    public void newTransaction3() {
        Client sender = clientService.getClientByLoginId("alice").get();
        Client receiver = clientService.getClientByLoginId("bob").get();

        // Alice pay Bob 60
        TransferJsonRequest transferJsonRequest = new TransferJsonRequest(sender.getLoginId(), receiver.getLoginId(),  60);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-COM-PERSIST", "true");

        HttpEntity<TransferJsonRequest> request = new HttpEntity<>(transferJsonRequest, headers);

        ResponseEntity<TransferJsonRequest> response = template.postForEntity(new StringBuffer(BASE_URL).append("/new").toString(), request, TransferJsonRequest.class);

        assertThat(response.getBody().getSenderId()).isEqualTo(sender.getLoginId());


        ResponseEntity<List> senderMessage =
                template.getForEntity(new StringBuffer(BASE_URL).append("/").append(sender.getLoginId()).toString(), List.class);

        assertThat(senderMessage.getBody().size()).isEqualTo(2);
        assertThat(senderMessage.getBody().get(1)).isEqualTo("Your balance is 170.");

        ResponseEntity<List> receiverMessage =
                template.getForEntity(new StringBuffer(BASE_URL).append("/").append(receiver.getLoginId()).toString(), List.class);

        assertThat(receiverMessage.getBody().size()).isEqualTo(2);
        assertThat(receiverMessage.getBody().get(1)).isEqualTo("Your balance is 10.");
    }


    /**
     * before Alice 170 & Bob = 10 & Sky = 50
     * Bob pay Sky 60
     * after Alice 170 & Bob = -50 & Sky = 110
     * no owning message
     */
    @Test
    public void newTransaction4() {
        Client sender = clientService.getClientByLoginId("bob").get();
        Client receiver = clientService.getClientByLoginId("sky").get();

        // Alice pay Bob 60
        TransferJsonRequest transferJsonRequest = new TransferJsonRequest(sender.getLoginId(), receiver.getLoginId(),  60);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-COM-PERSIST", "true");

        HttpEntity<TransferJsonRequest> request = new HttpEntity<>(transferJsonRequest, headers);

        ResponseEntity<TransferJsonRequest> response = template.postForEntity(new StringBuffer(BASE_URL).append("/new").toString(), request, TransferJsonRequest.class);

        assertThat(response.getBody().getSenderId()).isEqualTo(sender.getLoginId());


        ResponseEntity<List> senderMessage =
                template.getForEntity(new StringBuffer(BASE_URL).append("/").append(sender.getLoginId()).toString(), List.class);

        assertThat(senderMessage.getBody().size()).isEqualTo(3);
        assertThat(senderMessage.getBody().get(1)).isEqualTo("Your balance is 0.");
        assertThat(senderMessage.getBody().get(2)).isEqualTo("Owing 50 to Sky.");

        ResponseEntity<List> receiverMessage =
                template.getForEntity(new StringBuffer(BASE_URL).append("/").append(receiver.getLoginId()).toString(), List.class);

        assertThat(receiverMessage.getBody().size()).isEqualTo(3);
        assertThat(receiverMessage.getBody().get(1)).isEqualTo("Your balance is 110.");
        assertThat(receiverMessage.getBody().get(2)).isEqualTo("Owing 50 from Bob.");
    }
}
