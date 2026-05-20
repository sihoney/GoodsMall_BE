package com.example.payment.common.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
/**
 * payment ?덉쇅 ?묐떟???쒖? 肄붾뱶? 湲곕낯 硫붿떆吏瑜??뺤쓽?쒕떎.
 * 怨듯넻 ?덉쇅????enum??湲곗??쇰줈 HTTP ?곹깭? ?묐떟 肄붾뱶瑜??쇨??섍쾶 ?몄텧?쒕떎.
 */
public enum ErrorCode {

    INVALID_CHARGE_REQUEST(HttpStatus.BAD_REQUEST, "異⑹쟾 ?붿껌???щ컮瑜댁? ?딆뒿?덈떎."),
    INVALID_AUCTION_BID_FEE_REQUEST(HttpStatus.BAD_REQUEST, "寃쎈ℓ ?낆같 ?섏닔猷??붿껌???щ컮瑜댁? ?딆뒿?덈떎."),
    AUCTION_BID_FEE_EVENT_REQUIRED(HttpStatus.BAD_REQUEST, "寃쎈ℓ ?낆같 ?섏닔猷??붿껌 ?대깽?멸? 鍮꾩뼱 ?덉뒿?덈떎."),
    AUCTION_BID_FEE_AUCTION_ID_REQUIRED(HttpStatus.BAD_REQUEST, "寃쎈ℓ ?낆같 ?섏닔猷??붿껌??寃쎈ℓ ID媛 ?꾩슂?⑸땲??"),
    AUCTION_BID_FEE_HIGHEST_BIDDER_REQUIRED(HttpStatus.BAD_REQUEST, "寃쎈ℓ ?낆같 ?섏닔猷??붿껌??理쒓퀬 ?낆같??ID媛 ?꾩슂?⑸땲??"),
    AUCTION_BID_FEE_HIGHEST_FEE_INVALID(HttpStatus.BAD_REQUEST, "寃쎈ℓ ?낆같 ?섏닔猷??붿껌??理쒓퀬 ?낆같??蹂댁쬆湲덉씠 ?щ컮瑜댁? ?딆뒿?덈떎."),
    INVALID_CARD_PAYMENT_REQUEST(HttpStatus.BAD_REQUEST, "移대뱶 寃곗젣 ?붿껌???щ컮瑜댁? ?딆뒿?덈떎."),
    INVALID_ORDER_PAYMENT_REQUEST(HttpStatus.BAD_REQUEST, "二쇰Ц 寃곗젣 ?붿껌???щ컮瑜댁? ?딆뒿?덈떎."),
    INVALID_WITHDRAW_REQUEST(HttpStatus.BAD_REQUEST, "異쒓툑 ?붿껌???щ컮瑜댁? ?딆뒿?덈떎."),
    INVALID_WITHDRAW_ACCOUNT(HttpStatus.BAD_REQUEST, "異쒓툑 怨꾩쥖 ?뺣낫媛 ?щ컮瑜댁? ?딆뒿?덈떎."),
    WITHDRAW_AMOUNT_BELOW_MINIMUM(HttpStatus.BAD_REQUEST, "理쒖냼 異쒓툑 湲덉븸蹂대떎 ?곸? 湲덉븸? 異쒓툑?????놁뒿?덈떎."),
    WITHDRAW_AMOUNT_NOT_GREATER_THAN_FEE(HttpStatus.BAD_REQUEST, "異쒓툑 湲덉븸? ?섏닔猷뚮낫??而ㅼ빞 ?⑸땲??"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "?낅젰媛믪씠 ?щ컮瑜댁? ?딆뒿?덈떎."),

    CHARGE_NOT_FOUND(HttpStatus.NOT_FOUND, "異⑹쟾 ?댁뿭??李얠쓣 ???놁뒿?덈떎."),
    ORDER_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "二쇰Ц 寃곗젣 ?뺣낫瑜?李얠쓣 ???놁뒿?덈떎."),
    AUCTION_DEPOSIT_NOT_FOUND(HttpStatus.NOT_FOUND, "寃쎈ℓ ?덉튂湲??뺣낫瑜?李얠쓣 ???놁뒿?덈떎."),
    ESCROW_NOT_FOUND(HttpStatus.NOT_FOUND, "?먯뒪?щ줈 ?뺣낫瑜?李얠쓣 ???놁뒿?덈떎."),
    WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "吏媛??뺣낫瑜?李얠쓣 ???놁뒿?덈떎."),

    INSUFFICIENT_WALLET_BALANCE(HttpStatus.CONFLICT, "?덉튂湲??붿븸??遺議깊빀?덈떎."),
    INVALID_STATE(HttpStatus.CONFLICT, "?꾩옱 ?곹깭?먯꽌??泥섎━?????놁뒿?덈떎."),
    AUCTION_DEPOSIT_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "寃쎈ℓ ?낆같 ?섏닔猷?泥섎━ 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎."),
    PAYMENT_GATEWAY_ERROR(HttpStatus.BAD_GATEWAY, "寃곗젣 寃뚯씠?몄썾??泥섎━ 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");

    private final HttpStatus httpStatus;
    private final String message;
}
