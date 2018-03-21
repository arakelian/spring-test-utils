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

import javax.servlet.ServletContext;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;

import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.specification.RequestSpecification;

public abstract class AbstractSpringBootTest extends AbstractSpringTest {
    @LocalServerPort
    protected int port;

    @Autowired
    private ServletContext servletContext;

    /**
     * The method is invoked by JUnit before each test is executed; here, we configure RestAssured
     * wit the port that Spring MVC is listening on.
     */
    @Before
    public final void configureRestAssured() {
        // make sure RestAssured knows which port the app is listening on
        RestAssured.port = port;

        // tell RestAssured to log everything
        RestAssured.config = RestAssured.config()
                .logConfig(new LogConfig().enableLoggingOfRequestAndResponseIfValidationFails());
    }

    public final ServletContext getServletContext() {
        return servletContext;
    }

    protected final String buildUrl(final String... pieces) {
        final StringBuilder buf = new StringBuilder() //
                .append("http://") //
                .append("localhost").append(':').append(port) //
                .append(servletContext.getContextPath());
        for (int i = 0, size = pieces != null ? pieces.length : 0; i < size; i++) {
            buf.append(pieces[i]);
        }
        final String url = buf.toString();
        return url;
    }

    /**
     * Convenience method to create a new RestAssured request.
     *
     * @return new RestAssured request
     */
    protected RequestSpecification newRequest() {
        // this method will allow us to configure logging or filtering for all tests in the future
        return RestAssured.given();
    }
}
