package com.asrevo.cvhome.certificatemanager.repository;

//import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class DomainUtils {

/*
    static HashMap<String, AttributeValue> marshall(DomainCertificateOrder order) {
        HashMap<String, AttributeValue> challengesItem = new HashMap<>();
        order.getChallenges().forEach((key, value) -> {
            HashMap<String, AttributeValue> challengeItem = new HashMap<>();
            value.forEach((ckey, cvalue) -> {
                challengeItem.put(ckey, AttributeValue.builder().s(cvalue).build());
            });
            challengesItem.put(key, AttributeValue.builder().m(challengeItem).build());
        });

        HashMap<String, AttributeValue> item = new HashMap<>();
        item.put("domain", AttributeValue.builder().s(order.getDomain()).build());
        item.put("location", AttributeValue.builder().s(order.getLocation()).build());
        item.put(
                "certificateOrderStatus",
                AttributeValue.builder()
                        .s(order.getCertificateOrderStatus().name())
                        .build());
        item.put("challenges", AttributeValue.builder().m(challengesItem).build());
        return item;
    }

    public static HashMap<String, AttributeValue> marshall(DomainCertificate certificate) {
        HashMap<String, AttributeValue> item = new HashMap<>();
        item.put("domain", AttributeValue.builder().s(certificate.getDomain()).build());
        item.put("status", AttributeValue.builder().s(certificate.getStatus()).build());
        item.put(
                "domainCertificateOrder",
                AttributeValue.builder()
                        .m(marshall(certificate.getDomainCertificateOrder()))
                        .build());
        item.put(
                "notAfter",
                AttributeValue.builder().s(certificate.getNotAfter().toString()).build());
        item.put(
                "notBefore",
                AttributeValue.builder()
                        .s(certificate.getNotBefore().toString())
                        .build());
        item.put(
                "serialNumber",
                AttributeValue.builder().s(certificate.getSerialNumber()).build());
        item.put(
                "version",
                AttributeValue.builder().n(certificate.getVersion().toString()).build());
        item.put(
                "sigAlgName",
                AttributeValue.builder().s(certificate.getSigAlgName()).build());
        item.put(
                "sigAlgOID",
                AttributeValue.builder().s(certificate.getSigAlgOID()).build());
        return item;
    }

    public static DomainCertificateOrder unmarshallDomainCertificateOrder(Map<String, AttributeValue> item) {
        Map<String, Map<String, String>> challenges = new HashMap<>();
        Map<String, AttributeValue> challengesAttributes =
                item.get("challenges").m();
        challengesAttributes.forEach((key, value) -> {
            Map<String, String> challenge = new HashMap<>();
            value.m().forEach((ckey, cvalue) -> {
                challenge.put(ckey, cvalue.s());
            });
            challenges.put(key, challenge);
        });

        String domain = item.get("domain").s();
        String location = item.get("location").s();
        CertificateOrderStatus orderStatus = CertificateOrderStatus.valueOf(
                item.get("certificateOrderStatus").s());
        return DomainCertificateOrder.builder()
                .domain(domain)
                .location(location)
                .challenges(challenges)
                .certificateOrderStatus(orderStatus)
                .build();
    }
*/
}
