package com.sky.assignment.repository;

import com.sky.assignment.datamodel.Client;
import com.sky.assignment.datamodel.ClientTransaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository("transactionRepository")
public interface ClientTransactionRepository extends CrudRepository<ClientTransaction, UUID> {

    @Query("SELECT t FROM ClientTransaction t where t.senderId = :clientId or t.receiverId = :clientId order by t.id desc")
    List<ClientTransaction> findTransationByClientId(@Param("clientId") String clientId);

    @Query("SELECT distinct t.receiver FROM ClientTransaction t where t.senderId = :clientId and t.receiverId != :clientId")
    List<Client> findReceiverClientId (@Param("clientId") String clientId);

    @Query("SELECT distinct t.sender FROM ClientTransaction t where t.senderId != :clientId and t.receiverId = :clientId")
    List<Client> findSendClientId (@Param("clientId") String clientId);

}
