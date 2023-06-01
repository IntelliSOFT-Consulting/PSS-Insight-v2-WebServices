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
@Table(name = "versions")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VersionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
//    @Column(unique = true)
    private String versionName;
    private String versionDescription;
    private String status;
    private String createdBy;
    private String publishedBy;
    @CreationTimestamp
    private Date createdAt;
    @UpdateTimestamp
    private Date updatedAt;
    @ElementCollection
    private List<String> indicators;
    @Column(name = "is_latest")
    private boolean isLatest;

}
