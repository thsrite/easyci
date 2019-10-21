package com.easyci.ci.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("easyci")
public class EasyCiConfig {

    //sh脚本路径
    private String shpath;

    //logs路径
    private String logpath;

    //项目路径
    private String workpath;
}
