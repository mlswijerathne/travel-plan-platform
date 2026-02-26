package com.travelplan.review.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;

/**
 * Spring configuration for the AWS SQS client.
 *
 * <p>In production (ECS Fargate), the {@link DefaultCredentialsProvider}
 * automatically picks up the IAM Task Role credentials injected by AWS.
 *
 * <p>For local development an optional {@code aws.sqs.endpoint-override}
 * property can be set to point at a local mock such as
 * <a href="https://github.com/softwaremill/elasticmq">ElasticMQ</a>:
 * <pre>
 *   aws.sqs.endpoint-override: http://localhost:9324
 * </pre>
 *
 * <p>When no AWS credentials are present in the environment the client
 * creation will fail at startup.  In that scenario simply leave all SQS
 * queue URL properties empty — the publisher and listener will skip
 * gracefully without touching the SQS client.
 */
@Slf4j
@Configuration
public class SqsConfig {

    /** AWS region read from {@code aws.region} in application.yml */
    @Value("${aws.region:ap-south-1}")
    private String awsRegion;

    /**
     * Optional endpoint override for local development / testing.
     * Leave empty or unset for real AWS.
     */
    @Value("${aws.sqs.endpoint-override:}")
    private String endpointOverride;

    /**
     * Creates and exposes the AWS SQS client as a Spring bean.
     *
     * <p>Uses {@link DefaultCredentialsProvider} which resolves credentials
     * from (in order):
     * <ol>
     *   <li>Environment variables ({@code AWS_ACCESS_KEY_ID} / {@code AWS_SECRET_ACCESS_KEY})</li>
     *   <li>AWS profile / config files</li>
     *   <li>ECS / EC2 instance metadata (used in production)</li>
     * </ol>
     *
     * @return a configured, thread-safe {@link SqsClient}
     */
    @Bean
    public SqsClient sqsClient() {
        SqsClientBuilder builder = SqsClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create());

        // Apply local endpoint override when configured (e.g. ElasticMQ)
        if (endpointOverride != null && !endpointOverride.isBlank()) {
            log.info("Applying SQS endpoint override: {}", endpointOverride);
            builder.endpointOverride(URI.create(endpointOverride));
        }

        SqsClient client = builder.build();
        log.info("SqsClient initialised — region={}", awsRegion);
        return client;
    }
}
