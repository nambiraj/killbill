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

import org.skife.config.ConfigSource;

import com.ning.billing.overdue.OverdueService;
import com.ning.billing.overdue.glue.DefaultOverdueModule;

public class IntegrationTestOverdueModule extends DefaultOverdueModule {

    public IntegrationTestOverdueModule(final ConfigSource configSource) {
        super(configSource);
    }

    protected void installOverdueService() {
        bind(OverdueService.class).to(MockOverdueService.class);
    }
}
