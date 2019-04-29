package com.azimbabu.searchdemo.service;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.azimbabu.searchdemo.aws.AWSRequestSigningApacheInterceptor;
import com.azimbabu.searchdemo.dto.SearchDemoResponse;
import com.azimbabu.searchdemo.exception.SearchDemoException;
import com.azimbabu.searchdemo.properties.ESProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchDemoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchDemoService.class);

    public static final String PLAN_NAME = "PLAN_NAME";
    public static final String SPONSOR_DFE_NAME = "SPONSOR_DFE_NAME";
    public static final String SPONS_DFE_LOC_US_STATE = "SPONS_DFE_LOC_US_STATE";
    public static final String TEST_INDEX_1 = "test-index1";
    private RestHighLevelClient esClient;
    private ESProperties esProperties;

    @Autowired
    public SearchDemoService(RestHighLevelClient esClient, ESProperties esProperties) {
        this.esClient = esClient;
        this.esProperties = esProperties;
    }

    public SearchDemoResponse search(String planName, String sponsorName, String sponsorState, int limit) {
        validateParams(planName, sponsorName, sponsorState);

        SearchRequest searchRequest = new SearchRequest(TEST_INDEX_1);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.isNotEmpty(planName)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery(PLAN_NAME, planName.toLowerCase()));
        }

        if (StringUtils.isNotEmpty(sponsorName)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery(SPONSOR_DFE_NAME, sponsorName.toLowerCase()));
        }

        if (StringUtils.isNotEmpty(sponsorState)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery(SPONS_DFE_LOC_US_STATE, sponsorState.toLowerCase()));
        }

        searchSourceBuilder.size(limit > 0 && limit <= esProperties.getMaxSize() ? limit : esProperties.getMaxSize());
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse;
        try {
            searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException ex) {
            LOGGER.error("Failed to search: " + ex.getMessage(), ex);
            throw new SearchDemoException("Failed to search", ex);
        }

        return buildResponse(searchResponse);
    }

    private SearchDemoResponse buildResponse(SearchResponse searchResponse) {
        SearchDemoResponse response = new SearchDemoResponse();
        response.setTotal(searchResponse.getHits().getTotalHits());

        List<Map<String, Object>> data = new ArrayList<>();
        for (SearchHit searchHit : searchResponse.getHits()) {
            data.add(searchHit.getSourceAsMap());
        }

        response.setData(data);
        return response;
    }

    private void validateParams(String planName, String sponsorName, String sponsorState) {
        if (StringUtils.isEmpty(planName) && StringUtils.isEmpty(sponsorName) && StringUtils.isEmpty(sponsorState)) {
            throw new IllegalArgumentException("Empty search criteria");
        }
    }
}
