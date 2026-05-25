package com.example.resume.submission;

import com.example.resume.ml.MlClientService;
import com.example.resume.ml.MlPredictionResponse;
import com.example.resume.screeningresult.ScreeningResultRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
public class SubmissionControllerTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScreeningResultRepository resultRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private SubmissionService submissionService;

    @MockitoBean
    private MlClientService mlClientService;

    @Test(priority = 1)
    @Transactional
    public void createSubmission() throws Exception {

        MlPredictionResponse predictionResponse = new MlPredictionResponse(
                0.85,
                85.0,
                "test-model-v1",
                "Test prediction"
        );

        when(mlClientService.predict(anyString(), anyString())).thenReturn(predictionResponse);

        String requestbody = """
                {
                  "resumeText": "Java Spring Boot resume",
                                    "jobDescription": "Backend developer job"
                }
                """;
        mockMvc.perform(post("/api/v1/submissions").contentType(MediaType.APPLICATION_JSON).content(requestbody)).andExpect(status().isCreated()).andDo(print()).andExpect(jsonPath("$.submissionStatus").value("SCORED"));

    }

    @Test(priority = 2)
    public void SubmissionIfMlPredictionFails() throws Exception{
        when(mlClientService.predict(anyString(),anyString())).thenThrow(new RuntimeException("Cannot predict"));

        String requestbody = """
                {
                  "resumeText": "Java Spring Boot resume",
                                    "jobDescription": "Backend developer job"
                }
                """;
        mockMvc.perform(post("/api/v1/submissions").contentType(MediaType.APPLICATION_JSON).content(requestbody)).andExpect(status().isOk()).andDo(print()).andExpect(jsonPath("$.submissionStatus").value("FAILED"));

        Assert.assertEquals(submissionRepository.findAll().size(),2);
    }

    @Test(priority = 3)
    public void getSubmissionByIdIfSubmissionExists() throws Exception{

        Submission submission = new Submission();
        submission.setResumeText("Java Spring Boot resume");
        submission.setJobDescription("Backend developer job");
        submission.setStatus(SubmissionStatus.SCORED);
        submission.setCreatedAt(LocalDateTime.now());
        Submission saved = submissionRepository.save(submission);

        mockMvc.perform(get("/api/v1/submissions/"+saved.getId())).
                andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.resumeText").value("Java Spring Boot resume"))
                .andExpect(jsonPath("$.jobDescription").value("Backend developer job"));

    }


}