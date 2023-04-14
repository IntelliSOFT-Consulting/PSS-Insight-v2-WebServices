package com.intellisoft.pssnationalinstance.db;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@Entity
@Table(name = "about_us")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AboutUs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String aboutUs;
    private String contactUs;
    @CreationTimestamp
    private Date createdAt;
    @UpdateTimestamp
    private Date updatedAt;


}
