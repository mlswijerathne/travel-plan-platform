package com.travelplan.guide.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

@Configuration
public class SqsConfig {

        @Value("${spring.cloud.aws.region.static:us-east-1}")
        private String awsRegion;

        @Value("${spring.cloud.aws.credentials.access-key:test}")
        private String accessKey;

        @Value("${spring.cloud.aws.credentials.secret-key:test}")
        private String secretKey;

        @Value("${spring.cloud.aws.sqs.endpoint:http://localhost:4566}")
        private String sqsEndpoint;

        @Bean
        public SqsAsyncClient sqsAsyncClient() {
                return SqsAsyncClient.builder()
                                .region(Region.of(awsRegion))
                                .credentialsProvider(
                                                StaticCredentialsProvider.create(
                                                                AwsBasicCredentials.create(accessKey, secretKey)))
                                .endpointOverride(URI.create(sqsEndpoint))
                                .build();
        }
}
