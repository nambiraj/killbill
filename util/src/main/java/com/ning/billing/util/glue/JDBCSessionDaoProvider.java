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

package com.ning.billing.util.glue;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.skife.jdbi.v2.IDBI;

import com.ning.billing.util.security.shiro.dao.JDBCSessionDao;

public class JDBCSessionDaoProvider implements Provider<JDBCSessionDao> {

    private final SessionManager sessionManager;
    private final IDBI dbi;

    @Inject
    public JDBCSessionDaoProvider(final IDBI dbi, final SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.dbi = dbi;
    }

    @Override
    public JDBCSessionDao get() {
        final JDBCSessionDao jdbcSessionDao = new JDBCSessionDao(dbi);

        if (sessionManager instanceof DefaultSessionManager) {
            ((DefaultSessionManager) sessionManager).setSessionDAO(jdbcSessionDao);
        }

        return jdbcSessionDao;
    }
}
