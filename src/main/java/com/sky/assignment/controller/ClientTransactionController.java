package com.sky.assignment.controller;

import com.sky.assignment.datamodel.BalanceJsonResponse;
import com.sky.assignment.datamodel.Client;
import com.sky.assignment.datamodel.ClientTransaction;
import com.sky.assignment.service.BalanceJsonService;
import com.sky.assignment.service.ClientService;
import com.sky.assignment.service.ClientTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = { "http://localhost:3000"})
@Slf4j
@RestController
@RequestMapping("/api/v1/transaction")
public class ClientTransactionController {

    @Autowired
    private BalanceJsonService balanceJsonService;

    @Autowired
    private ClientTransactionService clientTransactionService;

    @Autowired
    private ClientService clientService;


    /**
     * insert new transactions
     * @param clientTransaction
     * @return
     */
    @PostMapping(value = "/new")
    public ResponseEntity<?> newTransfer(@RequestBody ClientTransaction clientTransaction){

        log.info("enter newTransaction");

        //assume the sender & receive are valid
        if ( clientTransaction != null
                && StringUtils.hasText(clientTransaction.getSenderId())
                && StringUtils.hasText(clientTransaction.getReceiverId())){

            Optional<Client> sender = clientService.getClientByLoginId(clientTransaction.getSenderId());
            Optional<Client> receiver = clientService.getClientByLoginId(clientTransaction.getReceiverId());

            List<String>  transactionMessageList = new ArrayList<>();

            // both sender & receiver are in DB
            if (sender.isPresent() && receiver.isPresent()){

                clientTransaction.setSender(sender.get());
                clientTransaction.setReceiver(receiver.get());

                // set sender current balance
                double senderBalance = clientTransactionService.getBalanceOfTheClient(clientTransaction.getSenderId());
                clientTransaction.setSenderBalance(senderBalance);

                // set receiver current balance
                double recevierBalance = clientTransactionService.getBalanceOfTheClient(clientTransaction.getReceiverId());
                clientTransaction.setReceiverBalance(recevierBalance);

                // insert transaction
                clientTransactionService.insertTransaction(clientTransaction);

                //populate transaction message to be display on GUI
                transactionMessageList = clientTransactionService.generateTransactionMessage(clientTransaction);
            }

            // return the balance Json according to the login user which is the sender
            BalanceJsonResponse balanceJsonResponse = balanceJsonService.intiBalanceJsonResponseByClient(clientTransaction.getSenderId(), false);
            balanceJsonResponse.setTransactionMessageList(transactionMessageList);

            return ResponseEntity.ok(balanceJsonResponse);
        }

        return ResponseEntity.ok("Invalid Transaction");
    }

}
