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

package com.arakelian.spring.test.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderedRules implements TestRule {
    private static interface Ordered {
        public int getOrder();
    }

    private static class OrderedRule implements TestRule, Ordered {
        private final TestRule delegate;
        private final int order;

        public OrderedRule(final TestRule delegate, final int order) {
            this.delegate = delegate;
            this.order = order;
        }

        @Override
        public Statement apply(final Statement base, final Description description) {
            return delegate.apply(base, description);
        }

        @Override
        public int getOrder() {
            return order;
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("OrderedRule [");
            if (delegate != null) {
                builder.append("delegate=");
                builder.append(delegate.getClass().getSimpleName());
                builder.append(", ");
            }
            builder.append("order=");
            builder.append(order);
            builder.append("]");
            return builder.toString();
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderedRules.class);

    public static OrderedRules newRules() {
        return new OrderedRules();
    }

    private final List<OrderedRule> rules = new ArrayList<>();

    private OrderedRules() {
    }

    public <T extends TestRule> T add(final T rule, final int order) {
        rules.add(new OrderedRule(rule, order));
        return rule;
    }

    @Override
    public Statement apply(Statement base, final Description description) {
        final List<OrderedRule> sortedRules = new ArrayList<>(rules);
        Collections.sort(sortedRules, new Comparator<OrderedRule>() {
            @Override
            public int compare(final OrderedRule o1, final OrderedRule o2) {
                return Integer.compare(o1.order, o2.order);
            }
        });

        for (final OrderedRule each : sortedRules) {
            LOGGER.info("Executing rule: {}", each);
            base = each.apply(base, description);
        }
        return base;
    }
}
