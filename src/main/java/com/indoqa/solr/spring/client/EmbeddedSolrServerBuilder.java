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

import static com.indoqa.solr.spring.client.EmbeddedSolrServerUrlHelper.getDataDir;
import static org.apache.solr.core.CoreDescriptor.CORE_DATADIR;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.*;
import org.apache.solr.core.NodeConfig.NodeConfigBuilder;

public final class EmbeddedSolrServerBuilder {

    private EmbeddedSolrServerBuilder() {
        // hide utility class constructor
    }

    public static SolrClient build(String url, String embeddedSolrConfigurationPath) {
        SolrResourceLoader loader = new SolrResourceLoader(getNormalizedPath("."));
        NodeConfig nodeConfig = new NodeConfigBuilder(null, loader).build();

        CoreContainer container = new CoreContainer(nodeConfig);
        container.load();

        Properties properties = new Properties();
        properties.setProperty(CORE_DATADIR, getNormalizedPath(getDataDir(url)));

        if (!new File(embeddedSolrConfigurationPath).exists()) {
            properties.setProperty(CoreDescriptor.CORE_CONFIG, embeddedSolrConfigurationPath + "/conf/solrconfig.xml");
            properties.setProperty(CoreDescriptor.CORE_SCHEMA, embeddedSolrConfigurationPath + "/conf/schema.xml");
        }

        CoreDescriptor coreDescriptor = new CoreDescriptor(container, "Embedded Core", embeddedSolrConfigurationPath, properties);
        SolrCore core = container.create(coreDescriptor);

        return new EmbeddedSolrServer(container, core.getName());
    }

    public static File getCanonicalFile(File file) {
        if (file == null) {
            return null;
        }

        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            return file;
        }
    }

    private static String getNormalizedPath(String path) {
        File file = new File(path);
        file = getCanonicalFile(file);
        return file.getAbsolutePath();
    }
}
