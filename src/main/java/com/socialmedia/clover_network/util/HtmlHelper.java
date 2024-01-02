package com.socialmedia.clover_network.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlHelper {
    private static final Logger logger = LoggerFactory.getLogger(HtmlHelper.class);
    public static String convertToHTML(String data) {
        if (StringUtils.isNotEmpty(data)) {
            Map<String, String> TAGS = new LinkedHashMap<>();

            TAGS.put("\n", "<br>");
            for (Map.Entry<String, String> tag : TAGS.entrySet()) {
                data = data.replaceAll(tag.getKey(), tag.getValue());
            }

            Pattern pattern = Pattern.compile("\\*URL\\*(\\[(.*?)\\]\\((.*?)\\))\\*\\/URL\\*");
            Matcher matcher = pattern.matcher(data);
            try {
                while (matcher.find()) {
                    if (matcher.groupCount() == 3) {
                        String tagUrl = "<a href=\\\"" + matcher.group(3) + "\\\" target=\\\"_blank\\\" rel=\\\"noreferrer noopener\\\">" + matcher.group(2) + "</a>";
                        data = matcher.replaceFirst(tagUrl);
                        matcher = pattern.matcher(data);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.info(e.getMessage());
            }
        }

        return data;
    }
}
