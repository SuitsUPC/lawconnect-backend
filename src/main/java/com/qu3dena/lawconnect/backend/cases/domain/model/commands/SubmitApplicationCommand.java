package com.qu3dena.lawconnect.backend.cases.domain.model.commands;

import java.util.UUID;

/**
 * Command to submit an application to a case.
 *
 * @param caseId the ID of the case to which the application is being submitted
 * @param lawyerId the ID of the lawyer submitting the application
 * @since 1.0
 * @author LawConnect Team
 */
public record SubmitApplicationCommand(UUID caseId, UUID lawyerId) {
}
