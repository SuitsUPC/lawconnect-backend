package com.qu3dena.lawconnect.backend.profiles.domain.services;

import com.qu3dena.lawconnect.backend.profiles.domain.model.aggregates.LawyerAggregate;
import com.qu3dena.lawconnect.backend.profiles.domain.model.queries.GetAllLawyersQuery;
import com.qu3dena.lawconnect.backend.profiles.domain.model.queries.GetLawyerByUserIdQuery;
import com.qu3dena.lawconnect.backend.profiles.domain.model.queries.GetLawyerBySpecialtyQuery;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for handling lawyer-related queries.
 * <p>
 * This service defines the contract for processing queries
 * related to lawyer operations, such as retrieving a lawyer by specialty or DNI.
 *
 * @author LawConnect Team
 * @since 1.0
 */
public interface LawyerQueryService {

    /**
     * Handles the retrieval of all lawyers.
     *
     * @param query the query to retrieve all lawyers
     * @return a {@link List} of {@link LawyerAggregate}
     */
    List<LawyerAggregate> handle(GetAllLawyersQuery query);

    /**
     * Handles the retrieval of a lawyer by specialty.
     *
     * @param query the query containing the specialty of the lawyer to retrieve
     * @return an {@link Optional} containing the retrieved {@link LawyerAggregate}, or empty if no lawyer is found
     */
    Optional<LawyerAggregate> handle(GetLawyerBySpecialtyQuery query);

    /**
     * Handles the retrieval of a lawyer by user ID.
     *
     * @param query the query containing the user ID of the lawyer to retrieve
     * @return an {@link Optional} containing the retrieved {@link LawyerAggregate}, or empty if no lawyer is found
     */
    Optional<LawyerAggregate> handle(GetLawyerByUserIdQuery query);
}
