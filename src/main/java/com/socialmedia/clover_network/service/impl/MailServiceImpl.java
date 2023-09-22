package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.constant.CommonConstant;
import com.socialmedia.clover_network.constant.CommonRegex;
import com.socialmedia.clover_network.entity.UserInfo;
import com.socialmedia.clover_network.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
public class MailServiceImpl implements MailService {

    Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Override
    @Async
    public void sendMailActiveAccount(UserInfo userInfo, String tokenId) throws MessagingException, UnsupportedEncodingException {
        logger.info("Start [sendMailActiveAccount]");
        String toEmail = userInfo.getEmail();
        
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String fromEmail = CommonConstant.HOST_EMAIL;
        String senderName = "Clover Network Admin";
        String title = "Please verify your registration";

        String content = "Dear [[name]],<br>"
                + "Thank you for using and supporting our product.<br>"
                + "Just one more step to experience this product.<br>"
                + "Please click the link below to verify your registration:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>"
                + "Thank you,<br>"
                + "Clover Network Admin.";

        content = content.replace("[[name]]", userInfo.getFirstname() + CommonRegex.REGEX_SPACE + userInfo.getLastname());
        String verifyURL = "http://localhost:8080/api/authenticate/verify?tokenId=" + tokenId;
        content = content.replace("[[URL]]", verifyURL);


        helper.setFrom(fromEmail, senderName);
        helper.setTo(toEmail);
        helper.setSubject(title);
        helper.setText(content, true);

        javaMailSender.send(message);
    }
}
