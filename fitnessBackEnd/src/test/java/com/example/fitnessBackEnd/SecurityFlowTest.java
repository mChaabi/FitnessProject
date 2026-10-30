package com.example.fitnessBackEnd;

import com.example.fitnessBackEnd.entity.User;
import com.example.fitnessBackEnd.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class SecurityFlowTest extends AbstractIntegrationTest {

    @Value("${local.server.port}") int port;
    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> call(String method, String path, String body, String... headers) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        if (body != null) b.header("Content-Type", "application/json");
        for (int i = 0; i + 1 < headers.length; i += 2) b.header(headers[i], headers[i + 1]);
        return client.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> login(String email, String password) throws Exception {
        return call("POST", "/auth/login", "{\"email\":\"" + email + "\",\"motDePasse\":\"" + password + "\"}");
    }

    private String token(String email, String password) throws Exception {
        Matcher m = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(login(email, password).body());
        assertTrue(m.find(), "el login debe devolver un token");
        return m.group(1);
    }

    private String cookieOf(HttpResponse<?> r) {
        return r.headers().allValues("Set-Cookie").stream().filter(c -> c.startsWith("fitforge_rt="))
                .map(c -> c.substring(0, c.indexOf(';'))).findFirst().orElseThrow();
    }

    private HttpResponse<String> refresh(String cookie) throws Exception {
        return call("POST", "/auth/refresh", null, "Cookie", cookie, "X-Requested-With", "fitforge");
    }

    private int get(String path, String token) throws Exception {
        return (token == null ? call("GET", path, null) : call("GET", path, null, "Authorization", "Bearer " + token)).statusCode();
    }

    // ---------- autorización ----------
    @Test
    void anonymousCannotReadMembers() throws Exception { assertEquals(401, get("/members", null)); }

    @Test void publicEndpointsAreOpen() throws Exception {
        assertEquals(200, get("/exercices", null));
        assertEquals(200, get("/plans/public", null));
    }

    @Test void roleMatrix() throws Exception {
        String member = token(user(Role.MEMBER, "Passw0rd!x").getEmail(), "Passw0rd!x");
        String reception = token(user(Role.RECEPTION, "Passw0rd!x").getEmail(), "Passw0rd!x");
        String admin = token("admin@test.local", "Admin12345!");

        assertEquals(403, get("/members", member));
        assertEquals(403, get("/dashboard", member));
        assertEquals(403, get("/admin/outbox", member));
        assertEquals(200, get("/members", reception));
        assertEquals(403, get("/dashboard", reception));
        assertEquals(200, get("/dashboard", admin));
    }

    @Test void tamperedTokenIsRejected() throws Exception {
        String t = token("admin@test.local", "Admin12345!");
        assertEquals(401, get("/dashboard", t + "x"));
    }

    // ---------- login ----------
    @Test void wrongCredentialsGiveTheSameMessageForUnknownAndKnownUsers() throws Exception {
        User u = user(Role.MEMBER, "Passw0rd!x");
        HttpResponse<String> a = login(u.getEmail(), "bad-password");
        HttpResponse<String> b = login("nobody-" + unique() + "@test.local", "bad-password");
        assertEquals(401, a.statusCode());
        assertEquals(401, b.statusCode());
        assertEquals(a.body(), b.body());
    }

    @Test void fiveFailuresLockTheAccountEvenForTheCorrectPassword() throws Exception {
        User u = user(Role.MEMBER, "Passw0rd!x");
        for (int i = 0; i < 5; i++) assertEquals(401, login(u.getEmail(), "bad-password").statusCode());
        HttpResponse<String> locked = login(u.getEmail(), "Passw0rd!x");
        assertEquals(429, locked.statusCode());
        assertTrue(locked.headers().firstValue("Retry-After").isPresent());
    }

    @Test void oversizedPasswordIsRejectedBeforeBcrypt() throws Exception {
        assertEquals(400, login("a@test.local", "x".repeat(500)).statusCode());
    }

    // ---------- refresh ----------
    @Test void refreshCookieIsHttpOnlyStrictAndRotates() throws Exception {
        User u = user(Role.MEMBER, "Passw0rd!x");
        HttpResponse<String> l = login(u.getEmail(), "Passw0rd!x");
        assertTrue(l.headers().allValues("Set-Cookie").stream().anyMatch(c -> c.contains("HttpOnly") && c.contains("SameSite=Strict")));
        assertFalse(l.body().contains("refresh"), "el refresh token nunca va en el cuerpo");

        String c1 = cookieOf(l);
        HttpResponse<String> r = refresh(c1);
        assertEquals(200, r.statusCode());
        assertNotEquals(c1, cookieOf(r));
    }

    @Test void reusingAnOldRefreshTokenRevokesTheWholeFamily() throws Exception {
        User u = user(Role.MEMBER, "Passw0rd!x");
        String c1 = cookieOf(login(u.getEmail(), "Passw0rd!x"));
        HttpResponse<String> rotated = refresh(c1);
        String c2 = cookieOf(rotated);

        assertEquals(401, refresh(c1).statusCode());      // reutilización = robo
        assertEquals(401, refresh(c2).statusCode());      // y el legítimo también queda invalidado
    }

    @Test void refreshWithoutTheAjaxHeaderIsRefused() throws Exception {
        String c = cookieOf(login(user(Role.MEMBER, "Passw0rd!x").getEmail(), "Passw0rd!x"));
        assertEquals(403, call("POST", "/auth/refresh", null, "Cookie", c).statusCode());
    }

    @Test void logoutRevokesTheSession() throws Exception {
        String c = cookieOf(login(user(Role.MEMBER, "Passw0rd!x").getEmail(), "Passw0rd!x"));
        assertEquals(204, call("POST", "/auth/logout", null, "Cookie", c, "X-Requested-With", "fitforge").statusCode());
        assertEquals(401, refresh(c).statusCode());
    }

    @Test void disabledUserCannotRefresh() throws Exception {
        User u = user(Role.MEMBER, "Passw0rd!x");
        String c = cookieOf(login(u.getEmail(), "Passw0rd!x"));
        u.setEnabled(false);
        users.save(u);
        assertEquals(401, refresh(c).statusCode());
    }
}
