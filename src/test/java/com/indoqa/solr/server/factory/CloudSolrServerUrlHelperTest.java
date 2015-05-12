/*
 * Licensed to the Indoqa Software Design und Beratung GmbH (Indoqa) under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Indoqa licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.indoqa.solr.server.factory;

import static com.indoqa.solr.server.factory.CloudSolrServerUrlHelper.getCollection;
import static com.indoqa.solr.server.factory.CloudSolrServerUrlHelper.getConnectString;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import org.junit.Test;

public class CloudSolrServerUrlHelperTest {

    @Test
    public void testGetCollection() {
        assertEquals("abcdef", getCollection("cloud://zkHost1:2181,zkHost2:2181?collection=abcdef"));
        assertEquals("abcdef", getCollection("cloud://zkHost1:2181,zkHost2:2181?collection=abcdef&timeout=10000"));
        assertEquals("abcdef", getCollection("cloud://zkHost1:2181,zkHost2:2181?timeout=10000&collection=abcdef"));
        assertEquals("abcdef", getCollection("cloud://zkHost1:2181,zkHost2:2181?collection=abcdef&wrong-collection=xyz"));
        assertEquals("abcdef", getCollection("cloud://zkHost1:2181,zkHost2:2181?wrong-collection=xyz&collection=abcdef&timeout=10000"));

        assertNull(getCollection("cloud://zkHost1:2181,zkHost2:2181?wrong-collection=xyz&timeout=10000"));
        assertNull(getCollection("cloud://zkHost1:2181,zkHost2:2181?wrong-collection=xyz&timeout=10000&collection="));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCollectionFromInvalidURL() {
        getCollection("http://localhost:8983/solr/abcdef");
    }

    @Test
    public void testGetConnectString() {
        assertEquals("zkHost1:2181,zkHost2:2181", getConnectString("cloud://zkHost1:2181,zkHost2:2181?collection=abcdef"));
        assertEquals("zkHost1:2181", getConnectString("cloud://zkHost1:2181?collection=abcdef&timeout=10000"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetConnectStringFromInvalidURL() {
        getConnectString("http://localhost:8983/solr/abcdef");
    }
}
