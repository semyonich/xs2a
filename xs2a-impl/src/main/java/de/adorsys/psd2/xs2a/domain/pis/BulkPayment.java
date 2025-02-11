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

package de.adorsys.psd2.xs2a.domain.pis;

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.AccountReferenceCollector;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class BulkPayment extends CommonPayment implements AccountReferenceCollector {

    @NotNull
    private AccountReference debtorAccount;
    private String debtorName;
    private LocalDate requestedExecutionDate;
    private OffsetDateTime requestedExecutionTime;
    private List<SinglePayment> payments;
    private Boolean batchBookingPreferred;

    @Override
    public Set<AccountReference> getAccountReferences() {
        Set<AccountReference> accountReferences = payments.stream()
                                                      .map(SinglePayment::getAccountReferences)
                                                      .flatMap(Set::stream)
                                                      .collect(Collectors.toSet());
        accountReferences.add(debtorAccount);

        return accountReferences;
    }

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.BULK;
    }
}
