package com.asrevo.cvhome.certificatemanager.repository;

import com.asrevo.cvhome.commons.domain.DomainCertificate;
import org.springframework.stereotype.Service;
/*
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
*/

@Service
public class DomainCertificateRepository {
/*

    private final DynamoDbClient dynamoDbClient;

    public DomainCertificateRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }
*/

    public DomainCertificate put(DomainCertificate certificate) {
/*
        HashMap<String, AttributeValue> item = DomainUtils.marshall(certificate);
        PutItemRequest request = PutItemRequest.builder()
                .tableName("dev-domain-certificate")
                .item(item)
                .build();
        dynamoDbClient.putItem(request);
*/
        return certificate;
    }
}
