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

package com.ning.billing.payment.dao;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import com.ning.billing.payment.api.Refund;
import com.ning.billing.util.audit.ChangeType;
import com.ning.billing.util.callcontext.InternalCallContext;
import com.ning.billing.util.callcontext.InternalTenantContext;
import com.ning.billing.util.entity.dao.Audited;
import com.ning.billing.util.entity.dao.EntitySqlDao;
import com.ning.billing.util.entity.dao.EntitySqlDaoStringTemplate;

@EntitySqlDaoStringTemplate
public interface RefundSqlDao extends EntitySqlDao<RefundModelDao, Refund> {

    @SqlUpdate
    @Audited(ChangeType.UPDATE)
    void updateStatus(@Bind("id") final String refundId,
                      @Bind("refundStatus") final String status,
                      @BindBean final InternalCallContext context);

    @SqlQuery
    List<RefundModelDao> getRefundsForPayment(@Bind("paymentId") final String paymentId,
                                              @BindBean final InternalTenantContext context);

    @SqlQuery
    List<RefundModelDao> getRefundsForAccount(@Bind("accountId") final String accountId,
                                              @BindBean final InternalTenantContext context);
}
