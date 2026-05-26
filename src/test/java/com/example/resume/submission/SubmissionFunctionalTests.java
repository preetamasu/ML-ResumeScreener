package com.example.resume.submission;

import com.example.resume.ml.MlClientService;
import com.example.resume.ml.MlPredictionResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.post;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubmissionFunctionalTests extends AbstractTestNGSpringContextTests {

    @LocalServerPort
    private int port;

    @Autowired
    private SubmissionRepository submissionRepository;

    @MockitoBean
    private MlClientService mlClientService;

    @BeforeMethod
    public void setUp(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    public void createSubmissionReturnsScored(){
        when(mlClientService.predict(anyString(),anyString())).thenReturn(new MlPredictionResponse(
                0.85, 85.0, "test-model-v1", "Test prediction"
        ));

        given()
                .contentType(ContentType.JSON)
                .body(
                        """
{
"resumeText": "Java Spring Boot resume",
                    "jobDescription": "Backend developer job"
}
""")
                                .when()
                .post("/api/v1/submissions")
                .then()
                .statusCode(201)
                .body("submissionStatus",equalTo("SCORED"))
                .body("id",notNullValue());
    }

    @Test
    public void createSubmissionIfMlPredictionFails(){
        when(mlClientService.predict(anyString(),anyString())).thenThrow(
                new RuntimeException("Cannot predict")
        );
        given()
                .contentType(ContentType.JSON)
                .body(
                        """
{
"resumeText": "Java Spring Boot resume",
                    "jobDescription": "Backend developer job"
}
""")
                .when()
                .post("/api/v1/submissions")
                .then()
                .statusCode(201)
                .body("submissionStatus",equalTo("FAILED"))
                .body("id",notNullValue());
    }

    @Test
    public void findBySubmissionByIdIfSubmissionExists(){

        Submission submission1 = new Submission();
        submission1.setResumeText("Java Spring Boot resume");
        submission1.setJobDescription("Backend developer job");
        submission1.setStatus(SubmissionStatus.SCORED);
        submission1.setCreatedAt(LocalDateTime.now());
        Submission saved = submissionRepository.save(submission1);


        given()
                .when().get("/api/v1/submissions/"+saved.getId())
                .then()
                .statusCode(200)
                .body("resumeText",equalTo("Java Spring Boot resume"))
                .body("id",notNullValue());
    }

    @Test
    public void findBySubmissionIdIfIdNotExists(){
        given()
                .when().get("/api/v1/submissions/9999")
                .then()
                .statusCode(404);
    }

}
