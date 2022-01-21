/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.controller;

import de.adorsys.psd2.api.FundsConfirmationApi;
import de.adorsys.psd2.model.ConfirmationOfFunds;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.psd2.xs2a.service.FundsConfirmationService;
import de.adorsys.psd2.xs2a.service.mapper.FundsConfirmationModelMapper;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@SuppressWarnings("unchecked") // This class implements autogenerated interface without proper return values generated
@Slf4j
@RestController
@AllArgsConstructor
public class FundsConfirmationController implements FundsConfirmationApi {
    private final ResponseMapper responseMapper;
    private final ResponseErrorMapper responseErrorMapper;
    private final FundsConfirmationService fundsConfirmationService;
    private final FundsConfirmationModelMapper fundsConfirmationModelMapper;

    @Override
    public ResponseEntity checkAvailabilityOfFunds(ConfirmationOfFunds body, UUID xRequestID, String consentID, String authorization, String digest, String signature, byte[] tpPSignatureCertificate) {
        ResponseObject<FundsConfirmationResponse> responseObject = fundsConfirmationService.fundsConfirmation(fundsConfirmationModelMapper.mapToFundsConfirmationRequest(body, consentID));

        return responseObject.hasError()
                   ? responseErrorMapper.generateErrorResponse(responseObject.getError())
                   : responseMapper.ok(responseObject, fundsConfirmationModelMapper::mapToInlineResponse2003);
    }
}
