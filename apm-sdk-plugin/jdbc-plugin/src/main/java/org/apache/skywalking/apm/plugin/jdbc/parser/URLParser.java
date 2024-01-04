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
package org.apache.skywalking.apm.plugin.jdbc.parser;


import org.apache.skywalking.apm.plugin.jdbc.ConnectionInfo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class URLParser {
    private static final Logger log = Logger.getLogger(URLParser.class.getName());

    private static final String MYSQL_JDBC_URL_PREFIX = "jdbc:mysql";
    private static final String MARIADB_JDBC_URL_PREFIX = "jdbc:mariadb";
    private static final Map<String, ConnectionURLParser> parserRegister = new LinkedHashMap<>();

    static {
        // put mysql parser firstly
        parserRegister.put(MYSQL_JDBC_URL_PREFIX, new MysqlURLParser());
        parserRegister.put(MARIADB_JDBC_URL_PREFIX, new MariadbURLParser());
    }

    /**
     * parse the url to the ConnectionInfo
     */
    public static ConnectionInfo parser(String url) {
        if (null == url) {
            return ConnectionInfo.UNKNOWN_CONNECTION_INFO;
        }
        String lowerCaseUrl = url.toLowerCase();
        ConnectionURLParser parser = findURLParser(lowerCaseUrl);
        if (parser == null) {
            return ConnectionInfo.UNKNOWN_CONNECTION_INFO;
        }
        try {
            return parser.parse(url);
        } catch (Exception e) {
            log.log(Level.WARNING, "error occurs when parsing jdbc url");
        }
        return ConnectionInfo.UNKNOWN_CONNECTION_INFO;
    }

    private static ConnectionURLParser findURLParser(String lowerCaseUrl) {
        for (Map.Entry<String, ConnectionURLParser> entry : parserRegister.entrySet()) {
            if (lowerCaseUrl.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * register new ConnectionURLParser. Can override existing parser.
     */
    public static void registerConnectionParser(String urlPrefix, ConnectionURLParser parser) {
        if (null == urlPrefix || parser == null) {
            throw new IllegalArgumentException("urlPrefix and parser can not be null");
        }
        parserRegister.put(urlPrefix.toLowerCase(), parser);
    }
}
