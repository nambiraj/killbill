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

package com.ning.billing.subscription.api.timeline;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;

import com.ning.billing.ErrorCode;
import com.ning.billing.ObjectType;
import com.ning.billing.catalog.api.CatalogApiException;
import com.ning.billing.catalog.api.CatalogService;
import com.ning.billing.catalog.api.Plan;
import com.ning.billing.catalog.api.PlanPhaseSpecifier;
import com.ning.billing.catalog.api.Product;
import com.ning.billing.catalog.api.ProductCategory;
import com.ning.billing.entitlement.api.Entitlement.EntitlementState;
import com.ning.billing.subscription.api.SubscriptionBaseApiService;
import com.ning.billing.subscription.api.SubscriptionBaseTransitionType;
import com.ning.billing.subscription.api.user.SubscriptionBaseApiException;
import com.ning.billing.subscription.api.user.SubscriptionBaseTransition;
import com.ning.billing.subscription.api.user.SubscriptionBuilder;
import com.ning.billing.subscription.api.user.DefaultSubscriptionBase;
import com.ning.billing.subscription.engine.addon.AddonUtils;
import com.ning.billing.subscription.engine.dao.SubscriptionDao;
import com.ning.billing.subscription.events.SubscriptionBaseEvent;
import com.ning.billing.subscription.events.SubscriptionBaseEvent.EventType;
import com.ning.billing.subscription.events.user.ApiEventBuilder;
import com.ning.billing.subscription.events.user.ApiEventCancel;
import com.ning.billing.entitlement.api.Entitlement.EntitlementState;
import com.ning.billing.util.callcontext.CallContext;
import com.ning.billing.util.callcontext.InternalCallContextFactory;
import com.ning.billing.clock.Clock;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class SubscriptionDataRepair extends DefaultSubscriptionBase {

    private final AddonUtils addonUtils;
    private final Clock clock;
    private final SubscriptionDao repairDao;
    private final CatalogService catalogService;
    private final List<SubscriptionBaseEvent> initialEvents;
    private final InternalCallContextFactory internalCallContextFactory;


    public SubscriptionDataRepair(final SubscriptionBuilder builder, final List<SubscriptionBaseEvent> initialEvents, final SubscriptionBaseApiService apiService,
                                  final SubscriptionDao dao, final Clock clock, final AddonUtils addonUtils, final CatalogService catalogService,
                                  final InternalCallContextFactory internalCallContextFactory) {
        super(builder, apiService, clock);
        this.repairDao = dao;
        this.addonUtils = addonUtils;
        this.clock = clock;
        this.catalogService = catalogService;
        this.initialEvents = initialEvents;
        this.internalCallContextFactory = internalCallContextFactory;
    }



    public SubscriptionDataRepair(final DefaultSubscriptionBase defaultSubscriptionBase, final SubscriptionBaseApiService apiService,
                                  final SubscriptionDao dao, final Clock clock, final AddonUtils addonUtils, final CatalogService catalogService,
                                  final InternalCallContextFactory internalCallContextFactory) {
        super(defaultSubscriptionBase, apiService , clock);
        this.repairDao = dao;
        this.addonUtils = addonUtils;
        this.clock = clock;
        this.catalogService = catalogService;
        this.initialEvents = defaultSubscriptionBase.getEvents();
        this.internalCallContextFactory = internalCallContextFactory;
    }

    DateTime getLastUserEventEffectiveDate() {
        DateTime res = null;
        for (final SubscriptionBaseEvent cur : events) {
            if (cur.getActiveVersion() != getActiveVersion()) {
                break;
            }
            if (cur.getType() == EventType.PHASE) {
                continue;
            }
            res = cur.getEffectiveDate();
        }
        return res;
    }

    public void addNewRepairEvent(final DefaultNewEvent input, final SubscriptionDataRepair baseSubscription, final List<SubscriptionDataRepair> addonSubscriptions, final CallContext context)
            throws SubscriptionBaseRepairException {

        try {
            final PlanPhaseSpecifier spec = input.getPlanPhaseSpecifier();
            switch (input.getSubscriptionTransitionType()) {
                case CREATE:
                case RE_CREATE:
                    recreate(spec, input.getRequestedDate(), context);
                    checkAddonRights(baseSubscription);
                    break;
                case CHANGE:
                    changePlan(spec.getProductName(), spec.getBillingPeriod(), spec.getPriceListName(), input.getRequestedDate(), context);
                    checkAddonRights(baseSubscription);
                    trickleDownBPEffectForAddon(addonSubscriptions, getLastUserEventEffectiveDate(), context);
                    break;
                case CANCEL:
                    cancel(input.getRequestedDate(), context);
                    trickleDownBPEffectForAddon(addonSubscriptions, getLastUserEventEffectiveDate(), context);
                    break;
                case PHASE:
                    break;
                default:
                    throw new SubscriptionBaseRepairException(ErrorCode.SUB_REPAIR_UNKNOWN_TYPE, input.getSubscriptionTransitionType(), id);
            }
        } catch (SubscriptionBaseApiException e) {
            throw new SubscriptionBaseRepairException(e);
        } catch (CatalogApiException e) {
            throw new SubscriptionBaseRepairException(e);
        }
    }

    public void addFutureAddonCancellation(final List<SubscriptionDataRepair> addOnSubscriptionInRepair, final CallContext context) {

        if (getCategory() != ProductCategory.BASE) {
            return;
        }

        final SubscriptionBaseTransition pendingTransition = getPendingTransition();
        if (pendingTransition == null) {
            return;
        }
        final Product baseProduct = (pendingTransition.getTransitionType() == SubscriptionBaseTransitionType.CANCEL) ? null :
                                    pendingTransition.getNextPlan().getProduct();

        addAddonCancellationIfRequired(addOnSubscriptionInRepair, baseProduct, pendingTransition.getEffectiveTransitionTime(), context);
    }

    private void trickleDownBPEffectForAddon(final List<SubscriptionDataRepair> addOnSubscriptionInRepair, final DateTime effectiveDate, final CallContext context)
            throws SubscriptionBaseApiException {

        if (getCategory() != ProductCategory.BASE) {
            return;
        }

        final Product baseProduct = (getState() == EntitlementState.CANCELLED) ?
                                    null : getCurrentPlan().getProduct();
        addAddonCancellationIfRequired(addOnSubscriptionInRepair, baseProduct, effectiveDate, context);
    }

    private void addAddonCancellationIfRequired(final List<SubscriptionDataRepair> addOnSubscriptionInRepair, final Product baseProduct,
                                                final DateTime effectiveDate, final CallContext context) {

        final DateTime now = clock.getUTCNow();
        final Iterator<SubscriptionDataRepair> it = addOnSubscriptionInRepair.iterator();
        while (it.hasNext()) {
            final SubscriptionDataRepair cur = it.next();
            if (cur.getState() == EntitlementState.CANCELLED ||
                cur.getCategory() != ProductCategory.ADD_ON) {
                continue;
            }
            final Plan addonCurrentPlan = cur.getCurrentPlan();
            if (baseProduct == null ||
                addonUtils.isAddonIncluded(baseProduct, addonCurrentPlan) ||
                !addonUtils.isAddonAvailable(baseProduct, addonCurrentPlan)) {

                final SubscriptionBaseEvent cancelEvent = new ApiEventCancel(new ApiEventBuilder()
                                                                                .setSubscriptionId(cur.getId())
                                                                                .setActiveVersion(cur.getActiveVersion())
                                                                                .setProcessedDate(now)
                                                                                .setEffectiveDate(effectiveDate)
                                                                                .setRequestedDate(now)
                                                                                .setFromDisk(true));
                repairDao.cancelSubscription(cur, cancelEvent, internalCallContextFactory.createInternalCallContext(cur.getId(), ObjectType.SUBSCRIPTION, context), 0);
                cur.rebuildTransitions(repairDao.getEventsForSubscription(cur.getId(), internalCallContextFactory.createInternalTenantContext(context)), catalogService.getFullCatalog());
            }
        }
    }

    private void checkAddonRights(final SubscriptionDataRepair baseSubscription)
            throws SubscriptionBaseApiException, CatalogApiException {
        if (getCategory() == ProductCategory.ADD_ON) {
            addonUtils.checkAddonCreationRights(baseSubscription, getCurrentPlan());
        }
    }

    public List<SubscriptionBaseEvent> getEvents() {
        return events;
    }

    public List<SubscriptionBaseEvent> getInitialEvents() {
        return initialEvents;
    }

    public Collection<SubscriptionBaseEvent> getNewEvents() {
        return Collections2.filter(events, new Predicate<SubscriptionBaseEvent>() {
            @Override
            public boolean apply(final SubscriptionBaseEvent input) {
                return !initialEvents.contains(input);
            }
        });
    }
}
