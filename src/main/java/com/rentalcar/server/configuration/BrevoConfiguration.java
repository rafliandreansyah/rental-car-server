package com.rentalcar.server.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sendinblue.ApiClient;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;

@Configuration
public class BrevoConfiguration {

    @Value("${brevo.apikey}")
    private String apiKey;
    @Value("${brevo.sender.email}")
    private String senderEmail;
    @Value("${brevo.sender.name}")
    private String senderName;

    @Bean
    public TransactionalEmailsApi transactionalEmailsApi() {
        ApiClient defaultClient = sendinblue.Configuration.getDefaultApiClient();
        // Configure API key authorization: api-key
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(apiKey);

        return new TransactionalEmailsApi();
    }

    @Bean
    public SendSmtpEmailSender sendSmtpEmailSender() {
        //set sender email
        SendSmtpEmailSender sender = new SendSmtpEmailSender();
        sender.setEmail(senderEmail);
        sender.setName(senderName);

        return sender;
    }

    @Bean
    public SendSmtpEmail sendSmtpEmail() {
        return new SendSmtpEmail();
    }

}
