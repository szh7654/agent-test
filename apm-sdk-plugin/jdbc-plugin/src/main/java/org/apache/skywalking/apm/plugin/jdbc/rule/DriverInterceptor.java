package org.apache.skywalking.apm.plugin.jdbc.rule;

import com.unionpay.upagent.initialize.proxy.WrapperProxy;
import net.bytebuddy.asm.Advice;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.plugin.jdbc.ConnectionInfo;
import org.apache.skywalking.apm.plugin.jdbc.TracingConnection;
import org.apache.skywalking.apm.plugin.jdbc.parser.URLParser;

import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class DriverInterceptor {
    public static final ILog LOGGER = LogManager.getLogger(DriverInterceptor.class);

    public static final AtomicReference<Driver> tracingDriver = new AtomicReference<Driver>();

    @Advice.OnMethodEnter(inline = true)
    public static ConnectionInfo enter(final @Advice.Argument(0) String url,
                                       final @Advice.Argument(1) Properties info) throws Exception {
        final ConnectionInfo connectionInfo = URLParser.parser(url);
        return connectionInfo;
    }

    @Advice.OnMethodExit(inline = true)
    public static void exit(@Advice.Enter final ConnectionInfo connectionInfo,
                            @Advice.Return(readOnly = false) Connection connection) {
        LOGGER.info("connection: {}", connection);
        if (connection == null) {
            return;
        }
        connection = WrapperProxy.wrap(connection, new TracingConnection(connection, connectionInfo));
    }
}
