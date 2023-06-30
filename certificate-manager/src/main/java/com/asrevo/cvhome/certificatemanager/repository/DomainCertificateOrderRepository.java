package com.asrevo.cvhome.certificatemanager.repository;

import com.asrevo.cvhome.certificatemanager.domain.DomainCertificateOrder;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.Condition;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.HashMap;
import java.util.Map;

@Repository
public class DomainCertificateOrderRepository {

    private final DynamoDbClient dynamoDbClient;

    public DomainCertificateOrderRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public DomainCertificateOrder put(DomainCertificateOrder order) {
        HashMap<String, AttributeValue> item = DomainUtils.marshall(order);
        PutItemRequest request = PutItemRequest.builder()
                .tableName("dev-domain-certificate-order")
                .item(item)
                .build();
        dynamoDbClient.putItem(request);
        return order;
    }

    public DomainCertificateOrder findOneByLocation(String location) {

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName("dev-domain-certificate-order")
                .indexName("domain-certificate-order-index")
                .keyConditions(Map.of(
                        "location",
                        Condition.builder()
                                .comparisonOperator(ComparisonOperator.EQ)
                                .attributeValueList(
                                        AttributeValue.builder().s(location).build())
                                .build()))
                .build();
        QueryResponse query = dynamoDbClient.query(queryRequest);

        if (query.hasItems() && query.count() == 1) {
            Map<String, AttributeValue> item = query.items().get(0);
            return DomainUtils.unmarshallDomainCertificateOrder(item);
        } else {
            return null;
        }
    }
}
