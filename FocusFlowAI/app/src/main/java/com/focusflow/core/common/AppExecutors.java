package com.focusflow.core.common;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class AppExecutors {

    private final ScheduledExecutorService diskIo;
    private final Executor mainThread;

    public AppExecutors() {
        this(
                Executors.newSingleThreadScheduledExecutor(),
                new MainThreadExecutor()
        );
    }

    AppExecutors(ScheduledExecutorService diskIo, Executor mainThread) {
        this.diskIo = diskIo;
        this.mainThread = mainThread;
    }

    public ScheduledExecutorService diskIo() {
        return diskIo;
    }

    public Executor mainThread() {
        return mainThread;
    }

    private static final class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    }
}
