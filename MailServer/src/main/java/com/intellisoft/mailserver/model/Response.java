package com.intellisoft.mailserver.model;

import lombok.*;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@ToString
public class Response {
    private String status;
    private String message;
}
