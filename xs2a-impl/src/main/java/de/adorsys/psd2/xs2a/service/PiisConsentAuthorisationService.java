/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.PiisAuthorisationConfirmationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.PiisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.PiisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.PiisAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.event.EventAuthorisationType;
import de.adorsys.psd2.xs2a.service.event.EventTypeService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.validator.ConsentEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.piis.dto.CreatePiisConsentAuthorisationObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.*;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PiisConsentAuthorisationService {
    private static final MessageError PIIS_CONSENT_NOT_FOUND_MESSAGE_ERROR = new MessageError(PIIS_403, of(CONSENT_UNKNOWN_403));

    private final Xs2aEventService xs2aEventService;
    private final Xs2aPiisConsentService xs2aPiisConsentService;
    private final PiisScaAuthorisationServiceResolver piisScaAuthorisationServiceResolver;
    private final ConfirmationOfFundsConsentValidationService confirmationOfFundsConsentValidationService;
    private final Xs2aAuthorisationService xs2aAuthorisationService;
    private final ConsentEndpointAccessCheckerService endpointAccessCheckerService;
    private final PiisAuthorisationConfirmationService piisAuthorisationConfirmationService;
    private final AuthorisationChainResponsibilityService authorisationChainResponsibilityService;
    private final LoggingContextService loggingContextService;
    private final PsuIdDataAuthorisationService psuIdDataAuthorisationService;
    private final Xs2aAuthorisationService authorisationService;
    private final EventTypeService eventTypeService;

    public ResponseObject<UpdateConsentPsuDataResponse> updateConsentPsuData(UpdateConsentPsuDataReq updatePsuData) {
        xs2aEventService.recordConsentTppRequest(updatePsuData.getConsentId(), eventTypeService.getEventType(updatePsuData, EventAuthorisationType.PIIS), updatePsuData);

        String consentId = updatePsuData.getConsentId();

        Optional<PiisConsent> piisConsentOptional = xs2aPiisConsentService.getPiisConsentById(consentId);

        if (piisConsentOptional.isEmpty()) {
            log.info("Consent-ID: [{}]. Update consent PSU data failed: consent not found by id", consentId);
            return ResponseObject.<UpdateConsentPsuDataResponse>builder()
                       .fail(PIIS_403, of(CONSENT_UNKNOWN_403)).build();
        }

        String authorisationId = updatePsuData.getAuthorizationId();
        boolean confirmationCodeReceived = StringUtils.isNotBlank(updatePsuData.getConfirmationCode());

        if (!endpointAccessCheckerService.isEndpointAccessible(authorisationId, confirmationCodeReceived)) {
            log.info("Consent-ID: [{}], Authorisation-ID [{}]. Update consent PSU data failed: update endpoint is blocked for current authorisation",
                     consentId, authorisationId);
            return ResponseObject.<UpdateConsentPsuDataResponse>builder()
                       .fail(PIIS_403, of(SERVICE_BLOCKED))
                       .build();
        }

        PiisConsent piisConsent = piisConsentOptional.get();

        loggingContextService.storeConsentStatus(piisConsent.getConsentStatus());
        ValidationResult validationResult = confirmationOfFundsConsentValidationService.validateConsentPsuDataOnUpdate(piisConsent, updatePsuData);

        if (validationResult.isNotValid()) {
            MessageErrorCode messageErrorCode = validationResult.getMessageError().getTppMessage().getMessageErrorCode();

            if (EnumSet.of(PSU_CREDENTIALS_INVALID, FORMAT_ERROR_NO_PSU).contains(messageErrorCode)) {
                xs2aAuthorisationService.updateAuthorisationStatus(authorisationId, ScaStatus.FAILED);
            }

            log.info("Consent-ID: [{}], Authorisation-ID [{}]. Update consent PSU data - validation failed: {}",
                     consentId, authorisationId, validationResult.getMessageError());
            return ResponseObject.<UpdateConsentPsuDataResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        if (piisConsent.isExpired()) {
            log.info("Consent-ID: [{}]. Update consent PSU data failed: consent expired", consentId);
            return ResponseObject.<UpdateConsentPsuDataResponse>builder()
                       .fail(PIIS_401, of(CONSENT_EXPIRED))
                       .build();
        }

        return getUpdateConsentPsuDataResponse(updatePsuData);
    }

    private ResponseObject<UpdateConsentPsuDataResponse> getUpdateConsentPsuDataResponse(UpdateConsentPsuDataReq updatePsuData) {
        PiisAuthorizationService service = piisScaAuthorisationServiceResolver.getService(updatePsuData.getAuthorizationId());

        Optional<Authorisation> authorizationOptional = service.getConsentAuthorizationById(updatePsuData.getAuthorizationId());

        if (authorizationOptional.isEmpty()) {
            log.info("Authorisation-ID: [{}]. Update consent PSU data failed: authorisation not found by id",
                     updatePsuData.getAuthorizationId());
            return ResponseObject.<UpdateConsentPsuDataResponse>builder()
                       .fail(PIIS_403, of(CONSENT_UNKNOWN_403)).build();
        }

        Authorisation authorisation = authorizationOptional.get();

        if (authorisation.getChosenScaApproach() == ScaApproach.REDIRECT) {
            return piisAuthorisationConfirmationService.processAuthorisationConfirmation(updatePsuData);
        }

        UpdateConsentPsuDataResponse response = (UpdateConsentPsuDataResponse) authorisationChainResponsibilityService.apply(
            new PiisAuthorisationProcessorRequest(authorisation.getChosenScaApproach(),
                                                  authorisation.getScaStatus(),
                                                  updatePsuData,
                                                  authorisation));

        loggingContextService.storeScaStatus(response.getScaStatus());
        return Optional.ofNullable(response)
                   .map(s -> Optional.ofNullable(s.getErrorHolder())
                                 .map(e -> ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                               .fail(e)
                                               .build())
                                 .orElseGet(ResponseObject.<UpdateConsentPsuDataResponse>builder().body(response)::build))
                   .orElseGet(ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                  .fail(PIIS_400, of(FORMAT_ERROR))
                                  ::build);
    }

    public ResponseObject<AuthorisationResponse> createPiisAuthorisation(PsuIdData psuData, String consentId, String password) {
        ResponseObject<CreateConsentAuthorizationResponse> createPiisAuthorizationResponse = createConsentAuthorizationWithResponse(psuData, consentId);

        if (createPiisAuthorizationResponse.hasError()) {
            return ResponseObject.<AuthorisationResponse>builder()
                       .fail(createPiisAuthorizationResponse.getError())
                       .build();
        }

        PsuIdData psuIdDataFromResponse = createPiisAuthorizationResponse.getBody().getPsuIdData();
        if (psuIdDataFromResponse == null || psuIdDataFromResponse.isEmpty() || StringUtils.isBlank(password)) {
            loggingContextService.storeScaStatus(createPiisAuthorizationResponse.getBody().getScaStatus());
            return ResponseObject.<AuthorisationResponse>builder()
                       .body(createPiisAuthorizationResponse.getBody())
                       .build();
        }

        String authorisationId = createPiisAuthorizationResponse.getBody().getAuthorisationId();

        UpdateConsentPsuDataReq updatePsuData = new UpdateConsentPsuDataReq();
        updatePsuData.setPsuData(psuData);
        updatePsuData.setConsentId(consentId);
        updatePsuData.setAuthorizationId(authorisationId);
        updatePsuData.setPassword(password);

        ResponseObject<UpdateConsentPsuDataResponse> updatePsuDataResponse = updateConsentPsuData(updatePsuData);
        if (updatePsuDataResponse.hasError()) {
            return ResponseObject.<AuthorisationResponse>builder()
                       .fail(updatePsuDataResponse.getError())
                       .build();
        }

        return ResponseObject.<AuthorisationResponse>builder()
                   .body(updatePsuDataResponse.getBody())
                   .build();
    }

    private ResponseObject<CreateConsentAuthorizationResponse> createConsentAuthorizationWithResponse(PsuIdData psuDataFromRequest, String consentId) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.START_PIIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED);

        Optional<PiisConsent> piisConsentOptional = xs2aPiisConsentService.getPiisConsentById(consentId);

        if (piisConsentOptional.isEmpty()) {
            log.info("Consent-ID: [{}]. Create consent authorisation with response failed: consent not found by id",
                     consentId);
            return ResponseObject.<CreateConsentAuthorizationResponse>builder()
                       .fail(PIIS_CONSENT_NOT_FOUND_MESSAGE_ERROR)
                       .build();
        }
        PiisConsent piisConsent = piisConsentOptional.get();

        ValidationResult validationResult = confirmationOfFundsConsentValidationService.validateConsentAuthorisationOnCreate(new CreatePiisConsentAuthorisationObject(piisConsent, psuDataFromRequest));

        if (validationResult.isNotValid()) {
            log.info("Consent-ID: [{}]. Create consent authorisation with response - validation failed: {}",
                     consentId, validationResult.getMessageError());
            return ResponseObject.<CreateConsentAuthorizationResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        PiisAuthorizationService service = piisScaAuthorisationServiceResolver.getService();
        PsuIdData psuIdData = getActualPsuData(psuDataFromRequest, piisConsent);

        return service.createConsentAuthorization(psuIdData, consentId)
                   .map(resp -> ResponseObject.<CreateConsentAuthorizationResponse>builder().body(resp).build())
                   .orElseGet(ResponseObject.<CreateConsentAuthorizationResponse>builder()
                                  .fail(PIIS_CONSENT_NOT_FOUND_MESSAGE_ERROR)
                                  ::build);
    }

    public ResponseObject<Xs2aAuthorisationSubResources> getConsentInitiationAuthorisations(String consentId) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.GET_PIIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED);

        Optional<PiisConsent> piisConsentOptional = xs2aPiisConsentService.getPiisConsentById(consentId);
        if (piisConsentOptional.isEmpty()) {
            log.info("Consent-ID: [{}]. Get consent initiation authorisations failed: consent not found by id",
                     consentId);
            return ResponseObject.<Xs2aAuthorisationSubResources>builder()
                       .fail(PIIS_CONSENT_NOT_FOUND_MESSAGE_ERROR)
                       .build();
        }
        PiisConsent piisConsent = piisConsentOptional.get();

        ValidationResult validationResult = confirmationOfFundsConsentValidationService.validateConsentAuthorisationOnGettingById(piisConsent);
        if (validationResult.isNotValid()) {
            log.info("Consent-ID: [{}]. Get consent authorisations - validation failed: {}",
                     consentId, validationResult.getMessageError());
            return ResponseObject.<Xs2aAuthorisationSubResources>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        return getAuthorisationSubResources(consentId)
                   .map(resp -> ResponseObject.<Xs2aAuthorisationSubResources>builder().body(resp).build())
                   .orElseGet(() -> {
                       log.info("Consent-ID: [{}]. Get consent initiation authorisations failed: authorisation not found at CMS by consent id",
                                consentId);
                       return ResponseObject.<Xs2aAuthorisationSubResources>builder()
                                  .fail(PIIS_404, of(RESOURCE_UNKNOWN_404))
                                  .build();
                   });
    }

    public ResponseObject<ConfirmationOfFundsConsentScaStatus> getConsentAuthorisationScaStatus(String consentId, String authorisationId) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.GET_PIIS_CONSENT_SCA_STATUS_REQUEST_RECEIVED);

        Optional<PiisConsent> piisConsentOptional = xs2aPiisConsentService.getPiisConsentById(consentId);
        if (piisConsentOptional.isEmpty()) {
            log.info("Consent-ID: [{}]. Get consent authorisation SCA status failed: consent not found by id", consentId);
            return ResponseObject.<ConfirmationOfFundsConsentScaStatus>builder()
                       .fail(PIIS_CONSENT_NOT_FOUND_MESSAGE_ERROR).build();
        }
        PiisConsent piisConsent = piisConsentOptional.get();

        ValidationResult validationResult = confirmationOfFundsConsentValidationService.validateConsentAuthorisationScaStatus(piisConsent, authorisationId);
        if (validationResult.isNotValid()) {
            log.info("Consent-ID: [{}], Authorisation-ID [{}]. Get consent authorisation SCA status - validation failed: {}",
                     consentId, authorisationId, validationResult.getMessageError());
            return ResponseObject.<ConfirmationOfFundsConsentScaStatus>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        PiisAuthorizationService authorizationService = piisScaAuthorisationServiceResolver.getService(authorisationId);
        Optional<ScaStatus> scaStatusOptional = authorizationService
                                                    .getAuthorisationScaStatus(consentId, authorisationId);

        if (scaStatusOptional.isEmpty()) {
            log.info("Consent-ID: [{}]. Get consent authorisation SCA status failed: consent not found at CMS by id",
                     consentId);
            return ResponseObject.<ConfirmationOfFundsConsentScaStatus>builder()
                       .fail(PIIS_403, of(RESOURCE_UNKNOWN_403))
                       .build();
        }

        ScaStatus scaStatus = scaStatusOptional.get();

        PsuIdData psuIdData = psuIdDataAuthorisationService.getPsuIdData(authorisationId, piisConsent.getPsuIdDataList());

        ConfirmationOfFundsConsentScaStatus consentScaStatus = new ConfirmationOfFundsConsentScaStatus(psuIdData, piisConsent, scaStatus);

        return ResponseObject.<ConfirmationOfFundsConsentScaStatus>builder()
                   .body(consentScaStatus)
                   .build();
    }

    private Optional<Xs2aAuthorisationSubResources> getAuthorisationSubResources(String consentId) {
        return authorisationService.getAuthorisationSubResources(consentId, AuthorisationType.CONSENT)
                   .map(Xs2aAuthorisationSubResources::new);
    }

    private PsuIdData getActualPsuData(PsuIdData psuDataFromRequest, PiisConsent piisConsent) {
        boolean isMultilevel = piisConsent.isMultilevelScaRequired();

        if (psuDataFromRequest.isNotEmpty() || isMultilevel) {
            return psuDataFromRequest;
        }

        return piisConsent.getPsuIdDataList().stream()
                   .findFirst()
                   .orElse(psuDataFromRequest);
    }
}
