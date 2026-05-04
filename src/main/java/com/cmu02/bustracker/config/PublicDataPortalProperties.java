package com.cmu02.bustracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 공공데이터 포털 인증키 설정.
 * service에서 환경변수를 직접 읽지 않고 본 프로퍼티를 주입받아 사용한다.
 */
@ConfigurationProperties(prefix = "public-data-portal.key")
public record PublicDataPortalProperties(String encode) {
}
