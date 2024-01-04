package org.apache.skywalking.apm.plugin.jdbc;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;

public class JdbcSpanUtils {

    public static void buildSpan(String sql, String operation, ConnectionInfo connectionInfo) {
        String operationName = connectionInfo.getDbType() + "/JDBC/" + operation;
        AbstractSpan span = ContextManager.createExitSpan(operationName, connectionInfo.getDbPeer());
        Tags.DB_TYPE.set(span, connectionInfo.getDbType());
        Tags.DB_INSTANCE.set(span, connectionInfo.getDbName());
        Tags.DB_STATEMENT.set(span, sql);
        span.setComponent(connectionInfo.getComponent());

        SpanLayer.asDB(span);
    }
}
