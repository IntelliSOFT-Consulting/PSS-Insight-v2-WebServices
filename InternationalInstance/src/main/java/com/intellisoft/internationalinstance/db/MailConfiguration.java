package com.intellisoft.internationalinstance.db;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@Entity
@Table(name = "mail_configuration")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MailConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String serverType;
    private String serverName;
    private String ports;
    private String username;
    private String fromEmail;
    private Boolean isActive;
    @Column(length = 8000)
    private String password;
    @CreationTimestamp
    private Date createdAt;
    @UpdateTimestamp
    private Date updatedAt;
}
