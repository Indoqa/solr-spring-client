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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.Test;

public class SolrServerFactoryTest {

    // @Test
    // disabled, because it needs infrastructure
    public void createCloudSolrServer() throws SolrServerException {
        SolrServerFactory solrServerFactory = new SolrServerFactory();
        solrServerFactory.setUrl("cloud://localhost:12181,localhost:12182?collection=deep-storage-1.10");
        solrServerFactory.initialize();

        SolrServer solrServer = solrServerFactory.getObject();

        QueryResponse response = solrServer.query(new SolrQuery("*:*"));
        assertNotNull(response);
        assertEquals(0, response.getResults().getNumFound());

        solrServer.shutdown();
        solrServerFactory.destroy();
    }

    @Test
    public void createEmbeddedSolrServer() throws SolrServerException {
        SolrServerFactory solrServerFactory = new SolrServerFactory();
        solrServerFactory.setUrl("file://./target/solr/embedded-test-core");
        solrServerFactory.setEmbeddedSolrConfigurationDir("./src/test/resources/solr/test-core");
        solrServerFactory.initialize();

        SolrServer solrServer = solrServerFactory.getObject();

        QueryResponse response = solrServer.query(new SolrQuery("*:*"));
        assertNotNull(response);
        assertEquals(0, response.getResults().getNumFound());

        solrServer.shutdown();
        solrServerFactory.destroy();
    }

    // @Test
    // disabled, because it needs infrastructure
    public void createHttpSolrServer() throws SolrServerException {
        SolrServerFactory solrServerFactory = new SolrServerFactory();
        solrServerFactory.setUrl("http://localhost:18983/test-core");
        solrServerFactory.initialize();

        SolrServer solrServer = solrServerFactory.getObject();

        QueryResponse response = solrServer.query(new SolrQuery("*:*"));
        assertNotNull(response);
        assertEquals(0, response.getResults().getNumFound());

        solrServer.shutdown();
        solrServerFactory.destroy();
    }
}
