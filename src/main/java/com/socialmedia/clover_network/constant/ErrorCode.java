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
     * Code from 1 => 49
     */
    public static class Authentication {
        public static ErrorCode ACTION_SUCCESS = of(-1, "Action success", "Thành công.");
        public static ErrorCode AUTHEN_ERROR = of(1, "Email or password is incorrect", "Sai email hoặc mật khẩu. Vui lòng thử lại.");
        public static ErrorCode INVALID_DATA = of(2, "Invalid data input", "Thông tin truyền vào không hợp lệ.");
        public static ErrorCode ACCOUNT_NOT_ACTIVE = of(3,
                "Account doesn't activated. Please verify account by link send to your email before first login",
                "Tài khoản chưa được kích hoat. Vui lòng kích hoạt tài khoản bằng đường dẫn được gửi tới email của bạn.");
        public static ErrorCode INVALID_PASSWORD = of(4, "Invalid password", "Mật khẩu không hợp lệ");
        public static ErrorCode INCORRECT_OTP_CODE = of(5, "Incorrect otp code", "Mã xác thực không đúng. Vui lòng kiểm tra lại.");
        public static ErrorCode EXPIRED_OTP_CODE = of(6, "Otp code is expired", "Mã xác thực hết hạn. Vui lòng kiểm tra lại.");
        public static ErrorCode NEW_PASSWORD_NO_CHANGE = of(7, "New password must be different old password", "Mật khẩu mới phải khác với mật khẩu cũ.");
    }

    /**
     * Code from 50 => 99
     */
    public static class SendMail {
        public static ErrorCode ACTION_SUCCESS = of(50, "Action success", "Thành công.");
        public static ErrorCode ACTION_FAIL = of(51, "Send mail fail. Please check your email", "Gửi thư không thành công. Vui lòng kiểm tra lại email.");
    }

    /**
     * Code from 100 => 150
     */
    public static class
    User {
        public static ErrorCode ACTION_SUCCESS = of(100, "Action success", "Thành công.");
        public static ErrorCode PROFILE_GET_EMPTY = of(101, "Profile empty", "Không tìm thấy dữ liệu user.");
        public static ErrorCode EXISTED_USER = of(102, "Existed user", "User đã tồn tại.");
        public static ErrorCode INVALID_IMAGE_FILE = of(103, "Invalid image file", "Hình ảnh truyền vào không hợp lệ.");
        public static ErrorCode LIST_NO_USER = of(104, "List no user", "Danh sách người dùng trống.");
    }

    /**
     * Code from 151 => 199
     */
    public static class Connection {
        public static ErrorCode ACTION_SUCCESS = of(150, "Action success", "Thành công.");
        public static ErrorCode ALREADY_CONNECTED = of(151, "User already connected", "Người dùng đã được kết nối trước đó.");
        public static ErrorCode CANNOT_CONNECT_YOURSELF = of(152, "You can't connect yourself", "Bạn không thể kết nối chính mình.");
    }

    /**
     * Code from 200 => 299
     */
    public static class Group {
        public static ErrorCode ACTION_SUCCESS = of(200, "Action success", "Thành công.");
        public static ErrorCode DISABLE_JOIN = of(201, "Can't join group", "Không được quyền truy cập nhóm.");
        public static ErrorCode GROUP_NOT_FOUND = of(202, "There is no group", "Không tìm được nhóm nào.");
        public static ErrorCode ALREADY_MEMBER = of(203, "User already is member of group", "User đã là thành viên của nhóm.");
        public static ErrorCode DISABLE_COMMENT = of(204, "Group disable comment", "Bình luận trong nhóm bị khóa.");
        public static ErrorCode NOT_PERMISSION = of(205, "You have not permission to do that", "Không có quyền thực hiện.");
        public static ErrorCode NOT_MEMBER = of(206, "You are not member of group", "Bạn không là thành viên của nhóm.");
        public static ErrorCode OWNER_CANNOT_LEAVE = of(207, "You are group owner, cannot leave", "Bạn là người tạo group. Không thể rời");
        public static ErrorCode LIST_MEMBER_EMPTY = of(208, "List member empty", "Danh sách thành viên trống");
        public static ErrorCode MEMBER_NOT_FOUND = of(209, "Member not found", "Không tìm thấy thành viên.");
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
        public static ErrorCode EMPTY_FEED = of(404, "Empty feed", "Không có bài viết nào.");
        public static ErrorCode POST_NOT_FOUND = of(405, "Post not found", "Không tìm thấy bài viết.");
        public static ErrorCode NOT_PERMISSION_VIEW_FEED = of(406, "User have not permission to view feed", "Người dùng không có quyền xem danh sách bài viết.");
    }

    /**
     * Code from 500 => 549
     */
    public static class Comment {
        public static ErrorCode ACTION_SUCCESS = of(500, "Action success", "Thành công.");
        public static ErrorCode INPUT_INVALID = of(501, "Input invalid", "Thông tin truyền vào không hợp lệ.");
    }

    /**
     * Code from 550 => 599
     */
    public static class Reaction {
        public static ErrorCode ACTION_SUCCESS = of(550, "Action success", "Thành công.");
        public static ErrorCode INPUT_INVALID = of(551, "Input invalid", "Thông tin truyền vào không hợp lệ.");
    }


}
