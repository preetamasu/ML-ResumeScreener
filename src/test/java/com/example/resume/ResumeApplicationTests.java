package com.example.resume;

import com.example.resume.ml.MlClientService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@SpringBootTest
class ResumeApplicationTests extends AbstractTestNGSpringContextTests {

	@MockitoBean
	private MlClientService mlClientService;

	@Test
	void contextLoads() {
	}

}
