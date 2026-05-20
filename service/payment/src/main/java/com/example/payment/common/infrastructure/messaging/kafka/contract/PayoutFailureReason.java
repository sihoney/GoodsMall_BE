package com.example.payment.common.infrastructure.messaging.kafka.contract;

/**
 * payment -> settlement 吏湲?寃곌낵 ?ㅽ뙣 ?ъ쑀 ?쒖? 肄붾뱶(contract)??
 * <p>
 * ?ъ떆??遺꾨쪟:
 * - NON_RETRYABLE: ?먮룞 ?ъ떆??湲덉?, ?댁쁺 ?뺤씤 ???섎룞 議곗튂
 * - RETRYABLE: 諛깆삤??backoff) ?ъ떆???꾨낫, ?숈씪 settlementId濡?硫깅벑 泥섎━ ?좎?
 */
public enum PayoutFailureReason {

    /**
     * ?먮ℓ??wallet???놁뼱 吏湲??ㅽ뻾 遺덇? ??NON_RETRYABLE
     */
    WALLET_NOT_FOUND(false),

    /**
     * 吏湲?湲덉븸??0 ?댄븯 ?먮뒗 ?뺤콉 ?꾨컲 ??NON_RETRYABLE
     */
    INVALID_PAYOUT_AMOUNT(false),

    /**
     * ?대? 媛숈? settlementId媛 吏湲?泥섎━????NON_RETRYABLE
     */
    DUPLICATE_PAYOUT(false),

    /**
     * settlement ?앸퀎 遺덇?(寃곌낵 諛섏쁺 ?쒖젏 遺덉씪移? ??RETRYABLE
     */
    SETTLEMENT_NOT_FOUND(true),

    /**
     * ?쇱떆??DB ?ㅻ쪟/??寃쏀빀?쇰줈 泥섎━ ?ㅽ뙣 ??RETRYABLE
     */
    TEMPORARY_DB_ERROR(true),

    /**
     * 寃곌낵 ?대깽??諛쒗뻾 ?ㅽ뙣 ??RETRYABLE
     */
    KAFKA_PUBLISH_ERROR(true),

    /**
     * ??肄붾뱶濡?遺꾨쪟?섏? ?딆? ?덉쇅 ??RETRYABLE
     */
    INTERNAL_ERROR(true);

    private final boolean retryable;

    PayoutFailureReason(boolean retryable) {
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}

