package com.virnect.workspace.api;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

import com.virnect.workspace.dto.request.WorkspaceTitleUpdateRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorkspaceControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("update remote logo")
	void updateRemoteLogo() throws Exception {
		String workspaceUUID = "4d6eab0860969a50acbfa4599fbb5ae8";
		String masterUserUUID = "498b1839dc29ed7bb2ee90ad6985c608";
		String url = String.format("/workspaces/%s/logo/remote", workspaceUUID);

		MockMultipartFile splashLogo = new MockMultipartFile(
			"remoteAndroidSplashLogo", "new-splash.png", "image/png", "image".getBytes());
		MockMultipartFile loginLogo = new MockMultipartFile(
			"remoteAndroidLoginLogo", "new-login.png", "image/png", "image".getBytes());
		MockMultipartFile hololens2Logo = new MockMultipartFile(
			"remoteHololens2CommonLogo", "new-hls.png", "image/png", "image".getBytes());

		MultiValueMap<String, String> workspaceRemoteLogoUpdateRequest = new LinkedMultiValueMap<>();
		workspaceRemoteLogoUpdateRequest.set("userId", masterUserUUID);

		mockMvc.perform(MockMvcRequestBuilders
				.multipart(url)
				.file(splashLogo)
				.file(loginLogo)
				.file(hololens2Logo)
				.params(workspaceRemoteLogoUpdateRequest)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andReturn();
	}

	@SneakyThrows
	@Test
	@DisplayName("get worksapce users")
	void getUserWorkspaces() {
		String workspaceUUID = "4d6eab0860969a50acbfa4599fbb5ae8";
		String url = String.format("/workspaces/home/%s", workspaceUUID);

		mockMvc.perform(MockMvcRequestBuilders
				.get(url)
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andReturn();
	}

	@Test
	@DisplayName("get worksapce defail info")
	void getWorkspaceDetailInfo() throws Exception {
		String userId = "498b1839dc29ed7bb2ee90ad6985c608";
		String url = "/workspaces?userId=" + userId;

		mockMvc.perform(MockMvcRequestBuilders
				.get(url)
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andReturn();
	}

	@Test
	@DisplayName("update workspace info")
	void setWorkspace() throws Exception {
		String workspaceUUID = "4d6eab0860969a50acbfa4599fbb5ae8";
		String masterUserUUID = "498b1839dc29ed7bb2ee90ad6985c608";

		String url = "/workspaces";

		MultiValueMap<String, String> workspaceUpdateRequest = new LinkedMultiValueMap<>();
		workspaceUpdateRequest.set("workspaceId", workspaceUUID);
		workspaceUpdateRequest.set("userId", masterUserUUID);
		workspaceUpdateRequest.set("name", "워크스페이스 이름");
		workspaceUpdateRequest.set("description", "워크스페이스 설명");

		mockMvc.perform(MockMvcRequestBuilders
				.put(url)
				.params(workspaceUpdateRequest)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andReturn();
	}

	@Test
	@DisplayName("get worksapce license info")
	void getWorkspaceLicenseInfo() throws Exception {
		String workspaceUUID = "4d6eab0860969a50acbfa4599fbb5ae8";

		String url = String.format("/workspaces/%s/license", workspaceUUID);

		mockMvc.perform(MockMvcRequestBuilders
				.get(url)
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andReturn();
	}

	@Test
	@DisplayName("delete all workspace info")
	void deleteAllWorkspaceInfo() throws Exception {
		String userUUID = "4b260e69bd6fa9a583c9bbe40f5aceb3";

		String url = String.format("/workspaces/secession/%s", userUUID);

		mockMvc.perform(MockMvcRequestBuilders
				.delete(url)
				.header("serviceID", "user-server")
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andReturn();
	}

	@Test
	@DisplayName("get workspace info")
	void getWorkspaceInfo() throws Exception {
		String workspaceUUID = "4d6eab0860969a50acbfa4599fbb5ae8";

		String url = String.format("/workspaces/%s/info", workspaceUUID);

		mockMvc.perform(MockMvcRequestBuilders
				.get(url)
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andReturn();
	}

	@Test
	@DisplayName("create workspace")
	void createWorkspace() throws Exception {
		String userUUID = "405893f61bec9ac295ca5fa270baca7f";

		String url = "/workspaces";

		MockMultipartFile profile = new MockMultipartFile(
			"profile", "new.png", "image/png", "image".getBytes());

		MultiValueMap<String, String> workspaceCreateRequest = new LinkedMultiValueMap<>();
		workspaceCreateRequest.set("userId", userUUID);
		workspaceCreateRequest.set("name", "워크스페이스 이름");
		workspaceCreateRequest.set("description", "워크스페이스 설명");

		mockMvc.perform(MockMvcRequestBuilders
				.multipart(url)
				.file(profile)
				.params(workspaceCreateRequest)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andReturn();
	}

	@Test
	@DisplayName("update workspace logo")
	void updateWorkspaceLogo() throws Exception {
		String workspaceUUID = "4d6eab0860969a50acbfa4599fbb5ae8";
		String masterUserUUID = "498b1839dc29ed7bb2ee90ad6985c608";

		String url = String.format("/workspaces/%s/logo", workspaceUUID);

		MockMultipartFile profile = new MockMultipartFile(
			"defaultLogo", "new.png", "image/png", "image".getBytes());

		MultiValueMap<String, String> workspaceLogoUpdateRequest = new LinkedMultiValueMap<>();
		workspaceLogoUpdateRequest.set("userId", masterUserUUID);

		mockMvc.perform(MockMvcRequestBuilders
				.multipart(url)
				.file(profile)
				.params(workspaceLogoUpdateRequest)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andReturn();
	}

	@Test
	@DisplayName("update workspace favicon")
	void updateWorkspaceFavicon() throws Exception {
		String workspaceUUID = "4d6eab0860969a50acbfa4599fbb5ae8";
		String masterUserUUID = "498b1839dc29ed7bb2ee90ad6985c608";

		String url = String.format("/workspaces/%s/favicon", workspaceUUID);

		MockMultipartFile favion = new MockMultipartFile("favicon", "new.png", "image/png", "image".getBytes());

		MultiValueMap<String, String> workspaceFaviconUpdateRequest = new LinkedMultiValueMap<>();
		workspaceFaviconUpdateRequest.set("userId", masterUserUUID);

		mockMvc.perform(MockMvcRequestBuilders
				.multipart(url)
				.file(favion)
				.params(workspaceFaviconUpdateRequest)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andReturn();
	}

	@Test
	@DisplayName("update workspace title")
	void updateWorkspaceTitle() throws Exception {
		String workspaceUUID = "4d6eab0860969a50acbfa4599fbb5ae8";
		String masterUserUUID = "498b1839dc29ed7bb2ee90ad6985c608";

		String url = String.format("/workspaces/%s/title", workspaceUUID);

		WorkspaceTitleUpdateRequest workspaceTitleUpdateRequest = new WorkspaceTitleUpdateRequest();
		workspaceTitleUpdateRequest.setTitle("VIRNECT");
		workspaceTitleUpdateRequest.setUserId(masterUserUUID);

		mockMvc.perform(MockMvcRequestBuilders
				.post(url)
				.content(objectMapper.writeValueAsString(workspaceTitleUpdateRequest))
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andReturn();
	}

}
