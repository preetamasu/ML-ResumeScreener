package com.example.resume.submission;

import com.example.resume.exception.ResourceNotFoundException;
import com.example.resume.ml.MlClientService;
import com.example.resume.ml.MlPredictionRequest;
import com.example.resume.ml.MlPredictionResponse;
import com.example.resume.screeningresult.ScreeningResultService;
import org.testng.annotations.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;


public class SubmissionServiceTest {


    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private MlClientService mlClientService;

    @Mock
    private ScreeningResultService screeningResultService;

    private SubmissionService submissionService;

    @BeforeMethod
    void setUp(){
        MockitoAnnotations.openMocks(this);

        submissionService =    new SubmissionService(
                submissionRepository,
                mlClientService,
                screeningResultService
        );
    }


    @Test
    public void getSubmissionById_whenSubmissionExists_returnsSubmissionResponse() {

        Submission submission = new Submission();
        submission.setId(1L);
        submission.setResumeText("Java Spring Boot resume");
        submission.setJobDescription("Backend developer job");
        submission.setStatus(SubmissionStatus.PENDING);
        submission.setCreatedAt(LocalDateTime.now());


        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));

        SubmissionResponse response = submissionService.getSubmissionById(1L);

        Assert.assertEquals(response.id(),1L);
        Assert.assertEquals(response.resumeText(),"Java Spring Boot resume");
        Assert.assertEquals(response.jobDescription(),"Backend developer job");
        Assert.assertEquals(response.submissionStatus(),SubmissionStatus.PENDING);

        verify(submissionRepository,times(1)).findById(1L);
    }

    @Test
    public void getSubmissionById_whenSubmissionDoesNotExist_throwsResourceNotFoundException(){
        when(submissionRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assert.expectThrows(
                ResourceNotFoundException.class,
                ()-> submissionService.getSubmissionById(1L)
        );

        Assert.assertEquals(exception.getMessage(),"We cannot find an submission with that id 1");
        verify(submissionRepository,times(1)).findById(1L);
    }

    @Test
    public void getAllSubmissions_whenSubmissionsExist_returnsSubmissionResponses(){
        Submission submission1 = new Submission();
        submission1.setId(1L);
        submission1.setResumeText("Java resume");
        submission1.setJobDescription("Backend job");
        submission1.setStatus(SubmissionStatus.PENDING);
        submission1.setCreatedAt(LocalDateTime.now());

        Submission submission2 = new Submission();
        submission2.setId(2L);
        submission2.setResumeText("Python resume");
        submission2.setJobDescription("ML job");
        submission2.setStatus(SubmissionStatus.SCORED);
        submission2.setCreatedAt(LocalDateTime.now());

        when(submissionRepository.findAll()).thenReturn(List.of(submission1,submission2));

        List<SubmissionResponse> responses = submissionService.getAllSubmissions();

        Assert.assertEquals(responses.size(),2);
        Assert.assertEquals(responses.get(0).id(),submission1.getId());
        Assert.assertEquals(responses.get(0).resumeText(),submission1.getResumeText());
        Assert.assertEquals(responses.get(0).jobDescription(),submission1.getJobDescription());
        Assert.assertEquals(responses.get(0).submissionStatus(),submission1.getStatus());

        Assert.assertEquals(responses.get(1).id(),submission2.getId());
        Assert.assertEquals(responses.get(1).resumeText(),submission2.getResumeText());
        Assert.assertEquals(responses.get(1).jobDescription(),submission2.getJobDescription());
        Assert.assertEquals(responses.get(1).submissionStatus(),submission2.getStatus());

        verify(submissionRepository,times(1)).findAll();

    }


    @Test(priority = 1)
    public void createSubmission(){

        SubmissionRequest submissionRequest = new SubmissionRequest(
                "Java Spring Boot resume",
                "Backend developer job"
        );
        MlPredictionResponse predictionResponse = new MlPredictionResponse(
                0.85,
                85.0,
                "test-model-v1",
                "Test prediction"
        );

        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation->{
            Submission response = invocation.getArgument(0);
            if(response.getId()==null){
               response.setId(1L);
            }
            return response;
        });

        when(mlClientService.predict("Java Spring Boot resume",
                "Backend developer job")).thenReturn(predictionResponse);
        SubmissionResponse responses = submissionService.createSubmission(submissionRequest);

        Assert.assertEquals(responses.id(),1L);
        Assert.assertEquals(responses.resumeText(),"Java Spring Boot resume");
        Assert.assertEquals(responses.jobDescription(),"Backend developer job");

        verify(submissionRepository,times(2)).save(any(Submission.class));
        verify(mlClientService,times(1)).predict("Java Spring Boot resume",
                "Backend developer job");
        verify(screeningResultService,times(1)).createScreeningResult(any(Submission.class),eq(predictionResponse));


    }

}