package com.maotuanzi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.maotuanzi.controller"))
                .paths(PathSelectors.any())
                .build()
                .groupName("C端 API");
    }

    @Bean
    public Docket adminApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(adminApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.maotuanzi.controller.admin"))
                .paths(PathSelectors.any())
                .build()
                .groupName("管理后台");
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("毛团子AI · C 端 API")
                .description("微信小程序 / HarmonyOS 客户端接口")
                .version("2.0.0")
                .build();
    }

    private ApiInfo adminApiInfo() {
        return new ApiInfoBuilder()
                .title("毛团子AI · 管理后台 API")
                .description("Web 后台管理系统接口")
                .version("2.0.0")
                .build();
    }
}