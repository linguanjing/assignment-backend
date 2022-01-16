package com.sky.assignment.datamodel;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransferJsonRequest {

    @JsonProperty
    private String senderId;

    @JsonProperty
    private String receiverId;

    @JsonProperty
    private double amount;
}
