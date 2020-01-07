package com.zishanfu.geosparksim;

import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SparkConfig {

    @Value("${spark.app.name}")
    private String appName;

    @Value("${spark.master}")
    private String masterUri;

    @Bean(destroyMethod = "close")
    public SparkSession ss() {
        return SparkSession.builder()
                .master(masterUri)
                .appName(appName)
                .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                .config(
                        "spark.kryo.registrator",
                        "org.datasyslab.geospark.serde.GeoSparkKryoRegistrator")
                .getOrCreate();
    }

    /**
     * Sets up cors mappings to allow endpoint access from the frontend.
     *
     * @return an implementation of {@link WebMvcConfigurer} adding Cors Mappings.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("*")
                        .allowedOrigins("http://localhost:6060");
            }
        };
    }
}
