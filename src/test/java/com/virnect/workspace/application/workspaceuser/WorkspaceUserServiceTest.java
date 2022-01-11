package com.virnect.workspace.application.workspaceuser;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.virnect.workspace.dto.request.WorkspaceMemberPasswordChangeRequest;

/**
 * Project: PF-Workspace
 * DATE: 2021-07-16
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorkspaceUserServiceTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void memberPasswordChange() throws Exception {
		String url = "/workspaces/4d6eab0860969a50acbfa4599fbb5ae8/members/password";
		WorkspaceMemberPasswordChangeRequest passwordChangeRequest = new WorkspaceMemberPasswordChangeRequest();
		passwordChangeRequest.setUserId("273435b53ca1462d9e8ec58b78e5ad22");
		passwordChangeRequest.setRequestUserId("498b1839dc29ed7bb2ee90ad6985c608");
		passwordChangeRequest.setPassword("virnect1!");

		mockMvc.perform(
				MockMvcRequestBuilders
					.post(url)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(passwordChangeRequest)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.passwordChangedDate").exists())
			.andReturn();
	}
}
