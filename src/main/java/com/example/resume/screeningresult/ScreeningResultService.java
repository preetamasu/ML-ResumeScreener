package com.example.resume.screeningresult;

import com.example.resume.exception.ResourceNotFoundException;
import com.example.resume.exception.SubmissionAlreadyExistsException;
import com.example.resume.ml.MlPredictionResponse;
import com.example.resume.submission.Submission;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class ScreeningResultService {

    private final ScreeningResultRepository screeningResultRepository;

    public ScreeningResultService(ScreeningResultRepository screeningResultRepository){
        this.screeningResultRepository = screeningResultRepository;
    }
   public ScreeningResponse createScreeningResult(Submission submission, MlPredictionResponse response){
        if(screeningResultRepository.existsBySubmissionId(submission.getId())){
            throw new SubmissionAlreadyExistsException("Submission Already Exists");
        }

        ScreeningResult result = new ScreeningResult();
        result.setSubmission(submission);
        result.setInterviewProbability(response.interviewProbability());
        result.setMatchScore(response.matchScore());
        result.setExplanation(response.explanation());
        result.setModelVersion(response.modelVersion());
        result.setCreatedAt(LocalDateTime.now());
        screeningResultRepository.save(result);
       return toResponse(result);
    }

    public ScreeningResponse getScreeningResultBySubmissionId(Long id){

        ScreeningResult result = screeningResultRepository.findBySubmissionId(id).orElseThrow(()-> new ResourceNotFoundException("Couldn't find an result with that submission"));

        return toResponse(result);
    }

    public ScreeningResponse toResponse(ScreeningResult result){
        return new ScreeningResponse(
                result.getId(),
                result.getSubmission().getId(),
                result.getInterviewProbability(),
                result.getMatchScore(),
                result.getModelVersion(),
                result.getExplanation(),
                result.getCreatedAt());

    }
}
