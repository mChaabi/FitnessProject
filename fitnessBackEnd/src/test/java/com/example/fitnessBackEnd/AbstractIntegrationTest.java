package com.example.fitnessBackEnd;

import com.example.fitnessBackEnd.entity.*;
import com.example.fitnessBackEnd.enums.MemberStatus;
import com.example.fitnessBackEnd.enums.Role;
import com.example.fitnessBackEnd.enums.SubscriptionStatus;
import com.example.fitnessBackEnd.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mysql.MySQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class AbstractIntegrationTest {

    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.0");
    static { MYSQL.start(); }                    // un contenedor para toda la ejecución (Ryuk lo limpia)

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", MYSQL::getJdbcUrl);
        r.add("spring.datasource.username", MYSQL::getUsername);
        r.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    protected UserRepository users;
    @Autowired protected MemberRepository members;
    @Autowired protected CoachRepository coaches;
    @Autowired protected PlanRepository plans;
    @Autowired protected SubscriptionRepository subs;
    @Autowired protected SessionRepository sessions;
    @Autowired protected PasswordEncoder encoder;

    protected static String unique() { return UUID.randomUUID().toString().substring(0, 8); }

    protected User user(Role role, String password) {
        return users.save(User.builder().nom("T").prenom("T").email(role.name().toLowerCase() + "-" + unique() + "@test.local")
                .motDePasse(encoder.encode(password)).role(role).enabled(true).build());
    }

    protected MembershipPlan plan() {
        return plans.save(MembershipPlan.builder().name("P-" + unique()).price(BigDecimal.TEN).durationDays(30).active(true).build());
    }

    protected Member member(MemberStatus status) {
        return members.save(Member.builder().firstName("M").lastName("T").email("m-" + unique() + "@test.local").status(status).build());
    }

    protected Member memberWithPlan(MembershipPlan p, LocalDate start, LocalDate end) {
        Member m = member(MemberStatus.ACTIVE);
        subs.save(Subscription.builder().member(m).plan(p).startDate(start).endDate(end).status(SubscriptionStatus.ACTIVE).build());
        return m;
    }

    protected Member activeMember(MembershipPlan p) {
        return memberWithPlan(p, LocalDate.now().minusDays(1), LocalDate.now().plusDays(60));
    }

    protected ClassSession session(int capacity) {
        Coach c = coaches.save(Coach.builder().user(user(Role.COACH, "Passw0rd!x")).active(true).build());
        ClassSession s = new ClassSession();
        s.setName("Yoga-" + unique()); s.setCoach(c); s.setCapacity(capacity);
        s.setSessionDate(LocalDate.now().plusDays(1));
        s.setStartTime(LocalTime.of(10, 0)); s.setEndTime(LocalTime.of(11, 0));
        return sessions.save(s);
    }

    protected Authentication admin() {
        return new UsernamePasswordAuthenticationToken("admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
