package com.example.fitnessBackEnd;

import com.example.fitnessBackEnd.dto.BookingResponse;
import com.example.fitnessBackEnd.entity.ClassSession;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.entity.MembershipPlan;
import com.example.fitnessBackEnd.enums.BookingStatus;
import com.example.fitnessBackEnd.enums.EmailType;
import com.example.fitnessBackEnd.exception.ConflictException;
import com.example.fitnessBackEnd.repository.BookingRepository;
import com.example.fitnessBackEnd.repository.OutboxRepository;
import com.example.fitnessBackEnd.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class BookingConcurrencyTest extends AbstractIntegrationTest {

    @Autowired
    BookingService bookingService;
    @Autowired
    BookingRepository bookings;
    @Autowired
    OutboxRepository outbox;

    @Test
    void tenPeopleCompeteForOneSeat_exactlyOneWins() throws Exception {
        ClassSession s = session(1);
        MembershipPlan p = plan();
        List<Member> people = IntStream.range(0, 10).mapToObj(i -> activeMember(p)).toList();

        ExecutorService pool = Executors.newFixedThreadPool(10);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<?>> results = new ArrayList<>();
        for (Member m : people) {
            results.add(pool.submit(() -> { go.await(); return bookingService.book(s.getId(), m.getId(), admin()); }));
        }
        go.countDown();                                     // todos a la vez
        for (Future<?> f : results) f.get(60, TimeUnit.SECONDS);
        pool.shutdown();

        assertEquals(1, bookings.countBySessionIdAndStatus(s.getId(), BookingStatus.CONFIRMED), "nunca más plazas que aforo");
        assertEquals(9, bookings.countBySessionIdAndStatus(s.getId(), BookingStatus.WAITLIST));
    }

    @Test
    void cancellationPromotesFirstInQueueAndQueuesEmail() {
        ClassSession s = session(1);
        MembershipPlan p = plan();
        Member a = activeMember(p), b = activeMember(p), c = activeMember(p);

        BookingResponse ra = bookingService.book(s.getId(), a.getId(), admin());
        bookingService.book(s.getId(), b.getId(), admin());
        bookingService.book(s.getId(), c.getId(), admin());

        bookingService.cancel(ra.bookingId(), admin());

        assertEquals(BookingStatus.CONFIRMED, bookings.findBySessionIdAndMemberId(s.getId(), b.getId()).orElseThrow().getStatus());
        assertEquals(BookingStatus.WAITLIST, bookings.findBySessionIdAndMemberId(s.getId(), c.getId()).orElseThrow().getStatus());
        assertTrue(outbox.findAll().stream().anyMatch(e -> e.getMemberId().equals(b.getId()) && e.getType() == EmailType.WAITLIST_PROMOTED));
        assertTrue(outbox.findAll().stream().noneMatch(e -> e.getMemberId().equals(c.getId()) && e.getType() == EmailType.WAITLIST_PROMOTED));
    }

    @Test
    void memberWithoutValidSubscriptionCannotBook() {
        ClassSession s = session(5);
        Member m = memberWithPlan(plan(), LocalDate.now().minusDays(40), LocalDate.now().minusDays(10));
        assertThrows(ConflictException.class, () -> bookingService.book(s.getId(), m.getId(), admin()));
    }
}
