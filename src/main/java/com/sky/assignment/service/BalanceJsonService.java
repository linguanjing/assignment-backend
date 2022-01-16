package com.sky.assignment.service;

import com.sky.assignment.datamodel.BalanceJsonResponse;
import com.sky.assignment.datamodel.Client;
import com.sky.assignment.datamodel.ClientTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;


@Slf4j
@Service
public class BalanceJsonService {

    @Autowired
    private ClientTransactionService clientTransactionService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private MessageSource messageSource;

    /**
     *
     * @param clientId
     * @return
     */
    public BalanceJsonResponse intiBalanceJsonResponseByClient(String clientId, boolean accountSummary){

        BalanceJsonResponse balanceJsonResponse = new BalanceJsonResponse();

        Optional<Client> client = clientService.getClientByLoginId(clientId);

        if (client.isPresent()){

            balanceJsonResponse.setLoginUserId(client.get().getLoginId());

            // calculate the balance
            double balance = clientTransactionService.getBalanceOfTheClient(client.get().getLoginId());

            balanceJsonResponse.appendMessage(messageSource.getMessage("transaction.balance", new Object[]{balance < 0 ? 0 : balance},  Locale.ENGLISH));

            //
            if (accountSummary){


                log.debug("here");
            }
        }

        return balanceJsonResponse;
    }



}
