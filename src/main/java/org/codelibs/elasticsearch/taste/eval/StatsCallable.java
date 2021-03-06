/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codelibs.elasticsearch.taste.eval;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.codelibs.elasticsearch.taste.common.RunningAverageAndStdDev;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class StatsCallable implements Callable<Void> {

    private static final Logger log = LoggerFactory
            .getLogger(StatsCallable.class);

    private final Callable<Void> delegate;

    private final boolean logStats;

    private final RunningAverageAndStdDev timing;

    private final AtomicInteger noEstimateCounter;

    StatsCallable(final Callable<Void> delegate, final boolean logStats,
            final RunningAverageAndStdDev timing,
            final AtomicInteger noEstimateCounter) {
        this.delegate = delegate;
        this.logStats = logStats;
        this.timing = timing;
        this.noEstimateCounter = noEstimateCounter;
    }

    @Override
    public Void call() throws Exception {
        final long start = System.currentTimeMillis();
        delegate.call();
        final long end = System.currentTimeMillis();
        timing.addDatum(end - start);
        if (logStats) {
            final Runtime runtime = Runtime.getRuntime();
            final int average = (int) timing.getAverage();
            log.info("Average time per recommendation: {}ms", average);
            final long totalMemory = runtime.totalMemory();
            final long memory = totalMemory - runtime.freeMemory();
            log.info("Approximate memory used: {}MB / {}MB", memory / 1000000L,
                    totalMemory / 1000000L);
            log.info("Unable to recommend in {} cases", noEstimateCounter.get());
        }
        return null;
    }

}
