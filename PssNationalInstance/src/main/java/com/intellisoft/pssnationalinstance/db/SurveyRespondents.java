package com.intellisoft.pssnationalinstance.db;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@Entity
@Table(name = "surveys_respondents")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SurveyRespondents {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String emailAddress;
    private String expiryTime; // yyyy-MM-dd HH:mm:ss
    private String surveyId;
    private String password;
    private String status;
    private String customUrl;
    @CreationTimestamp
    private Date createdAt;
    @UpdateTimestamp
    private Date updatedAt;
}
