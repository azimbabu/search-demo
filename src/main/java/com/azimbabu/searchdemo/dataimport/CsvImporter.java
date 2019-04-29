package com.azimbabu.searchdemo.dataimport;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.azimbabu.searchdemo.aws.AWSRequestSigningApacheInterceptor;
import com.opencsv.CSVReader;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class CsvImporter {

    private static final String ACK_ID = "ACK_ID";
    private static final int BATCH_SIZE = 1000;
    private static String SERVICE_NAME = "es";
    private static String REGION = "us-east-1";
    private static String AES_ENDPOINT = "https://search-demosearch-fzt7sqtlglab3p63td3fgv6d5e.us-east-1.es.amazonaws.com"; // e.g. https://search-mydomain.us-west-1.es.amazonaws.com
    private static String INDEX = "test-index1";
    private static String TYPE = "_doc";

    private static final AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(
            new BasicAWSCredentials("<your access key>", "<your secret key>"));

    public static void main(String[] args) {

        RestHighLevelClient esClient = esHighLevelClient(SERVICE_NAME, REGION);

        try (CSVReader csvReader = new CSVReader(new BufferedReader(new InputStreamReader(CsvImporter.class.getResourceAsStream("/" + "f_5500_2017_latest.csv"))))) {
            // read headers
            List<String> headers = readHeaders(csvReader);

            List<List<String>> batch;
            do {
                batch = readBatch(csvReader, BATCH_SIZE);
                indexBatch(headers, batch, esClient);
            } while (batch.size() == BATCH_SIZE);

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    private static List<String> readHeaders(CSVReader csvReader) throws IOException {
        String[] values = csvReader.readNext();
        List<String> headers;
        if (values != null) {
            headers = Arrays.asList(values);
        } else {
            headers = Collections.emptyList();
        }
        return headers;
    }

    private static List<List<String>> readBatch(CSVReader csvReader, int batchSize) throws IOException {
        List<List<String>> batch = new ArrayList<>();
        String[] values;
        for (int i = 0; i < batchSize; i++) {
            if ((values = csvReader.readNext()) != null) {
                batch.add(Arrays.asList(values));
            } else {
                break;
            }
        }
        return batch;
    }

    private static void indexBatch(List<String> headers, List<List<String>> batch, RestHighLevelClient esClient) throws IOException {
        if (batch.isEmpty()) {
            return;
        }

        BulkRequest bulkRequest = new BulkRequest();

        for (List<String> row : batch) {
            Map<String, String> sourceData = new HashMap<>();
            for (int i=0; i < headers.size(); i++) {
                if (StringUtils.isNotEmpty(row.get(i))) {
                    sourceData.put(headers.get(i), row.get(i));
                }
            }

            if (MapUtils.isNotEmpty(sourceData)) {
                IndexRequest indexRequest = new IndexRequest(INDEX);
                indexRequest.id(sourceData.get(ACK_ID));
                indexRequest.source(sourceData, XContentType.JSON);
                indexRequest.type(TYPE);
                bulkRequest.add(indexRequest);
                bulkRequest.timeout(TimeValue.timeValueMinutes(2));
            }
        }

        BulkResponse bulkResponse = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        if (bulkResponse.hasFailures()) {
            System.out.println(bulkResponse.buildFailureMessage());
        }
    }

    public static RestHighLevelClient esHighLevelClient(String serviceName, String region) {
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(serviceName);
        signer.setRegionName(region);
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(serviceName, signer, credentialsProvider);
        return new RestHighLevelClient(RestClient.builder(
                HttpHost.create(AES_ENDPOINT)).setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
    }
}
