/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.map;

import net.openhft.lang.io.Bytes;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;

public class CHMMetaDataTest {
    private static final String TMP = System.getProperty("java.io.tmpdir");

    @Test
    public void testAccessTimes() throws IOException {
        File file = new File(TMP, "testAccessTimes");
        BytesMapEventListener listener =
                new StringStringMapEventListener(new AtomicLong(1));
        ChronicleMap<String, String> map = ChronicleMapBuilder.of(String.class, String.class)
                .metaDataBytes(8).bytesEventListener(listener).create();

        try {
            map.put("a", "aye");
            map.put("b", "bee");
            map.put("c", "see");
            map.put("d", "dee");
//            assertEquals(5, timeStamps.longValue());
            assertEquals("aye", map.get("a"));
            assertEquals("bee", map.get("b"));
            assertEquals("see", map.get("c"));
            assertEquals("dee", map.get("d"));
//            assertEquals(9, timeStamps.longValue());
            assertEquals("aye", map.remove("a"));
            assertEquals("bee", map.remove("b"));
            assertEquals("see", map.remove("c"));
            assertEquals("dee", map.remove("d"));
//            assertEquals(9, timeStamps.longValue());
        } finally {
            map.close();
            file.delete();
        }
    }

    private static class StringStringMapEventListener
            extends BytesMapEventListener {
        private static final long serialVersionUID = 0L;

        private final AtomicLong timeStamps;

        public StringStringMapEventListener(AtomicLong timeStamps) {
            this.timeStamps = timeStamps;
        }

        @Override
        public void onGetFound(Bytes entry, long metaDataPos, long keyPos, long valuePos) {
            entry.writeLong(metaDataPos, timeStamps.incrementAndGet());
        }

        @Override
        public void onPut(Bytes entry, long metaDataPos, long keyPos, long valuePos, boolean added) {
            if (added)
                assertEquals(0, entry.readLong(metaDataPos));
            entry.writeLong(metaDataPos, timeStamps.incrementAndGet());
        }

        @Override
        public void onRemove(Bytes entry, long metaDataPos, long keyPos, long valuePos) {
            System.out.println("Removed entry with ts of " + entry.readLong(metaDataPos));;
        }
    }
}
