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

package com.arakelian.spring.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.arakelian.spring.test.annotation.WithRunListener;

public class RunListenerSpringJUnit4ClassRunner extends SpringJUnit4ClassRunner {

    private final List<RunListener> runListeners = new ArrayList<>();

    /**
     * Constructs a new {@code SpringJUnit4ClassRunner} and initializes a {@link TestContextManager}
     * to provide Spring testing functionality to standard JUnit tests.
     *
     * @param clazz
     *            the test class to be run
     * @throws InitializationError
     *             if exceptions occurs registering listeners
     */
    public RunListenerSpringJUnit4ClassRunner(Class<?> clazz) throws InitializationError {
        super(clazz);

        while (clazz != null) {
            final WithRunListener annotation = clazz.getAnnotation(WithRunListener.class);
            if (annotation != null) {
                try {
                    final RunListener runListener = annotation.value().getConstructor().newInstance();
                    runListeners.add(runListener);
                } catch (final Exception e) {
                    throw new InitializationError(e);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    @Override
    public void run(final RunNotifier notifier) {
        for (final RunListener runListener : runListeners) {
            notifier.addListener(runListener);
        }

        super.run(notifier);
    }
}
