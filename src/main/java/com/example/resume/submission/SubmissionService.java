package com.example.resume.submission;

import com.example.resume.exception.ResourceNotFoundException;
import com.example.resume.ml.MlClientService;
import com.example.resume.ml.MlPredictionResponse;
import com.example.resume.screeningresult.ScreeningResultService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final MlClientService mlClientService;
    private final ScreeningResultService screeningResultService;

    public SubmissionService(SubmissionRepository submissionRepository,MlClientService mlClientService,ScreeningResultService screeningResultService){
        this.submissionRepository = submissionRepository;
        this.mlClientService= mlClientService;
        this.screeningResultService = screeningResultService;
    }

    public List<SubmissionResponse> getAllSubmissions(){
        return submissionRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public SubmissionResponse createSubmission(SubmissionRequest submissionRequest){
          Submission submission = new Submission();
          submission.setResumeText(submissionRequest.resumeText());
          submission.setJobDescription(submissionRequest.jobDescription());
          submission.setStatus(SubmissionStatus.PENDING);
          submission.setCreatedAt(LocalDateTime.now());
          Submission submission1 =  submissionRepository.save(submission);

          try{
              MlPredictionResponse predictionResponse = mlClientService.predict(
                   submission1.getResumeText(),
                   submission1.getJobDescription()
              );
              screeningResultService.createScreeningResult(submission1,predictionResponse);
              submission1.setStatus(SubmissionStatus.SCORED);
              Submission scoredSubmission = submissionRepository.save(submission1);

              return toResponse(scoredSubmission);

          }
          catch(Exception exception){
              submission1.setStatus(SubmissionStatus.FAILED);
              Submission failedSubmission = submissionRepository.save(submission1);

              return toResponse(failedSubmission);
          }

    }


    public SubmissionResponse getSubmissionById(Long id){
        Submission submission = submissionRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException("We cannot find an submission with that id"+id));
        return toResponse(submission);
    }
    private SubmissionResponse toResponse(Submission submission){
        SubmissionResponse response = new SubmissionResponse(
                submission.getId(),
                submission.getResumeText(),
                submission.getJobDescription(),
                submission.getStatus(),
                submission.getCreatedAt()
        );
        return response;
    }

}
