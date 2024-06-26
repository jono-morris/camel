/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.impl.event;

import java.io.Serial;
import java.util.EventObject;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.CamelEvent.CamelContextEvent;

/**
 * Base class for {@link CamelContext} events.
 */
public abstract class AbstractContextEvent extends EventObject implements CamelContextEvent {
    private static final @Serial long serialVersionUID = 1L;
    private final CamelContext context;
    private long timestamp;

    public AbstractContextEvent(CamelContext source) {
        super(source);
        this.context = source;
    }

    @Override
    public CamelContext getContext() {
        return context;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
