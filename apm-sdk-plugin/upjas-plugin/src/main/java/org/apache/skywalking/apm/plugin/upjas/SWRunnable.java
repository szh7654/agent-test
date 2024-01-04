package org.apache.skywalking.apm.plugin.upjas;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;

public class SWRunnable implements Runnable {

    private static final String OPERATION_NAME = "UndertowDispatch";

    private Runnable runnable;

    private ContextSnapshot snapshot;

    public SWRunnable(Runnable runnable, ContextSnapshot snapshot) {
        this.runnable = runnable;
        this.snapshot = snapshot;
    }

    @Override
    public void run() {
        AbstractSpan span = ContextManager.createLocalSpan(SWRunnable.OPERATION_NAME);
        span.setComponent(ComponentsDefine.UNDERTOW);
        try {
            ContextManager.continued(snapshot);
            runnable.run();
        } finally {
            ContextManager.stopSpan(span);
        }
    }
}
