package com.example.member.seller.application.port.out;

import com.example.member.member.domain.entity.Member;
import com.example.member.seller.domain.entity.Seller;

public interface SellerEventPort {

    void publishSellerPromoted(Member member, Seller seller);
}
