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

package de.adorsys.psd2.xs2a.service.event;

import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EventTypeService {
    public EventType getEventType(UpdateAuthorisationRequest updateAuthorisationRequest, EventAuthorisationType eventAuthorisationType) {
        return getUpdateEventType(updateAuthorisationRequest).getEventType(eventAuthorisationType);
    }

    private UpdateEventType getUpdateEventType(UpdateAuthorisationRequest updateAuthorisationRequest) {
        if (updateAuthorisationRequest.getConfirmationCode() != null) {
            return UpdateEventType.CONFIRMATION_CODE;
        }

        if (updateAuthorisationRequest.getScaAuthenticationData() != null) {
            return UpdateEventType.TAN;
        }

        if (updateAuthorisationRequest.getAuthenticationMethodId() != null) {
            return UpdateEventType.SELECT_AUTHENTICATION_METHOD;
        }

        if (updateAuthorisationRequest.getPassword() != null) {
            return UpdateEventType.AUTHENTICATION;
        }

        return UpdateEventType.IDENTIFICATION;
    }

    private enum UpdateEventType {
        IDENTIFICATION(
            Map.of(
                EventAuthorisationType.AIS, EventType.UPDATE_AIS_CONSENT_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED,
                EventAuthorisationType.SB, EventType.UPDATE_SB_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED,
                EventAuthorisationType.PIIS, EventType.UPDATE_PIIS_CONSENT_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED,
                EventAuthorisationType.PIS, EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED,
                EventAuthorisationType.PIS_CANCELLATION, EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED
            )
        ), AUTHENTICATION(
            Map.of(
                EventAuthorisationType.AIS, EventType.UPDATE_AIS_CONSENT_PSU_DATA_AUTHENTICATION_REQUEST_RECEIVED,
                EventAuthorisationType.SB, EventType.UPDATE_SB_PSU_DATA_AUTHENTICATION_REQUEST_RECEIVED,
                EventAuthorisationType.PIIS, EventType.UPDATE_PIIS_CONSENT_PSU_DATA_AUTHENTICATION_REQUEST_RECEIVED,
                EventAuthorisationType.PIS, EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_AUTHENTICATION_REQUEST_RECEIVED,
                EventAuthorisationType.PIS_CANCELLATION, EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_AUTHENTICATION_REQUEST_RECEIVED
            )
        ), SELECT_AUTHENTICATION_METHOD(
            Map.of(
                EventAuthorisationType.AIS, EventType.UPDATE_AIS_CONSENT_PSU_DATA_SELECT_AUTHENTICATION_METHOD_REQUEST_RECEIVED,
                EventAuthorisationType.SB, EventType.UPDATE_SB_PSU_DATA_SELECT_AUTHENTICATION_METHOD_REQUEST_RECEIVED,
                EventAuthorisationType.PIIS, EventType.UPDATE_PIIS_CONSENT_PSU_DATA_SELECT_AUTHENTICATION_METHOD_REQUEST_RECEIVED,
                EventAuthorisationType.PIS, EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_SELECT_AUTHENTICATION_METHOD_REQUEST_RECEIVED,
                EventAuthorisationType.PIS_CANCELLATION, EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_SELECT_AUTHENTICATION_METHOD_REQUEST_RECEIVED
            )
        ), TAN(
            Map.of(
                EventAuthorisationType.AIS, EventType.UPDATE_AIS_CONSENT_PSU_DATA_TAN_REQUEST_RECEIVED,
                EventAuthorisationType.SB, EventType.UPDATE_SB_PSU_DATA_TAN_REQUEST_RECEIVED,
                EventAuthorisationType.PIIS, EventType.UPDATE_PIIS_CONSENT_PSU_DATA_TAN_REQUEST_RECEIVED,
                EventAuthorisationType.PIS, EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_TAN_REQUEST_RECEIVED,
                EventAuthorisationType.PIS_CANCELLATION, EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_TAN_REQUEST_RECEIVED
            )
        ), CONFIRMATION_CODE(
            Map.of(
                EventAuthorisationType.AIS, EventType.UPDATE_AIS_CONSENT_PSU_DATA_CONFIRMATION_CODE_REQUEST_RECEIVED,
                EventAuthorisationType.SB, EventType.UPDATE_SB_PSU_DATA_CONFIRMATION_CODE_REQUEST_RECEIVED,
                EventAuthorisationType.PIIS, EventType.UPDATE_PIIS_CONSENT_PSU_DATA_CONFIRMATION_CODE_REQUEST_RECEIVED,
                EventAuthorisationType.PIS, EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_CONFIRMATION_CODE_REQUEST_RECEIVED,
                EventAuthorisationType.PIS_CANCELLATION, EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_CONFIRMATION_CODE_REQUEST_RECEIVED
            )
        );

        private Map<EventAuthorisationType, EventType> eventServiceTypes;

        UpdateEventType(Map<EventAuthorisationType, EventType> eventServiceTypes) {
            this.eventServiceTypes = eventServiceTypes;
        }

        public EventType getEventType(EventAuthorisationType eventAuthorisationType) {
            return eventServiceTypes.get(eventAuthorisationType);
        }
    }
}
