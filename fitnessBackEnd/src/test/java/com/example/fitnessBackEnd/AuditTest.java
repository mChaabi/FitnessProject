package com.example.fitnessBackEnd;

import com.example.fitnessBackEnd.dto.SellRequest;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.enums.MemberStatus;
import com.example.fitnessBackEnd.enums.PaymentMethod;
import com.example.fitnessBackEnd.repository.AuditRepository;
import com.example.fitnessBackEnd.service.AuditService;
import com.example.fitnessBackEnd.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class AuditTest extends AbstractIntegrationTest {

    @Autowired AuditService audit;
    @Autowired AuditRepository log;
    @Autowired SubscriptionService subscriptions;

    @Test
    void sellingAPlanLeavesAnAuditLine() {
        Member m = member(MemberStatus.INACTIVE);
        subscriptions.sell(m.getId(), new SellRequest(plan().getId(), PaymentMethod.CASH), "reception@test.local");

        assertTrue(log.findAll().stream().anyMatch(a ->
                "SUBSCRIPTION_SOLD".equals(a.getAction()) && m.getId().equals(a.getEntityId())));
    }

    @Test
    void entriesWithoutALoggedInUserAreAttributedToTheSystem() {
        audit.record("TEST_ACTION", "MEMBER", 1L, "x".repeat(500));     // too long on purpose
        var e = log.findAll().stream().filter(a -> "TEST_ACTION".equals(a.getAction())).findFirst().orElseThrow();
        assertEquals("system", e.getActor());
        assertEquals(300, e.getSummary().length());                      // cut to the column size
    }
}