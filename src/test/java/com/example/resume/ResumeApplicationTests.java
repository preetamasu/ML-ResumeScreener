package com.example.resume;

import com.example.resume.ml.MlClientService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class ResumeApplicationTests {

	@MockitoBean
	private MlClientService mlClientService;

	@Test
	void contextLoads() {
	}

}
