package com.intellisoft.pssnationalinstance.db;


import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@Table(name = "benchmarks")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Benchmarks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String dataElement;
    @Column
    private String orgUnit;
    @Column
    private String period;
    @Column
    private String value;
    @Column
    private String indicatorCode;
    @Column
    private String nationalValue;
}
