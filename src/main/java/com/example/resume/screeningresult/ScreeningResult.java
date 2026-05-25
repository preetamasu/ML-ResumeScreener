    package com.example.resume.screeningresult;

    import com.example.resume.submission.Submission;
    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    import java.time.LocalDateTime;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Entity
    public class ScreeningResult {
        @Id
        @GeneratedValue(
                strategy = GenerationType.IDENTITY
        )
        private Long id;

        @OneToOne
        @JoinColumn(
                name="submission_id",
                nullable = false
        )
        private Submission submission;

        private Double interviewProbability;
        private Double matchScore;
        private String modelVersion;
        private String explanation;
        private LocalDateTime createdAt;

    }
