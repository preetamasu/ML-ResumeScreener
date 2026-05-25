package com.example.resume.screeningresult;

import com.example.resume.submission.Submission;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScreeningResultRepository extends JpaRepository<ScreeningResult, Long> {
    Optional<ScreeningResult> findBySubmissionId(Long submissionId);
    boolean existsBySubmissionId(Long submissionId);



}
