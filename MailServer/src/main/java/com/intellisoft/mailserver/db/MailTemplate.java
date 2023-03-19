package com.intellisoft.mailserver.db;

import lombok.*;

import javax.persistence.*;

@Data
@Builder
@Entity
@Table(name = "mail_template")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MailTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    @Lob
    private String templateContent;
    private Boolean isActive= Boolean.TRUE;
}
