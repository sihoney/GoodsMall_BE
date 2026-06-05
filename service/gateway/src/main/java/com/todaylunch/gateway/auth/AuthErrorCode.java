package com.todaylunch.gateway.auth;

public enum AuthErrorCode {
    TOKEN_EXPIRED("?≪꽭???좏겙??留뚮즺?섏뿀?듬땲??"),
    INVALID_TOKEN("?좏슚?섏? ?딆? ?≪꽭???좏겙?낅땲??"),
    UNAUTHORIZED("Authorization ?ㅻ뜑媛 ?녾굅???뺤떇???щ컮瑜댁? ?딆뒿?덈떎."),
    ACCESS_DENIED("?대떦 由ъ냼?ㅼ뿉 ?묎렐??沅뚰븳???놁뒿?덈떎.");

    private final String message;

    AuthErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

