package com.qu3dena.lawconnect.backend.cases.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.CreateCaseResource;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.InviteLawyerResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.transaction.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class InvitationsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/invitations -> 201 Created and body contains id, caseId, lawyerId and PENDING status")
    void inviteLawyer_shouldReturnCreated() throws Exception {
        // 1). Generate a client ID and create a case payload.
        var clientId = UUID.randomUUID();
        var createCase = new CreateCaseResource(clientId, "InvTest Case", "For Invitations");

        // 2). Perform POST to create the case.
        var createResp = mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCase)))
                // 3). Expect status 201.
                .andExpect(status().isCreated())
                .andReturn();

        // 4). Extract case ID from the response.
        var caseId = UUID.fromString(
                objectMapper.readTree(createResp.getResponse().getContentAsString()).get("id").asText()
        );

        // 5). Generate a lawyer ID and create the invitation payload.
        var lawyerId = UUID.randomUUID();
        var invite = new InviteLawyerResource(caseId, lawyerId, clientId);

        // 6). Perform POST to send the invitation.
        mockMvc.perform(post("/api/v1/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invite)))
                // 7). Expect status 201 and validate response body contains id, caseId, lawyerId.
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.caseId").value(caseId.toString()))
                .andExpect(jsonPath("$.lawyerId").value(lawyerId.toString()))
                // 8). Expect the invitation status to be PENDING.
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/v1/invitations?lawyerId= -> 200 OK and array with the invitation")
    void getInvitations_shouldReturnArray() throws Exception {
        // 1). Generate a client ID and create a case payload.
        var clientId = UUID.randomUUID();
        var createCase = new CreateCaseResource(clientId, "InvList Case", "For list test");

        // 2). Perform POST to create the case.
        var createResp = mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCase)))
                // 3). Expect status 201.
                .andExpect(status().isCreated())
                .andReturn();

        // 4). Extract the case ID from the response.
        var caseId = UUID.fromString(
                objectMapper.readTree(createResp.getResponse().getContentAsString()).get("id").asText()
        );

        // 5). Generate a lawyer ID and create invitation payload.
        var lawyerId = UUID.randomUUID();
        var invitation = new InviteLawyerResource(caseId, lawyerId, clientId);

        // 6). Perform POST to create invitation.
        var invResp = mockMvc.perform(post("/api/v1/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invitation)))
                // 7). Expect status 201.
                .andExpect(status().isCreated())
                .andReturn();

        // 8). Perform GET to retrieve invitation by lawyer ID.
        mockMvc.perform(get("/api/v1/invitations").param("lawyerId", lawyerId.toString()))
                // 9). Expect status 200.
                .andExpect(status().isOk())
                // 10). Expect an array response and validate the first invitation.
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("PUT /api/v1/invitations/{id}/accept -> 200 OK and status ACCEPTED")
    void acceptInvitation_shouldReturnAccepted() throws Exception {
        // 1). Generate a client ID and create a case payload.
        var clientId = UUID.randomUUID();
        var createCase = new CreateCaseResource(clientId, "InvAccept Case", "To accept");

        // 2). Perform POST to create the case.
        var createResp = mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCase)))
                // 3). Expect status 201.
                .andExpect(status().isCreated())
                .andReturn();

        // 4). Extract the case ID from the response.
        var caseId = UUID.fromString(
                objectMapper.readTree(createResp.getResponse().getContentAsString()).get("id").asText()
        );

        // 5). Generate a lawyer ID and create invitation payload.
        var lawyerId = UUID.randomUUID();
        var invitation = new InviteLawyerResource(caseId, lawyerId, clientId);

        // 6). Perform POST to create invitation.
        var invResp = mockMvc.perform(post("/api/v1/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invitation)))
                // 7). Expect status 201.
                .andExpect(status().isCreated())
                .andReturn();

        // 8). Extract invitation ID from the response.
        var invitationId = objectMapper.readTree(invResp.getResponse().getContentAsString()).get("id").asLong();

        // 9). Perform PUT to accept the invitation.
        mockMvc.perform(put("/api/v1/invitations/" + invitationId + "/accept")
                        .param("lawyerId", lawyerId.toString()))
                // 10). Expect status 200 and invitation status ACCEPTED.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("PUT /api/v1/invitations/{id}/reject -> 200 OK and status REJECTED")
    void rejectInvitation_shouldReturnRejected() throws Exception {
        // 1). Generate a client ID and create a case payload.
        var clientId = UUID.randomUUID();
        var createCase = new CreateCaseResource(clientId, "InvReject Case", "To reject");

        // 2). Perform POST to create the case.
        var createResp = mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCase)))
                // 3). Expect status 201.
                .andExpect(status().isCreated())
                .andReturn();

        // 4). Extract the case ID from the response.
        var caseId = UUID.fromString(
                objectMapper.readTree(createResp.getResponse().getContentAsString()).get("id").asText()
        );

        // 5). Generate a lawyer ID and create invitation payload.
        var lawyerId = UUID.randomUUID();
        var invite = new InviteLawyerResource(caseId, lawyerId, clientId);

        // 6). Perform POST to create the invitation.
        var invResp = mockMvc.perform(post("/api/v1/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invite)))
                // 7). Expect status 201.
                .andExpect(status().isCreated())
                .andReturn();

        // 8). Extract the invitation ID from the response.
        var invId = objectMapper.readTree(invResp.getResponse().getContentAsString()).get("id").asLong();

        // 9). Perform PUT to reject the invitation.
        mockMvc.perform(put("/api/v1/invitations/" + invId + "/reject")
                        .param("lawyerId", lawyerId.toString()))
                // 10). Expect status 200 and invitation status REJECTED.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }
}
