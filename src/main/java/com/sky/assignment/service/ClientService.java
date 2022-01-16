package com.sky.assignment.service;

import com.sky.assignment.datamodel.Client;
import com.sky.assignment.repository.ClientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClientService {

    @Value("${bank.client.default.balance:100}")
    public Double DEFAULT_BALANCE;

    @Autowired
    private ClientRepository repository;

    public Client createNewClient(Client client){

        if ( client  == null){
            //todo throw exception
        }

        return repository.save(client);
    }

    /**
     * find recipant Client list
     * @return
     */
    public List<Client> getReceiptanList(String loginClientId){

        if ( loginClientId  == null){
            //todo throw exception
        }


        List<Client> clientList = new ArrayList<>();
        repository.findAll().forEach(clientList :: add);

        return clientList.stream().filter(client -> !client.getLoginId().equals(loginClientId)).collect(Collectors.toList());
    }


    public Optional<Client> getClientByLoginId(String loginClientId){

        if ( loginClientId  == null){
            //todo throw exception
        }

        return repository.findById(loginClientId);
    }




}
