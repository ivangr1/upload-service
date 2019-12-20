package com.infobip.uploadservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UploadServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void parallelMultipartFileUpload() throws Exception {

		int fileNumber = 100;
		int fileSizeMb = 50;

		int fileSize = fileSizeMb * 1024 * 1024;

		for(int i = 0; i < fileNumber; i++) {
			MockMultipartHttpServletRequestBuilder mockMvcRequestBuilders = MockMvcRequestBuilders.multipart("/api/v1/upload");
			mockMvcRequestBuilders.header("Content-Length", fileSize);
			mockMvcRequestBuilders.file("test" + i + ".zip", new byte[fileSize]).contentType("multipart/form-data; boundary=Boundary");
			mockMvcRequestBuilders.header("X-Upload-File", "test" + i + ".zip");
			mockMvc.perform(mockMvcRequestBuilders).andExpect(status().isCreated());
		}
	}

}
