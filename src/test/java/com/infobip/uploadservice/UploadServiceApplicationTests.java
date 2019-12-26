package com.infobip.uploadservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
class UploadServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ExecutorService executorService;

	@Test
	void parallelFileUpload() throws Exception {

		int fileNumber = 100;

		List<HttpUploadRequest> mvcResults = new ArrayList<>();
		for(int i = 0; i < fileNumber; i++) {
			HttpUploadRequest task = new HttpUploadRequest(i);
			mvcResults.add(task);
		}

		List<Future<MvcResult>> mvcResultsFutures = executorService.invokeAll(mvcResults);

		for(Future<MvcResult> resultFuture : mvcResultsFutures) {
			MvcResult result = resultFuture.get();
			assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
		}
	}

	public class HttpUploadRequest implements Callable<MvcResult> {
		int fileSize = 50 * 1024 * 1024;
		int i;

		HttpUploadRequest(int i) {
			this.i = i;
		}

		public MvcResult call() throws Exception {
			MockMultipartHttpServletRequestBuilder mockMvcRequestBuilders = MockMvcRequestBuilders.multipart("/api/v1/upload");
			mockMvcRequestBuilders.header("Content-Length", fileSize);
			mockMvcRequestBuilders.file("test" + this.i + ".zip", new byte[fileSize]).contentType("multipart/form-data; boundary=Boundary");
			mockMvcRequestBuilders.header("X-Upload-File", "test" + this.i + ".zip");
			return mockMvc.perform(mockMvcRequestBuilders).andReturn();
		}
	}

}
