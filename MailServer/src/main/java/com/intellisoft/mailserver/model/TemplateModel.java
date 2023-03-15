package com.intellisoft.mailserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.validation.annotation.Validated;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@ToString
@Validated
public class TemplateModel {
    @JsonProperty(required = true)
    private String templateName;
    @JsonProperty(required = true)
    private String templateContent;
}
