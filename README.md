# Indoqa Solr Spring Client

This project offers a Spring based implementation of a FactoryBean for communicating with Apache Solr 5.x servers. 

The SolrClientFactory allows to communicate with Solr either embedded, via http or Apache ZooKeeper for SolrCloud installations.

The desired behavior is configured with the supplied url:

* file:// - uses the EmbeddedSolrClient
* http:// - uses the HttpSolrClient
* cloud:// - uses the CloudSolrClient


## Installation

### Requirements

  * Apache Solr 5.0+
  * Spring Beans 3.1+
  * Java 6+
  
### Build

  * Download the latest release via maven

```xml

    <dependency>
      <groupId>com.indoqa.solr</groupId>
      <artifactId>solr-spring-client</artifactId>
    </dependency>
    
```

  * Download source
  * run "maven clean install"
