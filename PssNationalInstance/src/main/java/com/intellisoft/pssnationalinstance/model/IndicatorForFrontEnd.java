package com.intellisoft.pssnationalinstance.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IndicatorForFrontEnd {
    private String id;
    private String code;
    private String formName;
}
