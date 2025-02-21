/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.streams.state.internals;

import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.internals.SessionWindow;
import org.apache.kafka.streams.processor.internals.ProcessorContextImpl;
import org.apache.kafka.streams.processor.internals.ProcessorRecordContext;
import org.apache.kafka.streams.query.Position;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.SessionStore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.apache.kafka.common.utils.Utils.mkEntry;
import static org.apache.kafka.common.utils.Utils.mkMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class ChangeLoggingSessionBytesStoreTest {

    @Mock
    private SessionStore<Bytes, byte[]> inner;
    @Mock
    private ProcessorContextImpl context;

    private ChangeLoggingSessionBytesStore store;
    private final byte[] value1 = {0};
    private final Bytes bytesKey = Bytes.wrap(value1);
    private final Windowed<Bytes> key1 = new Windowed<>(bytesKey, new SessionWindow(0, 0));

    private static final Position POSITION = Position.fromMap(mkMap(mkEntry("", mkMap(mkEntry(0, 1L)))));

    @BeforeEach
    public void setUp() {
        store = new ChangeLoggingSessionBytesStore(inner);
        store.init(context, store);
    }

    @AfterEach
    public void tearDown() {
        verify(inner).init(context, store);
    }

    @Test
    public void shouldDelegateInit() {
        // testing the combination of setUp and tearDown
    }

    @Test
    public void shouldLogPuts() {
        final Bytes binaryKey = SessionKeySchema.toBinary(key1);
        when(inner.getPosition()).thenReturn(Position.emptyPosition());
        when(context.recordContext()).thenReturn(new ProcessorRecordContext(0, 0, 0, "topic", new RecordHeaders()));

        store.put(key1, value1);

        verify(inner).put(key1, value1);
        verify(context).logChange(store.name(), binaryKey, value1, 0L, Position.emptyPosition());
    }

    @Test
    public void shouldLogPutsWithPosition() {
        final Bytes binaryKey = SessionKeySchema.toBinary(key1);
        when(inner.getPosition()).thenReturn(POSITION);
        when(context.recordContext()).thenReturn(new ProcessorRecordContext(0, 0, 0, "topic", new RecordHeaders()));

        store.put(key1, value1);

        verify(inner).put(key1, value1);
        verify(context).logChange(store.name(), binaryKey, value1, 0L, POSITION);
    }

    @Test
    public void shouldLogRemoves() {
        final Bytes binaryKey = SessionKeySchema.toBinary(key1);
        when(inner.getPosition()).thenReturn(Position.emptyPosition());
        when(context.recordContext()).thenReturn(new ProcessorRecordContext(0, 0, 0, "topic", new RecordHeaders()));

        store.remove(key1);
        store.remove(key1);

        verify(inner, times(2)).remove(key1);
        verify(context, times(2)).logChange(store.name(), binaryKey, null, 0L, Position.emptyPosition());
    }

    @SuppressWarnings({"resource", "unused"})
    @Test
    public void shouldDelegateToUnderlyingStoreWhenFetching() {
        try (final KeyValueIterator<Windowed<Bytes>, byte[]> unused = store.fetch(bytesKey)) {
            verify(inner).fetch(bytesKey);
        }
    }

    @SuppressWarnings({"resource", "unused"})
    @Test
    public void shouldDelegateToUnderlyingStoreWhenBackwardFetching() {
        try (final KeyValueIterator<Windowed<Bytes>, byte[]> unused = store.backwardFetch(bytesKey)) {
            verify(inner).backwardFetch(bytesKey);
        }
    }

    @SuppressWarnings({"resource", "unused"})
    @Test
    public void shouldDelegateToUnderlyingStoreWhenFetchingRange() {
        try (final KeyValueIterator<Windowed<Bytes>, byte[]> unused = store.fetch(bytesKey, bytesKey)) {
            verify(inner).fetch(bytesKey, bytesKey);
        }
    }

    @SuppressWarnings({"resource", "unused"})
    @Test
    public void shouldDelegateToUnderlyingStoreWhenBackwardFetchingRange() {
        try (final KeyValueIterator<Windowed<Bytes>, byte[]> unused = store.backwardFetch(bytesKey, bytesKey)) {
            verify(inner).backwardFetch(bytesKey, bytesKey);
        }
    }

    @SuppressWarnings({"resource", "unused"})
    @Test
    public void shouldDelegateToUnderlyingStoreWhenFindingSessions() {
        try (final KeyValueIterator<Windowed<Bytes>, byte[]> unused = store.findSessions(bytesKey, 0, 1)) {
            verify(inner).findSessions(bytesKey, 0, 1);
        }
    }

    @SuppressWarnings({"resource", "unused"})
    @Test
    public void shouldDelegateToUnderlyingStoreWhenBackwardFindingSessions() {
        try (final KeyValueIterator<Windowed<Bytes>, byte[]> unused = store.backwardFindSessions(bytesKey, 0, 1)) {
            verify(inner).backwardFindSessions(bytesKey, 0, 1);
        }
    }

    @SuppressWarnings({"resource", "unused"})
    @Test
    public void shouldDelegateToUnderlyingStoreWhenFindingSessionRange() {
        try (final KeyValueIterator<Windowed<Bytes>, byte[]> unused = store.findSessions(bytesKey, bytesKey, 0, 1)) {
            verify(inner).findSessions(bytesKey, bytesKey, 0, 1);
        }
    }

    @SuppressWarnings({"resource", "unused"})
    @Test
    public void shouldDelegateToUnderlyingStoreWhenBackwardFindingSessionRange() {
        try (final KeyValueIterator<Windowed<Bytes>, byte[]> unused = store.backwardFindSessions(bytesKey, bytesKey, 0, 1)) {
            verify(inner).backwardFindSessions(bytesKey, bytesKey, 0, 1);
        }
    }

    @Test
    public void shouldFlushUnderlyingStore() {
        store.flush();

        verify(inner).flush();
    }

    @Test
    public void shouldCloseUnderlyingStore() {
        store.close();

        verify(inner).close();
    }
}
