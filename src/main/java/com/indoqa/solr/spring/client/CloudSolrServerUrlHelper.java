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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CloudSolrServerUrlHelper {

    public static final String CLOUD_PREFIX = "cloud://";

    public static String getCollection(String url) {
        if (!isCloudSolrServerUrl(url)) {
            throw new IllegalArgumentException("URL '" + url + "' does not have prefix '" + CLOUD_PREFIX + "'.");
        }

        Pattern collectionPattern = Pattern.compile(".*(\\?|&)collection=([^&]+).*");
        Matcher matcher = collectionPattern.matcher(url);
        if (matcher.matches()) {
            return matcher.group(2);
        }

        return null;
    }

    public static String getConnectString(String url) {
        if (!isCloudSolrServerUrl(url)) {
            throw new IllegalArgumentException("URL '" + url + "' does not have prefix '" + CLOUD_PREFIX + "'.");
        }

        Pattern connectPattern = Pattern.compile("^cloud://([^\\?]+).*$");
        Matcher matcher = connectPattern.matcher(url);
        if (matcher.matches()) {
            return matcher.group(1);
        }

        return null;
    }

    public static ZookeeperSettings getZookeeperSettings(String url) {
        if (!isCloudSolrServerUrl(url)) {
            throw new IllegalArgumentException("URL '" + url + "' does not have prefix '" + CLOUD_PREFIX + "'.");
        }

        ZookeeperSettings zookeeperSettings = new ZookeeperSettings();

        String hostsPart;
        String parametersPart;

        int separatorIndex = url.indexOf('?');
        if (separatorIndex == -1) {
            hostsPart = url.substring(CLOUD_PREFIX.length());
            parametersPart = null;
        } else {
            hostsPart = url.substring(CLOUD_PREFIX.length(), separatorIndex);
            parametersPart = url.substring(separatorIndex + 1);
        }

        zookeeperSettings.setHosts(Arrays.asList(hostsPart.split("\\s*,\\s*")));

        if (parametersPart != null) {
            String[] parameters = parametersPart.split("\\s*&\\s*");
            for (String eachParameter : parameters) {
                String[] values = eachParameter.split("\\*=\\s*");
                if ("collection".equals(values[0])) {
                    zookeeperSettings.setCollection(values[1]);
                }

                if ("zkRoot".equals(values[0])) {
                    zookeeperSettings.setZkRoot(Optional.ofNullable(values[1]));
                }
            }
        }

        return zookeeperSettings;
    }

    public static boolean isCloudSolrServerUrl(String url) {
        return url.startsWith(CLOUD_PREFIX);
    }

    public static class ZookeeperSettings {

        private List<String> hosts;
        private String collection;
        private Optional<String> zkRoot = Optional.empty();

        public String getCollection() {
            return this.collection;
        }

        public List<String> getHosts() {
            return this.hosts;
        }

        public Optional<String> getZkRoot() {
            return this.zkRoot;
        }

        public void setCollection(String collection) {
            this.collection = collection;
        }

        public void setHosts(List<String> hosts) {
            this.hosts = hosts;
        }

        public void setZkRoot(Optional<String> zkRoot) {
            this.zkRoot = zkRoot;
        }
    }
}
