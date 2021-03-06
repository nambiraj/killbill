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

package com.ning.billing.ovedue.notification;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.joda.time.DateTime;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ning.billing.account.api.Account;
import com.ning.billing.entitlement.api.Blockable;
import com.ning.billing.notificationq.api.NotificationEventWithMetadata;
import com.ning.billing.notificationq.api.NotificationQueue;
import com.ning.billing.overdue.OverdueTestSuiteWithEmbeddedDB;
import com.ning.billing.overdue.service.DefaultOverdueService;
import com.ning.billing.subscription.api.SubscriptionBase;
import com.ning.billing.subscription.api.user.SubscriptionBaseBundle;
import com.ning.billing.util.entity.dao.EntitySqlDao;
import com.ning.billing.util.entity.dao.EntitySqlDaoTransactionWrapper;
import com.ning.billing.util.entity.dao.EntitySqlDaoTransactionalJdbiWrapper;
import com.ning.billing.util.entity.dao.EntitySqlDaoWrapperFactory;
import com.ning.billing.util.jackson.ObjectMapper;

public class TestDefaultOverdueCheckPoster extends OverdueTestSuiteWithEmbeddedDB {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private EntitySqlDaoTransactionalJdbiWrapper entitySqlDaoTransactionalJdbiWrapper;
    private NotificationQueue overdueQueue;
    private DateTime testReferenceTime;

    @Override
    @BeforeMethod(groups = "slow")
    public void beforeMethod() throws Exception {
        super.beforeMethod();
        entitySqlDaoTransactionalJdbiWrapper = new EntitySqlDaoTransactionalJdbiWrapper(dbi, clock, cacheControllerDispatcher, nonEntityDao);

        overdueQueue = notificationQueueService.getNotificationQueue(DefaultOverdueService.OVERDUE_SERVICE_NAME,
                                                                     DefaultOverdueCheckNotifier.OVERDUE_CHECK_NOTIFIER_QUEUE);
        Assert.assertTrue(overdueQueue.isStarted());

        testReferenceTime = clock.getUTCNow();
    }

    @Test(groups = "slow")
    public void testShouldntInsertMultipleNotificationsPerOverdueable() throws Exception {
        final UUID accountId = UUID.randomUUID();
        final Account overdueable = Mockito.mock(Account.class);
        Mockito.when(overdueable.getId()).thenReturn(accountId);

        insertOverdueCheckAndVerifyQueueContent(overdueable, 10, 10);
        insertOverdueCheckAndVerifyQueueContent(overdueable, 5, 5);
        insertOverdueCheckAndVerifyQueueContent(overdueable, 15, 5);

        // Check we don't conflict with other overdueables
        final UUID bundleId = UUID.randomUUID();
        final Account otherOverdueable = Mockito.mock(Account.class);
        Mockito.when(otherOverdueable.getId()).thenReturn(bundleId);

        insertOverdueCheckAndVerifyQueueContent(otherOverdueable, 10, 10);
        insertOverdueCheckAndVerifyQueueContent(otherOverdueable, 5, 5);
        insertOverdueCheckAndVerifyQueueContent(otherOverdueable, 15, 5);

        // Verify the final content of the queue
        Assert.assertEquals(overdueQueue.getFutureNotificationForSearchKey1(OverdueCheckNotificationKey.class, internalCallContext.getAccountRecordId()).size(), 2);
    }

    private void insertOverdueCheckAndVerifyQueueContent(final Account overdueable, final int nbDaysInFuture, final int expectedNbDaysInFuture) throws IOException {
        final DateTime futureNotificationTime = testReferenceTime.plusDays(nbDaysInFuture);
        poster.insertOverdueCheckNotification(overdueable, futureNotificationTime, internalCallContext);

        final OverdueCheckNotificationKey notificationKey = new OverdueCheckNotificationKey(overdueable.getId());
        final Collection<NotificationEventWithMetadata<OverdueCheckNotificationKey>> notificationsForKey = getNotificationsForOverdueable(overdueable);
        Assert.assertEquals(notificationsForKey.size(), 1);
        final NotificationEventWithMetadata nm = notificationsForKey.iterator().next();
        Assert.assertEquals(nm.getEvent(), notificationKey);
        Assert.assertEquals(nm.getEffectiveDate(), testReferenceTime.plusDays(expectedNbDaysInFuture));
    }

    private Collection<NotificationEventWithMetadata<OverdueCheckNotificationKey>> getNotificationsForOverdueable(final Account overdueable) {
        return entitySqlDaoTransactionalJdbiWrapper.execute(new EntitySqlDaoTransactionWrapper<Collection<NotificationEventWithMetadata<OverdueCheckNotificationKey>>>() {
            @Override
            public Collection<NotificationEventWithMetadata<OverdueCheckNotificationKey>> inTransaction(final EntitySqlDaoWrapperFactory<EntitySqlDao> entitySqlDaoWrapperFactory) throws Exception {
                return ((DefaultOverdueCheckPoster) poster).getFutureNotificationsForAccountAndOverdueableInTransaction(entitySqlDaoWrapperFactory, overdueQueue, overdueable, internalCallContext);
            }
        });
    }
}
