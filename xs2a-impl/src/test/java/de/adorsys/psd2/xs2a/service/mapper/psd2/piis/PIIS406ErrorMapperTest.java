/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper.psd2.piis;

import de.adorsys.psd2.model.Error406NGPIIS;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.function.Function;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONTENT_TYPE_NOT_SUPPORTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PIIS406ErrorMapperTest {
    private static final String ERROR_JSON_PATH = "json/service/mapper/psd2/piis/Error406NGPIIS.json";
    private static final String ERROR_CUSTOM_TEXT_JSON_PATH = "json/service/mapper/psd2/piis/Error406NGPIIS-custom-text.json";
    private static final String CUSTOM_ERROR_TEXT = "Custom text";
    private static final MessageError MESSAGE_ERROR = new MessageError(ErrorType.AIS_406,
                                                                       TppMessageInformation.of(CONTENT_TYPE_NOT_SUPPORTED, "text"));
    private static final MessageError MESSAGE_ERROR_WITHOUT_TEXT = new MessageError(ErrorType.AIS_406,
                                                                                    TppMessageInformation.of(CONTENT_TYPE_NOT_SUPPORTED));

    private JsonReader jsonReader = new JsonReader();
    @Mock
    private MessageService messageService;
    @InjectMocks
    private PIIS406ErrorMapper piis406ErrorMapper;

    @Test
    void getErrorStatus_shouldReturn406() {
        // When
        HttpStatus errorStatus = piis406ErrorMapper.getErrorStatus();

        // Then
        assertEquals(HttpStatus.NOT_ACCEPTABLE, errorStatus);
    }

    @Test
    void getMapper_shouldReturnCorrectErrorMapper() {
        when(messageService.getMessage(CONTENT_TYPE_NOT_SUPPORTED.name()))
            .thenReturn("Some %s");

        // Given
        Error406NGPIIS expectedError = jsonReader.getObjectFromFile(ERROR_JSON_PATH, Error406NGPIIS.class);

        // When
        Function<MessageError, Error406NGPIIS> mapper = piis406ErrorMapper.getMapper();
        Error406NGPIIS actualError = mapper.apply(MESSAGE_ERROR);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void getMapper_withNoTextInTppMessage_shouldGetTextFromMessageService() {
        when(messageService.getMessage(CONTENT_TYPE_NOT_SUPPORTED.name()))
            .thenReturn(CUSTOM_ERROR_TEXT);

        // Given
        Error406NGPIIS expectedError = jsonReader.getObjectFromFile(ERROR_CUSTOM_TEXT_JSON_PATH, Error406NGPIIS.class);

        // When
        Function<MessageError, Error406NGPIIS> mapper = piis406ErrorMapper.getMapper();
        Error406NGPIIS actualError = mapper.apply(MESSAGE_ERROR_WITHOUT_TEXT);

        // Then
        assertEquals(expectedError, actualError);
        verify(messageService).getMessage(CONTENT_TYPE_NOT_SUPPORTED.name());
    }
}
