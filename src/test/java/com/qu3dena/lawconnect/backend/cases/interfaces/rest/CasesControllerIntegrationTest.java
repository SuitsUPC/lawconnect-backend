package com.qu3dena.lawconnect.backend.cases.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.CreateCaseResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for CasesController endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class CasesControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/cases -> 201 Created and body contains id and title")
    void createCase_shouldReturnCreated() throws Exception {
        // 1). Create a case payload
        var payload = new CreateCaseResource(
                UUID.randomUUID(),
                "Test Case",
                "This is a test case description"
        );

        // 2). Perform the POST request to create a case
        mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                // 3). Expect the response status to be 201 Created
                .andExpect(status().isCreated())
                // 4). Expect the response body to contain the case ID and title
                .andExpect(jsonPath("$.id").exists())
                // 5). Expect the response body to contain the title
                .andExpect(jsonPath("$.title").value("Test Case"));
    }

    @Test
    @DisplayName("PUT /api/v1/cases/{id}/close -> 500 when case not accepted")
    void closeCase_whenNotAccepted_shouldReturnServerError() throws Exception {
        // 1). Create a client ID and a case payload
        var clientId = UUID.randomUUID();
        var create = new CreateCaseResource(clientId, "To Close", "Desc");

        // 2). Perform the POST request to create a case
        var createResult = mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                // 3). Expect the response status to be 201 Created
                .andExpect(status().isCreated())
                // 4). Capture the response to extract the case ID
                .andReturn();

        // 5). Extract the case ID from the response body
        var body = createResult.getResponse().getContentAsString();

        // 6). Convert the case ID from the response body to a UUID
        var caseId = UUID.fromString(objectMapper
                // 7). Read the JSON response and get the "id" field
                .readTree(body).get("id").asText());

        // 8). Perform the PUT request to close the case
        mockMvc.perform(put("/api/v1/cases/" + caseId + "/close")
                        .param("clientId", clientId.toString()))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("PUT /api/v1/cases/{id}/cancel -> 200 OK and status CANCELED")
    void cancelCase_shouldReturnOkWithCanceledStatus() throws Exception {
        // 1). Create a client ID and a case payload
        var clientId = UUID.randomUUID();
        var create = new CreateCaseResource(clientId, "To Cancel", "This case will be canceled");

        // 2). Perform the POST request to create a case
        var createResult = mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                // 3). Expect the response status to be 201 Created
                .andExpect(status().isCreated())
                .andReturn();

        // 4). Extract the case ID from the response body
        var body = createResult.getResponse()
                .getContentAsString();

        // 5). Convert the case ID from the response body to a UUID
        var caseId = UUID.fromString(objectMapper
                .readTree(body).get("id").asText());

        // 6). Perform the PUT request to cancel the case
        mockMvc.perform(put("/api/v1/cases/" + caseId + "/cancel")
                        .param("clientId", clientId.toString()))
                // 7). Expect the response status to be 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    @DisplayName("GET /api/v1/cases/{id} -> 200 OK and returns case")
    void getCaseById_shouldReturnCase() throws Exception {
        // 1). Create a client ID and a case payload
        var clientId = UUID.randomUUID();
        var create = new CreateCaseResource(clientId, "Fetch Me", "Desc");

        // 2). Perform the POST request to create a case
        var createResult = mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                // 3). Expect the response status to be 201 Created
                .andExpect(status().isCreated())
                .andReturn();

        // 4). Extract the case ID from the response body
        var caseId = UUID.fromString(objectMapper
                .readTree(createResult.getResponse().getContentAsString()).get("id").asText());

        // 5). Perform the GET request to retrieve the case by ID
        mockMvc.perform(get("/api/v1/cases/" + caseId))
                // 6). Expect the response status to be 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(caseId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/cases?clientId= -> 200 OK and returns list of cases")
    void getCasesByClient_shouldReturnList() throws Exception {
        // 1). Create a client ID and a case payload
        var clientId = UUID.randomUUID();
        var create = new CreateCaseResource(clientId, "Client Cases", "Desc");

        // 2). Perform the POST request to create a case
        mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                // 3). Expect the response status to be 201 Created
                .andExpect(status().isCreated());

        // 4). Perform the GET request to retrieve cases by client ID
        mockMvc.perform(get("/api/v1/cases").param("clientId", clientId.toString()))
                // 5). Expect the response status to be 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/cases/suggested?lawyerId= -> 200 OK and array response")
    void getSuggestedCases_shouldReturnArray() throws Exception {
        // 1). Perform the GET request to retrieve suggested cases for a lawyer
        mockMvc.perform(get("/api/v1/cases/suggested")
                        .param("lawyerId", UUID.randomUUID().toString()))
                // 2). Expect the response status to be 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/cases/status?status=OPEN -> 200 OK and array response")
    void getCasesByStatus_ShouldReturnArray() throws Exception {
        // 1). Create a client ID and a case payload
        var clientId = UUID.randomUUID();
        var create = new CreateCaseResource(clientId, "Status Test", "Desc");

        // 2). Perform the POST request to create a case
        mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                // 3). Expect the response status to be 201 Created
                .andExpect(status().isCreated());

        // 4). Perform the GET request to retrieve cases by status
        mockMvc.perform(get("/api/v1/cases/status").param("status", "OPEN"))
                // 5). Expect the response status to be 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/cases/lawyer/{lawyerId} -> 200 OK and array response")
    void getCasesByLawyer_shouldReturnArray() throws Exception {
        // 1). Perform the GET request to retrieve cases by lawyer ID
        mockMvc.perform(get("/api/v1/cases/lawyer/" + UUID.randomUUID()))
                // 2). Expect the response status to be 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
