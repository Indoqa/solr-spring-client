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

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

public class SolrClientFactory implements FactoryBean<SolrClient> {

    public static final String PARAMETER_URL = "url";
    public static final String PARAMETER_EMBEDDED_SOLR_CONFIGURATION_DIR = "embeddedSolrConfigurationDir";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String url;
    private String embeddedSolrConfigurationDir;

    private SolrClient solrClient;

    @PreDestroy
    public void destroy() throws IOException {
        if (this.solrClient == null) {
            return;
        }

        if (EmbeddedSolrServerUrlHelper.isEmbeddedSolrServerUrl(this.url)) {
            this.destroyEmbeddedSolrServer();
        } else {
            this.destroyHttpSolrServer();
        }
    }

    @Override
    public SolrClient getObject() {
        return this.solrClient;
    }

    @Override
    public Class<?> getObjectType() {
        return SolrClient.class;
    }

    @PostConstruct
    public void initialize() {
        if (StringUtils.isBlank(this.url)) {
            throw new IllegalArgumentException("The property 'url' is not set or empty.");
        }

        try {
            if (EmbeddedSolrServerUrlHelper.isEmbeddedSolrServerUrl(this.url)) {
                this.initializeEmbeddedSolrServer();
            } else if (CloudSolrServerUrlHelper.isCloudSolrServerUrl(this.url)) {
                this.initializeCloudSolrServer();
            } else {
                this.initializeHttpSolrServer();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "An exception occurred during the initialization of the Solr server: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setEmbeddedSolrConfigurationDir(String embeddedSolrConfigurationDir) {
        this.embeddedSolrConfigurationDir = embeddedSolrConfigurationDir;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private void destroyEmbeddedSolrServer() throws IOException {
        this.logger.info("Shutting down embedded Solr server with url: " + this.url);
        this.solrClient.close();

        if (this.solrClient instanceof EmbeddedSolrServer) {
            EmbeddedSolrServer embeddedSolrServer = (EmbeddedSolrServer) this.solrClient;
            embeddedSolrServer.getCoreContainer().shutdown();
        }

        this.solrClient = null;
    }

    private void destroyHttpSolrServer() throws IOException {
        this.logger.info("Shutting down HTTP Solr client  with url: " + this.url);

        this.solrClient.close();
        this.solrClient = null;
    }

    private void initializeCloudSolrServer() {
        this.logger.info("Initializing Cloud Solr client with URL: " + this.url);

        CloudSolrClient cloudSolrClient = new CloudSolrClient(CloudSolrServerUrlHelper.getConnectString(this.url));
        cloudSolrClient.setDefaultCollection(CloudSolrServerUrlHelper.getCollection(this.url));
        cloudSolrClient.connect();

        this.solrClient = cloudSolrClient;

        this.logger.info("Created Cloud Solr client with URL: " + this.url);
    }

    private void initializeEmbeddedSolrServer() throws IOException {
        this.logger.info("Initializing embedded Solr server with URL: " + this.url);

        this.solrClient = EmbeddedSolrServerBuilder.build(this.url, this.embeddedSolrConfigurationDir);

        this.logger.info("Created embedded Solr server with URL: " + this.url);
    }

    private void initializeHttpSolrServer() {
        this.logger.info("Initializing HTTP Solr client with url: " + this.url);

        this.solrClient = new HttpSolrClient(this.url);

        this.logger.info("Created HTTP Solr client with url: " + this.url);
    }
}