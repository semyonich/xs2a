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

package de.adorsys.psd2.xs2a.service.authorization.piis;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.service.authorization.ConsentAuthorisationConfirmationService;
import de.adorsys.psd2.xs2a.service.authorization.ConsentAuthorizationMappersHolder;
import de.adorsys.psd2.xs2a.service.authorization.ConsentAuthorizationServicesHolder;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCheckConfirmationCodeRequest;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PiisConsentSpi;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PiisAuthorisationConfirmationService extends ConsentAuthorisationConfirmationService<PiisConsent> {
    private final PiisConsentSpi piisConsentSpi;
    private final ConsentAuthorizationMappersHolder consentAuthorizationMappersHolder;
    private final ConsentAuthorizationServicesHolder consentAuthorizationServicesHolder;

    public PiisAuthorisationConfirmationService(AspspProfileServiceWrapper aspspProfileServiceWrapper,
                                                SpiContextDataProvider spiContextDataProvider,
                                                SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                                PiisConsentSpi piisConsentSpi,
                                                ConsentAuthorizationMappersHolder consentAuthorizationMappersHolder,
                                                ConsentAuthorizationServicesHolder consentAuthorizationServicesHolder) {
        super(aspspProfileServiceWrapper, spiContextDataProvider, aspspConsentDataProviderFactory, consentAuthorizationMappersHolder, consentAuthorizationServicesHolder);
        this.piisConsentSpi = piisConsentSpi;
        this.consentAuthorizationServicesHolder = consentAuthorizationServicesHolder;
        this.consentAuthorizationMappersHolder = consentAuthorizationMappersHolder;
    }

    @Override
    protected ErrorType getErrorType403() {
        return ErrorType.PIIS_403;
    }

    @Override
    protected void updateConsentStatus(String consentId, ConsentStatus consentStatus) {
        consentAuthorizationServicesHolder.updateConsentStatus(consentId, consentStatus);
    }

    @Override
    protected void findAndTerminateOldConsentsByNewConsentId(String consentId) {
        // this method is empty because one tpp could have more then one valid piis consent
    }

    @Override
    protected SpiResponse<SpiConsentConfirmationCodeValidationResponse> notifyConfirmationCodeValidation(SpiContextData spiContextData, boolean isCodeCorrect, PiisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return piisConsentSpi.notifyConfirmationCodeValidation(spiContextData, isCodeCorrect, consentAuthorizationMappersHolder.mapToSpiPiisConsent(consent), spiAspspConsentDataProvider);
    }

    @Override
    protected Optional<PiisConsent> getConsentById(String consentId) {
        return consentAuthorizationServicesHolder.getPiisConsentById(consentId);
    }

    @Override
    protected SpiResponse<SpiConsentConfirmationCodeValidationResponse> checkConfirmationCode(SpiContextData spiContextData, SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return piisConsentSpi.checkConfirmationCode(spiContextData, spiCheckConfirmationCodeRequest, spiAspspConsentDataProvider);
    }

    @Override
    protected boolean checkConfirmationCodeInternally(String authorisationId, String confirmationCode, String scaAuthenticationData, SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return piisConsentSpi.checkConfirmationCodeInternally(authorisationId, confirmationCode, scaAuthenticationData, aspspConsentDataProvider);
    }

    @Override
    protected ErrorType getErrorType400() {
        return ErrorType.PIIS_400;
    }

    @Override
    protected ServiceType getServiceType() {
        return ServiceType.PIIS;
    }
}
