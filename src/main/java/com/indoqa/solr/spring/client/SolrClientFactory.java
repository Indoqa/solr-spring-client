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
import java.util.concurrent.TimeUnit;

import com.indoqa.solr.spring.client.ConfigurationHelper.ConfigurationSettings;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudHttp2SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 * @see <a href="http://www.indoqa.com/a/solr-spring-client-integration-mit-version-5-1">
 *      http://www.indoqa.com/a/solr-spring-client-integration-mit-version-5-1</a>
 *
 */
public class SolrClientFactory implements FactoryBean<SolrClient>, InitializingBean, DisposableBean {

    public static final String PARAMETER_URL = "url";

    private static final int DEFAULT_IDLE_TIMEOUT = 3_600_000;
    private static final Logger LOGGER = LoggerFactory.getLogger(SolrClientFactory.class);

    private String url;

    private SolrClient solrClient;

    @Override
    public void afterPropertiesSet() {
        this.initialize();
    }

    @Override
    public void destroy() throws IOException {
        if (this.solrClient == null) {
            return;
        }

        LOGGER.info("Shutting down Solr client  with url: " + this.url);
        this.solrClient.close();
        this.solrClient = null;
    }

    @Override
    public SolrClient getObject() {
        return this.solrClient;
    }

    @Override
    public Class<?> getObjectType() {
        return SolrClient.class;
    }

    public void initialize() {
        if (this.url == null || this.url.isBlank()) {
            throw new IllegalArgumentException("The property 'url' is not set or empty.");
        }

        ConfigurationSettings settings = ConfigurationHelper.getConfigurationSettings(this.url);

        switch (settings.getType()) {
            case CLOUD:
                this.initializeCloudSolrServer(settings);
                break;

            case HTTP_1:
                this.initializeHttpSolrServer(settings);
                break;

            case HTTP_2:
                this.initializeHttp2SolrServer(settings);
                break;

            default:
                throw new IllegalArgumentException("Unsupported configuration type '" + settings.getType() + "'.");
        }
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private void initializeCloudSolrServer(ConfigurationSettings settings) {
        LOGGER.info("Initializing CloudHttp2SolrClient with URL '{}'.", this.url);

        CloudHttp2SolrClient cloudSolrClient = new CloudHttp2SolrClient.Builder(settings.getHosts(), settings.getZkRoot())
            .withDefaultCollection(settings.getCollection())
            .build();
        cloudSolrClient.connect();

        this.solrClient = cloudSolrClient;

        LOGGER.info("Created CloudHttp2SolrClient with URL '{}'.", this.url);
    }

    private void initializeHttp2SolrServer(ConfigurationSettings settings) {
        LOGGER.info("Initializing Http2SolrClient with URL '{}'.", this.url);

        if (settings.getHosts().size() == 1) {
            this.solrClient = new Http2SolrClient.Builder(settings.getUrl())
                .withConnectionTimeout(settings.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .withRequestTimeout(settings.getRequestTimeout(), TimeUnit.MILLISECONDS)
                .withIdleTimeout(DEFAULT_IDLE_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
            LOGGER.info("Created Http2SolrClient with URL '{}'.", this.url);
            return;
        }

        CloudHttp2SolrClient cloudSolrClient = new CloudHttp2SolrClient.Builder(settings.getHosts())
            .withDefaultCollection(settings.getCollection())
            .build();
        cloudSolrClient.connect();

        this.solrClient = cloudSolrClient;
        LOGGER.info("Created Http2SolrClient with URL '{}'.", this.url);
    }

    private void initializeHttpSolrServer(ConfigurationSettings settings) {
        LOGGER.info("Initializing HttpJdkSolrClient with URL '{}'.", this.url);

        this.solrClient = new HttpJdkSolrClient.Builder(settings.getUrl()).useHttp1_1(true)
            .withConnectionTimeout(settings.getConnectTimeout(), TimeUnit.MILLISECONDS)
            .withRequestTimeout(settings.getRequestTimeout(), TimeUnit.MILLISECONDS)
            .withIdleTimeout(DEFAULT_IDLE_TIMEOUT, TimeUnit.MILLISECONDS)
            .build();

        LOGGER.info("Created HttpJdkSolrClient with URL '{}'.", this.url);
    }
}
