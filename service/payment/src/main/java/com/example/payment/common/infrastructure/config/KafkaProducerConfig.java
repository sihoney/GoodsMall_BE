package com.example.payment.common.infrastructure.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

    /**
     * ProducerFactory Bean ?깅줉
     * <p>
     * ProducerFactory??Kafka Producer瑜??앹꽦?섎뒗 怨듭옣 ??븷???쒕떎.
     * Kafka???묒냽?섍린 ?꾪븳 二쇱냼, key/value 吏곷젹??諛⑹떇 媛숈?
     * Producer??湲곕낯 ?ㅼ젙??媛吏怨??덈떎.
     */
    @Bean
    public ProducerFactory<String, String> producerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        Map<String, Object> props = new HashMap<>();
        // Kafka broker 二쇱냼 ?ㅼ젙
        // Producer媛 ?대뒓 Kafka ?쒕쾭???곌껐?좎? 吏?뺥븳??
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // 硫붿떆吏 key瑜??대뼡 諛⑹떇?쇰줈 諛붿씠??諛곗뿴濡?蹂?섑븷吏 吏?뺥븳??
        // ?꾩옱 key ??낆씠 String ?대?濡?StringSerializer瑜??ъ슜?쒕떎.
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // 硫붿떆吏 value瑜??대뼡 諛⑹떇?쇰줈 諛붿씠??諛곗뿴濡?蹂?섑븷吏 吏?뺥븳??
        // ?꾩옱 value ??낅룄 String ?대?濡?StringSerializer瑜??ъ슜?쒕떎.
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // ???ㅼ젙媛믪쓣 諛뷀깢?쇰줈 Kafka ProducerFactory瑜??앹꽦?쒕떎.
        // ???⑺넗由щ뒗 ?대??곸쑝濡??ㅼ젣 Producer ?몄뒪?댁뒪瑜?留뚮뱾?댁???
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(
            // ?꾩뿉???깅줉??ProducerFactory Bean??二쇱엯諛쏅뒗??
            ProducerFactory<String, String> producerFactory
    ) {
        // ProducerFactory瑜?湲곕컲?쇰줈 KafkaTemplate ?앹꽦
        return new KafkaTemplate<>(producerFactory);
    }
}
