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
package org.apache.camel.processor.aggregator;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;

public class AggregationStrategyCompleteByPropertyTest extends ContextTestSupport {

    @Test
    public void testAggregateCompletionAware() throws Exception {
        MockEndpoint result = getMockEndpoint("mock:aggregated");
        result.expectedBodiesReceived("A+B+C", "X+Y+ZZZZ");
        result.message(0).exchangeProperty(Exchange.AGGREGATED_COMPLETED_BY).isEqualTo("strategy");
        result.message(1).exchangeProperty(Exchange.AGGREGATED_COMPLETED_BY).isEqualTo("strategy");

        // org.apache.camel.builder.ExpressionBuilder.headerExpression(java.lang.String) is going to property fallback
        // the test (without the fix) will fail into error:
        // java.lang.AssertionError: Assertion error at index 0 on mock mock://aggregated with predicate:
        // header(CamelAggregationCompleteCurrentGroup) is null evaluated as: true is null on Exchange[ID-MacBook-Pro-1578822701664-0-2]
        getMockEndpoint("mock:aggregated").allMessages().header(Exchange.AGGREGATION_COMPLETE_CURRENT_GROUP).isNull();
        // according to manual
        getMockEndpoint("mock:aggregated").allMessages().exchangeProperty(Exchange.AGGREGATION_COMPLETE_CURRENT_GROUP).isNull();

        template.sendBodyAndHeader("direct:start", "A", "id", 123);
        template.sendBodyAndHeader("direct:start", "B", "id", 123);
        template.sendBodyAndHeader("direct:start", "C", "id", 123);
        template.sendBodyAndHeader("direct:start", "X", "id", 123);
        template.sendBodyAndHeader("direct:start", "Y", "id", 123);
        template.sendBodyAndHeader("direct:start", "ZZZZ", "id", 123);

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start").aggregate(header("id"), new MyCompletionStrategy()).completionTimeout(1000)
                        .to("mock:aggregated");
            }
        };
    }

    private static final class MyCompletionStrategy implements AggregationStrategy {

        @Override
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            if (oldExchange == null) {
                return newExchange;
            }

            String body = oldExchange.getIn().getBody(String.class) + "+" + newExchange.getIn().getBody(String.class);
            oldExchange.getIn().setBody(body);
            if (body.length() >= 5) {
                oldExchange.setProperty(Exchange.AGGREGATION_COMPLETE_CURRENT_GROUP, true);
            }
            return oldExchange;
        }
    }
}
