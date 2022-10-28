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

import static junit.framework.Assert.*;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.Test;

public class SolrClientFactoryTest {

    // @Test
    // disabled, because it needs infrastructure
    public void createCloudSolrClient() throws SolrServerException, IOException {
        SolrClientFactory solrClientFactory = new SolrClientFactory();
        solrClientFactory.setUrl("cloud://localhost:12181,localhost:12182?collection=deep-storage-1.10");
        solrClientFactory.initialize();

        SolrClient solrClient = solrClientFactory.getObject();

        QueryResponse response = solrClient.query(new SolrQuery("*:*"));
        assertNotNull(response);
        assertEquals(0, response.getResults().getNumFound());

        solrClientFactory.destroy();
    }

     @Test
    public void createEmbeddedSolrClientClasspath() throws SolrServerException, IOException {
        SolrClientFactory solrClientFactory = new SolrClientFactory();
        solrClientFactory.setUrl("file://./target/solr/classpath-test-core");
        solrClientFactory.setEmbeddedSolrConfigurationPath("solr/classpath");
        solrClientFactory.initialize();

        SolrClient solrClient = solrClientFactory.getObject();

        QueryResponse response = solrClient.query(new SolrQuery("*:*"));
        assertNotNull(response);
        assertEquals(0, response.getResults().getNumFound());

        solrClientFactory.destroy();
    }

    @Test
    public void createEmbeddedSolrClientFile() throws SolrServerException, IOException {
        SolrClientFactory solrClientFactory = new SolrClientFactory();
        solrClientFactory.setUrl("file://./target/solr/file-test-core");
        solrClientFactory.setEmbeddedSolrConfigurationPath("./src/test/resources/solr/file");
        solrClientFactory.initialize();

        SolrClient solrClient = solrClientFactory.getObject();

        QueryResponse response = solrClient.query(new SolrQuery("*:*"));
        assertNotNull(response);
        assertEquals(0, response.getResults().getNumFound());

        solrClientFactory.destroy();
    }

    // @Test
    // disabled, because it needs infrastructure
    public void createHttpSolrClient() throws SolrServerException, IOException {
        SolrClientFactory solrClientFactory = new SolrClientFactory();
        solrClientFactory.setUrl("http://localhost:18983/test-core");
        solrClientFactory.initialize();

        SolrClient solrClient = solrClientFactory.getObject();

        QueryResponse response = solrClient.query(new SolrQuery("*:*"));
        assertNotNull(response);
        assertEquals(0, response.getResults().getNumFound());

        solrClientFactory.destroy();
    }
}
