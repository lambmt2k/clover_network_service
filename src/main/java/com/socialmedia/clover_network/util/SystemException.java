package com.socialmedia.clover_network.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SystemException extends Exception {
    private ResponseCode rc;
}
