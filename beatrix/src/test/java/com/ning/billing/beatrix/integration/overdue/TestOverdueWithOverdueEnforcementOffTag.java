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

package com.ning.billing.beatrix.integration.overdue;

import java.math.BigDecimal;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import com.ning.billing.ObjectType;
import com.ning.billing.api.TestApiListener.NextEvent;
import com.ning.billing.beatrix.util.InvoiceChecker.ExpectedInvoiceItemCheck;
import com.ning.billing.catalog.api.ProductCategory;
import com.ning.billing.entitlement.api.DefaultEntitlement;
import com.ning.billing.invoice.api.InvoiceItemType;
import com.ning.billing.util.svcapi.junction.DefaultBlockingState;
import com.ning.billing.util.tag.ControlTagType;

@Test(groups = "slow")
public class TestOverdueWithOverdueEnforcementOffTag extends TestOverdueBase {


    @Override
    public String getOverdueConfig() {
        final String configXml = "<overdueConfig>" +
                                 "   <accountOverdueStates>" +
                                 "       <state name=\"OD1\">" +
                                 "           <condition>" +
                                 "               <timeSinceEarliestUnpaidInvoiceEqualsOrExceeds>" +
                                 "                   <unit>DAYS</unit><number>5</number>" +
                                 "               </timeSinceEarliestUnpaidInvoiceEqualsOrExceeds>" +
                                 "           </condition>" +
                                 "           <externalMessage>Reached OD1</externalMessage>" +
                                 "           <blockChanges>true</blockChanges>" +
                                 "           <disableEntitlementAndChangesBlocked>false</disableEntitlementAndChangesBlocked>" +
                                 "           <autoReevaluationInterval>" +
                                 "               <unit>DAYS</unit><number>5</number>" +
                                 "           </autoReevaluationInterval>" +
                                 "       </state>" +
                                 "   </accountOverdueStates>" +
                                 "</overdueConfig>";
        return configXml;
    }

    @Test(groups = "slow")
    public void testNonOverdueAccountWithOverdueEnforcementOffTag() throws Exception {

        clock.setTime(new DateTime(2012, 5, 1, 0, 3, 42, 0));

        // Set the OVERDUE_ENFORCEMENT_OFF tag
        busHandler.pushExpectedEvent(NextEvent.TAG);
        tagUserApi.addTag(account.getId(), ObjectType.ACCOUNT, ControlTagType.OVERDUE_ENFORCEMENT_OFF.getId(), callContext);
        busHandler.isCompleted(DELAY);

        // Set next invoice to fail and create subscription
        paymentPlugin.makeAllInvoicesFailWithError(true);
        final DefaultEntitlement baseEntitlement = createBaseEntitlementAndCheckForCompletion(account.getId(), "externalKey", productName, ProductCategory.BASE, term, NextEvent.CREATE, NextEvent.INVOICE);
        bundle = subscriptionApi.getSubscriptionBundle(baseEntitlement.getBundleId(), callContext);

        invoiceChecker.checkInvoice(account.getId(), 1, callContext, new ExpectedInvoiceItemCheck(new LocalDate(2012, 5, 1), null, InvoiceItemType.FIXED, new BigDecimal("0")));
        invoiceChecker.checkChargedThroughDate(baseEntitlement.getId(), new LocalDate(2012, 5, 1), callContext);

        // DAY 30 have to get out of trial before first payment
        addDaysAndCheckForCompletion(30, NextEvent.PHASE, NextEvent.INVOICE, NextEvent.PAYMENT_ERROR);

        invoiceChecker.checkInvoice(account.getId(), 2, callContext, new ExpectedInvoiceItemCheck(new LocalDate(2012, 5, 31), new LocalDate(2012, 6, 30), InvoiceItemType.RECURRING, new BigDecimal("249.95")));
        invoiceChecker.checkChargedThroughDate(baseEntitlement.getId(), new LocalDate(2012, 6, 30), callContext);

        // DAY 36 -- RIGHT AFTER OD1
        addDaysAndCheckForCompletion(6);

        // Should still be in clear state
        checkODState(DefaultBlockingState.CLEAR_STATE_NAME);

        // Now remove OVERDUE_ENFORCEMENT_OFF tag
        busHandler.pushExpectedEvent(NextEvent.TAG);
        tagUserApi.removeTag(account.getId(), ObjectType.ACCOUNT, ControlTagType.OVERDUE_ENFORCEMENT_OFF.getId(), callContext);
        busHandler.isCompleted(DELAY);
        checkODState("OD1");
    }


    @Test(groups = "slow")
    public void testOverdueAccountWithOverdueEnforcementOffTag() throws Exception {

        clock.setTime(new DateTime(2012, 5, 1, 0, 3, 42, 0));

        // Set next invoice to fail and create subscription
        paymentPlugin.makeAllInvoicesFailWithError(true);
        final DefaultEntitlement baseEntitlement = createBaseEntitlementAndCheckForCompletion(account.getId(), "externalKey", productName, ProductCategory.BASE, term, NextEvent.CREATE, NextEvent.INVOICE);
        bundle = subscriptionApi.getSubscriptionBundle(baseEntitlement.getBundleId(), callContext);

        invoiceChecker.checkInvoice(account.getId(), 1, callContext, new ExpectedInvoiceItemCheck(new LocalDate(2012, 5, 1), null, InvoiceItemType.FIXED, new BigDecimal("0")));
        invoiceChecker.checkChargedThroughDate(baseEntitlement.getId(), new LocalDate(2012, 5, 1), callContext);

        // DAY 30 have to get out of trial before first payment
        addDaysAndCheckForCompletion(30, NextEvent.PHASE, NextEvent.INVOICE, NextEvent.PAYMENT_ERROR);

        invoiceChecker.checkInvoice(account.getId(), 2, callContext, new ExpectedInvoiceItemCheck(new LocalDate(2012, 5, 31), new LocalDate(2012, 6, 30), InvoiceItemType.RECURRING, new BigDecimal("249.95")));
        invoiceChecker.checkChargedThroughDate(baseEntitlement.getId(), new LocalDate(2012, 6, 30), callContext);

        // DAY 36 -- RIGHT AFTER OD1
        addDaysAndCheckForCompletion(6);

        // Account should be in overdue
        checkODState("OD1");

        // Set the OVERDUE_ENFORCEMENT_OFF tag
        busHandler.pushExpectedEvent(NextEvent.TAG);
        tagUserApi.addTag(account.getId(), ObjectType.ACCOUNT, ControlTagType.OVERDUE_ENFORCEMENT_OFF.getId(), callContext);
        busHandler.isCompleted(DELAY);

        // Should now be in clear state
        checkODState(DefaultBlockingState.CLEAR_STATE_NAME);

        // Now remove OVERDUE_ENFORCEMENT_OFF tag
        busHandler.pushExpectedEvent(NextEvent.TAG);
        tagUserApi.removeTag(account.getId(), ObjectType.ACCOUNT, ControlTagType.OVERDUE_ENFORCEMENT_OFF.getId(), callContext);
        busHandler.isCompleted(DELAY);

        // Account should be back in overdue
        checkODState("OD1");
    }
}
