package com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa;

import com.qu3dena.lawconnect.backend.cases.domain.model.aggregates.CaseAggregate;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Application;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Comment;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Invitation;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.*;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.CaseRepository;
import com.qu3dena.lawconnect.backend.shared.domain.model.valueobjects.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.util.*;

/**
 * Data seeder for initializing the Cases database with sample data.
 * <p>
 * Creates 3 unique cases per client with realistic assignments to lawyers.
 * Queries Profiles service to get real profile IDs.
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
@Configuration
public class DataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner initCasesDatabase(CaseRepository caseRepository) {
        return args -> {
            // Check if data already exists
            if (caseRepository.count() > 0) {
                logger.info("Cases database already contains data. Skipping seeding.");
                return;
            }

            logger.info("Starting Cases database seeding...");

            // Wait for Profiles service to be ready
            logger.info("Waiting for Profiles service to initialize...");
            Thread.sleep(20000);

            RestTemplate restTemplate = new RestTemplate();
            
            // Get lawyers from Profiles service
            String lawyersUrl = "http://profiles-service:8082/api/v1/lawyers";
            List<Map<String, Object>> lawyers = null;
            int retries = 5;
            
            while (retries > 0 && lawyers == null) {
                try {
                    var response = restTemplate.exchange(
                            lawyersUrl,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                    );
                    lawyers = response.getBody();
                    logger.info("Successfully retrieved {} lawyers from Profiles service", lawyers.size());
                } catch (Exception e) {
                    logger.warn("Failed to connect to Profiles service, retrying... ({} attempts left)", retries);
                    Thread.sleep(5000);
                    retries--;
                }
            }

            // Get clients from Profiles service
            String clientsUrl = "http://profiles-service:8082/api/v1/clients";
            List<Map<String, Object>> clients = null;
            retries = 5;
            
            while (retries > 0 && clients == null) {
                try {
                    var response = restTemplate.exchange(
                            clientsUrl,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                    );
                    clients = response.getBody();
                    logger.info("Successfully retrieved {} clients from Profiles service", clients.size());
                } catch (Exception e) {
                    logger.warn("Failed to connect to Profiles service for clients, retrying... ({} attempts left)", retries);
                    Thread.sleep(5000);
                    retries--;
                }
            }

            if (lawyers == null || lawyers.size() < 5 || clients == null || clients.size() < 5) {
                logger.error("Not enough lawyers or clients available. Skipping cases seeding.");
                return;
            }

            // Extract IDs by matching names to ensure correct assignment
            Map<String, UUID> lawyerIdsByName = new HashMap<>();
            Map<String, UUID> clientIdsByName = new HashMap<>();
            
            for (Map<String, Object> lawyer : lawyers) {
                Map<String, Object> fullName = (Map<String, Object>) lawyer.get("fullName");
                String name = fullName.get("firstname") + " " + fullName.get("lastname");
                UUID userId = UUID.fromString((String) lawyer.get("userId"));
                lawyerIdsByName.put(name, userId);
            }
            
            for (Map<String, Object> client : clients) {
                Map<String, Object> fullName = (Map<String, Object>) client.get("fullName");
                String name = fullName.get("firstname") + " " + fullName.get("lastname");
                UUID userId = UUID.fromString((String) client.get("userId"));
                clientIdsByName.put(name, userId);
            }

            // Get specific lawyer IDs by name
            UUID mariaRodriguezId = lawyerIdsByName.get("María Rodríguez");
            UUID carlosMendezId = lawyerIdsByName.get("Carlos Méndez");
            UUID anaTorresId = lawyerIdsByName.get("Ana Torres");
            UUID pedroSanchezId = lawyerIdsByName.get("Pedro Sánchez");
            UUID luciaGarciaId = lawyerIdsByName.get("Lucía García");

            // Get specific client IDs by name
            UUID juanPerezId = clientIdsByName.get("Juan Pérez");
            UUID sofiaMartinezId = clientIdsByName.get("Sofía Martínez");
            UUID robertoLopezId = clientIdsByName.get("Roberto López");
            UUID carmenDiazId = clientIdsByName.get("Carmen Díaz");
            UUID miguelFernandezId = clientIdsByName.get("Miguel Fernández");

            logger.info("Using real user IDs matched by profile names");

            // ============ JUAN PÉREZ (CLIENT 1) - 3 casos únicos ============
            
            CaseAggregate case11 = CaseAggregate.create(
                    juanPerezId,
                    new CaseTitle("Divorcio Contencioso"),
                    new Description("Necesito asesoría legal para divorcio contencioso con bienes compartidos.")
            );
            case11.evaluation();
            case11.accept(anaTorresId); // Ana Torres - Family Law
            Comment c11 = Comment.create(new CommentText("Podemos iniciar el proceso de mediación familiar."), case11, anaTorresId, CommentType.GENERAL);
            case11.getComments().add(c11);
            caseRepository.save(case11);

            CaseAggregate case12 = CaseAggregate.create(
                    juanPerezId,
                    new CaseTitle("Despido Laboral Injustificado"),
                    new Description("Fui despedido sin causa después de 5 años. Necesito demandar.")
            );
            Application app12 = Application.create(case12, luciaGarciaId, ApplicationStatus.SUBMITTED, "Estimado cliente, cuento con 10 años de experiencia en derecho laboral y me gustaría representarlo en este caso.");
            case12.getApplications().add(app12);
            caseRepository.save(case12);

            CaseAggregate case13 = CaseAggregate.create(
                    juanPerezId,
                    new CaseTitle("Accidente de Tránsito con Lesiones"),
                    new Description("Accidente vehicular grave por negligencia del otro conductor.")
            );
            case13.evaluation();
            case13.accept(mariaRodriguezId); // María - Criminal/Personal Injury
            caseRepository.save(case13);

            // ============ CLIENT 2 - 3 casos únicos ============

            CaseAggregate case21 = CaseAggregate.create(
                    sofiaMartinezId,
                    new CaseTitle("Constitución de Empresa SRL"),
                    new Description("3 socios queremos constituir una SRL para nuestro negocio tecnológico.")
            );
            case21.evaluation();
            case21.accept(carlosMendezId); // Carlos - Corporate
            Comment c21 = Comment.create(new CommentText("Trámites iniciados en Registros Públicos."), case21, carlosMendezId, CommentType.GENERAL);
            case21.getComments().add(c21);
            caseRepository.save(case21);

            CaseAggregate case22 = CaseAggregate.create(
                    sofiaMartinezId,
                    new CaseTitle("Compra de Departamento"),
                    new Description("Necesito revisión de contrato de compra-venta antes de firmar.")
            );
            case22.evaluation();
            case22.accept(pedroSanchezId); // Pedro - Real Estate
            caseRepository.save(case22);

            CaseAggregate case23 = CaseAggregate.create(
                    sofiaMartinezId,
                    new CaseTitle("Planificación Sucesoria"),
                    new Description("Redacción de testamento y planificación de herencia.")
            );
            case23.evaluation();
            case23.accept(carlosMendezId); // Carlos
            case23.close();
            caseRepository.save(case23);

            // ============ CLIENT 3 - 3 casos únicos ============

            CaseAggregate case31 = CaseAggregate.create(
                    robertoLopezId,
                    new CaseTitle("Pensión Alimenticia"),
                    new Description("Solicitud de pensión de alimentos para dos hijos menores.")
            );
            Invitation inv31 = Invitation.create(case31, anaTorresId, InvitationStatus.PENDING);
            case31.getInvitations().add(inv31);
            caseRepository.save(case31);

            CaseAggregate case32 = CaseAggregate.create(
                    robertoLopezId,
                    new CaseTitle("Defensa Penal - Difamación"),
                    new Description("Acusación injusta de difamación en redes sociales.")
            );
            case32.evaluation();
            case32.accept(mariaRodriguezId); // María - Criminal
            caseRepository.save(case32);

            CaseAggregate case33 = CaseAggregate.create(
                    robertoLopezId,
                    new CaseTitle("Reclamo a Aseguradora"),
                    new Description("Rechazo injustificado de reclamo por robo de vehículo.")
            );
            case33.cancel();
            caseRepository.save(case33);

            // ============ CLIENT 4 - 3 casos únicos ============

            CaseAggregate case41 = CaseAggregate.create(
                    carmenDiazId,
                    new CaseTitle("Revisión Contrato Laboral"),
                    new Description("Nuevo empleo con cláusulas de confidencialidad complejas.")
            );
            case41.evaluation();
            case41.accept(luciaGarciaId); // Lucía - Employment
            caseRepository.save(case41);

            CaseAggregate case42 = CaseAggregate.create(
                    carmenDiazId,
                    new CaseTitle("Conflicto de Herencia"),
                    new Description("Desacuerdos entre hermanos sobre distribución de herencia paterna.")
            );
            Application app42 = Application.create(case42, anaTorresId, ApplicationStatus.SUBMITTED, "Buenos días, soy especialista en derecho familiar y herencias. Me gustaría ayudarle con su caso.");
            case42.getApplications().add(app42);
            caseRepository.save(case42);

            CaseAggregate case43 = CaseAggregate.create(
                    carmenDiazId,
                    new CaseTitle("Daños por Construcción Vecina"),
                    new Description("Construcción vecina dañó estructura de mi vivienda.")
            );
            case43.evaluation();
            case43.accept(pedroSanchezId); // Pedro - Real Estate
            caseRepository.save(case43);

            // ============ CLIENT 5 - 3 casos únicos ============

            CaseAggregate case51 = CaseAggregate.create(
                    miguelFernandezId,
                    new CaseTitle("Asesoría Fiscal Empresarial"),
                    new Description("Declaración de impuestos para negocio independiente.")
            );
            case51.evaluation();
            case51.accept(carlosMendezId); // Carlos - Tax
            case51.close();
            caseRepository.save(case51);

            CaseAggregate case52 = CaseAggregate.create(
                    miguelFernandezId,
                    new CaseTitle("Retención Indebida de Garantía"),
                    new Description("Arrendador retiene garantía sin justificación.")
            );
            case52.evaluation();
            case52.accept(mariaRodriguezId); // María
            caseRepository.save(case52);

            CaseAggregate case53 = CaseAggregate.create(
                    miguelFernandezId,
                    new CaseTitle("Negociación Salarial"),
                    new Description("Ascenso con bajo incremento salarial. Necesito asesoría.")
            );
            Invitation inv53 = Invitation.create(case53, luciaGarciaId, InvitationStatus.ACCEPTED);
            case53.getInvitations().add(inv53);
            caseRepository.save(case53);

            long totalCases = caseRepository.count();
            logger.info("✓ Created {} cases", totalCases);

            logger.info("========================================");
            logger.info("Cases Database seeding completed!");
            logger.info("Total cases: {}", totalCases);
            logger.info("Distribution: 3 cases per client x 5 clients");
            logger.info("Each case has unique title and description");
            logger.info("Lawyers assigned based on their specialty");
            logger.info("========================================");
        };
    }
}
