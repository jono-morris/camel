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
package org.apache.camel.management;

import java.util.Set;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.apache.camel.management.DefaultManagementObjectNameStrategy.TYPE_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisabledOnOs(OS.AIX)
public class ManagedTimerTest extends ManagementTestSupport {

    @Test
    public void testTimer() throws Exception {
        MBeanServer mbeanServer = getMBeanServer();

        ObjectName name = getCamelObjectName(TYPE_ENDPOINT, "timer://foo\\?delay=5000&period=8000");
        assertTrue(mbeanServer.isRegistered(name), "Should be registered");

        Long period = (Long) mbeanServer.getAttribute(name, "Period");
        assertEquals(8000, period.longValue());

        String camelId = (String) mbeanServer.getAttribute(name, "CamelId");
        assertEquals(context.getManagementName(), camelId);

        // change period and delay
        mbeanServer.setAttribute(name, new Attribute("Period", 500));
        mbeanServer.setAttribute(name, new Attribute("Delay", 250));

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.reset();

        // restart consumer
        Set<ObjectName> set = mbeanServer.queryNames(new ObjectName("*:type=consumers,*"), null);
        assertEquals(1, set.size());
        ObjectName on = set.iterator().next();
        mbeanServer.invoke(on, "stop", null, null);
        mbeanServer.invoke(on, "start", null, null);

        // Take the time to check the service is started to help avoid
        // sporadic failure on slower machines.
        String state = (String) mbeanServer.getAttribute(on, "State");
        assertEquals(org.apache.camel.ServiceStatus.Started.name(), state, "Should be started");

        // start and we should be done in at most 3 second
        mock.expectedMinimumMessageCount(3);
        mock.setResultWaitTime(3900);

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("timer://foo?delay=5000&period=8000").to("mock:result");
            }
        };
    }

}
