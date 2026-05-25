package com.example.resume.submission;


import com.example.resume.screeningresult.ScreeningResult;
import jakarta.persistence.*;
import jakarta.transaction.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Submission {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    private String resumeText;

    private String jobDescription;

    private SubmissionStatus status;

    private LocalDateTime createdAt;

    @OneToOne(
            mappedBy = "submission"
    )
    private ScreeningResult screeningResult;

}
