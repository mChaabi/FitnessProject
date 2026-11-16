package com.example.fitnessBackEnd;

import com.example.fitnessBackEnd.dto.CheckInRequest;
import com.example.fitnessBackEnd.dto.FreezeRequest;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.enums.DenyReason;
import com.example.fitnessBackEnd.enums.MemberStatus;
import com.example.fitnessBackEnd.exception.BadRequestException;
import com.example.fitnessBackEnd.exception.ConflictException;
import com.example.fitnessBackEnd.service.CheckInService;
import com.example.fitnessBackEnd.service.FreezeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FreezeTest extends AbstractIntegrationTest {

    @Autowired FreezeService freezeService;
    @Autowired CheckInService checkIn;

    @Test
    void freezeExtendsThePlanAndBlocksEntry() {
        Member m = activeMember(plan());                       // plan ends today + 60
        var res = freezeService.freeze(m.getId(), new FreezeRequest(14, "voyage"), admin());

        assertEquals(LocalDate.now().plusDays(74), res.newEndDate());
        assertEquals(MemberStatus.FROZEN, members.findById(m.getId()).orElseThrow().getStatus());
        assertEquals(DenyReason.FROZEN, checkIn.check(new CheckInRequest(m.getQrCode(), null), "t").reason());
    }

    @Test
    void earlyResumeGivesTheUnusedDaysBack() {
        Member m = activeMember(plan());
        freezeService.freeze(m.getId(), new FreezeRequest(30, null), admin());
        var res = freezeService.unfreeze(m.getId(), "admin");

        assertEquals(LocalDate.now().plusDays(60), res.newEndDate());      // frozen 0 days really: nothing added
        assertEquals(MemberStatus.ACTIVE, members.findById(m.getId()).orElseThrow().getStatus());
    }

    @Test
    void cannotFreezeTwice() {
        Member m = activeMember(plan());
        freezeService.freeze(m.getId(), new FreezeRequest(7, null), admin());
        assertThrows(ConflictException.class, () -> freezeService.freeze(m.getId(), new FreezeRequest(7, null), admin()));
    }

    @Test
    void durationOutsideTheLimitsIsRejected() {
        Member m = activeMember(plan());
        assertThrows(BadRequestException.class, () -> freezeService.freeze(m.getId(), new FreezeRequest(3, null), admin()));
        assertThrows(BadRequestException.class, () -> freezeService.freeze(m.getId(), new FreezeRequest(200, null), admin()));
    }

    @Test
    void memberWithoutACurrentPlanCannotBeFrozen() {
        Member m = member(MemberStatus.ACTIVE);                // no subscription at all
        assertThrows(ConflictException.class, () -> freezeService.freeze(m.getId(), new FreezeRequest(14, null), admin()));
    }
}