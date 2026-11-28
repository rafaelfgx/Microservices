package com.company.starter.logging;

public interface ApplicationLogger {
    LevelStep at();

    interface LevelStep {
        MarkerStep info();

        MarkerStep warn();

        MarkerStep error();
    }

    interface MarkerStep {
        MessageStep marker(final String marker);
    }

    interface MessageStep {
        OptionalStep message(final String message);
    }

    interface OptionalStep extends LogStep {
        OptionalStep attribute(final String key, final Object value);

        AfterExceptionStep exception(final Throwable exception);

        LogStep enabled(final boolean enabled);
    }

    interface AfterExceptionStep extends LogStep {
        AfterExceptionStep attribute(final String key, final Object value);

        LogStep enabled(final boolean enabled);
    }

    interface LogStep {
        void log();
    }
}
