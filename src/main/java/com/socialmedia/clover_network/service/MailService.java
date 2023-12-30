package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.entity.UserInfo;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public interface MailService {
    void sendMailActiveAccount(UserInfo userInfo, String tokenId) throws MessagingException, UnsupportedEncodingException;
    String sendMailOtp(UserInfo userInfo, String otp) throws MessagingException, UnsupportedEncodingException;
}
