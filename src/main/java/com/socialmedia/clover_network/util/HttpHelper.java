package com.socialmedia.clover_network.util;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class HttpHelper {
    private HttpServletRequest request;

    public HttpHelper(HttpServletRequest request){
        this.request = request;
    }
    public String getClientIp(){
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    public String getUserAgent(){
        return request.getHeader("User-Agent");
    }

    public String getBearer(){
        String tokenID = request.getHeader("Authorization");
        if (StringUtils.hasText(tokenID) && tokenID.startsWith("Bearer ")) {
            return tokenID.substring(7);
        }
        return null;
    }



    public static Map<String, String> getQueryMap(String urlData) {
        Map<String, String> map = new HashMap<>();
        try {
            URL url = new URL(urlData);

            String query = url.getQuery();
            String[] params = query.split("&");
            for (String param : params) {
                String name = param.split("=")[0];
                String value = param.split("=")[1];
                map.put(name, value);
            }
        } catch (Exception ex) {
        }

        return map;
    }

    public static String parseLinkYoutube(String url) throws IOException, InterruptedException {
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(url)) {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.youtube.com/oembed?url=" + url))
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        }
        return null;
    }
}
