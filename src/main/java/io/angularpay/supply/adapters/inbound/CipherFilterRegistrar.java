package io.angularpay.supply.adapters.inbound;

import io.angularpay.supply.adapters.outbound.CipherServiceAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CipherFilterRegistrar {

    @ConditionalOnProperty(
            value = "angularpay.cipher.enabled",
            havingValue = "true",
            matchIfMissing = true)
    @Bean
    public FilterRegistrationBean<CipherFilter> registerPostCommentsRateLimiter(CipherServiceAdapter cipherServiceAdapter) {
        FilterRegistrationBean<CipherFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CipherFilter(cipherServiceAdapter));
        registrationBean.addUrlPatterns(
                "/supply/requests",
                "/supply/requests/*/summary",
                "/supply/requests/*/commodity-unit-price",
                "/supply/requests/*/commodity-quantity",
                "/supply/requests/*/suppliers",
                "/supply/requests/*/bargains",
                "/supply/requests/*/investors/*/quantity",
                "/supply/requests/*/investors/*/payment"
        );
        return registrationBean;
    }
}
