package com.example.payment.wallet.infrastructure.messaging.kafka;




import com.example.payment.common.infrastructure.messaging.kafka.KafkaConsumerGroups;
import com.example.payment.common.infrastructure.messaging.kafka.KafkaTopics;
import com.example.payment.outbox.infrastructure.messaging.kafka.SellerSettlementPayoutResultOutboxEventSaver;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.wallet.domain.entity.Wallet;
import com.example.payment.wallet.domain.entity.WalletTransaction;
import com.example.payment.wallet.domain.enumtype.WalletTransactionType;
import com.example.payment.wallet.domain.repository.WalletRepository;
import com.example.payment.wallet.domain.repository.WalletTransactionRepository;
import com.example.payment.common.domain.service.IdentifierGenerator;
import com.example.payment.common.domain.service.TimeProvider;
import com.example.payment.common.infrastructure.messaging.kafka.contract.PayoutFailureReason;
import com.example.payment.common.infrastructure.messaging.kafka.contract.SellerSettlementPayoutRequestedMessage;
import com.example.payment.common.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import com.example.payment.common.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultStatus;
import com.example.payment.common.infrastructure.messaging.kafka.contract.SettlementPayoutType;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.LocalDateTime;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * settlement -> payment ?뺤궛 吏湲??붿껌 ?대깽?몃? ?뚮퉬?섍퀬 wallet ?곷┰??泥섎━?섎뒗 Kafka consumer(?뚮퉬湲???
 */
@Component
@Transactional
public class SellerSettlementPayoutRequestedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SellerSettlementPayoutRequestedEventConsumer.class);
    private static final String SELLER_SETTLEMENT_PAYOUT_REQUESTED_EVENT_TYPE = "SELLER_SETTLEMENT_PAYOUT_REQUESTED";
    private static final TypeReference<EventEnvelope<SellerSettlementPayoutRequestedMessage>>
            SELLER_SETTLEMENT_PAYOUT_REQUESTED_ENVELOPE_TYPE = new TypeReference<>() {
    };

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final IdentifierGenerator identifierGenerator;
    private final TimeProvider timeProvider;
    private final SellerSettlementPayoutResultOutboxEventSaver payoutResultOutboxEventSaver;
    private final ObjectMapper objectMapper;

    public SellerSettlementPayoutRequestedEventConsumer(
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            IdentifierGenerator identifierGenerator,
            TimeProvider timeProvider,
            SellerSettlementPayoutResultOutboxEventSaver payoutResultOutboxEventSaver,
            ObjectMapper objectMapper
    ) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.identifierGenerator = identifierGenerator;
        this.timeProvider = timeProvider;
        this.payoutResultOutboxEventSaver = payoutResultOutboxEventSaver;
        this.objectMapper = objectMapper;
    }

    /**
     * 吏湲??붿껌 ?대깽?몃? 泥섎━?섍퀬 寃곌낵瑜?payment -> settlement ?대깽?몃줈 諛쒗뻾?쒕떎.
     * <p>
     * - 鍮꾩옱?쒕룄 鍮꾩쫰?덉뒪 ?ㅽ뙣(?? WALLET_NOT_FOUND)??FAILED 寃곌낵 ?대깽?몃줈 諛섏쁺?쒕떎.
     * - 洹????덉쇅??Kafka ?먮윭 泥섎━湲곕줈 ?꾪뙆??retry(?ъ떆??/DLQ(?ы썑泥섎━?? ?뺤콉???꾩엫?쒕떎.
     */
    @KafkaListener(
            topics = KafkaTopics.SETTLEMENT_PAYOUT_REQUESTED,
            groupId = KafkaConsumerGroups.PAYMENT_SERVICE,
            containerFactory = "sellerSettlementPayoutRequestedKafkaListenerContainerFactory"
    )
    public void listen(String eventJson) {
        EventEnvelope<SellerSettlementPayoutRequestedMessage> envelope = readEnvelope(eventJson);
        validateEnvelope(envelope);
        SellerSettlementPayoutRequestedMessage event = envelope.payload();

        // ?대깽??寃利?        validateEvent(event);

        // ?쒓컙 ?쇨??깆쓣 ?꾪빐 ?대깽??泥섎━ ?쒖옉 ?쒖젏???쒓컙??湲곗??쇰줈 ?ъ슜?쒕떎.
        LocalDateTime now = timeProvider.now();
        try {
            WalletTransaction existingTransaction = walletTransactionRepository
                    .findByReferenceIdAndReferenceType(event.settlementId(), resolveSettlementReferenceType(event.settlementType()))
                    .orElse(null);
            // ?대? ?숈씪??settlementId濡?吏湲?泥섎━??嫄곕옒媛 ?덉쑝硫?以묐났 泥섎━ 諛⑹? ?꾪빐 ?깃났?쇰줈 媛꾩＜?섍퀬 醫낅즺?쒕떎.
            // settlement ?쒕퉬?ㅺ? ?곹깭瑜?留욎텧 ???덈룄濡??깃났 寃곌낵 ?대깽?몃뒗 ??긽 諛쒗뻾.
            if (existingTransaction != null) {
                publishSuccess(event, now);
                return;
            }
            Wallet wallet = walletRepository.findByMemberId(event.sellerMemberId())
                    .orElseThrow(WalletNotFoundException::new);

            // 利앷? ???붿븸(balanceAfter)??諛섑솚??嫄곕옒 ?대젰???ㅻ깄?룹쿂???④릿??
            java.math.BigDecimal balanceAfter = wallet.increaseBalance(event.payoutAmount(), now);
            walletRepository.save(wallet);

            // ?덉튂湲덉씠 蹂寃쎈릺??湲곕줉???④릿??
            WalletTransaction settlementTransaction = WalletTransaction.create(
                    identifierGenerator.generateUuid(),
                    wallet.getWalletId(),
                    event.payoutAmount(),
                    balanceAfter,
                    WalletTransactionType.SETTLEMENT,
                    event.settlementId(),
                    resolveSettlementReferenceType(event.settlementType()),
                    resolveSettlementDescription(event.settlementType()),
                    now
            );
            walletTransactionRepository.save(settlementTransaction);

            // ?깃났 ?대깽?몃? 諛쒗뻾
            publishSuccess(event, now);
        } catch (WalletNotFoundException e) {
            log.error("[PayoutFailure] settlementId={} reason={}", event.settlementId(), PayoutFailureReason.WALLET_NOT_FOUND);
            // 吏媛묒씠 ?놁뼱???ㅽ뙣??寃쎌슦 ?ъ떆???대룄 ?섎? ?놁쑝誘濡??ъ떆?꾪븯吏 ?딄쾶 ?ㅽ뙣 ?대깽?몃? 諛쒗뻾
            publishFailure(event, now);
        } catch (RuntimeException e) {
            // RETRYABLE ?ㅻ쪟??Kafka ?먮윭 泥섎━湲곗뿉???ъ떆??諛깆삤?꾨? ?섑뻾?섎룄濡??꾪뙆?쒕떎.
            log.warn("[PayoutRetryDelegated] settlementId={} message={}", event.settlementId(), e.getMessage(), e);
            throw e;
        }
    }

    private EventEnvelope<SellerSettlementPayoutRequestedMessage> readEnvelope(String eventJson) {
        try {
            return objectMapper.readValue(eventJson, SELLER_SETTLEMENT_PAYOUT_REQUESTED_ENVELOPE_TYPE);
        } catch (Exception e) {
            log.error("?먮ℓ???뺤궛 吏湲??붿껌 ?대깽???붾쾶濡쒗봽 ??쭅?ы솕???ㅽ뙣?덉뒿?덈떎.", e);
            throw new RuntimeException("?먮ℓ???뺤궛 吏湲??붿껌 ?대깽???붾쾶濡쒗봽 ??쭅?ы솕???ㅽ뙣?덉뒿?덈떎.", e);
        }
    }

    private void validateEnvelope(EventEnvelope<SellerSettlementPayoutRequestedMessage> envelope) {
        Objects.requireNonNull(envelope, "?먮ℓ???뺤궛 吏湲??붿껌 ?붾쾶濡쒗봽???꾩닔?낅땲??");
        if (envelope.eventId() == null) {
            throw new IllegalArgumentException("eventId???꾩닔?낅땲??");
        }
        if (!SELLER_SETTLEMENT_PAYOUT_REQUESTED_EVENT_TYPE.equals(envelope.eventType())) {
            throw new IllegalArgumentException("吏?먰븯吏 ?딅뒗 eventType?낅땲?? eventType=" + envelope.eventType());
        }
        if (envelope.source() == null || envelope.source().isBlank()) {
            throw new IllegalArgumentException("source???꾩닔?낅땲??");
        }
        if (envelope.aggregateId() == null) {
            throw new IllegalArgumentException("aggregateId???꾩닔?낅땲??");
        }
        if (envelope.occurredAt() == null) {
            throw new IllegalArgumentException("occurredAt? ?꾩닔?낅땲??");
        }
        if (envelope.traceId() == null || envelope.traceId().isBlank()) {
            throw new IllegalArgumentException("traceId???꾩닔?낅땲??");
        }
        if (envelope.payload() == null) {
            throw new IllegalArgumentException("payload???꾩닔?낅땲??");
        }
        if (!Objects.equals(envelope.aggregateId(), envelope.payload().settlementId())) {
            throw new IllegalArgumentException("aggregateId? payload.settlementId???쇱튂?댁빞 ?⑸땲??");
        }
        if (envelope.recipientId() != null
                && !Objects.equals(envelope.recipientId(), envelope.payload().sellerMemberId())) {
            throw new IllegalArgumentException("recipientId? payload.sellerMemberId???쇱튂?댁빞 ?⑸땲??");
        }
    }

    /**
     * wallet 諛섏쁺???뺤긽 ?꾨즺??寃쎌슦 SUCCESS 寃곌낵 ?대깽?몃? settlement濡?諛쒗뻾?쒕떎.
     */
    private void publishSuccess(SellerSettlementPayoutRequestedMessage event, LocalDateTime processedAt) {
        payoutResultOutboxEventSaver.save(new SellerSettlementPayoutResultMessage(
                identifierGenerator.generateUuid(),
                event.eventId(),
                event.settlementId(),
                event.sellerMemberId(),
                event.payoutAmount(),
                SellerSettlementPayoutResultStatus.SUCCESS,
                null,
                processedAt
        ));
    }

    /**
     * wallet 誘몄〈?ъ쿂??利됱떆 ?ㅽ뙣濡??뺤젙 媛?ν븳 寃쎌슦 FAILED 寃곌낵 ?대깽?몃? settlement濡?諛쒗뻾?쒕떎.
     * ??consumer?먯꽌???꾩옱 {@code WALLET_NOT_FOUND}留?鍮꾩옱?쒕룄 ?ㅽ뙣濡?留ㅽ븨?쒕떎.
     * <p>
     * settlement 紐⑤뱢? ???대깽?몃? ?섏떊?섏뿬 settlement ?곹깭瑜?FAILED濡?蹂寃쏀븯怨?
     * failureReason????ν븳?? NON_RETRYABLE ?ㅽ뙣???섎룞 議곗튂 ??곸쑝濡??뚮옒洹몃맂??
     */
    private void publishFailure(SellerSettlementPayoutRequestedMessage event, LocalDateTime processedAt) {
        payoutResultOutboxEventSaver.save(new SellerSettlementPayoutResultMessage(
                identifierGenerator.generateUuid(),
                event.eventId(),
                event.settlementId(),
                event.sellerMemberId(),
                event.payoutAmount(),
                SellerSettlementPayoutResultStatus.FAILED,
                PayoutFailureReason.WALLET_NOT_FOUND,
                processedAt
        ));
    }

    /**
     * settlement payout ?붿껌 ?대깽?몄쓽 ?꾩닔 ?꾨뱶? 湲곕낯 ?뺤떇??寃利앺븳??
     */
    private void validateEvent(SellerSettlementPayoutRequestedMessage event) {
        Objects.requireNonNull(event, "?먮ℓ???뺤궛 吏湲??붿껌 ?대깽?몃뒗 ?꾩닔?낅땲??");
        if (event.eventId() == null) {
            throw new IllegalArgumentException("eventId???꾩닔?낅땲??");
        }
        if (event.settlementId() == null) {
            throw new IllegalArgumentException("settlementId???꾩닔?낅땲??");
        }
        if (event.sellerMemberId() == null) {
            throw new IllegalArgumentException("sellerMemberId???꾩닔?낅땲??");
        }
        if (event.settlementType() == null) {
            throw new IllegalArgumentException("settlementType? ?꾩닔?낅땲??");
        }
        if (event.payoutAmount() == null || event.payoutAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("吏湲?湲덉븸? 0蹂대떎 而ㅼ빞 ?⑸땲??");
        }
        if (event.requestedAt() == null) {
            throw new IllegalArgumentException("requestedAt? ?꾩닔?낅땲??");
        }
    }

    private String resolveSettlementReferenceType(SettlementPayoutType settlementPayoutType) {
        return switch (settlementPayoutType) {
            case MONTHLY -> "MONTHLY_SETTLEMENT";
            case PARTIAL -> "PARTIAL_SETTLEMENT";
        };
    }

    private String resolveSettlementDescription(SettlementPayoutType settlementPayoutType) {
        return switch (settlementPayoutType) {
            case MONTHLY -> "MONTHLY_SETTLEMENT_PAYOUT";
            case PARTIAL -> "PARTIAL_SETTLEMENT_PAYOUT";
        };
    }
}
