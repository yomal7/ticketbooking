package com.yomal7.ticketbooking.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(int status, String message, Map<String, String> fieldErrors) {

    public static ApiError of(int status, String message) {
        return new ApiError(status, message, null);
    }
}