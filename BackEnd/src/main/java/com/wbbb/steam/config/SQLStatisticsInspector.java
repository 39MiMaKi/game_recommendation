package com.wbbb.steam.config;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import java.util.concurrent.atomic.AtomicLong;

public class SQLStatisticsInspector implements StatementInspector {
    private static final AtomicLong queryCount = new AtomicLong();
    private static final AtomicLong totalTime = new AtomicLong();

    @Override
    public String inspect(String sql) {
        return sql;
    }

    public static void reset() {
        queryCount.set(0);
        totalTime.set(0);
    }

    public static long getAverageTime() {
        return queryCount.get() == 0 ? 0 : totalTime.get() / queryCount.get();
    }
}