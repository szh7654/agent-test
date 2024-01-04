/*
 * Copyright 2017-2020 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.skywalking.apm.plugin.jdbc;


import com.unionpay.upagent.initialize.proxy.WrapperProxy;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.plugin.jdbc.parser.URLParser;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TracingDriver implements Driver {
    public static final String IGNORE_FOR_TRACING_REGEX = "ignoreForTracing=\"((?:\\\\\"|[^\"])*)\"[;]*";
    protected static final String TRACE_WITH_ACTIVE_SPAN_ONLY = "traceWithActiveSpanOnly";
    protected static final String WITH_ACTIVE_SPAN_ONLY = TRACE_WITH_ACTIVE_SPAN_ONLY + "=true";
    protected static final Pattern PATTERN_FOR_IGNORING = Pattern.compile(IGNORE_FOR_TRACING_REGEX);
    private static final ILog LOGGER = LogManager.getLogger(TracingDriver.class);
    private static final Driver INSTANCE = new TracingDriver();
    private static boolean withActiveSpanOnly;

    /**
     * 将{@code TracingDriver}注册至{@link DriverManager}
     * 1. 将已注册的driver重新注册
     * 2. 将TracingDriver作为第一个driver
     *
     * @return TracingDriver的单例对象
     */
    public synchronized static Driver load() {
        try {
            final Enumeration<Driver> enumeration = DriverManager.getDrivers();
            List<Driver> drivers = new ArrayList<>();
            for (int i = 0; enumeration.hasMoreElements(); ++i) {
                final Driver driver = enumeration.nextElement();
                if (i == 0) {
                    if (driver == INSTANCE) {
                        return driver;
                    }
                }

                drivers.add(driver);
            }

            if (drivers.size() != 0) {
                for (final Driver driver : drivers) {
                    DriverManager.deregisterDriver(driver);
                }
            }

            DriverManager.registerDriver(INSTANCE);

            if (drivers.size() != 0) {
                for (final Driver driver : drivers) {
                    DriverManager.registerDriver(driver);
                }
            }


        } catch (SQLException e) {
            LOGGER.error(e, "Could not register TracingDriver with DriverManager");
        }
        return INSTANCE;
    }

    /**
     * Sets the {@code withActiveSpanOnly} property for "interceptor mode".
     *
     * @param withActiveSpanOnly The {@code withActiveSpanOnly} value.
     */
    public static void setInterceptorProperty(final boolean withActiveSpanOnly) {
        TracingDriver.withActiveSpanOnly = withActiveSpanOnly;
    }


    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (url == null) {
            throw new SQLException("url is required");
        }

        boolean withActiveSpanOnly = false;

        // find the real driver for the URL
        final Driver wrappedDriver = findDriver(url);
        final ConnectionInfo connectionInfo = URLParser.parser(url);
        Connection connection = null;
        try {
            connection = wrappedDriver.connect(url, info);
        } catch (Exception e) {
            LOGGER.error("获取driver失败", e);
            throw e;
        }


        return WrapperProxy
                .wrap(connection, new TracingConnection(connection, connectionInfo));
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url != null;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return findDriver(url).getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() {
        // There is no way to get it from wrapped driver
        return 1;
    }

    @Override
    public int getMinorVersion() {
        // There is no way to get it from wrapped driver
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return true;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        // There is no way to get it from wrapped driver
        return null;
    }

    protected Driver findDriver(String realUrl) throws SQLException {
        if (realUrl == null || realUrl.trim().length() == 0) {
            throw new IllegalArgumentException("url is required");
        }

        for (Driver candidate : Collections.list(DriverManager.getDrivers())) {
            try {
                if (!(candidate instanceof TracingDriver) && candidate.acceptsURL(realUrl)) {
                    return candidate;
                }
            } catch (SQLException ignored) {
                // intentionally ignore exception
            }
        }

        throw new SQLException("Unable to find a driver that accepts url: " + realUrl);
    }


    protected Set<String> extractIgnoredStatements(String url) {

        final Matcher matcher = PATTERN_FOR_IGNORING.matcher(url);

        Set<String> results = new HashSet<>(8);

        while (matcher.find()) {
            String rawValue = matcher.group(1);
            String finalValue = rawValue.replace("\\\"", "\"");
            results.add(finalValue);
        }

        return results;
    }
}
