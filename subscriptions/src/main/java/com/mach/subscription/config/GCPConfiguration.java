package com.mach.subscription.config;

import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.pubsub.v1.Subscriber;
import com.mach.subscription.consumer.CTProductConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.core.GcpProjectIdProvider;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GCPConfiguration {

    @Bean
    public GcpProjectIdProvider gcpProjectIdProvider(final CredentialsProvider credentialsProvider) throws IOException {
        final String projectId = ((ServiceAccountCredentials) credentialsProvider.getCredentials()).getProjectId();
        return () -> projectId;
    }

    @Bean
    public Subscriber ctProductSubscriber(final PubSubTemplate pubSubTemplate, final CTProductConsumer consumer) {
        return pubSubTemplate.subscribe("ct-products-subscription", consumer);
    }
}
