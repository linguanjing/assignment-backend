package com.sky.assignment.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BalanceJsonResponse {

    @JsonProperty
    private String loginUserId;

    @JsonProperty
    private List<String> transactionMessageList =new ArrayList<>();

    public void appendMessage (String message){
        transactionMessageList.add(message);
    }

    public void appendMessage (String message, int position){
        transactionMessageList.add(position, message);
    }
}
