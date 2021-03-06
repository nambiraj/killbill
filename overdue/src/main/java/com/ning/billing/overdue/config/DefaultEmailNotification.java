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

package com.ning.billing.overdue.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.ning.billing.overdue.EmailNotification;

@XmlAccessorType(XmlAccessType.NONE)
public class DefaultEmailNotification implements EmailNotification {

    @XmlElement(required = true, name = "subject")
    private String subject;

    @XmlElement(required = true, name = "templateName")
    private String templateName;

    @XmlElement(required = false, name = "isHTML")
    private Boolean isHTML = false;

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public String getTemplateName() {
        return templateName;
    }

    @Override
    public Boolean isHTML() {
        return isHTML;
    }
}
