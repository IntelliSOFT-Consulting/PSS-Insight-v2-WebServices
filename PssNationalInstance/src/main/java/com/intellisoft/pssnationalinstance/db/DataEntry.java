package com.intellisoft.pssnationalinstance.db;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@Entity
@Table(name = "data_entry")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DataEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orgUnit;
    private String selectedPeriod;
    private String status; // DRAFT / SUBMITTED
    private String dataEntryPersonId; //The person's user id
    private String username; //The Person's username
    private String firstName; //The Person's firstName
    private String surname; //The Person's surname
    private String email; //The Person's email
    private String dataEntryDate; // The format is yyyymmdd
    private String versionNumber; // Current version number
    @CreationTimestamp
    private Date createdAt;
    @UpdateTimestamp
    private Date updatedAt;


}
