package com.qu3dena.lawconnect.backend.cases.domain.model.commands;

import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseTitle;
import com.qu3dena.lawconnect.backend.shared.domain.model.valueobjects.Description;

import java.util.UUID;

/**
 * Command to create a new case.
 *
 * @param clientId    the ID of the client associated with the case
 * @param title       the title of the case
 * @param description the description of the case
 * @param specialtyId the ID of the legal specialty required for this case
 * @author LawConnect Team
 * @since 1.0
 */
public record CreateCaseCommand(
        UUID clientId,
        CaseTitle title,
        Description description,
        Long specialtyId
) {
}
