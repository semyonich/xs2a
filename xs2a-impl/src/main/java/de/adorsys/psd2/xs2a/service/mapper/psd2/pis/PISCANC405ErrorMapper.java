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

package de.adorsys.psd2.xs2a.service.mapper.psd2.pis;

import de.adorsys.psd2.model.Error405NGPISCANC;
import de.adorsys.psd2.model.TppMessage405PISCANC;
import de.adorsys.psd2.model.TppMessageCategory;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.Psd2ErrorMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PISCANC405ErrorMapper extends Psd2ErrorMapper<MessageError, Error405NGPISCANC> {

    @Override
    public Function<MessageError, Error405NGPISCANC> getMapper() {
        return this::mapToPsd2Error;
    }

    @Override
    public HttpStatus getErrorStatus() {
        return HttpStatus.METHOD_NOT_ALLOWED;
    }

    private Error405NGPISCANC mapToPsd2Error(MessageError messageError) {
        return new Error405NGPISCANC().tppMessages(mapToTppMessage405PISCANC(messageError.getTppMessages()));
    }

    private List<TppMessage405PISCANC> mapToTppMessage405PISCANC(Set<TppMessageInformation> tppMessages) {
        return tppMessages.stream()
                   .map(m -> new TppMessage405PISCANC()
                                 .category(TppMessageCategory.fromValue(m.getCategory().name()))
                                 .code(m.getMessageErrorCode().getName())
                                 .path(m.getPath())
                                 .text(getErrorText(m))
                   ).collect(Collectors.toList());
    }
}
