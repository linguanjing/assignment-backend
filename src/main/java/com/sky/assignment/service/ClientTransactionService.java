package com.sky.assignment.service;

import com.sky.assignment.datamodel.Client;
import com.sky.assignment.datamodel.ClientTransaction;
import com.sky.assignment.repository.ClientTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Service
public class ClientTransactionService {

    @Autowired
    private ClientTransactionRepository repository;

    @Autowired
    private MessageSource messageSource;


    /**
     * Insert transaction into DB
     * Since every payment need to stored, so only can do insertion and not updating
     *
     * @param clientTransaction
     * @return null if the transaction obj is null
     */
    public ClientTransaction insertTransaction(ClientTransaction clientTransaction) {

        if (clientTransaction != null) {
            clientTransaction.setTransactionTime(LocalTime.now());
            return repository.save(clientTransaction);
        }

        return null;
    }

    /**
     * find the list of transaction under the login client ID
     *
     * @param clientId
     * @return
     */
    public List<ClientTransaction> getTransactionListByClientId(String clientId) {

        List<ClientTransaction> returnList = new ArrayList<>();
        if (StringUtils.hasText(clientId)) {
            returnList = repository.findTransationByClientId(clientId);
        }
        return returnList;
    }


    public List<String> generateTransactionMessage(ClientTransaction clientTransaction) {

        List<String> messageList = new ArrayList<>();

        // assume all the fields are not null
        Client sender = clientTransaction.getSender();
        Client receiver = clientTransaction.getReceiver();

        double transferAmount = clientTransaction.getAmount();

        if (sender != null && receiver != null) {

            getBalanceSummary(sender.getLoginId());

            // top up
            if (sender.equals(receiver)) {
                // e.g. Top up 50.
                messageList.add(messageSource.getMessage("transaction.topup", new Object[]{transferAmount}, Locale.ENGLISH));
            }
            // transfer
            else {
                // e.g. Transferred 50 to Alice.
                messageList.add(messageSource.getMessage("transaction.transfer", new Object[]{transferAmount, receiver.getDisplayName()}, Locale.ENGLISH));

                double senderBalance = clientTransaction.getSenderBalance();
                double difference = 0;

                // sender has no balance
                if (senderBalance < 0) {
                    difference = 0 - transferAmount;
                }
                // otherwise, it means the sender has balance
                else {
                    difference = senderBalance - transferAmount;
                }

                // e.g. Owing 40 to Bob.
                if (difference < 0) {
                    messageList.add(messageSource.getMessage("transaction.owing.to", new Object[]{Math.abs(difference), receiver.getDisplayName()}, Locale.ENGLISH));
                }
            }
        }

        return messageList;
    }


    /**
     * get the latest balance of the client
     * @param clientId
     * @return 0 if no found
     */
    public double getBalanceOfTheClient(String clientId) {

        double balance = 0;

        // find sender balance
        List<ClientTransaction> clientTransactionList = repository.findTransationByClientId(clientId);

        if (!CollectionUtils.isEmpty(clientTransactionList)) {
//            // add amount if the login user is the receiver
//            balance = balance + clientTransactionList.stream()
//                    .filter(t -> t.getReceiverId().equals(loginClientId))
//                    .mapToDouble(ClientTransaction::getAmount).sum();
//
//            // deduct amount if the login user is the sender and not the receiver
//            balance = balance - clientTransactionList
//                    .stream()
//                    .filter(t -> (t.getSenderId().equals(loginClientId) && !t.getReceiverId().equals(t.getSenderId())))
//                    .mapToDouble(ClientTransaction::getAmount)
//                    .sum();

            ClientTransaction transaction = clientTransactionList.iterator().next();

            if (transaction.getReceiverId().equals(clientId)){
                balance = transaction.getReceiverBalance() + transaction.getAmount();
            }
            else{
                balance = transaction.getSenderBalance() - transaction.getAmount();
            }
        }

        return balance;
    }

    public ClientTransaction getLatestTransactionBySenderAndReceiver(String senderId, String receiverId, List<ClientTransaction> clientTransactionList){

        if (!CollectionUtils.isEmpty(clientTransactionList)){

            Optional<ClientTransaction> result = clientTransactionList
                                                .stream()
                                                .filter(t -> (t.getSenderId().equals(senderId) && t.getReceiverId().equals(receiverId)))
                                                .findFirst();

            if (result.isPresent()){
                return result.get();
            }
        }
        return null;
    }


    public void getBalanceSummary(String loginClientId) {

        // test
        List<String> testList = repository.findBalanceClientId(loginClientId);

        for (String rid : testList){



        }

    }

}
