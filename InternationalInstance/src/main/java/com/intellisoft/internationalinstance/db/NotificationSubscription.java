package com.intellisoft.internationalinstance.db;

import lombok.*;

import javax.persistence.*;

@Data
@Builder
@Entity
@Table(name = "notification_subscription")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NotificationSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String email;
    private String phone;
    private Boolean isActive = Boolean.TRUE;
    private String userId;
}
