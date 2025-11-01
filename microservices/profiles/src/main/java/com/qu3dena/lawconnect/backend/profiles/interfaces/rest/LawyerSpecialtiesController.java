package com.qu3dena.lawconnect.backend.profiles.interfaces.rest;

import com.qu3dena.lawconnect.backend.profiles.domain.model.queries.GetAllLawyerSpecialtiesQuery;
import com.qu3dena.lawconnect.backend.profiles.domain.services.LawyerSpecialtyQueryService;
import com.qu3dena.lawconnect.backend.profiles.interfaces.rest.resources.LawyerSpecialtyResource;
import com.qu3dena.lawconnect.backend.profiles.interfaces.rest.transform.LawyerSpecialtyResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/lawyer-specialties", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Lawyer Specialties", description = "Lawyer Specialties Management Endpoints")
public class LawyerSpecialtiesController {

    private final LawyerSpecialtyQueryService queryService;

    public LawyerSpecialtiesController(LawyerSpecialtyQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    @Operation(summary = "Get lawyer specialties", description = "Get lawyer specialties")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lawyer Specialties retrieved successfully.")})
    public ResponseEntity<Set<LawyerSpecialtyResource>> getAllLawyerSpecialties() {
        var query = new GetAllLawyerSpecialtiesQuery();
        var lawyerSpecialties = queryService.handle(query);

        var lawyerSpecialtyResources = lawyerSpecialties
                .stream()
                .map(LawyerSpecialtyResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(lawyerSpecialtyResources);
    }
}
