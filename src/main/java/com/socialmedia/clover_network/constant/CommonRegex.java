package com.socialmedia.clover_network.constant;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class CommonRegex {
    public static final String REGEX_EMAIL = "@";
    public static final String REGEX_SPACE = " ";
    public static final String REGEX_BLANK = "";

    public static final Pattern PATTERN_DATE = Pattern.compile("dd/MM/yyyy");

    public static final String REGEX_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])[a-zA-Z0-9!@#\\$%\\^\\&*\\)\\(+=._-]{8,}$";
}
