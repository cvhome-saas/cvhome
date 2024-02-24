package com.asrevo.cvhome.domaincertificatemanager.config;
/*
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

import javax.sql.DataSource;

@Configuration
@EnableJdbcRepositories
public class AwsRdsJdbcDataSourceConfig extends AbstractJdbcConfiguration {
    @Bean
    DataSource dataSource(RdsSecretParams rdsSecretParams, RdsIamAuthTokenGenerator generator) {
        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .url("jdbc:postgresql://" + rdsSecretParams.host() + ":" + rdsSecretParams.port() + "/" + rdsSecretParams.dbName())
                .username(rdsSecretParams.user)
                .password(generator.getToken())
                .build();
        dataSource.addDataSourceProperty("requireSSL", true);
        dataSource.addDataSourceProperty("requireSSL", true);
        dataSource.addDataSourceProperty("verifyServerCertificate", true);
        return dataSource;
    }


    @Autowired
    private SecretsManagerClient secretsClient;

    @Bean
    public RdsSecretParams dbIamSecret(@Value("${db.secret}") String dbSecret) throws JsonProcessingException {
        GetSecretValueRequest valueRequest =
                GetSecretValueRequest.builder().secretId(dbSecret).build();

        GetSecretValueResponse valueResponse = secretsClient.getSecretValue(valueRequest);
        String secretString = valueResponse.secretString();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.readValue(secretString, Map.class);
        return new RdsSecretParams(
                map.get("host").toString(),
                map.get("dbname").toString(),
                map.get("username").toString(),
                Integer.parseInt(map.get("port").toString()));
    }

    @Bean
    public RdsClient rdsClient() {
        return RdsClient.builder().region(Region.EU_CENTRAL_1).build();
    }

    @Bean
    public RdsIamAuthTokenGenerator rdsIamAuthTokenGenerator(RdsClient rdsClient, RdsSecretParams dbIamSecret) {
        return () -> {
            RdsUtilities utilities = rdsClient.utilities();
            GenerateAuthenticationTokenRequest tokenRequest = GenerateAuthenticationTokenRequest.builder()
                    .region(Region.EU_CENTRAL_1)
                    .hostname(dbIamSecret.host())
                    .port(dbIamSecret.port())
                    .username(dbIamSecret.user())
                    .build();
            return utilities.generateAuthenticationToken(tokenRequest);
        };
    }

    @Bean
    public DataSource dataSource(
            RdsIamAuthTokenGenerator rdsIamAuthTokenGenerator,
            EbsaHikariConfig hikariConfig,
            RdsSecretParams rdsSecretParams) {
        return createDataSource(rdsIamAuthTokenGenerator, hikariConfig, rdsSecretParams);
    }

    private HikariDataSource createDataSource(
            RdsIamAuthTokenGenerator rdsIamAuthTokenGenerator,
            HikariConfig hikariConfig,
            RdsSecretParams rdsSecretParams) {
        hikariConfig.addDataSourceProperty("useSSL", true);
        hikariConfig.addDataSourceProperty("requireSSL", true);
        hikariConfig.addDataSourceProperty("verifyServerCertificate", true);
        hikariConfig.setJdbcUrl("jdbc:postgresql://"
                + rdsSecretParams.host()
                + ":"
                + rdsSecretParams.port()
                + "/"
                + rdsSecretParams.dbName());
        hikariConfig.setUsername(rdsSecretParams.user());
        hikariConfig.setPassword(rdsIamAuthTokenGenerator.getToken());
        return new HikariDataSource(hikariConfig);
    }

    @Configuration
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    static class EbsaHikariConfig extends HikariConfig {
    }

    private interface RdsIamAuthTokenGenerator {
        String getToken();
    }

    private record RdsSecretParams(String host, String dbName, String user, Integer port) {
    }
}
*/