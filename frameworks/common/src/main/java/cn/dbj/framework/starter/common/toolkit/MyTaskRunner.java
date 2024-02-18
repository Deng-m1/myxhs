package cn.dbj.framework.starter.common.toolkit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyTaskRunner {
    private boolean hasError;

    public static MyTaskRunner newTaskRunner() {
        return new MyTaskRunner();
    }

    public void run(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            log.error("Failed to run task: ", t);
            hasError = true;
        }
    }

    public boolean isHasError() {
        return hasError;
    }

}
