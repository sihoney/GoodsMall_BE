package com.todaylunch.auction;

import com.todaylunch.auction.infrastructure.client.PaymentClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class AuctionApplicationTests {

	@MockitoBean
	@SuppressWarnings("rawtypes")
	private KafkaTemplate kafkaTemplate;

	@MockitoBean
	private PaymentClient paymentClient;

	@Test
	void contextLoads() {
	}

}
