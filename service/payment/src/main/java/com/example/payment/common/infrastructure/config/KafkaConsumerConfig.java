package com.example.payment.common.infrastructure.config;

import com.example.payment.common.common.exception.WalletNotFoundException;
import com.example.payment.common.infrastructure.messaging.kafka.KafkaConsumerGroups;
import com.example.payment.common.infrastructure.messaging.kafka.KafkaRetryPolicy;
import com.example.payment.common.infrastructure.messaging.kafka.KafkaTopics;
import com.example.payment.common.infrastructure.messaging.kafka.contract.MemberCreatedMessage;
import com.example.payment.common.infrastructure.messaging.kafka.contract.OrderPurchaseConfirmedMessage;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.FixedBackOff;
import lombok.extern.slf4j.Slf4j;

/**
 * payment 紐⑤뱢 Kafka consumer(?뚮퉬湲? ?ㅼ젙???대떦?쒕떎.
 * <p>
 * ??븷:
 * 1. ?대깽????낅퀎 ConsumerFactory瑜?留뚮뱺??
 * 2. @KafkaListener媛 ?ъ슜??ListenerContainerFactory瑜?留뚮뱺??
 * 3. ?뱀젙 ?대깽?몄뿉 ????ъ떆?? 諛깆삤?? DLQ 媛숈? ?ㅽ뙣 泥섎━ ?뺤콉???ㅼ젙?쒕떎.
 */
@Slf4j
@Configuration
public class KafkaConsumerConfig {

    /**
     * ?뚯썝 ?앹꽦 ?대깽?몃? ?뚮퉬?섍린 ?꾪븳 ConsumerFactory
     * <p>
     * ?ш린??bootstrap server, consumer group, deserializer 媛숈?
     * 怨듯넻 ?뚮퉬 ?ㅼ젙???ㅼ뼱媛꾨떎.
     */
    @Bean
    public ConsumerFactory<String, MemberCreatedMessage> memberCreatedConsumerFactory(
            // Kafka broker 二쇱냼
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        return createConsumerFactory(bootstrapServers, KafkaConsumerGroups.PAYMENT_SERVICE, MemberCreatedMessage.class);
    }

    /**
     * ?뚯썝 ?앹꽦 ?대깽?몃? 泥섎━??KafkaListenerContainerFactory
     * <p>
     * ?ㅼ젣 @KafkaListener?먯꽌 containerFactory ?대쫫?쇰줈 李몄“?댁꽌 ?ъ슜?쒕떎.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MemberCreatedMessage>
        memberCreatedKafkaListenerContainerFactory(
            ConsumerFactory<String, MemberCreatedMessage> memberCreatedConsumerFactory
    ) {
        // ConcurrentKafkaListenerContainerFactory??@KafkaListener ?ㅽ뻾 ?섍꼍??留뚮뱶??怨듭옣
        ConcurrentKafkaListenerContainerFactory<String, MemberCreatedMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        // ?대뼡 ConsumerFactory瑜??ъ슜?댁꽌 Consumer瑜?留뚮뱾吏 吏??        factory.setConsumerFactory(memberCreatedConsumerFactory);
        return factory;
    }

    /**
     * 二쇰Ц 援щℓ ?뺤젙 ?대깽?몄슜 ConsumerFactory
     */
    @Bean
    public ConsumerFactory<String, OrderPurchaseConfirmedMessage> orderPurchaseConfirmedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        return createConsumerFactory(bootstrapServers, KafkaConsumerGroups.PAYMENT_SERVICE, OrderPurchaseConfirmedMessage.class);
    }

    /**
     * 諛곗넚 ?꾨즺 ?대깽?몄슜 ConsumerFactory
     */

    /**
     * 諛곗넚 ?꾨즺 ?대깽?몃? 泥섎━??ListenerContainerFactory
     */
    /**
     * 二쇰Ц 援щℓ ?뺤젙 ?대깽?몃? 泥섎━??ListenerContainerFactory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderPurchaseConfirmedMessage>
        orderPurchaseConfirmedKafkaListenerContainerFactory(
            ConsumerFactory<String, OrderPurchaseConfirmedMessage> orderPurchaseConfirmedConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, OrderPurchaseConfirmedMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderPurchaseConfirmedConsumerFactory);
        return factory;
    }

    /**
     * 寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 ?대깽?몄슜 ConsumerFactory
     */
    @Bean
    public ConsumerFactory<String, String> auctionBidFeeChargeRequestedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        return createConsumerFactory(bootstrapServers, KafkaConsumerGroups.PAYMENT_SERVICE, String.class);
    }

    /**
     * 寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 ?대깽?몃? 泥섎━??ListenerContainerFactory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
        auctionBidFeeChargeRequestedKafkaListenerContainerFactory(
            ConsumerFactory<String, String> auctionBidFeeChargeRequestedConsumerFactory,
            @Value("${kafka.consumer.auction-bid-fee.concurrency:8}") int concurrency
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(auctionBidFeeChargeRequestedConsumerFactory);
        factory.setConcurrency(concurrency);
        factory.setCommonErrorHandler(createAuctionBidFeeChargeRequestedErrorHandler());
        return factory;
    }

    /**
     * 寃쎈ℓ ?낆같 ?덉튂湲??섎텋 ?붿껌 ?대깽?몄슜 ConsumerFactory
     */
    @Bean
    public ConsumerFactory<String, String> auctionBidFeeRefundRequestedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        return createConsumerFactory(bootstrapServers, KafkaConsumerGroups.PAYMENT_SERVICE, String.class);
    }

    /**
     * 寃쎈ℓ ?낆같 ?덉튂湲??섎텋 ?붿껌 ?대깽?몃? 泥섎━??ListenerContainerFactory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
        auctionBidFeeRefundRequestedKafkaListenerContainerFactory(
            ConsumerFactory<String, String> auctionBidFeeRefundRequestedConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(auctionBidFeeRefundRequestedConsumerFactory);
        factory.setCommonErrorHandler(createAuctionBidFeeChargeRequestedErrorHandler());
        return factory;
    }

    /**
     * ?먮ℓ???뺤궛 吏湲??붿껌 ?대깽?몄슜 ConsumerFactory
     * <p>
     * ???대깽?몃뒗 ?ㅻⅨ ?대깽?몃낫???ㅽ뙣 泥섎━ ?뺤콉??以묒슂?섎?濡?     * 蹂꾨룄 ListenerContainerFactory?먯꽌 ?먮윭 ?몃뱾?ш퉴吏 ?곌껐?쒕떎.
     */
    @Bean
    public ConsumerFactory<String, String> sellerSettlementPayoutRequestedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        return createConsumerFactory(bootstrapServers, KafkaConsumerGroups.PAYMENT_SERVICE, String.class);
    }

    /**
     * ?먮ℓ???뺤궛 吏湲??붿껌 ?대깽???꾩슜 ListenerContainerFactory
     *
     * ?쇰컲 ?대깽?몄? ?ㅻⅨ ??     * - ConsumerFactory瑜??곌껐??肉??꾨땲??     * - 怨듯넻 ?먮윭 ?몃뱾??CommonErrorHandler)瑜??곌껐?쒕떎.
     *
     * ???먮윭 ?몃뱾?щ뒗
     * - ?ъ떆??     * - ?ъ떆??媛??湲??쒓컙 利앷?(諛깆삤??
     * - 理쒖쥌 ?ㅽ뙣 ??DLQ 諛쒗뻾
     * ???대떦?쒕떎.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
        sellerSettlementPayoutRequestedKafkaListenerContainerFactory(
            ConsumerFactory<String, String> sellerSettlementPayoutRequestedConsumerFactory,
            // DLQ濡?硫붿떆吏瑜??ㅼ떆 諛쒗뻾?????ъ슜??KafkaTemplate
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        // ??Listener媛 ?ъ슜??ConsumerFactory ?ㅼ젙
        factory.setConsumerFactory(sellerSettlementPayoutRequestedConsumerFactory);
        // 怨듯넻 ?먮윭 ?몃뱾???곌껐
        // listener 泥섎━ 以??덉쇅媛 諛쒖깮?섎㈃ ???뺤콉???곕씪 ?ъ떆??/ DLQ ?섑뻾
        factory.setCommonErrorHandler(createPayoutRequestedErrorHandler(
                kafkaTemplate
        ));
        return factory;
    }

    /**
     * 怨듯넻 ConsumerFactory ?앹꽦 硫붿꽌??     * <p>
     * ?쒕꽕由?쑝濡???낅쭔 諛붽퓭 ?ъ궗?⑺븯?ㅻ뒗 ?섎룄??
     * ?꾩옱??targetType???몄옄濡?諛쏆?留??대??먯꽌 ?ㅼ젣濡??ъ슜?섏????딅뒗??
     */
    private <T> ConsumerFactory<String, T> createConsumerFactory(
            String bootstrapServers,
            String groupId,
            Class<T> targetType
    ) {
        Map<String, Object> props = new HashMap<>();
        // Kafka ?쒕쾭 二쇱냼
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // consumer group ?대쫫
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        // ?ㅽ봽?뗭씠 ?놁쓣 ??媛??泥섏쓬 硫붿떆吏遺???쎌쓬
        // ?댁쁺 ?섍꼍?먯꽌???좎쨷?섍쾶 ?좏깮?댁빞 ?섎뒗 ?듭뀡.
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // key??臾몄옄?대줈 ??쭅?ы솕
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // value??臾몄옄?대줈 ??쭅?ы솕
        // 利? ?꾩옱 ?ㅼ젙留?蹂대㈃ 硫붿떆吏瑜?諛붾줈 媛앹껜濡?蹂?섑븯??援ъ“???꾨땲??
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    private DefaultErrorHandler createAuctionBidFeeChargeRequestedErrorHandler() {
        return new DefaultErrorHandler((record, exception) ->
                log.error("寃쎈ℓ ?낆같 蹂댁쬆湲??붿껌 Kafka 由ъ뒪??理쒖쥌 ?ㅽ뙣: topic={}, partition={}, offset={}, key={}, payloadSnippet={}",
                        record.topic(), record.partition(), record.offset(), record.key(), summarizePayload(record), exception),
                new FixedBackOff(0L, 0L));
    }

    /**
     * ?먮ℓ???뺤궛 吏湲??붿껌 ?대깽??泥섎━ ?ㅽ뙣 ???ъ슜???먮윭 ?몃뱾???앹꽦
     * <p>
     * ?숈옉 諛⑹떇:
     * 1. ?덉쇅 諛쒖깮
     * 2. ?ъ떆??媛?ν븳 ?덉쇅硫?吏??諛깆삤?꾨줈 ?ъ떆??     * 3. ?ъ떆???잛닔 ?뚯쭊 ??DLQ濡?諛쒗뻾
     * 4. 鍮꾩옱?쒕룄 ?덉쇅??利됱떆 DLQ濡?蹂대깂
     */
    private DefaultErrorHandler createPayoutRequestedErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        // ?ъ떆??媛꾧꺽???먯젏 ?섎━??諛깆삤???뺤콉
        ExponentialBackOffWithMaxRetries backOff =
                new ExponentialBackOffWithMaxRetries(KafkaRetryPolicy.MAX_RETRIES);
        backOff.setInitialInterval(KafkaRetryPolicy.INITIAL_INTERVAL_MS);
        backOff.setMultiplier(KafkaRetryPolicy.MULTIPLIER);
        backOff.setMaxInterval(KafkaRetryPolicy.MAX_INTERVAL_MS);

        // ?ъ떆???앷퉴吏 ?ㅽ뙣??硫붿떆吏瑜?DLQ ?좏뵿?쇰줈 蹂대궡??recoverer
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(KafkaTopics.SETTLEMENT_PAYOUT_REQUESTED_DLQ, record.partition())
        );

        // recoverer + backOff瑜??ъ슜?섎뒗 ?먮윭 ?몃뱾???앹꽦
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // ?꾨옒 ?덉쇅???ъ떆?꾪빐???깃났 媛?μ꽦????떎怨??먮떒?댁꽌 利됱떆 DLQ 泥섎━
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class, WalletNotFoundException.class);

        return errorHandler;
    }

    private String summarizePayload(ConsumerRecord<?, ?> record) {
        Object value = record.value();
        if (value == null) {
            return "<empty>";
        }
        String normalized = value.toString().replaceAll("\\s+", " ").trim();
        return normalized.length() <= 300 ? normalized : normalized.substring(0, 300) + "...";
    }
}
