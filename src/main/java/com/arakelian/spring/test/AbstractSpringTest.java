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

package com.arakelian.spring.test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import com.arakelian.spring.test.rule.OrderedRules;
import com.arakelian.spring.test.rule.SpringClassRule;
import com.arakelian.spring.test.rule.SpringMethodRule;
import com.google.common.base.CaseFormat;
import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;

@ActiveProfiles(resolver = ImprovedActiveProfilesResolver.class, profiles = { "test" })
public abstract class AbstractSpringTest {
    /**
     * Alternative to using SpringJUnit4ClassRunner which allows us to use other runners, like
     * JUnit's Parameterized, or JUnitParamsRunner. This rule enables the Spring-ified use
     * of @BeforeClass, @AfterClass, and @ProfileValueSourceConfiguration, @IfProfileValue.
     **/
    @ClassRule
    public static final OrderedRules ORDERED_RULES = OrderedRules.newRules();

    /** Must have this field exposed for SpringMethodRule **/
    public static final SpringClassRule SPRING_CLASS_RULE = ORDERED_RULES.add(new SpringClassRule(), 100);

    /** Log4J2 configuration file **/
    private static final String LOG4J_CONFIG_UNIT_TESTS = "log4j2-test.xml";

    /** Log4J configuration class **/
    private static final String LOG4J_CONFIGURATOR_CLASS = "org.apache.logging.log4j.core.config.Configurator";

    /**
     * Reads a resource file into a String. The resource file is assumed to be the simple name of
     * this class, concatenated with a user given suffix.
     *
     * @param suffix
     *            suffix to add to the simple class name
     * @param clazz
     *            class
     * @return resource file as String
     */
    public static String getClassResourceAsString(final String suffix, final Class<?> clazz) {
        final String base = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazz.getSimpleName());
        final String name = base + suffix;
        return getResourceAsString(name, clazz, true);
    }

    /**
     * Reads the resource file with the given name and returns it as a String.
     *
     * @param name
     *            resource name, assumed to be relative to given class
     * @param contextClass
     *            context class for resource loading
     * @param required
     *            true if resource must be found
     * @return resource file as String
     */
    public static String getResourceAsString(
            final String name,
            final Class<?> contextClass,
            final boolean required) {
        Preconditions.checkArgument(!StringUtils.isEmpty(name), "name must be non-empty");
        Preconditions.checkArgument(contextClass != null, "contextClass must be non-null");
        URL url = null;
        try {
            url = Resources.getResource(contextClass, name);
        } catch (final IllegalArgumentException e) {
            if (required) {
                throw e;
            }
        }
        try {
            final String text = Resources.toString(url, Charsets.UTF_8);
            return text;
        } catch (final IOException e) {
            throw new IllegalStateException("Cannot find resource: \"" + name + "\"", e);
        }
    }

    /**
     * Alternative to using SpringJUnit4ClassRunner which allows us to use other runners, like
     * JUnit's Parameterized, or JUnitParamsRunner. This rule enables the Spring-ified use
     * of @Before, @After, @Repeat, @Timeout, @ProfileValueSourceConfiguration, @IfProfileValue
     **/
    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private Environment environment;

    /** Active Spring profiles **/
    private Set<String> activeProfiles;

    public AbstractSpringTest() {
        final ClassLoader loader = MoreObjects.firstNonNull( //
                Thread.currentThread().getContextClassLoader(), //
                Resources.class.getClassLoader());

        final URL url = loader.getResource(LOG4J_CONFIG_UNIT_TESTS);
        if (url != null) {
            Class<?> clazz;
            try {
                clazz = Class.forName(LOG4J_CONFIGURATOR_CLASS);
            } catch (final ClassNotFoundException e1) {
                // LOG4J not available
                return;
            }

            try {
                // equivalent to Configurator.initialize(null, loader, url.toURI());
                final Method method = clazz
                        .getMethod("initialize", String.class, ClassLoader.class, URI.class);
                method.invoke(null, null, loader, url.toURI());
            } catch (final Exception e) {
                throw new RuntimeException("Unable to initialize Log4J", e);
            }
        }
    }

    /**
     * Ensure that we compute active profiles before each test
     */
    @Before
    public final void configureActiveProfiles() {
        activeProfiles = Sets.newHashSet(getEnvironment().getActiveProfiles());
    }

    public final Set<String> getActiveProfiles() {
        return activeProfiles;
    }

    public final Environment getEnvironment() {
        return environment;
    }

    /**
     * Reads a resource file into a String. The resource file is assumed to be the simple name of
     * this class, concatenated with a user given suffix.
     *
     * @param suffix
     *            suffix to add to the simple class name
     * @return resource file as String
     */
    protected final String getClassResourceAsString(final String suffix) {
        final Class<?> clazz = this.getClass();
        return getClassResourceAsString(suffix, clazz);
    }

    /**
     * Reads the resource file with the given name and returns it as a String.
     *
     * @param name
     *            resource name, assumed to be relative to current class
     * @param required
     *            true if resource must be found
     * @return resource file as String
     */
    protected final String getResourceAsString(final String name, final boolean required) {
        final Class<?> clazz = this.getClass();
        return getResourceAsString(name, clazz, required);
    }
}
