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

import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;
import org.apache.skywalking.apm.network.trace.component.OfficialComponent;

public class ConnectionInfo {

    public static ConnectionInfo UNKNOWN_CONNECTION_INFO = new ConnectionInfo("unknown_type",
            "unknown_user", "unknown_name", "unknown_host", -1);

    private final String dbType;
    private final String dbUser;
    private final String dbName;
    private final String dbPeer;
    private final String dbPeerService;

    public ConnectionInfo(String dbType, String dbUser, String dbName, String dbHost,
                          Integer dbPort) {
        this.dbType = dbType;
        this.dbUser = dbUser;
        this.dbName = dbName;
        if (dbHost != null && dbPort != null) {
            this.dbPeer = dbHost + ":" + dbPort;
        } else {
            this.dbPeer = "";
        }

        this.dbPeerService = makePeerService();
    }

    public ConnectionInfo(String dbType, String dbUser, String dbName, String dbPeer) {
        this.dbType = dbType;
        this.dbUser = dbUser;
        this.dbName = dbName;
        this.dbPeer = dbPeer;

        this.dbPeerService = makePeerService();
    }

    /**
     * Make a unique serviceName that could be used in dependency diagram.
     */
    private String makePeerService() {
        if (null != dbName && !dbName.isEmpty()) {
            return dbName + "[" + dbType + "(" + dbPeer + ")]";
        } else {
            return dbType + "(" + dbPeer + ")";
        }
    }

    public OfficialComponent getComponent() {
        return ComponentsDefine.MSSQL_JDBC_DRIVER;
    }

    public String getDbType() {
        return dbType;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbPeer() {
        return dbPeer;
    }

    public String getPeerService() {
        return dbPeerService;
    }
}
