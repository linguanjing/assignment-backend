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
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

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

    /**
     * get the latest balance of the client
     * @param clientId
     * @return 0 if no found
     */
    public double getBalanceOfTheClient(String clientId) {

        double balance = 0;

        // find sender balance
        List<ClientTransaction> clientTransactionList = getTransactionListByClientId(clientId);

        if (!CollectionUtils.isEmpty(clientTransactionList)) {

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


    public List<String> generateTransactionMessage(String loginClientId) {

        List<String> messageList = new ArrayList<>();

        // find all transaction of the sender
        List<ClientTransaction> clientTransactionList = getTransactionListByClientId(loginClientId);

        if (!CollectionUtils.isEmpty(clientTransactionList)) {

            //top up message
            ClientTransaction clientTransaction = clientTransactionList.get(0);

            // always add the topup message if this is the last transaction
            if (clientTransaction.getSenderId().equals(clientTransaction.getReceiverId())){
                // e.g. Top up 50.
                messageList.add(messageSource.getMessage("transaction.topup", new Object[]{clientTransaction.getAmount()}, Locale.ENGLISH));
            }

            // search for own to/from
            List<Client> otherPartyList = repository.findReceiverClientId(loginClientId);
            otherPartyList.addAll(repository.findSendClientId(loginClientId));

            List<Client> listWithoutDuplicates = new ArrayList<>(new HashSet<>(otherPartyList));


            for (Client otherParty : listWithoutDuplicates){

                // find the transaction between login user & the another party
                List<ClientTransaction> shortList = clientTransactionList
                        .stream()
                        .filter(t -> ((t.getSenderId().equals(loginClientId) && t.getReceiverId().equals(otherParty.getLoginId()))
                                || (t.getSenderId().equals(otherParty.getLoginId()) && t.getReceiverId().equals(loginClientId))))
                        .collect(Collectors.toList());

                ClientTransaction lastTransaction = shortList.get(0);

                double balance = 0;

                // if last transaction sender has out of balance
                if (lastTransaction.getAmount() > lastTransaction.getSenderBalance() ){
                    // search own to
                    if (lastTransaction.getSenderId().equals(loginClientId)){
                        balance = searchOwningAmount (loginClientId, otherParty.getLoginId(), clientTransactionList);
                    }
                    // else search for own from
                    else{
                        balance = - searchOwningAmount (otherParty.getLoginId(), loginClientId, clientTransactionList);
                    }

                }

                log.debug("{} own to {} = {}", loginClientId, otherParty.getLoginId(), balance);

                if (balance > 0){
                    messageList.add(messageSource.getMessage("transaction.owing.to", new Object[]{Math.abs(balance),
                            otherParty.getDisplayName()}, Locale.ENGLISH));
                }
                else if (balance < 0){
                    messageList.add(messageSource.getMessage("transaction.owing.from", new Object[]{Math.abs(balance),
                            otherParty.getDisplayName()}, Locale.ENGLISH));
                }

            }

        }
        return messageList;
    }

    private double searchOwningAmount(String senderId, String receiverId, List<ClientTransaction> clientTransactionList){

        double amount = 0;

        if (!CollectionUtils.isEmpty(clientTransactionList)) {

            List<ClientTransaction> shortList = clientTransactionList
                                                .stream()
                                                .filter(t -> (t.getSenderId().equals(senderId) && t.getReceiverId().equals(receiverId)))
                                                .collect(Collectors.toList());

            for (ClientTransaction transaction : shortList){

                // Scenario  1, in this transaction sender has no balance
                if (transaction.getSenderBalance() < 0){
                    amount += transaction.getAmount();
                }
                // Scenario  2, in this transaction sender out of balance
                else if (transaction.getAmount() > transaction.getSenderBalance()){
                    amount += transaction.getAmount() - transaction.getSenderBalance();
                }
                // Scenario  3, sender has enough balance
                else{
                    break;
                }
            }
        }


        return amount;
    }

}
