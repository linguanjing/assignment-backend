package com.sky.assignment.datamodel;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Entity
public class Client {

    @Id
    private String loginId;

    @NotBlank
    private String password;

    @NotBlank
    private String displayName;

    public Client(){}

    public Client(@JsonProperty("loginId") String loginId,
                  @JsonProperty("password") String password,
                  @JsonProperty("displayName") String displayName){
        this.loginId = loginId;
        this.password = password;
        this.displayName = displayName;
    }
}
