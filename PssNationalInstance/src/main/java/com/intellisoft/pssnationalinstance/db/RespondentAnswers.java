package com.intellisoft.pssnationalinstance.db;

import javax.persistence.*;

@Entity
@Table(name = "respondent_answers")
public class RespondentAnswers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String respondentId;
    private String indicatorId;
    private String answer;
    private String comments;
    private String attachment;
    private String status;
    public RespondentAnswers() {
    }

    public RespondentAnswers(String respondentId, String indicatorId,
                             String answer, String comments,
                             String attachment) {
        this.respondentId = respondentId;
        this.indicatorId = indicatorId;
        this.answer = answer;
        this.comments = comments;
        this.attachment = attachment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRespondentId() {
        return respondentId;
    }

    public void setRespondentId(String respondentId) {
        this.respondentId = respondentId;
    }

    public String getIndicatorId() {
        return indicatorId;
    }

    public void setIndicatorId(String indicatorId) {
        this.indicatorId = indicatorId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getAttachment() {
        return attachment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }
}


