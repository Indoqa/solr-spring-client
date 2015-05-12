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

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * 
 * @see http://indoqa.com/a/solr-spring-server-integration-mit-version-4-10-3
 * 
 */
public class SolrServerFactory implements FactoryBean<SolrServer> {

    public static final String PARAMETER_URL = "url";
    public static final String PARAMETER_EMBEDDED_SOLR_CONFIGURATION_DIR = "embeddedSolrConfigurationDir";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String url;
    private String embeddedSolrConfigurationDir;

    private SolrServer solrServer;

    @PreDestroy
    public void destroy() {
        if (this.solrServer == null) {
            return;
        }

        if (EmbeddedSolrServerUrlHelper.isEmbeddedSolrServerUrl(this.url)) {
            this.destroyEmbeddedSolrServer();
        } else {
            this.destroyHttpSolrServer();
        }
    }

    @Override
    public SolrServer getObject() {
        return this.solrServer;
    }

    @Override
    public Class<?> getObjectType() {
        return SolrServer.class;
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

    public void setEmbeddedSolrConfigurationDir(String dir) {
        this.embeddedSolrConfigurationDir = dir;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private void destroyEmbeddedSolrServer() {
        this.logger.info("Shutting down embedded Solr server with url: " + this.url);
        this.solrServer.shutdown();

        if (this.solrServer instanceof EmbeddedSolrServer) {
            EmbeddedSolrServer embeddedSolrServer = (EmbeddedSolrServer) this.solrServer;
            embeddedSolrServer.getCoreContainer().shutdown();
        }

        this.solrServer = null;
    }

    private void destroyHttpSolrServer() {
        this.logger.info("Shutting down HTTP Solr server with url: " + this.url);
        this.solrServer.shutdown();
        this.solrServer = null;
    }

    private void initializeCloudSolrServer() {
        this.logger.info("Initializing Cloud Solr server with URL: " + this.url);

        this.solrServer = new CloudSolrServer(CloudSolrServerUrlHelper.getConnectString(this.url));
        ((CloudSolrServer) this.solrServer).setDefaultCollection(CloudSolrServerUrlHelper.getCollection(this.url));

        this.logger.info("Created Cloud Solr server with URL: " + this.url);
    }

    private void initializeEmbeddedSolrServer() throws IOException {
        this.logger.info("Initializing embedded Solr server with URL: " + this.url);

        this.solrServer = EmbeddedSolrServerBuilder.build(this.url, this.embeddedSolrConfigurationDir);

        this.logger.info("Created embedded Solr server with URL: " + this.url);
    }

    private void initializeHttpSolrServer() {
        this.logger.info("Initializing HTTP Solr server with url: " + this.url);

        this.solrServer = new HttpSolrServer(this.url);

        this.logger.info("Created HTTP Solr server with url: " + this.url);
    }
}
