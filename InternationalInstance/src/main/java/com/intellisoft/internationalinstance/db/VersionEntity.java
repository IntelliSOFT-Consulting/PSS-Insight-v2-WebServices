package com.intellisoft.internationalinstance.db;

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
    public Long id;
//    @Column(unique = true)
    public String versionName;
    public String versionDescription;
    public String status;
    public String createdBy;
    public String publishedBy;
    @CreationTimestamp
    private Date createdAt;
    @UpdateTimestamp
    private Date updatedAt;
    @ElementCollection
    private List<String> indicators;
    @Builder.Default
    @Column(name = "is_latest")
    private boolean isLatest = true;
}
