//package com.asrevo.session.dynamodb;
//
//
//
//import java.io.*;
//import java.nio.ByteBuffer;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.session.Session;
//import org.springframework.session.SessionRepository;
//
//
//
//import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
//import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
//import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
//import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
//
//@Slf4j
//public class DynamoDBSessionRepository implements SessionRepository<DynamoDBSessionRepository.DynamoDBSession> {
//
//    private static final String ITEM_SESSION_ID_ATTRIBUTE_NAME = "SessionID";
//    private static final String ITEM_SESSION_EXPIRATION_TIME_ATTRIBUTE_NAME = "SessionExpirationTime";
//    private static final String ITEM_SESSION_DATA_ATTRIBUTE_NAME = "SessionData";
//
//    private final DynamoDbClient dynamoDB;
//    private final int maxInactiveIntervalInSeconds;
//    private final String sessionsTableName;
//
//    public DynamoDBSessionRepository(DynamoDbClient dynamoDB, int maxInactiveIntervalInSeconds, String sessionsTableName) {
//        this.dynamoDB = dynamoDB;
//        this.maxInactiveIntervalInSeconds = maxInactiveIntervalInSeconds;
//        this.sessionsTableName = sessionsTableName;
//    }
//
//    @Override
//    public DynamoDBSession createSession() {
//        return new DynamoDBSession(UUID.randomUUID().toString(), this.maxInactiveIntervalInSeconds);
//    }
//
//    @Override
//    public void save(DynamoDBSession session) {
//        try {
//            this.dynamoDB.putItem(toDynamoDBItem(session));
//        } catch (IOException e) {
//            log.error(e);
//        }
//    }
//
//
//    @Override
//    public DynamoDBSession findById(String id) {
//        try {
//
//
//            HashMap<String, AttributeValue> keyToGet = new HashMap<String,AttributeValue>();
//
//            keyToGet.put(ITEM_SESSION_ID_ATTRIBUTE_NAME, AttributeValue.builder()
//                    .s(id).build());
//
//            GetItemRequest request = GetItemRequest.builder()
//                    .key(keyToGet)
//                    .tableName(this.sessionsTableName)
//                    .build();
//            Map<String, AttributeValue> sessionItem = dynamoDB.getItem(request).item();
//
//            if (sessionItem == null) {
//                return null;
//            }
//            DynamoDBSession session = toSession(sessionItem);
//            if (session.isExpired()) {
//                log.info("Session: '{}' has expired. It will be deleted.", id);
//                deleteById(id);
//                return null;
//            }
//            session.setLastAccessedTime(Instant.now());
//            return session;
//        } catch (IOException | ClassNotFoundException e) {
//            log.error(e);
//        }
//        return null;
//    }
//
//    @Override
//    public void deleteById(String id) {
//        this.dynamoDB.getTable(this.sessionsTableName).deleteItem(ITEM_SESSION_ID_ATTRIBUTE_NAME, id);
//    }
//
//
//    public static class DynamoDBSession implements Session, Serializable {
//
//        @Serial
//        private static final long serialVersionUID = 6459851973327402721L;
//
//        private String id;
//        private final long creationTime;
//        private long lastAccessedTime;
//        private long maxInactiveIntervalSeconds;
//        private final Map<String, Object> attributes;
//        private Date expireAt;
//
//        public DynamoDBSession(String id, long maxInactiveIntervalSeconds) {
//            this.id = id;
//            this.creationTime = System.currentTimeMillis();
//            this.lastAccessedTime = this.creationTime;
//            this.maxInactiveIntervalSeconds = maxInactiveIntervalSeconds;
//            attributes = new HashMap<>();
//        }
//
//        @Override
//        public String changeSessionId() {
//            String changedId = UUID.randomUUID().toString();
//            this.id = changedId;
//            return changedId;
//        }
//
//        @Override
//        public Instant getCreationTime() {
//            return Instant.ofEpochMilli(this.creationTime);
//        }
//
//        @Override
//        public void setLastAccessedTime(Instant lastAccessedTime) {
//            this.lastAccessedTime = lastAccessedTime.toEpochMilli();
//            this.expireAt = Date.from(lastAccessedTime.plus(Duration.ofSeconds(this.maxInactiveIntervalSeconds)));
//        }
//
//        @Override
//        public Instant getLastAccessedTime() {
//            return Instant.ofEpochMilli(lastAccessedTime);
//        }
//
//        @Override
//        public void setMaxInactiveInterval(Duration interval) {
//            this.maxInactiveIntervalSeconds = interval.getSeconds();
//        }
//
//
//        @Override
//        public Duration getMaxInactiveInterval() {
//            return Duration.ofSeconds(this.maxInactiveIntervalSeconds);
//        }
//
//        @Override
//        public boolean isExpired() {
//            return this.maxInactiveIntervalSeconds >= 0 && new Date().after(this.expireAt);
//        }
//
//        @Override
//        public String getId() {
//            return this.id;
//        }
//
//        @SuppressWarnings("unchecked")
//        @Override
//        public <T> T getAttribute(String attributeName) {
//            return (T) this.attributes.get(attributeName);
//        }
//
//        @Override
//        public Set<String> getAttributeNames() {
//            return this.attributes.keySet().stream()
//                    .collect(Collectors.toSet());
//        }
//
//        @Override
//        public void setAttribute(String attributeName, Object attributeValue) {
//            if (attributeValue == null) {
//                removeAttribute(attributeName);
//            } else {
//                this.attributes.put(attributeName, attributeValue);
//            }
//        }
//
//        @Override
//        public void removeAttribute(String attributeName) {
//            this.attributes.remove(attributeName);
//        }
//
//    }
//
//    private PutItemRequest toDynamoDBItem(DynamoDBSession session) throws IOException {
//        ObjectOutputStream oos = null;
//        try {
//            PutItemRequest.builder()
//            Item item = new Item().withPrimaryKey(ITEM_SESSION_ID_ATTRIBUTE_NAME, session.getId());
//            updateTimeToLive(item, session);
//            ByteArrayOutputStream fos = new ByteArrayOutputStream();
//            oos = new ObjectOutputStream(fos);
//            oos.writeObject(session);
//            oos.close();
//            item.withBinary(ITEM_SESSION_DATA_ATTRIBUTE_NAME, ByteBuffer.wrap(fos.toByteArray()));
//            return item;
//        } finally {
//            IOUtils.closeQuietly(oos, null);
//        }
//    }
//
//    private DynamoDBSession toSession(Map<String, AttributeValue> item) throws IOException, ClassNotFoundException {
//        ObjectInputStream ois = null;
//        try {
//            ByteArrayInputStream bis = new ByteArrayInputStream(item.getBinary(ITEM_SESSION_DATA_ATTRIBUTE_NAME));
//            ois = new ObjectInputStream(bis);
//            DynamoDBSession session = (DynamoDBSession) ois.readObject();
//            return session;
//        } finally {
//            IOUtils.closeQuietly(ois, null);
//        }
//    }
//
//    private void updateTimeToLive(Item item, DynamoDBSession session) {
//        if (session.getMaxInactiveInterval().getSeconds() >= 0) {
//            final long lastAccessTimeSeconds =
//                    TimeUnit.MILLISECONDS.toSeconds(session.getLastAccessedTime().toEpochMilli());
//
//            final long ttlInSeconds = lastAccessTimeSeconds + session.getMaxInactiveInterval().getSeconds();
//            item.withNumber(ITEM_SESSION_EXPIRATION_TIME_ATTRIBUTE_NAME, ttlInSeconds);
//        }
//    }
//}