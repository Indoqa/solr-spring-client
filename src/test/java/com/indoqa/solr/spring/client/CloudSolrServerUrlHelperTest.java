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

package com.indoqa.solr.spring.client;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.indoqa.solr.spring.client.ConfigurationHelper.ConfigurationSettings;
import com.indoqa.solr.spring.client.ConfigurationHelper.Type;

import org.junit.Test;

public class CloudSolrServerUrlHelperTest {

    private static void validate(ConfigurationSettings settings, ConfigurationHelper.Type type, List<String> hosts, String collections,
            String url, Optional<String> zkRoot, int connectTimeout, int requestTimeout) {
        assertEquals(collections, settings.getCollection());
        assertEquals(connectTimeout, settings.getConnectTimeout());
        assertEquals(hosts, settings.getHosts());
        assertEquals(requestTimeout, settings.getRequestTimeout());
        assertEquals(type, settings.getType());
        assertEquals(url, settings.getUrl());
        assertEquals(zkRoot, settings.getZkRoot());
    }

    @Test
    public void test() {
        validate(
            ConfigurationHelper.getConfigurationSettings("cloud://zkHost1:2181,zkHost2:2181?collection=abcdef"),
            Type.CLOUD,
            Arrays.asList("zkHost1:2181", "zkHost2:2181"),
            "abcdef",
            "cloud://zkHost1:2181,zkHost2:2181",
            Optional.empty(),
            ConfigurationHelper.DEFAULT_CONNECT_TIMEOUT,
            ConfigurationHelper.DEFAULT_REQUEST_TIMEOUT);

        validate(
            ConfigurationHelper.getConfigurationSettings("cloud://zkHost1:2181,zkHost2:2181?collection=abcdef&request-timeout=10000"),
            Type.CLOUD,
            Arrays.asList("zkHost1:2181", "zkHost2:2181"),
            "abcdef",
            "cloud://zkHost1:2181,zkHost2:2181",
            Optional.empty(),
            ConfigurationHelper.DEFAULT_CONNECT_TIMEOUT,
            10_000);

        validate(
            ConfigurationHelper.getConfigurationSettings("cloud://zkHost1:2181,zkHost2:2181?request-timeout=10000&collection=abcdef"),
            Type.CLOUD,
            Arrays.asList("zkHost1:2181", "zkHost2:2181"),
            "abcdef",
            "cloud://zkHost1:2181,zkHost2:2181",
            Optional.empty(),
            ConfigurationHelper.DEFAULT_CONNECT_TIMEOUT,
            10_000);

        validate(
            ConfigurationHelper
                .getConfigurationSettings("cloud://zkHost1:2181,zkHost2:2181/solr?&request-timeout=10000&collection=abcdef"),
            Type.CLOUD,
            Arrays.asList("zkHost1:2181", "zkHost2:2181"),
            "abcdef",
            "cloud://zkHost1:2181,zkHost2:2181/solr",
            Optional.of("/solr"),
            ConfigurationHelper.DEFAULT_CONNECT_TIMEOUT,
            10_000);

        validate(
            ConfigurationHelper
                .getConfigurationSettings("cloud://zkHost1:2181,zkHost2:2181/?&request-timeout=10000&collection=abcdef"),
            Type.CLOUD,
            Arrays.asList("zkHost1:2181", "zkHost2:2181"),
            "abcdef",
            "cloud://zkHost1:2181,zkHost2:2181/",
            Optional.empty(),
            ConfigurationHelper.DEFAULT_CONNECT_TIMEOUT,
            10_000);

        validate(
            ConfigurationHelper
                .getConfigurationSettings("cloud://zkHost1:2181,zkHost2:2181?wrong-collection=xyz&request-timeout=10000&collection="),
            Type.CLOUD,
            Arrays.asList("zkHost1:2181", "zkHost2:2181"),
            null,
            "cloud://zkHost1:2181,zkHost2:2181",
            Optional.empty(),
            ConfigurationHelper.DEFAULT_CONNECT_TIMEOUT,
            10_000);

        validate(
            ConfigurationHelper
                .getConfigurationSettings("http://localhost:8983/solr/abcdef"),
            Type.HTTP_2,
            Arrays.asList("localhost:8983/solr/abcdef"),
            null,
            "http://localhost:8983/solr/abcdef",
            Optional.empty(),
            ConfigurationHelper.DEFAULT_CONNECT_TIMEOUT,
            ConfigurationHelper.DEFAULT_REQUEST_TIMEOUT);

        validate(
            ConfigurationHelper
                .getConfigurationSettings("http_1://localhost:8983/solr/abcdef"),
            Type.HTTP_1,
            Arrays.asList("localhost:8983/solr/abcdef"),
            null,
            "http://localhost:8983/solr/abcdef",
            Optional.empty(),
            ConfigurationHelper.DEFAULT_CONNECT_TIMEOUT,
            ConfigurationHelper.DEFAULT_REQUEST_TIMEOUT);

        validate(
            ConfigurationHelper
                .getConfigurationSettings(
                    "http://solr-1:8983/solr,solr-2:8983/solr,solr-3:8983/solr?collection=abcdef"),
            Type.HTTP_2,
            Arrays.asList("solr-1:8983/solr", "solr-2:8983/solr", "solr-3:8983/solr"),
            "abcdef",
            "http://solr-1:8983/solr,solr-2:8983/solr,solr-3:8983/solr",
            Optional.empty(),
            ConfigurationHelper.DEFAULT_CONNECT_TIMEOUT,
            ConfigurationHelper.DEFAULT_REQUEST_TIMEOUT);
    }
}
