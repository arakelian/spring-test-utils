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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.test.context.junit4.statements.ProfileValueChecker;
import org.springframework.test.context.junit4.statements.RunAfterTestClassCallbacks;
import org.springframework.test.context.junit4.statements.RunBeforeTestClassCallbacks;
import org.springframework.util.Assert;

/**
 * Forked from {@link SpringMethodRule} to remove validation code that prevents usage of
 * {@link SpringClassRule} and {@link SpringMethodRule} wihtin ordered rule chains.
 */
public class SpringClassRule implements TestRule {
    private static class TestContextManagerCacheEvictor extends Statement {
        private final Statement next;
        private final Class<?> testClass;

        TestContextManagerCacheEvictor(final Statement next, final Class<?> testClass) {
            this.next = next;
            this.testClass = testClass;
        }

        @Override
        public void evaluate() throws Throwable {
            try {
                next.evaluate();
            } finally {
                CACHE.remove(testClass);
            }
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringClassRule.class);

    private static final Map<Class<?>, TestContextManager> CACHE = new ConcurrentHashMap<>();

    static TestContextManager getTestContextManager(final Class<?> testClass) {
        Assert.notNull(testClass, "testClass must not be null");
        synchronized (CACHE) {
            TestContextManager testContextManager = CACHE.get(testClass);
            if (testContextManager == null) {
                testContextManager = new TestContextManager(testClass);
                CACHE.put(testClass, testContextManager);
            }
            return testContextManager;
        }
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        final Class<?> testClass = description.getTestClass();
        LOGGER.debug("Applying SpringClassRule to test class [{}]", testClass.getName());

        final TestContextManager testContextManager = getTestContextManager(testClass);
        Statement statement = base;
        statement = new RunBeforeTestClassCallbacks(statement, testContextManager);
        statement = new RunAfterTestClassCallbacks(statement, testContextManager);
        statement = new ProfileValueChecker(statement, testClass, null);
        statement = new TestContextManagerCacheEvictor(statement, testClass);
        return statement;
    }
}
