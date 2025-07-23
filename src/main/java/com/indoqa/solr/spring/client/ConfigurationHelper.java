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

public class ConfigurationHelper {

    private static final String HTTP_1_PREFIX = "http_1://";
    private static final String HTTP_PREFIX = "http://";
    private static final String CLOUD_PREFIX = "cloud://";

    public static final int DEFAULT_CONNECT_TIMEOUT = 5_000;
    public static final int DEFAULT_REQUEST_TIMEOUT = 60_000;

    public static ConfigurationSettings getConfigurationSettings(String url) {
        ConfigurationSettings settings = new ConfigurationSettings();

        if (url.startsWith(HTTP_1_PREFIX)) {
            settings.setType(Type.HTTP_1);
        } else if (url.startsWith(HTTP_PREFIX)) {
            settings.setType(Type.HTTP_2);
        } else if (url.startsWith(CLOUD_PREFIX)) {
            settings.setType(Type.CLOUD);
        }

        String urlPart = getStringBefore(url, "?");
        String hostsPart = getStringAfter(urlPart, "://");
        String parametersPart = getStringAfter(url, "?");

        settings.setUrl(urlPart.replace(HTTP_1_PREFIX, HTTP_PREFIX));
        parseHosts(settings, hostsPart);
        parseParameters(settings, parametersPart);

        return settings;
    }

    private static String getStringAfter(String url, String separator) {
        int index = url.indexOf(separator);
        if (index == -1 || index == url.length() - 1) {
            return null;
        }

        return url.substring(index + separator.length());
    }

    private static String getStringBefore(String url, String separator) {
        int index = url.indexOf(separator);
        if (index == -1) {
            return url;
        }

        return url.substring(0, index);
    }

    private static void parseHosts(ConfigurationSettings settings, String hostsPart) {
        if (settings.getType() == Type.CLOUD) {
            settings.setZkRoot(Optional.ofNullable(getStringAfter(hostsPart, "/")).map(value -> "/" + value));

            String hosts = getStringBefore(hostsPart, "/");
            settings.setHosts(Arrays.asList(hosts.split("\\s*,\\s*")));
            return;
        }

        settings.setHosts(Arrays.asList(hostsPart.split("\\s*,\\s*")));
    }

    private static int parseInteger(String name, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Could not parse '" + value + "' for parameter '" + name + "' as integer.", e);
        }
    }

    private static void parseParameters(ConfigurationSettings settings, String parametersPart) {
        if (parametersPart != null) {
            String[] parameters = parametersPart.split("\\s*&\\s*");
            for (String eachParameter : parameters) {
                String[] values = eachParameter.split("\\s*=\\s*");
                if (values.length != 2) {
                    continue;
                }

                if ("collection".equals(values[0])) {
                    settings.setCollection(values[1]);
                }

                if ("request-timeout".equals(values[0])) {
                    settings.setRequestTimeout(parseInteger(values[0], values[1]));
                }

                if ("connect-timeout".equals(values[0])) {
                    settings.setConnectTimeout(parseInteger(values[0], values[1]));
                }
            }
        }
    }

    public static class ConfigurationSettings {

        private Type type;
        private String url;
        private List<String> hosts;
        private String collection;
        private Optional<String> zkRoot = Optional.empty();
        private int requestTimeout = DEFAULT_REQUEST_TIMEOUT;
        private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;

        public String getCollection() {
            return this.collection;
        }

        public int getConnectTimeout() {
            return this.connectTimeout;
        }

        public String getFirstHost() {
            if (this.hosts == null || this.hosts.isEmpty()) {
                return null;
            }

            return this.hosts.getFirst();
        }

        public List<String> getHosts() {
            return this.hosts;
        }

        public int getRequestTimeout() {
            return this.requestTimeout;
        }

        public Type getType() {
            return this.type;
        }

        public String getUrl() {
            return this.url;
        }

        public Optional<String> getZkRoot() {
            return this.zkRoot;
        }

        public void setCollection(String collection) {
            this.collection = collection;
        }

        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public void setHosts(List<String> hosts) {
            this.hosts = hosts;
        }

        public void setRequestTimeout(int requestTimeout) {
            this.requestTimeout = requestTimeout;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setZkRoot(Optional<String> zkRoot) {
            this.zkRoot = zkRoot;
        }
    }

    public enum Type {
        HTTP_1, HTTP_2, CLOUD;
    }
}
