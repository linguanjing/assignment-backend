package com.sky.assignment.datamodel;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalTime;
import java.util.UUID;

/**
 * this is data object to store the transactions
 * if sender & receiver are the same, then it is top up transaction
 * otherwise, its sender pay to the receiver
 */
@Data
@Entity
public class ClientTransaction {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name="senderId", referencedColumnName = "loginId", nullable = false, insertable =  false, updatable = false)
    private Client sender;

    @NotBlank
    @JsonProperty
    private String senderId;

    @ManyToOne
    @JoinColumn(name="receiverId", referencedColumnName = "loginId", nullable = false, insertable =  false, updatable = false)
    private Client receiver;

    @NotBlank
    @JsonProperty
    private String receiverId;

    @JsonProperty
    private double amount;

    @JsonProperty
    private double senderBalance;

    @JsonProperty
    private double receiverBalance;

    @JsonProperty
    private LocalTime transactionTime;

    public ClientTransaction(){}

    public ClientTransaction(String senderId, String receiverId, double amount){
        this.setSenderId(senderId);
        this.setReceiverId(receiverId);
        this.setAmount(amount);
    }

}
