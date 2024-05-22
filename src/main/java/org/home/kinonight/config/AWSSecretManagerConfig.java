package org.home.kinonight.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClient;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.home.kinonight.model.TelegramCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSSecretManagerConfig {
    @Value("${aws.region}")
    private String region;
    @Value("${aws.securityManager.telegramSecretName}")
    private String telegramSecretName;
    @Value("${aws.securityManager.accessKey}")
    private String awsClientID;
    @Value("${aws.securityManager.secretKey}")
    private String awsSecretKey;

    @Bean
    public AWSSecretsManager awsSecretsManagerClient() {
        return AWSSecretsManagerClient.builder()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsClientID, awsSecretKey)))
                .build();
    }

    @Bean(name = "telegramSecret")
    public TelegramCredentials getTelegramSecret(AWSSecretsManager awsSecretsManager, ObjectMapper objectMapper) {
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(telegramSecretName);
        GetSecretValueResult secretValue = awsSecretsManager.getSecretValue(getSecretValueRequest);
        String secretString = secretValue.getSecretString();

        try {
            return objectMapper.readValue(secretString, TelegramCredentials.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
