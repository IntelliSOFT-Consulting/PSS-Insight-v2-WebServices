package com.intellisoft.pssnationalinstance.db;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@Entity
@Table(name = "resent_indicators")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResendIndicators {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String respondentId;
    private String surveyId;

    @ElementCollection
    private List<String> resentIndicators;


}
