/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.receiver.service;

import com.navercorp.pinpoint.profiler.context.ActiveTrace;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCount;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCountRes;

import org.apache.thrift.TBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

/**
 * @author Taejin Koo
 */
public class ActiveThreadServiceTest {

    // defence weakReference
    private List<ActiveTrace> weakList;
    private long activeTraceId = 0;

    private static final int FAST_COUNT = 1;
    private static final long FAST_EXECUTION_TIME = 500;

    private static final int NORMAL_COUNT = 2;
    private static final long NORMAL_EXECUTION_TIME = 1500;

    private static final int SLOW_COUNT = 3;
    private static final long SLOW_EXECUTION_TIME = 3500;

    private static final int VERY_SLOW_COUNT = 4;
    private static final long VERY_SLOW_EXECUTION_TIME = 5500;

    @Before
    public void setUp() throws Exception {
        this.weakList = new ArrayList<ActiveTrace>();
    }

    @Test
    public void serviceTest1() throws InterruptedException {
        ActiveTraceRepository activeTraceRepository = new ActiveTraceRepository();

        addActiveTrace(activeTraceRepository, FAST_EXECUTION_TIME, FAST_COUNT);
        addActiveTrace(activeTraceRepository, NORMAL_EXECUTION_TIME, NORMAL_COUNT);
        addActiveTrace(activeTraceRepository, SLOW_EXECUTION_TIME, SLOW_COUNT);
        addActiveTrace(activeTraceRepository, VERY_SLOW_EXECUTION_TIME, VERY_SLOW_COUNT);

        ActiveThreadCountService service = new ActiveThreadCountService(activeTraceRepository);
        TBase<?, ?> tBase = service.requestCommandService(new TCmdActiveThreadCount());
        if (tBase instanceof TCmdActiveThreadCountRes) {
            List<Integer> activeThreadCount = ((TCmdActiveThreadCountRes) tBase).getActiveThreadCount();
            Assert.assertThat(activeThreadCount.get(0), is(FAST_COUNT));
            Assert.assertThat(activeThreadCount.get(1), is(NORMAL_COUNT));
            Assert.assertThat(activeThreadCount.get(2), is(SLOW_COUNT));
            Assert.assertThat(activeThreadCount.get(3), is(VERY_SLOW_COUNT));
        } else {
            Assert.fail();
        }
    }

    private void addActiveTrace(ActiveTraceRepository activeTraceRepository, long executionTime, int addCount) {
        for (int i = 0; i < addCount; i++) {
            ActiveTrace activeTrace = createActiveTrace(executionTime);
            this.weakList.add(activeTrace);
            activeTraceRepository.put(activeTrace);
        }
    }

    private ActiveTrace createActiveTrace(long executionTime) {
        ActiveTrace activeTrace = Mockito.mock(ActiveTrace.class);
        Mockito.when(activeTrace.getStartTime()).thenReturn(System.currentTimeMillis() - executionTime);
        Mockito.when(activeTrace.getId()).thenReturn(nextLocalTransactionId());
        return activeTrace;
    }

    private long nextLocalTransactionId() {
        return activeTraceId++;
    }

}
