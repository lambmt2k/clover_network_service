package com.socialmedia.clover_network.constant;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class CommonRegex {
    public static final String REGEX_EMAIL = "@";
    public static final String REGEX_SPACE = " ";
    public static final String REGEX_BLANK = "";

    public static final Pattern PATTERN_DATE = Pattern.compile("dd/MM/yyyy");
}
