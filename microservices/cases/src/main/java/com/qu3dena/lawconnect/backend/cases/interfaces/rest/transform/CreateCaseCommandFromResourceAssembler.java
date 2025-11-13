package com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.CreateCaseCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseTitle;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.CreateCaseResource;
import com.qu3dena.lawconnect.backend.shared.domain.model.valueobjects.Description;

public class CreateCaseCommandFromResourceAssembler {
    public static CreateCaseCommand toCommandFromResource(CreateCaseResource resource) {
        return new CreateCaseCommand(
                resource.clientId(),
                new CaseTitle(resource.title()),
                new Description(resource.description()),
                resource.specialtyId()
        );
    }
}
