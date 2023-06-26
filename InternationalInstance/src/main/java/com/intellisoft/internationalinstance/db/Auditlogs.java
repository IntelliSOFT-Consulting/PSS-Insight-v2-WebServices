package com.intellisoft.internationalinstance.db;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@Setter
@Getter
@Entity
@Table(name = "audit_logs")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Auditlogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private Long version;
    @Column
    private String country;
    @CreationTimestamp
    private LocalDateTime dateAdded;
    @Column
    private String changes;
    @Column
    private String indicator;

}
