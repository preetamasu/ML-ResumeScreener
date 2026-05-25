package com.example.resume.screeningresult;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/submissions")
public class ScreeningResultController {
    private final ScreeningResultService screeningResultService;

    public ScreeningResultController(ScreeningResultService screeningResultService) {
        this.screeningResultService = screeningResultService;
    }

    @GetMapping("/{submissionId}/result")
    public ResponseEntity<ScreeningResponse> getScreeningResultBySubmissionId(
            @PathVariable Long submissionId
    ) {
        ScreeningResponse response = screeningResultService
                .getScreeningResultBySubmissionId(submissionId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
//    @PostMapping("/{submissionId}/result/test")
//    public ResponseEntity<ScreeningResponse> createTestScreeningResult(
//            @PathVariable Long submissionId
//    ) {
//        ScreeningResponse response = screeningResultService
//                .createScreeningResult(submissionId,);
//
//        return new ResponseEntity<>(response, HttpStatus.CREATED);
//    }
}
