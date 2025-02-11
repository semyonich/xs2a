/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.spi.service;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;

import java.time.OffsetDateTime;
import java.util.List;

public interface SpiPayment {
    String getPaymentId();

    PaymentType getPaymentType();

    String getPaymentProduct();

    TransactionStatus getPaymentStatus();

    void setPaymentStatus(TransactionStatus paymentStatus);

    OffsetDateTime getStatusChangeTimestamp();

    List<SpiPsuData> getPsuDataList();

    String getContentType();

    String getInstanceId();

    String getDebtorName();

    void setDebtorName(String debtorName);
}
