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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
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

    @Test
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
        Assert.assertEquals(submissionRepository.findAll().size(), 1);

    }


}