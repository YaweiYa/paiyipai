package com.example.paiyipai.mockServer;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

public class PaymentServiceWireMockInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        WireMockServer paymentServiceWireMockServer = new WireMockServer(
                new WireMockConfiguration().dynamicPort().usingFilesUnderClasspath("mock-server/payment-service/"));
        paymentServiceWireMockServer.start();

        configurableApplicationContext.getBeanFactory()
                .registerSingleton("paymentServiceWireMockServer", paymentServiceWireMockServer);

        configurableApplicationContext.addApplicationListener(applicationEvent -> {
            if (applicationEvent instanceof ContextClosedEvent) {
                paymentServiceWireMockServer.stop();
            }
        });

        TestPropertyValues
                .of("service.idvs.baseUrl=http://localhost:" + paymentServiceWireMockServer.port())
                .applyTo(configurableApplicationContext);
    }
}
