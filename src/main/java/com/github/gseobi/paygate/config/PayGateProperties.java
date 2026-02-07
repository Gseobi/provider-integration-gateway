package com.github.gseobi.paygate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "paygate")
public class PayGateProperties {

    /**
     * 서버에 올려두는 운영팀 수정용 가중치 파일 경로
     * 예: /opt/paygate/conf/pg-weights.conf
     * 로컬: ./config/pg-weights.conf
     */
    private String weightFilePath = "./config/pg-weights.conf";

    /** reload 주기(ms) */
    private long weightReloadIntervalMs = 3000;
}
