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

package com.ning.billing.util.entity;

import com.ning.billing.util.callcontext.CallContext;
import com.ning.billing.util.customfield.CustomField;
import com.ning.billing.util.customfield.Customizable;
import com.ning.billing.util.customfield.DefaultFieldStore;
import com.ning.billing.util.customfield.FieldStore;
import com.ning.billing.util.tag.DefaultTagStore;
import com.ning.billing.util.tag.DescriptiveTag;
import com.ning.billing.util.tag.Tag;
import com.ning.billing.util.tag.TagDefinition;
import com.ning.billing.util.tag.TagStore;
import com.ning.billing.util.tag.Taggable;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public abstract class ExtendedEntityBase extends EntityBase implements Customizable, Taggable {
    protected final FieldStore fields;
    protected final TagStore tags;

    public ExtendedEntityBase() {
        super();
        this.fields = DefaultFieldStore.create(getId(), getObjectName());
        this.tags = new DefaultTagStore(id, getObjectName());
    }

    public ExtendedEntityBase(final UUID id, @Nullable final String createdBy, @Nullable final DateTime createdDate) {
        super(id, createdBy, createdDate);
        this.fields = DefaultFieldStore.create(getId(), getObjectName());
        this.tags = new DefaultTagStore(id, getObjectName());
    }

    @Override
    public String getFieldValue(final String fieldName) {
        return fields.getValue(fieldName);
    }

    @Override
    public void setFieldValue(final String fieldName, final String fieldValue) {
        fields.setValue(fieldName, fieldValue);
    }

    @Override
    public List<CustomField> getFieldList() {
        return fields.getEntityList();
    }

    @Override
    public void setFields(final List<CustomField> fields) {
        if (fields != null) {
            this.fields.add(fields);
        }
    }

    @Override
    public void clearFields() {
        fields.clear();
    }

    @Override
	public List<Tag> getTagList() {
		return tags.getEntityList();
	}

	@Override
	public boolean hasTag(final String tagName) {
		return tags.containsTag(tagName);
	}

	@Override
	public void addTag(final TagDefinition definition) {
		Tag tag = new DescriptiveTag(definition);
		tags.add(tag) ;
	}

	@Override
	public void addTags(final List<Tag> tags) {
		if (tags != null) {
			this.tags.add(tags);
		}
	}

	@Override
	public void clearTags() {
		this.tags.clear();
	}

	@Override
	public void removeTag(final TagDefinition definition) {
		tags.remove(definition.getName());
	}

	@Override
	public boolean generateInvoice() {
		return tags.generateInvoice();
	}

	@Override
	public boolean processPayment() {
		return tags.processPayment();
	}

    @Override
    public abstract String getObjectName();

    @Override
    public abstract void saveFieldValue(String fieldName, String fieldValue, CallContext context);

    @Override
    public abstract void saveFields(List<CustomField> fields, CallContext context);

    @Override
    public abstract void clearPersistedFields(CallContext context);
}