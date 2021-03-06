/*
 * Copyright (c) 2008-2017, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.map.impl.querycache.subscriber.record;

import com.hazelcast.nio.serialization.Data;
import com.hazelcast.spi.serialization.SerializationService;

/**
 * Represents a record with {@link Data} key and value.
 */
class DataQueryCacheRecord extends AbstractQueryCacheRecord {

    private final Data keyData;

    private final Data valueData;

    private final SerializationService serializationService;

    public DataQueryCacheRecord(Data keyData, Data valueData, SerializationService serializationService) {
        this.keyData = keyData;
        this.valueData = valueData;
        this.serializationService = serializationService;
    }

    @Override
    public Object getValue() {
        return serializationService.toObject(valueData);
    }

    @Override
    public final Data getKey() {
        return keyData;
    }
}
