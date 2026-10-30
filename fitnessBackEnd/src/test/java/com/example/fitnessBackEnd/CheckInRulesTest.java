package com.example.fitnessBackEnd;

import com.example.fitnessBackEnd.dto.CheckInRequest;
import com.example.fitnessBackEnd.dto.CheckInResult;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.enums.CheckInOutcome;
import com.example.fitnessBackEnd.enums.DenyReason;
import com.example.fitnessBackEnd.enums.MemberStatus;
import com.example.fitnessBackEnd.exception.BadRequestException;
import com.example.fitnessBackEnd.service.CheckInService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CheckInRulesTest extends AbstractIntegrationTest {

    @Autowired
    CheckInService checkIn;

    private CheckInResult scan(Member m) { return checkIn.check(new CheckInRequest(m.getQrCode(), null), "test"); }

    @Test
    void validMemberEntersThenSecondScanIsDuplicate() {
        Member m = activeMember(plan());
        assertEquals(CheckInOutcome.ALLOWED, scan(m).outcome());
        assertEquals(CheckInOutcome.DUPLICATE, scan(m).outcome());
    }

    @Test void expiredSubscriptionIsDeniedEvenIfJobHasNotRun() {
        Member m = memberWithPlan(plan(), LocalDate.now().minusDays(40), LocalDate.now().minusDays(1));
        CheckInResult r = scan(m);
        assertEquals(CheckInOutcome.DENIED, r.outcome());
        assertEquals(DenyReason.EXPIRED, r.reason());
    }

    @Test void memberWithoutSubscriptionIsDenied() {
        CheckInResult r = scan(member(MemberStatus.ACTIVE));
        assertEquals(DenyReason.NO_SUBSCRIPTION, r.reason());
    }

    @Test void inactiveMemberIsDeniedEvenWithPlan() {
        Member m = activeMember(plan());
        m.setStatus(MemberStatus.INACTIVE);
        members.save(m);
        assertEquals(DenyReason.INACTIVE, scan(m).reason());
    }

    @Test void lastDayStillWorksAndWarns() {
        Member m = memberWithPlan(plan(), LocalDate.now().minusDays(29), LocalDate.now());
        CheckInResult r = scan(m);
        assertEquals(CheckInOutcome.ALLOWED, r.outcome());
        assertTrue(r.expiringSoon());
        assertEquals(0L, r.daysLeft());
    }

    @Test void unknownQrIsDenied() {
        CheckInResult r = checkIn.check(new CheckInRequest("00000000-fake", null), "test");
        assertEquals(DenyReason.NOT_FOUND, r.reason());
    }

    @Test void sendingBothOrNeitherIsBadRequest() {
        assertThrows(BadRequestException.class, () -> checkIn.check(new CheckInRequest(null, null), "t"));
        assertThrows(BadRequestException.class, () -> checkIn.check(new CheckInRequest("x", 1L), "t"));
    }
}