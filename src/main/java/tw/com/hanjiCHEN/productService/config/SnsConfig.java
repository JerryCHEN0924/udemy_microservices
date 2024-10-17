package tw.com.hanjiCHEN.productService.config;

import com.amazonaws.xray.interceptors.TracingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.Topic;

@Configuration
public class SnsConfig {
    @Value("aws.region")
    private String awsRegion;

    @Value("${aws.sns.topic.product.events}")
    private String productEventsTopic;

    @Bean
    public SnsAsyncClient snsAsyncClient() {
        return SnsAsyncClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(awsRegion))
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .addExecutionInterceptor(new TracingInterceptor())
                        .build())
                .build();
    }

    //create representation of the topic,真正的Topic在CDK中建立了,這只是一個amazon resource name
    @Bean(name = "productEventsTopic")
    public Topic productEventsTopic() {
        return Topic.builder()
                .topicArn(productEventsTopic)
                .build();
    }
}
