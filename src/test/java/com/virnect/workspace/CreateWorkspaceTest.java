package com.virnect.workspace;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import com.virnect.workspace.exception.WorkspaceException;

/**
 * Project: PF-Workspace
 * DATE: 2020-05-25
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
public class CreateWorkspaceTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void createWorkspace_SecondWorkspace_WorkspaceException() throws Exception {
        // given
        RequestBuilder request = post("/workspaces")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("userId", "498b1839dc29ed7bb2ee90ad6985c608")
                .param("name", "이름")
                .param("description", "설명");
        // when
        this.mockMvc.perform(request)
                .andDo(print())
                // then
                .andExpect((result -> assertTrue(result.getResponse().getContentAsString().contains("1001"))))//ㅎㅎ;;;
                .andExpect((result -> assertTrue(result.getResolvedException().getClass().isAssignableFrom(WorkspaceException.class))));
    }
    @Test
    public void creete(){
    /*    for(int temp=635;temp<695;temp++){

            for (int a=1; a<7; a++){
                System.out.println("INSERT INTO `license` (`created_at`, `updated_at`, `serial_key`, `license_status`, `user_id`, `workspace_id`, `license_product_id`) VALUES ('2020-07-30 21:54:26', '2020-07-30 21:54:26', '"+
                        UUID.randomUUID().toString().toUpperCase()+"', 1, NULL, NULL, "+temp+");");

            }
        }
*/
        Locale indiaLocale = new Locale("en", "");

        System.out.println(indiaLocale.getCountry());
        System.out.println(indiaLocale.getLanguage());


    }


}
