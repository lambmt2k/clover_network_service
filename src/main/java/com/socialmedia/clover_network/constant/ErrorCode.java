package com.socialmedia.clover_network.constant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class ErrorCode {
    private int code;
    private String messageEN;
    private String messageVN;

    /**
     * Code from 1 => 99
     */
    public static class Authentication {
        public static ErrorCode ACTION_SUCCESS = of(-1, "Action success", "Thành công.");
        public static ErrorCode AUTHEN_ERROR = of(1, "Email or password is incorrect", "Sai email hoặc mật khẩu. Vui lòng thử lại.");
        public static ErrorCode INVALID_DATA = of(2, "Invalid data input", "Thông tin truyền vào không hợp lệ.");
        public static ErrorCode ACCOUNT_NOT_ACTIVE = of(3,
                "Account doesn't activated. Please verify account by link send to your email before first login",
                "Tài khoản chưa được kích hoat. Vui lòng kích hoạt tài khoản bằng đường dẫn được gửi tới email của bạn.");
        public static ErrorCode INVALID_PASSWORD = of(4, "Invalid password", "Mật khẩu không hợp lệ");
    }

    /**
     * Code from 100 => 199
     */
    public static class User {
        public static ErrorCode ACTION_SUCCESS = of(100, "Action success", "Thành công.");
        public static ErrorCode PROFILE_GET_EMPTY = of(101, "Profile empty", "Không tìm thấy dữ liệu user.");
        public static ErrorCode EXISTED_USER = of(102, "Existed user", "User đã tồn tại.");
    }
    /**
     * Code from 200 => 299
     */
    public static class Group {
        public static ErrorCode ACTION_SUCCESS = of(200, "Action success", "Thành công.");
        public static ErrorCode DISABLE_JOIN = of(201, "Can't join group", "Không được quyền truy cập nhóm.");
        public static ErrorCode GROUP_NOT_FOUND = of(202, "There is no group", "Không tìm được nhóm nào.");
        public static ErrorCode ALREADY_MEMBER = of(203, "User already is member of group", "User đã là thành viên của nhóm.");
    }

    /**
     * Code from 300 => 399
     */
    public static class Token {
        public static ErrorCode ACTION_SUCCESS = of(300, "Action success", "Thành công.");
        public static ErrorCode FORBIDDEN = of(301, "Not permission to do that", "Không có quyền thực hiện yêu cầu.");
    }

    /**
     * Code from 400 => 499
     */
    public static class Feed {
        public static ErrorCode ACTION_SUCCESS = of(400, "Action success", "Thành công.");
        public static ErrorCode INPUT_INVALID = of(401, "Input invalid", "Thông tin truyền vào không hợp lệ.");
        public static ErrorCode FORBIDDEN = of(402, "Not permission to post in group", "Không có quyền đăng bài viết.");
        public static ErrorCode GENERATE_POST_ID_ERROR = of(403, "Can't generate post Id", "Không thể khởi tạo postId.");
    }
}
