package com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.InviteLawyerCommand;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.InviteLawyerResource;

public class InviteLawyerCommandFromResourceAssembler {
    public static InviteLawyerCommand toCommandFromResource(InviteLawyerResource resource) {
        return new InviteLawyerCommand(
                resource.caseId(),
                resource.lawyerId(),
                resource.clientId()
        );
    }
}
