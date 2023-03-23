package com.intellisoft.pssnationalinstance.db;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Builder
@Entity
@Table(name = "surveys")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Surveys {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String status;
    private String creatorId;
    @ElementCollection
    private List<String> indicators;
    @CreationTimestamp
    private Date createdAt;
    @UpdateTimestamp
    private Date updatedAt;
}
