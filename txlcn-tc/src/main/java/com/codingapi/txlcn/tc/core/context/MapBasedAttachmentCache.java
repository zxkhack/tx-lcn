/*
 * Copyright 2017-2019 CodingApi .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codingapi.txlcn.tc.core.context;

import com.codingapi.txlcn.common.util.function.Supplier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description: 基于JDK线程安全的 {@code ConcurrentHashMap} 实现的 {@code AttachmentCache}
 * Date: 19-1-23 下午12:04
 *
 * @author ujued
 * @see AttachmentCache
 */
@Component
public class MapBasedAttachmentCache implements AttachmentCache {

    private Map<String, Map<Object, Object>> cache = new ConcurrentHashMap<>(64);

    private Map<Object, Object> singlePropCache = new ConcurrentHashMap<>(64);

    @Override
    public void attach(String mainKey, Object key, Object attachment) {
        Objects.requireNonNull(mainKey, "mainKey requiredNonNull");
        Objects.requireNonNull(key, "key requiredNonNull");
        Objects.requireNonNull(attachment, "attachment requiredNonNull");

        if (cache.containsKey(mainKey)) {
            Map<Object, Object> map = cache.get(mainKey);
            map.put(key, attachment);
            return;
        }

        Map<Object, Object> map = new HashMap<>();
        map.put(key, attachment);
        cache.put(mainKey, map);
    }

    @Override
    public void attach(Object key, Object attachment) {
        Objects.requireNonNull(key, "key requiredNonNull");
        Objects.requireNonNull(attachment, "attachment requiredNonNull");
        this.singlePropCache.put(key, attachment);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T attachment(String mainKey, Object key) {
        Objects.requireNonNull(key, "key requiredNonNull");
        Objects.requireNonNull(mainKey, "mainKey requiredNonNull");

        if (cache.containsKey(mainKey)) {
            if (cache.get(mainKey).containsKey(key)) {
                return (T) cache.get(mainKey).get(key);
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T attachment(Object key) {
        Objects.requireNonNull(key, "key requiredNonNull");
        return (T) this.singlePropCache.get(key);
    }

    @Override
    public void remove(String mainKey, Object key) {
        Objects.requireNonNull(mainKey, "mainKey requiredNonNull");
        Objects.requireNonNull(key, "key requiredNonNull");
        if (cache.containsKey(mainKey)) {
            cache.get(mainKey).remove(key);
        }
    }

    @Override
    public void removeAll(String mainKey) {
        Objects.requireNonNull(mainKey, "mainKey requiredNonNull");
        this.cache.remove(mainKey);
    }

    @Override
    public boolean containsKey(String mainKey, Object key) {
        Objects.requireNonNull(mainKey, "mainKey requiredNonNull");
        Objects.requireNonNull(key, "key requiredNonNull");
        return cache.containsKey(mainKey) && cache.get(mainKey).containsKey(key);
    }

    @Override
    public boolean containsKey(Object key) {
        Objects.requireNonNull(key, "key requiredNonNull");
        return singlePropCache.containsKey(key);
    }

    @Override
    public Map<Object, Object> attachments(String mainKey) {
        Objects.requireNonNull(mainKey, "mainKey requiredNonNull");
        if (cache.containsKey(mainKey)) {
            return cache.get(mainKey);
        }
        return new HashMap<>(0);
    }

    @Override
    public void remove(Object key) {
        Objects.requireNonNull(key, "key requiredNonNull");
        this.singlePropCache.remove(key);
    }
}
