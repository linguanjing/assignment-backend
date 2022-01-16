package com.sky.assignment.controller;

import com.sky.assignment.datamodel.BalanceJsonResponse;
import com.sky.assignment.datamodel.Client;
import com.sky.assignment.service.BalanceJsonService;
import com.sky.assignment.service.ClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Restful API provider for the Client relevant services
 * Login & Create new client
 */
@CrossOrigin(origins = { "http://localhost:3000"})
@Slf4j
@RestController
@RequestMapping("api/v1/client")
public class ClientController {

    @Autowired
    private BalanceJsonService balanceJsonService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private MessageSource messageSource;

    /**
     * controller for handle the user login
     * @param client
     * @return
     */
    @PostMapping(value = "/login")
    public ResponseEntity<?> userLogin(@RequestBody Client client){
        log.info("enter userLogin");

        if (log.isDebugEnabled()){
            log.debug("client = {} " , client);
        }

        if (client == null
                || !StringUtils.hasText(client.getLoginId())
                || !StringUtils.hasText(client.getPassword()) ){
            log.error("empty client");
        }
        else{
            Optional<Client> loginClient = clientService.getClientByLoginId(client.getLoginId());

            // login success
            if (loginClient.isPresent() && loginClient.get().getPassword().equals(client.getPassword())){

                if (log.isDebugEnabled()){
                    log.debug("Login Success");
                }
                BalanceJsonResponse balanceJsonResponse = balanceJsonService.intiBalanceJsonResponseByClient(loginClient.get().getLoginId(), true);

                balanceJsonResponse.appendMessage(messageSource.getMessage("login.success", new Object[]{loginClient.get().getDisplayName()},  Locale.ENGLISH), 0);

                return ResponseEntity.ok(balanceJsonResponse);
            }
        }


        return ResponseEntity.ok(messageSource.getMessage("login.invalidUser", null,  Locale.ENGLISH));
    }


    /**
     * new user sign up
     * @param client
     * @return
     */
    @PostMapping(value = "/signup")
    public ResponseEntity<?> createNewClient(@RequestBody Client client){
        log.info("enter createNewClient");

        if (clientService.getClientByLoginId(client.getLoginId()).isPresent()){

            log.info("Client ID is exits");

            return ResponseEntity.ok(messageSource.getMessage("signup.client.exits", null,  Locale.ENGLISH));
        }

        Client newClient = clientService.createNewClient(client);

        if (log.isDebugEnabled()){
            log.debug("newClient = {} " , newClient);
        }
        return ResponseEntity.ok(newClient);
    }


    /**
     * get list of client to be displayed in the receiver's dropdown list
     * @param loginClientId
     * @return
     */
    @GetMapping(value = "/{loginClientId}/receivers")
    public ResponseEntity<?> getReceiverList(@PathVariable String loginClientId){
        log.info("enter getReceiverList");

        if (log.isDebugEnabled()){
            log.debug("loginClientId = {} " , loginClientId);
        }

        List<Client> clientList = clientService.getReceiptanList(loginClientId);

        if (log.isDebugEnabled()){
            log.debug("clientList = {} " , clientList);
        }
        return ResponseEntity.ok(clientList);
    }

}
