/*
 * Copyright 2012-2017 the original author or authors.
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

package com.arakelian.spring.test.rule;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit4.statements.ProfileValueChecker;
import org.springframework.test.context.junit4.statements.RunAfterTestMethodCallbacks;
import org.springframework.test.context.junit4.statements.RunBeforeTestMethodCallbacks;
import org.springframework.test.context.junit4.statements.RunPrepareTestInstanceCallbacks;
import org.springframework.test.context.junit4.statements.SpringFailOnTimeout;
import org.springframework.test.context.junit4.statements.SpringRepeat;

/**
 * Forked from {@link SpringMethodRule} to remove validation code that prevents usage of
 * {@link SpringClassRule} and {@link SpringMethodRule} wihtin ordered rule chains.
 */
public class SpringMethodRule implements MethodRule {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringMethodRule.class);

    @Override
    public Statement apply(
            final Statement base,
            final FrameworkMethod frameworkMethod,
            final Object testInstance) {
        LOGGER.debug("Applying SpringMethodRule to test method [{}]", frameworkMethod.getMethod());
        final Class<?> testClass = testInstance.getClass();
        final TestContextManager testContextManager = SpringClassRule.getTestContextManager(testClass);

        Statement statement = base;
        statement = new RunBeforeTestMethodCallbacks(statement, testInstance, frameworkMethod.getMethod(),
                testContextManager);
        statement = new RunAfterTestMethodCallbacks(statement, testInstance, frameworkMethod.getMethod(),
                testContextManager);
        statement = new RunPrepareTestInstanceCallbacks(statement, testInstance, testContextManager);
        statement = new SpringRepeat(statement, frameworkMethod.getMethod());
        statement = new SpringFailOnTimeout(statement, frameworkMethod.getMethod());
        statement = new ProfileValueChecker(statement, testInstance.getClass(), frameworkMethod.getMethod());
        return statement;
    }
}
