package com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources;

import java.util.UUID;

/**
 * Resource representation for submitting an application.
 * Encapsulates the details required to submit an application, including the case ID,
 * lawyer ID, and a cover letter message.
 *
 * @param caseId   the unique identifier of the case associated with the application
 * @param lawyerId the unique identifier of the lawyer submitting the application
 * @param message  the cover letter or introduction message from the lawyer
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record SubmitApplicationResource(
        UUID caseId,
        UUID lawyerId,
        String message
) { }
