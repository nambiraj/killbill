/*
 * Copyright 2010-2013 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.billing.jaxrs.json;

import java.util.UUID;

import org.joda.time.DateTimeZone;

import com.ning.billing.account.api.Account;
import com.ning.billing.account.api.AccountData;
import com.ning.billing.catalog.api.Currency;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class AccountJson extends AccountJsonSimple {

    protected final String name;
    protected final Integer firstNameLength;
    protected final String email;
    protected final Integer billCycleDayLocal;
    protected final String currency;
    protected final String paymentMethodId;
    protected final String timeZone;
    protected final String address1;
    protected final String address2;
    protected final String postalCode;
    protected final String company;
    protected final String city;
    protected final String state;
    protected final String country;
    protected final String locale;
    protected final String phone;
    protected final Boolean isMigrated;
    protected final Boolean isNotifiedForInvoices;

    public AccountJson(final Account account) {
        super(account.getId().toString(), account.getExternalKey());
        this.name = account.getName();
        this.firstNameLength = account.getFirstNameLength();
        this.email = account.getEmail();
        this.billCycleDayLocal = account.getBillCycleDayLocal();
        this.currency = account.getCurrency() != null ? account.getCurrency().toString() : null;
        this.paymentMethodId = account.getPaymentMethodId() != null ? account.getPaymentMethodId().toString() : null;
        this.timeZone = account.getTimeZone().toString();
        this.address1 = account.getAddress1();
        this.address2 = account.getAddress2();
        this.postalCode = account.getPostalCode();
        this.company = account.getCompanyName();
        this.city = account.getCity();
        this.state = account.getStateOrProvince();
        this.country = account.getCountry();
        this.locale = account.getLocale();
        this.phone = account.getPhone();
        this.isMigrated = account.isMigrated();
        this.isNotifiedForInvoices = account.isNotifiedForInvoices();
    }

    public AccountData toAccountData() {
        return new AccountData() {
            @Override
            public DateTimeZone getTimeZone() {
                return (timeZone != null) ? DateTimeZone.forID(timeZone) : null;
            }

            @Override
            public String getStateOrProvince() {
                return state;
            }

            @Override
            public String getPostalCode() {
                return postalCode;
            }

            @Override
            public String getPhone() {
                return phone;
            }

            @Override
            public Boolean isMigrated() {
                return Objects.firstNonNull(isMigrated, false);
            }

            @Override
            public Boolean isNotifiedForInvoices() {
                return Objects.firstNonNull(isNotifiedForInvoices, false);
            }

            @Override
            public UUID getPaymentMethodId() {
                return paymentMethodId != null ? UUID.fromString(paymentMethodId) : null;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getLocale() {
                return locale;
            }

            @Override
            public Integer getFirstNameLength() {
                if (firstNameLength == null && name == null) {
                    return 0;
                } else if (firstNameLength == null) {
                    return name.length();
                } else {
                    return firstNameLength;
                }
            }

            @Override
            public String getExternalKey() {
                return externalKey;
            }

            @Override
            public String getEmail() {
                return email;
            }

            @Override
            public Currency getCurrency() {
                if (currency == null) {
                    return null;
                } else {
                    return Currency.valueOf(currency);
                }
            }

            @Override
            public String getCountry() {
                return country;
            }

            @Override
            public String getCompanyName() {
                return company;
            }

            @Override
            public String getCity() {
                return city;
            }

            @Override
            public Integer getBillCycleDayLocal() {
                return billCycleDayLocal;
            }

            @Override
            public String getAddress2() {
                return address2;
            }

            @Override
            public String getAddress1() {
                return address1;
            }
        };
    }

    @JsonCreator
    public AccountJson(@JsonProperty("accountId") final String accountId,
                       @JsonProperty("name") final String name,
                       @JsonProperty("firstNameLength") final Integer firstNameLength,
                       @JsonProperty("externalKey") final String externalKey,
                       @JsonProperty("email") final String email,
                       @JsonProperty("billCycleDayLocal") final Integer billCycleDayLocal,
                       @JsonProperty("currency") final String currency,
                       @JsonProperty("paymentMethodId") final String paymentMethodId,
                       @JsonProperty("timeZone") final String timeZone,
                       @JsonProperty("address1") final String address1,
                       @JsonProperty("address2") final String address2,
                       @JsonProperty("postalCode") final String postalCode,
                       @JsonProperty("company") final String company,
                       @JsonProperty("city") final String city,
                       @JsonProperty("state") final String state,
                       @JsonProperty("country") final String country,
                       @JsonProperty("locale") final String locale,
                       @JsonProperty("phone") final String phone,
                       @JsonProperty("isMigrated") final Boolean isMigrated,
                       @JsonProperty("isNotifiedForInvoices") final Boolean isNotifiedForInvoices) {
        super(accountId, externalKey);
        this.name = name;
        this.firstNameLength = firstNameLength;
        this.email = email;
        this.billCycleDayLocal = billCycleDayLocal;
        this.currency = currency;
        this.paymentMethodId = paymentMethodId;
        this.timeZone = timeZone;
        this.address1 = address1;
        this.address2 = address2;
        this.postalCode = postalCode;
        this.company = company;
        this.city = city;
        this.state = state;
        this.country = country;
        this.locale = locale;
        this.phone = phone;
        this.isMigrated = isMigrated;
        this.isNotifiedForInvoices = isNotifiedForInvoices;
    }

    public String getName() {
        return name;
    }

    public Integer getFirstNameLength() {
        return firstNameLength;
    }

    public String getEmail() {
        return email;
    }

    public Integer getBillCycleDayLocal() {
        return billCycleDayLocal;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCompany() {
        return company;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getLocale() {
        return locale;
    }

    public String getPhone() {
        return phone;
    }

    @JsonProperty("isMigrated")
    public Boolean isMigrated() {
        return isMigrated;
    }

    @JsonProperty("isNotifiedForInvoices")
    public Boolean isNotifiedForInvoices() {
        return isNotifiedForInvoices;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("AccountJson");
        sb.append("{name='").append(name).append('\'');
        sb.append(", firstNameLength=").append(firstNameLength);
        sb.append(", email='").append(email).append('\'');
        sb.append(", billCycleDayJson=").append(billCycleDayLocal);
        sb.append(", currency='").append(currency).append('\'');
        sb.append(", paymentMethodId='").append(paymentMethodId).append('\'');
        sb.append(", timeZone='").append(timeZone).append('\'');
        sb.append(", address1='").append(address1).append('\'');
        sb.append(", address2='").append(address2).append('\'');
        sb.append(", postalCode='").append(postalCode).append('\'');
        sb.append(", company='").append(company).append('\'');
        sb.append(", city='").append(city).append('\'');
        sb.append(", state='").append(state).append('\'');
        sb.append(", country='").append(country).append('\'');
        sb.append(", locale='").append(locale).append('\'');
        sb.append(", phone='").append(phone).append('\'');
        sb.append(", isMigrated=").append(isMigrated);
        sb.append(", isNotifiedForInvoices=").append(isNotifiedForInvoices);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AccountJson that = (AccountJson) o;

        if (accountId != null ? !accountId.equals(that.accountId) : that.accountId != null) {
            return false;
        } else {
            return equalsNoId(that);
        }
    }

    // Used to check POST versus GET
    public boolean equalsNoId(final AccountJson that) {
        if (address1 != null ? !address1.equals(that.address1) : that.address1 != null) {
            return false;
        }
        if (address2 != null ? !address2.equals(that.address2) : that.address2 != null) {
            return false;
        }
        if (billCycleDayLocal != null ? !billCycleDayLocal.equals(that.billCycleDayLocal) : that.billCycleDayLocal != null) {
            return false;
        }
        if (city != null ? !city.equals(that.city) : that.city != null) {
            return false;
        }
        if (company != null ? !company.equals(that.company) : that.company != null) {
            return false;
        }
        if (country != null ? !country.equals(that.country) : that.country != null) {
            return false;
        }
        if (currency != null ? !currency.equals(that.currency) : that.currency != null) {
            return false;
        }
        if (email != null ? !email.equals(that.email) : that.email != null) {
            return false;
        }
        if (externalKey != null ? !externalKey.equals(that.externalKey) : that.externalKey != null) {
            return false;
        }
        if (isMigrated != null ? !isMigrated.equals(that.isMigrated) : that.isMigrated != null) {
            return false;
        }
        if (isNotifiedForInvoices != null ? !isNotifiedForInvoices.equals(that.isNotifiedForInvoices) : that.isNotifiedForInvoices != null) {
            return false;
        }
        if (firstNameLength != null ? !firstNameLength.equals(that.firstNameLength) : that.firstNameLength != null) {
            return false;
        }
        if (locale != null ? !locale.equals(that.locale) : that.locale != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (paymentMethodId != null ? !paymentMethodId.equals(that.paymentMethodId) : that.paymentMethodId != null) {
            return false;
        }
        if (phone != null ? !phone.equals(that.phone) : that.phone != null) {
            return false;
        }
        if (postalCode != null ? !postalCode.equals(that.postalCode) : that.postalCode != null) {
            return false;
        }
        if (state != null ? !state.equals(that.state) : that.state != null) {
            return false;
        }
        if (timeZone != null ? !timeZone.equals(that.timeZone) : that.timeZone != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (firstNameLength != null ? firstNameLength.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (billCycleDayLocal != null ? billCycleDayLocal.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (paymentMethodId != null ? paymentMethodId.hashCode() : 0);
        result = 31 * result + (timeZone != null ? timeZone.hashCode() : 0);
        result = 31 * result + (address1 != null ? address1.hashCode() : 0);
        result = 31 * result + (address2 != null ? address2.hashCode() : 0);
        result = 31 * result + (postalCode != null ? postalCode.hashCode() : 0);
        result = 31 * result + (company != null ? company.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (locale != null ? locale.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (isMigrated != null ? isMigrated.hashCode() : 0);
        result = 31 * result + (isNotifiedForInvoices != null ? isNotifiedForInvoices.hashCode() : 0);
        return result;
    }
}
