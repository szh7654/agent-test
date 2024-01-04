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

public class MysqlURLParser extends AbstractURLParser {

    private static final int DEFAULT_PORT = 3306;

    private static final String DB_TYPE = "mysql";

    private static final String DEFAULT_USER = "defaultUser";

    @Override
    protected URLLocation fetchDatabaseHostsIndexRange(String url) {
        int hostLabelStartIndex = url.indexOf("//") + 2;
        int hostLabelEndIndex = url.indexOf("/", hostLabelStartIndex);
        if (hostLabelEndIndex == -1) {
            int queryStringStartIndex = url.indexOf("?", hostLabelStartIndex);
            if (queryStringStartIndex == -1) {
                hostLabelEndIndex = url.length();
            } else {
                hostLabelEndIndex = queryStringStartIndex;
            }
        }
        return new URLLocation(hostLabelStartIndex, hostLabelEndIndex);
    }

    protected String fetchDatabaseNameFromURL(String url, int startSize) {
        URLLocation hostsLocation = fetchDatabaseNameIndexRange(url, startSize);
        if (hostsLocation == null) {
            return "";
        }
        return url.substring(hostsLocation.startIndex(), hostsLocation.endIndex());
    }

    protected URLLocation fetchDatabaseNameIndexRange(String url, int startSize) {
        int databaseStartTag = url.indexOf("/", startSize);
        if (databaseStartTag == -1) {
            return null;
        }
        int databaseEndTag = url.indexOf("?", databaseStartTag);
        if (databaseEndTag == -1) {
            databaseEndTag = url.length();
        }
        return new URLLocation(databaseStartTag + 1, databaseEndTag);
    }

    @Override
    protected URLLocation fetchDatabaseNameIndexRange(String url) {
        int databaseStartTag = url.lastIndexOf("/");
        int databaseEndTag = url.indexOf("?", databaseStartTag);
        if (databaseEndTag == -1) {
            databaseEndTag = url.length();
        }
        return new URLLocation(databaseStartTag + 1, databaseEndTag);
    }

    @Override
    public ConnectionInfo parse(String url) {
        URLLocation location = fetchDatabaseHostsIndexRange(url);
        String hosts = url.substring(location.startIndex(), location.endIndex());
        String[] hostSegment = hosts.split(",");
        if (hostSegment.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (String host : hostSegment) {
                if (host.split(":").length == 1) {
                    sb.append(host + ":" + DEFAULT_PORT + ",");
                } else {
                    sb.append(host + ",");
                }
            }
            if (',' == sb.charAt(sb.length() - 1)) {
                sb.deleteCharAt(sb.length() - 1);
            }
            String dbName = fetchDatabaseNameFromURL(url);
            String dbPeer = sb.toString();
            return new ConnectionInfo(DB_TYPE, DEFAULT_USER, dbName, dbPeer);
        } else {
            String[] hostAndPort = hostSegment[0].split(":");
            String dbName = fetchDatabaseNameFromURL(url, location.endIndex());
            String dbHost = hostAndPort[0];
            if (hostAndPort.length != 1) {
                Integer dbPort = Integer.valueOf(hostAndPort[1]);
                return new ConnectionInfo(DB_TYPE, DEFAULT_USER, dbName, dbHost, dbPort);
            } else {
                return new ConnectionInfo(DB_TYPE, DEFAULT_USER, dbName, dbHost, DEFAULT_PORT);
            }
        }
    }

}
