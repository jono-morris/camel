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
package org.apache.camel.component.language;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Expression;
import org.apache.camel.builder.RouteBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class LanguageNoCacheScriptTest extends ContextTestSupport {

    private LanguageEndpoint endpoint;

    @Test
    public void testNoCache() throws Exception {
        getMockEndpoint("mock:result").expectedBodiesReceived("World", "Camel");

        template.sendBody("direct:start", "World");
        Expression first = endpoint.getExpression();

        template.sendBody("direct:start", "Camel");
        Expression second = endpoint.getExpression();

        assertMockEndpointsSatisfied();

        assertSame(first, second);
        assertNull(first);
        assertNull(second);
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                String script = URLEncoder.encode("Hello ${body}", StandardCharsets.UTF_8);
                endpoint = context.getEndpoint("language:simple:" + script + "?transform=false&cacheScript=false",
                        LanguageEndpoint.class);

                from("direct:start").to(endpoint).to("mock:result");
            }
        };
    }
}
