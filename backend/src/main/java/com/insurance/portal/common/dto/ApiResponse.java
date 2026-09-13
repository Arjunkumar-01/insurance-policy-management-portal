package com.insurance.portal.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T>{
//    Indicates whether the API request was successful.
    private boolean success;

//    HTTP status code.
    private int status;

//    Human-readable response message.
    private String message;

//    Response payload.
    private T data;

//    Response generation timestamp.
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
