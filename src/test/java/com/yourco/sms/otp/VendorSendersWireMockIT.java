package com.yourco.sms.otp;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.sp.twofa.sms.SmsConfig;
import com.sp.twofa.sms.sender.InfobipSender;

class VendorSendersWireMockIT {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance().options(
            com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig().dynamicPort()).build();

    private SmsConfig infobipConfig(String baseUrl) {
        return new SmsConfig("infobip", "api-key", null, null, "ACME", baseUrl, null, 1000, 60, 6, 45, 5);
    }

    @Test
    void infobipHappyPathSendsPayload(WireMockRuntimeInfo runtimeInfo) throws Exception {
        wireMock.stubFor(post("/sms/2/text/advanced").willReturn(okJson("{\"messages\":[]}")));
        InfobipSender sender = new InfobipSender(infobipConfig(runtimeInfo.getHttpBaseUrl()));

        sender.send("+12025550123", "hi");

        wireMock.verify(postRequestedFor(com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo("/sms/2/text/advanced"))
                .withHeader("Authorization", equalTo("App api-key"))
                .withRequestBody(containing("\"to\":\"+12025550123\""))
                .withRequestBody(containing("\"text\":\"hi\"")));
    }

    @Test
    void infobipFailurePropagatesException(WireMockRuntimeInfo runtimeInfo) {
        wireMock.stubFor(post("/sms/2/text/advanced").willReturn(aResponse().withStatus(500)));
        InfobipSender sender = new InfobipSender(infobipConfig(runtimeInfo.getHttpBaseUrl()));

        assertThatThrownBy(() -> sender.send("+12025550123", "hi")).isInstanceOf(Exception.class);
    }
}
