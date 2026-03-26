package com.example.member.infrastructure.messaging;

import com.example.member.application.event.MemberSignedUpEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberEventKafkaProducer { // KafkaProducer 역할을 하는 컴포넌트

    private final KafkaTemplate<String, MemberSignedUpEvent> kafkaTemplate;

    @Value("${member.kafka.topic.signed-up}")
    private String memberSignedUpTopic;

    public void sendMemberSignedUp(MemberSignedUpEvent event) {
        // Kafka 토픽으로 이벤트 발행 : key는 memberId, value는 이벤트 객체
        kafkaTemplate.send(
            memberSignedUpTopic, 
            event.memberId().toString(), 
            event
        ).whenComplete((result, exception) -> {
            if (exception != null) {
                log.error(
                        "Failed to publish MemberSignedUpEvent. topic={} memberId={}",
                        memberSignedUpTopic,
                        event.memberId(),
                        exception
                );
                return;
            }

            log.info(
                    "Published MemberSignedUpEvent. topic={} partition={} offset={} memberId={}",
                    memberSignedUpTopic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    event.memberId()
            );
        });
    }
}
