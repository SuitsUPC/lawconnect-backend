package com.qu3dena.lawconnect.backend.cases.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.CreateCaseResource;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.CreateFinalCommentResource;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.CreateGeneralCommentResource;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class CommentsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/comments/general -> 201 Created and returns comment resource")
    void createGeneralComment_ShouldReturnCreated() throws Exception {
        // 1). Generate a client ID and create a case payload for comments.
        var clientId = UUID.randomUUID();
        var createCase = new CreateCaseResource(clientId, "Comment Case", "For commenting");

        // 2). Perform POST to create the case.
        var caseResp = mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCase)))
                .andExpect(status().isCreated())
                .andReturn();

        // 3). Extract the case ID from the response.
        var caseId = UUID.fromString(
                objectMapper.readTree(caseResp.getResponse().getContentAsString()).get("id").asText()
        );

        // 4). Use clientId as the authorId and build the general comment payload.
        var general = new CreateGeneralCommentResource(caseId, clientId, "This is a general comment");

        // 5). Perform POST to create the general comment.
        mockMvc.perform(post("/api/v1/comments/general")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(general)))
                .andExpect(status().isCreated())
                // 6). Validate that commentId exists in the response.
                .andExpect(jsonPath("$.commentId").exists())
                // 7). Validate the caseId in the response.
                .andExpect(jsonPath("$.caseId").value(caseId.toString()))
                // 8). Validate the authorId in the response.
                .andExpect(jsonPath("$.authorId").value(clientId.toString()))
                // 9). Validate the comment text.
                .andExpect(jsonPath("$.comment").value("This is a general comment"));
    }

    @Test
    @DisplayName("POST /api/v1/comments/final -> 500 Internal Server Error when case not accepted")
    void createFinalComment_WhenNotAccepted_ShouldReturnServerError() throws Exception {
        // 1). Generate a client ID and create a case payload for final comment.
        var clientId = UUID.randomUUID();
        var createCase = new CreateCaseResource(clientId, "Comment Case", "For final comment");

        // 2). Perform POST to create the case.
        var caseResp = mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCase)))
                .andExpect(status().isCreated())
                .andReturn();

        // 3). Extract the case ID from the response.
        var caseId = UUID.fromString(
                objectMapper.readTree(caseResp.getResponse().getContentAsString()).get("id").asText()
        );

        // 4). Set clientId as authorId and build the final comment payload.
        var finalComment = new CreateFinalCommentResource(caseId, clientId, "Final review comment");

        // 5). Perform POST to attempt creating a final comment.
        mockMvc.perform(post("/api/v1/comments/final")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(finalComment)))
                // 6). Expect a server error (5xx) since the case is not accepted.
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("GET /api/v1/comments?caseId= -> 200 OK and returns array of comments")
    void getCommentsByCase_ShouldReturnArray() throws Exception {
        // 1). Generate a client ID and create a case payload.
        var clientId = UUID.randomUUID();
        var createCase = new CreateCaseResource(clientId, "Comment Case", "For get comments");

        // 2). Perform POST to create the case.
        var caseResp = mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCase)))
                .andExpect(status().isCreated())
                .andReturn();

        // 3). Extract the case ID from the response.
        var caseId = UUID.fromString(
                objectMapper.readTree(caseResp.getResponse().getContentAsString()).get("id").asText()
        );

        // 4). Build and perform POST call to create a general comment.
        mockMvc.perform(post("/api/v1/comments/general")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateGeneralCommentResource(caseId, clientId, "Comment text")
                        )))
                .andExpect(status().isCreated());

        // 5). Perform GET to retrieve comments by case ID.
        mockMvc.perform(get("/api/v1/comments").param("caseId", caseId.toString()))
                // 6). Expect HTTP status 200.
                .andExpect(status().isOk())
                // 7). Validate that response is an array.
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("DELETE /api/v1/comments/{commentId}?authorId= -> 204 No Content")
    void deleteComment_ShouldReturnNoContent() throws Exception {
        // 1). Generate a client ID and create a case payload.
        var clientId = UUID.randomUUID();
        var createCase = new CreateCaseResource(clientId, "Comment Case", "For delete comment");

        // 2). Perform POST to create the case.
        var caseResp = mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCase)))
                .andExpect(status().isCreated())
                .andReturn();

        // 3). Extract the case ID from the response.
        var caseId = UUID.fromString(
                objectMapper.readTree(caseResp.getResponse().getContentAsString()).get("id").asText()
        );

        // 4). Build a general comment payload to be deleted.
        var general = new CreateGeneralCommentResource(caseId, clientId, "To be deleted");

        // 5). Perform POST to create the comment.
        var commentResp = mockMvc.perform(post("/api/v1/comments/general")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(general)))
                .andExpect(status().isCreated())
                .andReturn();

        // 6). Extract the comment ID from the response.
        var commentId = objectMapper.readTree(commentResp.getResponse().getContentAsString())
                .get("commentId").asLong();

        // 7). Perform DELETE to remove the comment using commentId and authorId.
        mockMvc.perform(delete("/api/v1/comments/" + commentId)
                        .param("authorId", clientId.toString()))
                // 8). Expect a 204 No Content HTTP status.
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/comments/lawyer/{lawyerId}/final -> 200 OK and returns array")
    void getFinalCommentsByLawyer_ShouldReturnArray() throws Exception {
        // 1). Generate a lawyer ID.
        var lawyerId = UUID.randomUUID();

        // 2). Perform GET to retrieve final comments for the lawyer.
        mockMvc.perform(get("/api/v1/comments/lawyer/" + lawyerId + "/final"))
                // 3). Expect HTTP status 200.
                .andExpect(status().isOk())
                // 4). Validate that response is an array.
                .andExpect(jsonPath("$").isArray());
    }
}
