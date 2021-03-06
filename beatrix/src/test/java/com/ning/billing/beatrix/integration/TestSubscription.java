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

package com.ning.billing.beatrix.integration;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import com.ning.billing.account.api.Account;
import com.ning.billing.api.TestApiListener.NextEvent;
import com.ning.billing.beatrix.util.InvoiceChecker.ExpectedInvoiceItemCheck;
import com.ning.billing.catalog.api.BillingActionPolicy;
import com.ning.billing.catalog.api.BillingPeriod;
import com.ning.billing.catalog.api.PriceListSet;
import com.ning.billing.catalog.api.ProductCategory;
import com.ning.billing.entitlement.api.DefaultEntitlement;
import com.ning.billing.invoice.api.Invoice;
import com.ning.billing.invoice.api.InvoiceItemType;

import com.google.common.collect.ImmutableList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class TestSubscription extends TestIntegrationBase {


    @Test(groups = "slow")
    public void testForcePolicy() throws Exception {
        // We take april as it has 30 days (easier to play with BCD)
        final LocalDate today = new LocalDate(2012, 4, 1);
        final Account account = createAccountWithNonOsgiPaymentMethod(getAccountData(1));

        // Set clock to the initial start date - we implicitly assume here that the account timezone is UTC
        clock.setDeltaFromReality(today.toDateTimeAtCurrentTime(DateTimeZone.UTC).getMillis() - clock.getUTCNow().getMillis());

        final String productName = "Shotgun";
        final BillingPeriod term = BillingPeriod.ANNUAL;
        final String planSetName = PriceListSet.DEFAULT_PRICELIST_NAME;

        //
        // CREATE SUBSCRIPTION AND EXPECT BOTH EVENTS: NextEvent.CREATE NextEvent.INVOICE
        //

        final DefaultEntitlement bpEntitlement = createBaseEntitlementAndCheckForCompletion(account.getId(), "externalKey", productName, ProductCategory.BASE, BillingPeriod.ANNUAL, NextEvent.CREATE, NextEvent.INVOICE);
        assertNotNull(bpEntitlement);
        assertEquals(invoiceUserApi.getInvoicesByAccount(account.getId(), callContext).size(), 1);

        assertEquals(bpEntitlement.getSubscriptionBase().getCurrentPlan().getBillingPeriod(), BillingPeriod.ANNUAL);

        // Move out of trials for interesting invoices adjustments
        busHandler.pushExpectedEvents(NextEvent.PHASE, NextEvent.INVOICE, NextEvent.PAYMENT);
        clock.addDays(40);
        assertTrue(busHandler.isCompleted(DELAY));
        assertListenerStatus();

        List<Invoice> invoices = invoiceUserApi.getInvoicesByAccount(account.getId(), callContext);
        assertEquals(invoices.size(), 2);
        ImmutableList<ExpectedInvoiceItemCheck> toBeChecked = ImmutableList.<ExpectedInvoiceItemCheck>of(
                new ExpectedInvoiceItemCheck(new LocalDate(2012, 5, 1), new LocalDate(2013, 5, 1), InvoiceItemType.RECURRING, new BigDecimal("2399.95")));
        invoiceChecker.checkInvoice(invoices.get(1).getId(), callContext, toBeChecked);

        //
        // FORCE AN IMMEDIATE CHANGE OF THE BILLING PERIOD
        //
        changeEntitlementAndCheckForCompletion(bpEntitlement, productName, BillingPeriod.MONTHLY, BillingActionPolicy.IMMEDIATE, NextEvent.CHANGE, NextEvent.INVOICE, NextEvent.INVOICE_ADJUSTMENT);

        invoices = invoiceUserApi.getInvoicesByAccount(account.getId(), callContext);
        assertEquals(invoices.size(), 3);
        toBeChecked = ImmutableList.<ExpectedInvoiceItemCheck>of(
                new ExpectedInvoiceItemCheck(new LocalDate(2012, 5, 1), new LocalDate(2013, 5, 1), InvoiceItemType.RECURRING, new BigDecimal("2399.95")),
                new ExpectedInvoiceItemCheck(new LocalDate(2012, 5, 11), new LocalDate(2013, 5, 1), InvoiceItemType.REPAIR_ADJ, new BigDecimal("-2334.19")),
                new ExpectedInvoiceItemCheck(new LocalDate(2012, 5, 11), new LocalDate(2012, 5, 11), InvoiceItemType.CBA_ADJ, new BigDecimal("2334.19")));
        invoiceChecker.checkInvoice(invoices.get(1).getId(), callContext, toBeChecked);

        toBeChecked = ImmutableList.<ExpectedInvoiceItemCheck>of(
                new ExpectedInvoiceItemCheck(new LocalDate(2012, 5, 11), new LocalDate(2012, 6, 1), InvoiceItemType.RECURRING, new BigDecimal("169.32")),
                new ExpectedInvoiceItemCheck(new LocalDate(2012, 5, 11), new LocalDate(2012, 5, 11), InvoiceItemType.CBA_ADJ, new BigDecimal("-169.32")));
        invoiceChecker.checkInvoice(invoices.get(2).getId(), callContext, toBeChecked);


        //
        // FORCE ANOTHER CHANGE
        //
        changeEntitlementAndCheckForCompletion(bpEntitlement, productName, BillingPeriod.ANNUAL, BillingActionPolicy.IMMEDIATE, NextEvent.CHANGE, NextEvent.INVOICE, NextEvent.INVOICE_ADJUSTMENT);

        invoices = invoiceUserApi.getInvoicesByAccount(account.getId(), callContext);
        assertEquals(invoices.size(), 4);


        toBeChecked = ImmutableList.<ExpectedInvoiceItemCheck>of(
                new ExpectedInvoiceItemCheck(new LocalDate(2012, 5, 11), new LocalDate(2012, 6, 1), InvoiceItemType.RECURRING, new BigDecimal("169.32")),
                new ExpectedInvoiceItemCheck(new LocalDate(2012, 5, 11), new LocalDate(2012, 5, 11), InvoiceItemType.CBA_ADJ, new BigDecimal("-169.32")),
                new ExpectedInvoiceItemCheck(new LocalDate(2012, 5, 11), new LocalDate(2012, 6, 1), InvoiceItemType.REPAIR_ADJ, new BigDecimal("-169.32")),
                new ExpectedInvoiceItemCheck(new LocalDate(2012, 5, 11), new LocalDate(2012, 5, 11), InvoiceItemType.CBA_ADJ, new BigDecimal("169.32")));
        invoiceChecker.checkInvoice(invoices.get(2).getId(), callContext, toBeChecked);


        toBeChecked = ImmutableList.<ExpectedInvoiceItemCheck>of(
                new ExpectedInvoiceItemCheck(new LocalDate(2012, 5, 11), new LocalDate(2012, 6, 1), InvoiceItemType.RECURRING, new BigDecimal("137.76")),
                new ExpectedInvoiceItemCheck(new LocalDate(2012, 5, 11), new LocalDate(2012, 5, 11), InvoiceItemType.CBA_ADJ, new BigDecimal("-137.76")));
        invoiceChecker.checkInvoice(invoices.get(3).getId(), callContext, toBeChecked);


    }

}
