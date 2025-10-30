package com.asrevo.cvhome.order.config;

import com.asrevo.cvhome.commons.domain.StorageProviderType;
import com.asrevo.cvhome.s2s.model.CdnStorageProperties;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Slf4j
public class S3Config {

	@Bean
	@ConditionalOnMissingBean
	public S3Client s3Client(CdnStorageProperties properties) throws URISyntaxException {
		if (StorageProviderType.MINIO.equals(properties.provider())) {
			return S3Client.builder()
				.endpointOverride(new URI(properties.s3Url()))
				.serviceConfiguration(e -> e.pathStyleAccessEnabled(true).chunkedEncodingEnabled(false))
				.region(Region.of(properties.region()))
				.credentialsProvider(StaticCredentialsProvider
					.create(AwsBasicCredentials.create(properties.s3AccessKey(), properties.s3SecretKey())))
				.build();
		}
		else {
			return S3Client.create();
		}
	}

}
