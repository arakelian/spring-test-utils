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

import static org.springframework.test.util.MetaAnnotationUtils.findAnnotationDescriptor;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ActiveProfilesResolver;
import org.springframework.test.util.MetaAnnotationUtils.AnnotationDescriptor;
import org.springframework.util.Assert;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

public class ImprovedActiveProfilesResolver implements ActiveProfilesResolver {
    /** Logger **/
    private static final Logger LOGGER = LoggerFactory.getLogger(ImprovedActiveProfilesResolver.class);

    /** Comma-separated string splitter **/
    private static final Splitter COMMA_SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();

    @Override
    public String[] resolve(final Class<?> testClass) {
        Assert.notNull(testClass, "Class must not be null");

        final Set<String> activeProfiles = Sets.newLinkedHashSet();

        // load environment profile
        final String envProfiles = System.getenv("spring.profiles.active");
        if (!StringUtils.isEmpty(envProfiles)) {
            LOGGER.trace("Active profiles in environment properties: {}", envProfiles);
            activeProfiles.addAll(COMMA_SPLITTER.splitToList(envProfiles));
        }

        // load system profiles
        final String sysProfiles = System.getProperty("spring.profiles.active");
        if (!StringUtils.isEmpty(sysProfiles)) {
            LOGGER.trace("Active profiles in system properties: {}", sysProfiles);
            activeProfiles.addAll(COMMA_SPLITTER.splitToList(sysProfiles));
        }

        // append @ActiveProfiles annotations
        final AnnotationDescriptor<ActiveProfiles> descriptor = findAnnotationDescriptor(
                testClass,
                ActiveProfiles.class);
        if (descriptor != null) {
            final ActiveProfiles annotation = descriptor.synthesizeAnnotation();
            final String[] profiles = annotation.profiles();
            LOGGER.trace(
                    "Active profiles in {} (via @ActiveProfiles): {}",
                    testClass.getSimpleName(),
                    Joiner.on(", ").join(profiles));
            for (final String profile : profiles) {
                if (!StringUtils.isEmpty(profile)) {
                    activeProfiles.add(profile.trim());
                }
            }
        }

        LOGGER.trace("Merged active profiles: {}", activeProfiles);
        return activeProfiles.toArray(new String[activeProfiles.size()]);
    }
}
