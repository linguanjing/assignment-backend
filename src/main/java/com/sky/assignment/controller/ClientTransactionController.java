package com.sky.assignment.controller;

import com.sky.assignment.datamodel.BalanceJsonResponse;
import com.sky.assignment.datamodel.Client;
import com.sky.assignment.datamodel.ClientTransaction;
import com.sky.assignment.datamodel.TransferJsonRequest;
import com.sky.assignment.service.ClientService;
import com.sky.assignment.service.ClientTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@CrossOrigin(origins = { "http://localhost:3000"})
@Slf4j
@RestController
@RequestMapping("/api/v1/transaction")
public class ClientTransactionController {

    @Autowired
    private ClientTransactionService clientTransactionService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private MessageSource messageSource;


    /**
     * insert new transactions
     *
     * @param transferJsonRequest
     * @return
     */
    @PostMapping(value = "/new")
    public ResponseEntity<?> newTransfer(@RequestBody TransferJsonRequest transferJsonRequest){

        log.info("enter newTransaction");

        //assume the sender & receive are valid
        if ( transferJsonRequest != null
                && StringUtils.hasText(transferJsonRequest.getSenderId())
                && StringUtils.hasText(transferJsonRequest.getReceiverId())){

            Optional<Client> sender = clientService.getClientByLoginId(transferJsonRequest.getSenderId());
            Optional<Client> receiver = clientService.getClientByLoginId(transferJsonRequest.getReceiverId());

            ClientTransaction clientTransaction = new ClientTransaction(transferJsonRequest.getSenderId(),
                                                                        transferJsonRequest.getReceiverId(),
                                                                        transferJsonRequest.getAmount());

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

            }

            return ResponseEntity.ok(transferJsonRequest);
        }

        return ResponseEntity.ok("Invalid Transaction");
    }


    /**
     * get list of transaction message of the login user
     * @param loginClientId
     * @return
     */
    @GetMapping(value = "/{loginClientId}")
    public ResponseEntity<?> getTransationMessages(@PathVariable String loginClientId){
        log.info("enter getTransationMessages");

        if (log.isDebugEnabled()){
            log.debug("loginClientId = {} " , loginClientId);
        }

        List<String> transactionMessageList = new ArrayList<>();


        Optional<Client> client = clientService.getClientByLoginId(loginClientId);

        if (client.isPresent()){

            // hello message
            transactionMessageList.add(messageSource.getMessage("login.success", new Object[]{client.get().getDisplayName()},  Locale.ENGLISH));

            // calculate the balance
            double balance = clientTransactionService.getBalanceOfTheClient(client.get().getLoginId());
            transactionMessageList.add(messageSource.getMessage("transaction.balance", new Object[]{balance < 0 ? 0 : balance},  Locale.ENGLISH));

            // populate transaction message to be display on GUI
            transactionMessageList.addAll(clientTransactionService.generateTransactionMessage(client.get().getLoginId()));

        }


        return ResponseEntity.ok(transactionMessageList);
    }


}
