package com.qu3dena.lawconnect.backend.cases.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.CreateCaseResource;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.SubmitApplicationResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.transaction.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ApplicationsController endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class ApplicationsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/applications -> 201 Created and body contains id, caseId, lawyerId, and SUBMITTED status")
    void submitApplication_shouldReturnCreated() throws Exception {
        // 1). Generate a client ID and create a case payload.
        var clientId = UUID.randomUUID();
        var createCase = new CreateCaseResource(clientId, "AppTest Case", "For Applications");

        // 2). Perform the POST call to create a case.
        var caseResp = mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCase)))
                // 3). Expect the case creation to return 201 Created.
                .andExpect(status().isCreated())
                .andReturn();

        // 4). Extract the case ID from the response.
        var caseId = UUID.fromString(
                objectMapper.readTree(caseResp.getResponse().getContentAsString()).get("id").asText()
        );

        // 5). Generate a lawyer ID and create a SubmitApplication payload.
        var lawyerId = UUID.randomUUID();
        var submit = new SubmitApplicationResource(caseId, lawyerId);

        // 6). Perform the POST call to submit the application.
        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submit)))
                // 7). Expect a 201 Created status and validate fields exist.
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.caseId").value(caseId.toString()))
                .andExpect(jsonPath("$.lawyerId").value(lawyerId.toString()))
                // 8). Verify that the application status is SUBMITTED.
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    @DisplayName("PUT /api/v1/applications/{id}/accept -> 200 OK and status ACCEPTED")
    void acceptApplication_shouldReturnAccepted() throws Exception {
        // 1). Generate a client ID and create a case payload.
        var clientId = UUID.randomUUID();
        var createCase = new CreateCaseResource(clientId, "AppAccept Case", "To accept");

        // 2). Perform a POST call to create the case.
        var caseResp = mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCase)))
                // 3). Expect the case creation to return 201 Created.
                .andExpect(status().isCreated())
                .andReturn();

        // 4). Extract the case ID from the response.
        var caseId = UUID.fromString(
                objectMapper.readTree(caseResp.getResponse().getContentAsString()).get("id").asText()
        );

        // 5). Generate a lawyer ID and create a SubmitApplication payload.
        var lawyerId = UUID.randomUUID();
        var submit = new SubmitApplicationResource(caseId, lawyerId);

        // 6). Perform a POST call to submit the application.
        var appResp = mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submit)))
                // 7). Expect the application submission to return 201 Created.
                .andExpect(status().isCreated())
                .andReturn();

        // 8). Extract the application ID from the response.
        var appId = objectMapper.readTree(appResp.getResponse().getContentAsString()).get("id").asLong();

        // 9). Perform the PUT call to accept the application.
        mockMvc.perform(put("/api/v1/applications/" + appId + "/accept")
                        .param("clientId", clientId.toString()))
                // 10). Expect a 200 OK status and verify the status is ACCEPTED.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("PUT /api/v1/applications/{id}/reject -> 200 OK and status REJECTED")
    void rejectApplication_shouldReturnRejected() throws Exception {
        // 1). Generate a client ID and create a case payload.
        var clientId = UUID.randomUUID();
        var createCase = new CreateCaseResource(clientId, "AppReject Case", "To reject");

        // 2). Perform a POST call to create the case.
        var caseResp = mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCase)))
                // 3). Expect the case creation to return 201 Created.
                .andExpect(status().isCreated())
                .andReturn();

        // 4). Extract the case ID from the response.
        var caseId = UUID.fromString(
                objectMapper.readTree(caseResp.getResponse().getContentAsString()).get("id").asText()
        );

        // 5). Generate a lawyer ID and create a SubmitApplication payload.
        var lawyerId = UUID.randomUUID();
        var submit = new SubmitApplicationResource(caseId, lawyerId);

        // 6). Perform a POST call to submit the application.
        var appResp = mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submit)))
                // 7). Expect the application submission to return 201 Created.
                .andExpect(status().isCreated())
                .andReturn();

        // 8). Extract the application ID from the response.
        var appId = objectMapper.readTree(appResp.getResponse().getContentAsString()).get("id").asLong();

        // 9). Perform the PUT call to reject the application.
        mockMvc.perform(put("/api/v1/applications/" + appId + "/reject")
                        .param("clientId", clientId.toString()))
                // 10). Expect a 200 OK status and verify the status is REJECTED.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @DisplayName("GET /api/v1/applications?caseId= -> 200 OK and returns array of applications")
    void getApplicationsByCase_shouldReturnArray() throws Exception {
        // 1). Generate a client ID and create a case payload.
        var clientId = UUID.randomUUID();
        var createCase = new CreateCaseResource(clientId, "AppList Case", "For list");

        // 2). Perform a POST call to create the case.
        var caseResp = mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCase)))
                // 3). Expect the case creation to return 201 Created.
                .andExpect(status().isCreated())
                .andReturn();

        // 4). Extract the case ID from the response.
        var caseId = UUID.fromString(
                objectMapper.readTree(caseResp.getResponse().getContentAsString()).get("id").asText()
        );

        // 5). Generate a SubmitApplication payload and perform a POST to submit the application.
        var submit = new SubmitApplicationResource(caseId, UUID.randomUUID());

        // 6). Perform the POST call to submit the application.
        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submit)))
                // 7). Expect the application submission to return 201 Created.
                .andExpect(status().isCreated());

        // 8). Perform the GET call to retrieve applications by case ID.
        mockMvc.perform(get("/api/v1/applications").param("caseId", caseId.toString()))
                // 9). Expect a 200 OK status.
                .andExpect(status().isOk())
                // 10). Verify that the response is an array.
                .andExpect(jsonPath("$").isArray());
    }
}
