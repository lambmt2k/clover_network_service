package com.socialmedia.clover_network.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@JsonPropertyOrder({
        "code",
        "data",
        "messageEN",
        "messageVN"
})
public class ApiResponse implements Serializable {

    @JsonIgnore
    private static final long serialVersionUID = 7702134516418120340L;

    @JsonProperty("code")
    private int code;

    @JsonProperty("data")
    private Object data;

    @JsonProperty("messageEN")
    private String messageEN;

    @JsonProperty("messageVN")
    private String messageVN;

    public ApiResponse() {
    }

    public ApiResponse(int code, Object data, String messageEN, String messageVN) {
        this.code = code;
        this.data = data;
        this.messageEN = messageEN;
        this.messageVN = messageVN;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessageEN() {
        return messageEN;
    }

    public void setMessageEN(String messageEN) {
        this.messageEN = messageEN;
    }

    public String getMessageVN() {
        return messageVN;
    }

    public void setMessageVN(String messageVN) {
        this.messageVN = messageVN;
    }
}
