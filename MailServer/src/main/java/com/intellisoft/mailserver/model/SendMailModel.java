package com.intellisoft.mailserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@ToString
@Validated
public class SendMailModel {
    @JsonProperty(required = true)
    private List<Map<String,String>> emailsAndNames;
    @JsonProperty(required = true)
    private String subject;
    @JsonProperty(required = true)
    private Long templateId;
    @JsonProperty(required = true)
    private Map<String, Object> variables;
}
