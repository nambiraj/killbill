/* 
 * Copyright 2010-2011 Ning, Inc.
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
package com.ning.billing.entitlement.api.repair;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.ning.billing.catalog.api.BillingPeriod;
import com.ning.billing.catalog.api.Duration;
import com.ning.billing.catalog.api.PhaseType;
import com.ning.billing.catalog.api.Plan;
import com.ning.billing.catalog.api.PlanPhase;
import com.ning.billing.catalog.api.PlanPhaseSpecifier;
import com.ning.billing.catalog.api.PriceListSet;
import com.ning.billing.catalog.api.ProductCategory;
import com.ning.billing.entitlement.api.SubscriptionTransitionType;
import com.ning.billing.entitlement.api.TestApiBase;
import com.ning.billing.entitlement.api.repair.SubscriptionRepair.DeletedEvent;
import com.ning.billing.entitlement.api.repair.SubscriptionRepair.ExistingEvent;
import com.ning.billing.entitlement.api.repair.SubscriptionRepair.NewEvent;
import com.ning.billing.entitlement.api.user.Subscription;
import com.ning.billing.entitlement.api.user.SubscriptionData;
import com.ning.billing.entitlement.api.user.SubscriptionEvents;
import com.ning.billing.entitlement.glue.MockEngineModuleSql;

public class TestRepair extends TestApiBase {

    @Override
    public Injector getInjector() {
        return Guice.createInjector(Stage.DEVELOPMENT, new MockEngineModuleSql());
    }

    @Test(groups={"slow"})
    public void testFetchBundleRepair() {
        try {

            String baseProduct = "Shotgun";
            BillingPeriod baseTerm = BillingPeriod.MONTHLY;
            String basePriceList = PriceListSet.DEFAULT_PRICELIST_NAME;

            // CREATE BP
            Subscription baseSubscription = createSubscription(baseProduct, baseTerm, basePriceList);

            String aoProduct = "Telescopic-Scope";
            BillingPeriod aoTerm = BillingPeriod.MONTHLY;
            String aoPriceList = PriceListSet.DEFAULT_PRICELIST_NAME;

            SubscriptionData aoSubscription = createSubscription(aoProduct, aoTerm, aoPriceList);

            BundleRepair bundleRepair = repairApi.getBundleRepair(bundle.getId());
            List<SubscriptionRepair> subscriptionRepair = bundleRepair.getSubscriptions();
            assertEquals(subscriptionRepair.size(), 2);

            for (SubscriptionRepair cur : subscriptionRepair) {
                assertNull(cur.getDeletedEvents());
                assertNull(cur.getNewEvents());                

                List<ExistingEvent> events = cur.getExistingEvents();
                assertEquals(events.size(), 2);
                sortExistingEvent(events);

                assertEquals(events.get(0).getSubscriptionTransitionType(), SubscriptionTransitionType.CREATE);
                assertEquals(events.get(1).getSubscriptionTransitionType(), SubscriptionTransitionType.PHASE);                    
                final boolean isBP = cur.getId().equals(baseSubscription.getId());
                if (isBP) {
                    assertEquals(cur.getId(), baseSubscription.getId());

                    assertEquals(events.get(0).getPlanPhaseSpecifier().getProductName(), baseProduct);
                    assertEquals(events.get(0).getPlanPhaseSpecifier().getPhaseType(), PhaseType.TRIAL);
                    assertEquals(events.get(0).getPlanPhaseSpecifier().getProductCategory(),ProductCategory.BASE);                    
                    assertEquals(events.get(0).getPlanPhaseSpecifier().getPriceListName(), basePriceList);                    
                    assertEquals(events.get(0).getPlanPhaseSpecifier().getBillingPeriod(), BillingPeriod.NO_BILLING_PERIOD);

                    assertEquals(events.get(1).getPlanPhaseSpecifier().getProductName(), baseProduct);
                    assertEquals(events.get(1).getPlanPhaseSpecifier().getPhaseType(), PhaseType.EVERGREEN);
                    assertEquals(events.get(1).getPlanPhaseSpecifier().getProductCategory(),ProductCategory.BASE);                    
                    assertEquals(events.get(1).getPlanPhaseSpecifier().getPriceListName(), basePriceList);                    
                    assertEquals(events.get(1).getPlanPhaseSpecifier().getBillingPeriod(), baseTerm);
                } else {
                    assertEquals(cur.getId(), aoSubscription.getId());

                    assertEquals(events.get(0).getPlanPhaseSpecifier().getProductName(), aoProduct);
                    assertEquals(events.get(0).getPlanPhaseSpecifier().getPhaseType(), PhaseType.DISCOUNT);                    
                    assertEquals(events.get(0).getPlanPhaseSpecifier().getProductCategory(),ProductCategory.ADD_ON); 
                    assertEquals(events.get(0).getPlanPhaseSpecifier().getPriceListName(), aoPriceList); 
                    assertEquals(events.get(1).getPlanPhaseSpecifier().getBillingPeriod(), aoTerm);                    

                    assertEquals(events.get(1).getPlanPhaseSpecifier().getProductName(), aoProduct);
                    assertEquals(events.get(1).getPlanPhaseSpecifier().getPhaseType(), PhaseType.EVERGREEN);                    
                    assertEquals(events.get(1).getPlanPhaseSpecifier().getProductCategory(),ProductCategory.ADD_ON); 
                    assertEquals(events.get(1).getPlanPhaseSpecifier().getPriceListName(), aoPriceList);  
                    assertEquals(events.get(1).getPlanPhaseSpecifier().getBillingPeriod(), aoTerm);                    
                }
            }
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }



    @Test(groups={"slow"})
    public void testSimpleBPRepairAddChangeBeforePhase() throws Exception {

        String baseProduct = "Shotgun";
        BillingPeriod baseTerm = BillingPeriod.MONTHLY;
        String basePriceList = PriceListSet.DEFAULT_PRICELIST_NAME;

        // CREATE BP
        Subscription baseSubscription = createSubscription(baseProduct, baseTerm, basePriceList);
        BundleRepair bundleRepair = repairApi.getBundleRepair(bundle.getId());
        sortEventsOnBundle(bundleRepair);

        // MOVE CLOCK-- STAYS in TRIAL
        Duration moveTenDays = getDurationDay(10);
        clock.setDeltaFromReality(moveTenDays, 0);


        DateTime changeTime = baseSubscription.getStartDate().plusDays(3);
        String newBaseProduct = "Assault-Rifle";
        BillingPeriod newBaseTerm = BillingPeriod.MONTHLY;
        String newBasePriceList = PriceListSet.DEFAULT_PRICELIST_NAME;
        PlanPhaseSpecifier spec = new PlanPhaseSpecifier(newBaseProduct, ProductCategory.BASE, newBaseTerm, newBasePriceList, PhaseType.TRIAL);


        NewEvent ne = createNewEvent(SubscriptionTransitionType.CHANGE, changeTime, spec);
        DeletedEvent de = createDeletedEvent(bundleRepair.getSubscriptions().get(0).getExistingEvents().get(1).getEventId());
        SubscriptionRepair sRepair = createSubscriptionReapir(baseSubscription.getId(), Collections.singletonList(de), Collections.singletonList(ne));
        
        // FIRST ISSUE DRY RUN
        BundleRepair bRepair =  createBundleRepair(bundle.getId(), bundleRepair.getViewId(), Collections.singletonList(sRepair));

        
        boolean dryRun = true;
        BundleRepair dryRunBundleRepair = repairApi.repairBundle(bRepair, dryRun, context);
        List<SubscriptionRepair> subscriptionRepair = dryRunBundleRepair.getSubscriptions();
        assertEquals(subscriptionRepair.size(), 1);
        SubscriptionRepair cur = subscriptionRepair.get(0);
        assertEquals(cur.getId(), baseSubscription.getId());

        List<ExistingEvent> events = cur.getExistingEvents();
       assertEquals(events.size(), 3);
        
       assertEquals(events.get(0).getPlanPhaseSpecifier().getProductName(), baseProduct);
       assertEquals(events.get(0).getPlanPhaseSpecifier().getPhaseType(), PhaseType.TRIAL);
       assertEquals(events.get(0).getPlanPhaseSpecifier().getProductCategory(),ProductCategory.BASE);                    
       assertEquals(events.get(0).getPlanPhaseSpecifier().getPriceListName(), basePriceList);                    
       assertEquals(events.get(0).getPlanPhaseSpecifier().getBillingPeriod(), BillingPeriod.NO_BILLING_PERIOD);

       assertEquals(events.get(1).getPlanPhaseSpecifier().getProductName(), newBaseProduct);
       assertEquals(events.get(1).getPlanPhaseSpecifier().getPhaseType(), PhaseType.TRIAL);
       assertEquals(events.get(1).getPlanPhaseSpecifier().getProductCategory(),ProductCategory.BASE);                    
       assertEquals(events.get(1).getPlanPhaseSpecifier().getPriceListName(), basePriceList);                    
       assertEquals(events.get(1).getPlanPhaseSpecifier().getBillingPeriod(), BillingPeriod.NO_BILLING_PERIOD);

        assertEquals(events.get(2).getPlanPhaseSpecifier().getProductName(), newBaseProduct);
        assertEquals(events.get(2).getPlanPhaseSpecifier().getPhaseType(), PhaseType.EVERGREEN);
        assertEquals(events.get(2).getPlanPhaseSpecifier().getProductCategory(),ProductCategory.BASE);                    
        assertEquals(events.get(2).getPlanPhaseSpecifier().getPriceListName(), basePriceList);                    
        assertEquals(events.get(2).getPlanPhaseSpecifier().getBillingPeriod(), baseTerm);
     
        SubscriptionData dryRunBaseSubscription = (SubscriptionData) entitlementApi.getSubscriptionFromId(baseSubscription.getId());
        
        assertEquals(dryRunBaseSubscription.getActiveVersion(), SubscriptionEvents.INITIAL_VERSION);
        assertEquals(dryRunBaseSubscription.getBundleId(), bundle.getId());
        assertEquals(dryRunBaseSubscription.getStartDate(), baseSubscription.getStartDate());

        Plan currentPlan = dryRunBaseSubscription.getCurrentPlan();
        assertNotNull(currentPlan);
        assertEquals(currentPlan.getProduct().getName(), baseProduct);
        assertEquals(currentPlan.getProduct().getCategory(), ProductCategory.BASE);
        assertEquals(currentPlan.getBillingPeriod(), baseTerm);

        PlanPhase currentPhase = dryRunBaseSubscription.getCurrentPhase();
        assertNotNull(currentPhase);
        assertEquals(currentPhase.getPhaseType(), PhaseType.TRIAL);
        
        
       // SECOND RE-ISSUE CALL-- NON DRY RUN
        
    }


    private SubscriptionRepair createSubscriptionReapir(final UUID id, final List<DeletedEvent> deletedEvents, final List<NewEvent> newEvents) {
        return new SubscriptionRepair() {
            @Override
            public UUID getId() {
                return id;
            }
            @Override
            public List<NewEvent> getNewEvents() {
                return newEvents;
            }
            @Override
            public List<ExistingEvent> getExistingEvents() {
                return null;
            }
            @Override
            public List<DeletedEvent> getDeletedEvents() {
                return deletedEvents;
            }
        };
    }

    private BundleRepair createBundleRepair(final UUID bundleId, final String viewId, final List<SubscriptionRepair> subscriptionRepair) {
        return new BundleRepair() {
            @Override
            public String getViewId() {
                return viewId;
            }
            @Override
            public List<SubscriptionRepair> getSubscriptions() {
                return subscriptionRepair;
            }
            @Override
            public UUID getBundleId() {
                return bundleId;
            }
        };
    }

    private DeletedEvent createDeletedEvent(final UUID eventId) {
        return new DeletedEvent() {
            @Override
            public UUID getEventId() {
                return eventId;
            }
        };
    }

    private NewEvent createNewEvent(final SubscriptionTransitionType type, final DateTime requestedDate, final PlanPhaseSpecifier spec) {

        return new NewEvent() {
            @Override
            public SubscriptionTransitionType getSubscriptionTransitionType() {
                return type;
            }
            @Override
            public DateTime getRequestedDate() {
                return requestedDate;
            }
            @Override
            public PlanPhaseSpecifier getPlanPhaseSpecifier() {
                return spec;
            }
        };
    }

    private void sortEventsOnBundle(final BundleRepair bundle) {
        if (bundle.getSubscriptions() == null) {
            return;
        }
        for (SubscriptionRepair cur : bundle.getSubscriptions()) {
            if (cur.getExistingEvents() != null) {
                sortExistingEvent(cur.getExistingEvents());
            }
            if (cur.getNewEvents() != null) {
                sortNewEvent(cur.getNewEvents());
            }
        }
    }

    private void sortExistingEvent(final List<ExistingEvent> events) {
        Collections.sort(events, new Comparator<ExistingEvent>() {
            @Override
            public int compare(ExistingEvent arg0, ExistingEvent arg1) {
                return arg0.getEffectiveDate().compareTo(arg1.getEffectiveDate());
            }
        });
    }
    private void sortNewEvent(final List<NewEvent> events) {
        Collections.sort(events, new Comparator<NewEvent>() {
            @Override
            public int compare(NewEvent arg0, NewEvent arg1) {
                return arg0.getRequestedDate().compareTo(arg1.getRequestedDate());
            }
        });
    }

}