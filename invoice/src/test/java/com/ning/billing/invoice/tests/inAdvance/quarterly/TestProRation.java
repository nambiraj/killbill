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

package com.ning.billing.invoice.tests.inAdvance.quarterly;

import static com.ning.billing.invoice.TestInvoiceHelper.*;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import com.ning.billing.catalog.api.BillingPeriod;
import com.ning.billing.invoice.model.InvalidDateSequenceException;
import com.ning.billing.invoice.tests.inAdvance.ProRationInAdvanceTestBase;

public class TestProRation extends ProRationInAdvanceTestBase {

    @Override
    protected BillingPeriod getBillingPeriod() {
        return BillingPeriod.QUARTERLY;
    }

    @Test(groups = "fast")
    public void testSinglePlan_WithPhaseChange() throws InvalidDateSequenceException {
        final LocalDate startDate = invoiceUtil.buildDate(2011, 2, 10);
        final LocalDate phaseChangeDate = invoiceUtil.buildDate(2011, 2, 24);
        final LocalDate targetDate = invoiceUtil.buildDate(2011, 3, 6);

        BigDecimal expectedValue;
        expectedValue = FOURTEEN.divide(EIGHTY_NINE, NUMBER_OF_DECIMALS, ROUNDING_METHOD);
        testCalculateNumberOfBillingCycles(startDate, phaseChangeDate, targetDate, 10, expectedValue);

        expectedValue = FOURTEEN.divide(NINETY, NUMBER_OF_DECIMALS, ROUNDING_METHOD);
        testCalculateNumberOfBillingCycles(phaseChangeDate, targetDate, 10, expectedValue);
    }

    @Test(groups = "fast")
    public void testSinglePlan_WithPhaseChange_BeforeBillCycleDay() throws InvalidDateSequenceException {
        final LocalDate startDate = invoiceUtil.buildDate(2011, 2, 3);
        final LocalDate phaseChangeDate = invoiceUtil.buildDate(2011, 2, 17);
        final LocalDate targetDate = invoiceUtil.buildDate(2011, 3, 1);

        BigDecimal expectedValue;
        expectedValue = FOURTEEN.divide(EIGHTY_NINE, NUMBER_OF_DECIMALS, ROUNDING_METHOD);
        testCalculateNumberOfBillingCycles(startDate, phaseChangeDate, targetDate, 3, expectedValue);

        expectedValue = FOURTEEN.divide(NINETY, NUMBER_OF_DECIMALS, ROUNDING_METHOD);
        testCalculateNumberOfBillingCycles(phaseChangeDate, targetDate, 3, expectedValue);
    }

    @Test(groups = "fast")
    public void testSinglePlan_WithPhaseChange_OnBillCycleDay() throws InvalidDateSequenceException {
        final LocalDate startDate = invoiceUtil.buildDate(2011, 2, 3);
        final LocalDate phaseChangeDate = invoiceUtil.buildDate(2011, 2, 17);
        final LocalDate targetDate = invoiceUtil.buildDate(2011, 3, 3);

        BigDecimal expectedValue;
        expectedValue = FOURTEEN.divide(EIGHTY_NINE, NUMBER_OF_DECIMALS, ROUNDING_METHOD);
        testCalculateNumberOfBillingCycles(startDate, phaseChangeDate, targetDate, 3, expectedValue);

        expectedValue = FOURTEEN.divide(NINETY, NUMBER_OF_DECIMALS, ROUNDING_METHOD).add(ONE);
        testCalculateNumberOfBillingCycles(phaseChangeDate, targetDate, 3, expectedValue);
    }

    @Test(groups = "fast")
    public void testSinglePlan_WithPhaseChange_AfterBillCycleDay() throws InvalidDateSequenceException {
        final LocalDate startDate = invoiceUtil.buildDate(2011, 2, 3);
        final LocalDate phaseChangeDate = invoiceUtil.buildDate(2011, 2, 17);
        final LocalDate targetDate = invoiceUtil.buildDate(2011, 3, 4);

        BigDecimal expectedValue;
        expectedValue = FOURTEEN.divide(EIGHTY_NINE, NUMBER_OF_DECIMALS, ROUNDING_METHOD);
        testCalculateNumberOfBillingCycles(startDate, phaseChangeDate, targetDate, 3, expectedValue);

        expectedValue = FOURTEEN.divide(NINETY, NUMBER_OF_DECIMALS, ROUNDING_METHOD).add(ONE);
        testCalculateNumberOfBillingCycles(phaseChangeDate, targetDate, 3, expectedValue);
    }

    @Test(groups = "fast")
    public void testPlanChange_WithChangeOfBillCycleDayToLaterDay() throws InvalidDateSequenceException {
        final LocalDate startDate = invoiceUtil.buildDate(2011, 2, 1);
        final LocalDate planChangeDate = invoiceUtil.buildDate(2011, 2, 15);
        final LocalDate targetDate = invoiceUtil.buildDate(2011, 3, 1);

        final BigDecimal expectedValue = FOURTEEN.divide(EIGHTY_NINE, NUMBER_OF_DECIMALS, ROUNDING_METHOD);
        testCalculateNumberOfBillingCycles(startDate, planChangeDate, targetDate, 1, expectedValue);
        testCalculateNumberOfBillingCycles(planChangeDate, targetDate, 15, ONE);
    }

    @Test(groups = "fast")
    public void testPlanChange_WithChangeOfBillCycleDayToEarlierDay() throws InvalidDateSequenceException {
        final LocalDate startDate = invoiceUtil.buildDate(2011, 2, 20);
        final LocalDate planChangeDate = invoiceUtil.buildDate(2011, 3, 6);
        final LocalDate targetDate = invoiceUtil.buildDate(2011, 3, 9);

        final BigDecimal expectedValue = FOURTEEN.divide(EIGHTY_NINE, NUMBER_OF_DECIMALS, ROUNDING_METHOD);
        testCalculateNumberOfBillingCycles(startDate, planChangeDate, targetDate, 20, expectedValue);
        testCalculateNumberOfBillingCycles(planChangeDate, targetDate, 6, ONE);
    }

    @Test(groups = "fast")
    public void testSinglePlan_CrossingYearBoundary() throws InvalidDateSequenceException {
        final LocalDate startDate = invoiceUtil.buildDate(2010, 12, 15);
        final LocalDate targetDate = invoiceUtil.buildDate(2011, 1, 16);

        testCalculateNumberOfBillingCycles(startDate, targetDate, 15, ONE);
    }

    @Test(groups = "fast")
    public void testSinglePlan_LeapYear_StartingMidFebruary() throws InvalidDateSequenceException {
        final LocalDate startDate = invoiceUtil.buildDate(2012, 2, 15);
        final LocalDate targetDate = invoiceUtil.buildDate(2012, 3, 15);

        testCalculateNumberOfBillingCycles(startDate, targetDate, 15, ONE);
    }

    @Test(groups = "fast")
    public void testSinglePlan_LeapYear_StartingBeforeFebruary() throws InvalidDateSequenceException {
        final LocalDate startDate = invoiceUtil.buildDate(2012, 1, 15);
        final LocalDate targetDate = invoiceUtil.buildDate(2012, 2, 3);

        testCalculateNumberOfBillingCycles(startDate, targetDate, 15, ONE);
    }

    @Test(groups = "fast")
    public void testSinglePlan_LeapYear_IncludingAllOfFebruary() throws InvalidDateSequenceException {
        final LocalDate startDate = invoiceUtil.buildDate(2012, 1, 30);
        final LocalDate targetDate = invoiceUtil.buildDate(2012, 3, 1);

        testCalculateNumberOfBillingCycles(startDate, targetDate, 30, ONE);
    }

    @Test(groups = "fast")
    public void testSinglePlan_ChangeBCDTo31() throws InvalidDateSequenceException {
        final LocalDate startDate = invoiceUtil.buildDate(2011, 2, 1);
        final LocalDate planChangeDate = invoiceUtil.buildDate(2011, 2, 14);
        final LocalDate targetDate = invoiceUtil.buildDate(2011, 3, 1);

        BigDecimal expectedValue;

        expectedValue = THIRTEEN.divide(EIGHTY_NINE, NUMBER_OF_DECIMALS, ROUNDING_METHOD);
        testCalculateNumberOfBillingCycles(startDate, planChangeDate, targetDate, 1, expectedValue);

        expectedValue = ONE.add(FOURTEEN.divide(NINETY_TWO, NUMBER_OF_DECIMALS, ROUNDING_METHOD));
        testCalculateNumberOfBillingCycles(planChangeDate, targetDate, 31, expectedValue);
    }

    @Test(groups = "fast")
    public void testSinglePlan_ChangeBCD() throws InvalidDateSequenceException {
        final LocalDate startDate = invoiceUtil.buildDate(2011, 2, 1);
        final LocalDate planChangeDate = invoiceUtil.buildDate(2011, 2, 14);
        final LocalDate targetDate = invoiceUtil.buildDate(2011, 5, 1);

        BigDecimal expectedValue;

        expectedValue = THIRTEEN.divide(EIGHTY_NINE, NUMBER_OF_DECIMALS, ROUNDING_METHOD);
        testCalculateNumberOfBillingCycles(startDate, planChangeDate, targetDate, 1, expectedValue);

        expectedValue = ONE.add(THIRTEEN.divide(NINETY_TWO, NUMBER_OF_DECIMALS, ROUNDING_METHOD));
        testCalculateNumberOfBillingCycles(planChangeDate, targetDate, 27, expectedValue);
    }

    @Test(groups = "fast")
    public void testSinglePlan_LeapYearFebruaryProRation() throws InvalidDateSequenceException {
        final LocalDate startDate = invoiceUtil.buildDate(2012, 2, 1);
        final LocalDate endDate = invoiceUtil.buildDate(2012, 2, 15);
        final LocalDate targetDate = invoiceUtil.buildDate(2012, 2, 19);

        final BigDecimal expectedValue;
        expectedValue = FOURTEEN.divide(NINETY, NUMBER_OF_DECIMALS, ROUNDING_METHOD);

        testCalculateNumberOfBillingCycles(startDate, endDate, targetDate, 1, expectedValue);
    }

    @Test(groups = "fast")
    public void testPlanChange_BeforeBillingDay() throws InvalidDateSequenceException {
        final LocalDate startDate = invoiceUtil.buildDate(2011, 2, 7);
        final LocalDate changeDate = invoiceUtil.buildDate(2011, 2, 15);
        final LocalDate targetDate = invoiceUtil.buildDate(2011, 9, 21);

        final BigDecimal expectedValue;

        expectedValue = EIGHT.divide(EIGHTY_NINE, NUMBER_OF_DECIMALS, ROUNDING_METHOD);
        testCalculateNumberOfBillingCycles(startDate, changeDate, targetDate, 7, expectedValue);

        testCalculateNumberOfBillingCycles(changeDate, targetDate, 15, THREE);
    }

    @Test(groups = "fast")
    public void testPlanChange_OnBillingDay() throws InvalidDateSequenceException {
        final LocalDate startDate = invoiceUtil.buildDate(2011, 2, 7);
        final LocalDate changeDate = invoiceUtil.buildDate(2011, 5, 7);
        final LocalDate targetDate = invoiceUtil.buildDate(2011, 7, 21);

        testCalculateNumberOfBillingCycles(startDate, changeDate, targetDate, 7, ONE);

        final BigDecimal expectedValue;
        expectedValue = EIGHT.divide(EIGHTY_NINE, NUMBER_OF_DECIMALS, ROUNDING_METHOD).add(ONE);
        testCalculateNumberOfBillingCycles(changeDate, targetDate, 15, expectedValue);
    }

    @Test(groups = "fast")
    public void testPlanChange_AfterBillingDay() throws InvalidDateSequenceException {
        final LocalDate startDate = invoiceUtil.buildDate(2011, 2, 7);
        final LocalDate changeDate = invoiceUtil.buildDate(2011, 5, 10);
        final LocalDate targetDate = invoiceUtil.buildDate(2011, 9, 21);

        BigDecimal expectedValue;

        expectedValue = ONE.add(THREE.divide(NINETY_TWO, NUMBER_OF_DECIMALS, ROUNDING_METHOD));
        testCalculateNumberOfBillingCycles(startDate, changeDate, targetDate, 7, expectedValue);

        expectedValue = FIVE.divide(EIGHTY_NINE, NUMBER_OF_DECIMALS, ROUNDING_METHOD).add(TWO);
        testCalculateNumberOfBillingCycles(changeDate, targetDate, 15, expectedValue);
    }

    @Test(groups = "fast")
    public void testPlanChange_DoubleProRation() throws InvalidDateSequenceException {
        final LocalDate startDate = invoiceUtil.buildDate(2011, 1, 31);
        final LocalDate planChangeDate = invoiceUtil.buildDate(2011, 5, 10);
        final LocalDate targetDate = invoiceUtil.buildDate(2011, 5, 21);

        BigDecimal expectedValue;
        expectedValue = SEVEN.divide(NINETY_TWO, NUMBER_OF_DECIMALS, ROUNDING_METHOD);
        expectedValue = expectedValue.add(ONE);
        expectedValue = expectedValue.add(THREE.divide(NINETY_TWO, NUMBER_OF_DECIMALS, ROUNDING_METHOD));
        testCalculateNumberOfBillingCycles(startDate, planChangeDate, targetDate, 7, expectedValue);

        expectedValue = FIVE.divide(EIGHTY_NINE, NUMBER_OF_DECIMALS, ROUNDING_METHOD).add(ONE);
        testCalculateNumberOfBillingCycles(planChangeDate, targetDate, 15, expectedValue);
    }

    @Test(groups = "fast")
    public void testStartTargetEnd() throws InvalidDateSequenceException {
        final LocalDate startDate = invoiceUtil.buildDate(2010, 12, 15);
        final LocalDate targetDate = invoiceUtil.buildDate(2011, 6, 15);
        final LocalDate endDate = invoiceUtil.buildDate(2011, 6, 17);

        final BigDecimal expectedValue = TWO.add(TWO.divide(NINETY_TWO, NUMBER_OF_DECIMALS, ROUNDING_METHOD));
        testCalculateNumberOfBillingCycles(startDate, endDate, targetDate, 15, expectedValue);
    }
}
