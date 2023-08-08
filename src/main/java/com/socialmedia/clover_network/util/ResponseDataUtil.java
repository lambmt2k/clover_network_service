package com.socialmedia.clover_network.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDataUtil<T> {
    private T data;
    private ResponseCode rc;

    public static <T> ResponseDataUtil<T> of(T data, ResponseCode rc) {
        return new ResponseDataUtil<>(data, rc);
    }

    public static <T> ResponseDataUtil<T> error(ResponseCode error) {
        return new ResponseDataUtil<>(null, error);
    }

    public static <T> ResponseDataUtil<T> errorMess(T data) {
        return new ResponseDataUtil<>(data, null);
    }

    public static <T> ResponseDataUtil<T> ok(T data) {
        return new ResponseDataUtil<>(data, null);
    }
}
