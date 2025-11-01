package com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.SubmitApplicationCommand;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.SubmitApplicationResource;

public class SubmitApplicationCommandFromResourceAssembler {
    public static SubmitApplicationCommand toCommandFromResource(SubmitApplicationResource resource) {
        return new SubmitApplicationCommand(
                resource.caseId(),
                resource.lawyerId()
        );
    }
}
