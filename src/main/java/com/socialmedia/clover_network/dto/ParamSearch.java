package com.socialmedia.clover_network.dto;

import com.google.gson.Gson;
import lombok.*;

import java.io.Serializable;
import java.util.List;

public class ParamSearch {
    static Gson gson = new Gson();

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor(staticName = "of")
    @NoArgsConstructor
    @Data
    public static class ResponseSearchParam implements Serializable {
        private int code;
        private Boolean result;
        private String message;
        private String exception;
        private String version;
        private ResultData data;

        public String toJson(){
            return gson.toJson(this);
        }


        @Builder
        @Getter
        @Setter
        @AllArgsConstructor(staticName = "of")
        @NoArgsConstructor
        @Data
        public static class ResultData implements Serializable {
            private int took;
            private int total;
            private List<Object> result;
            private Object aggs;
        }


    }
}
