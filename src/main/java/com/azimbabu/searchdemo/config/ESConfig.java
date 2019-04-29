package com.azimbabu.searchdemo.config;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.azimbabu.searchdemo.aws.AWSRequestSigningApacheInterceptor;
import com.azimbabu.searchdemo.properties.ESProperties;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ESConfig {

    @Bean
    public RestHighLevelClient restHighLevelClient(ESProperties esProperties) {
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(esProperties.getServiceName());
        signer.setRegionName(esProperties.getRegion());
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(esProperties.getServiceName(), signer, awsCredentialsProvider(esProperties));
        return new RestHighLevelClient(RestClient.builder(HttpHost.create(esProperties.getAesEndpoint()))
                                               .setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
    }

    private AWSCredentialsProvider awsCredentialsProvider(ESProperties esProperties) {
        return new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(esProperties.getAccessKey(), esProperties.getSecretKey()));
    }
}
