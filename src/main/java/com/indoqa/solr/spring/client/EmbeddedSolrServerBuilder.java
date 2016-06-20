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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
        Properties properties = new Properties();
        properties.setProperty(CORE_DATADIR, getNormalizedPath(getDataDir(url)));

        SolrResourceLoader loader;

        if (new File(embeddedSolrConfigurationPath).exists()) {
            loader = new SolrResourceLoader(getNormalizedPath(embeddedSolrConfigurationPath));
        } else {
            // use a temporary directory as instance-dir because Solr needs a real directory for this
            Path tempDirectory = createTempDirectory();
            loader = new SolrResourceLoader(tempDirectory.toString());

            // change config and schema locations to something that can be found on the classpath
            properties.setProperty(CoreDescriptor.CORE_CONFIG, embeddedSolrConfigurationPath + "/conf/solrconfig.xml");
            properties.setProperty(CoreDescriptor.CORE_SCHEMA, embeddedSolrConfigurationPath + "/conf/schema.xml");
        }

        NodeConfig nodeConfig = new NodeConfigBuilder(null, loader).build();
        CoreContainer container = new CoreContainer(nodeConfig);
        container.load();

        CoreDescriptor coreDescriptor = new CoreDescriptor(container, "Embedded Core", ".", properties);
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

    private static Path createTempDirectory() {
        try {
            Path tempDirectory = Files.createTempDirectory("embedded-solr-client");
            Runtime.getRuntime().addShutdownHook(new CleanupThread(tempDirectory));
            return tempDirectory;
        } catch (IOException e) {
            throw new SetupException("Failed to create temporary directory", e);
        }
    }

    private static String getNormalizedPath(String path) {
        File file = new File(path);
        file = getCanonicalFile(file);
        return file.getAbsolutePath();
    }

    private static class CleanupThread extends Thread {

        private Path directory;

        public CleanupThread(Path directory) {
            this.directory = directory;
        }

        @Override
        public void run() {
            try {
                Files.walkFileTree(this.directory, new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);

                        return super.postVisitDirectory(dir, exc);
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);

                        return super.visitFile(file, attrs);
                    }
                });
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
