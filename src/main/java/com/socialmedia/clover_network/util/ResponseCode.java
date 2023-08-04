package com.socialmedia.clover_network.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class ResponseCode {
    private int code;
    private String msg;

    public static boolean equal(ResponseCode rc1, ResponseCode rc2) {
        return rc1.code == rc2.code;
    }

    public static ResponseCode SUCCESS = of(200, "");

    /**
     *  Error type: User auth
     *  Error code: 40xx
     */
    public static class UserAuthError {
        //  Login error - code 400x
        public static ResponseCode DEFAULT = of(4000, "Hệ thống bị gián đoạn, vui lòng thử lại sau.");
        public static ResponseCode USER_AUTH_DEFAULT = of(4001, "Email hoặc mật khẩu chưa chính xác.");
        public static ResponseCode USER_AUTH_NOT_EXIST = of(4002, "Tài khoản này không tồn tại.");
    }
}
