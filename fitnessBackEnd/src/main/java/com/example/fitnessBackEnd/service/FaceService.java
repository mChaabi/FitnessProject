package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.FaceMatch;
import com.example.fitnessBackEnd.dto.FaceStatus;
import com.example.fitnessBackEnd.entity.FaceTemplate;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.exception.BadRequestException;
import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.repository.FaceRepository;
import com.example.fitnessBackEnd.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FaceService {

    private final FaceClient client;
    private final FaceRepository faces;
    private final MemberRepository members;

    @Value("${app.face.threshold:0.55}") private double threshold;
    @Value("${app.face.margin:0.10}") private double margin;

    // ---------- enrollment ----------
    // No @Transactional here: the call to Python must not hold a DB connection. save() has its own.
    public FaceStatus enroll(Long memberId, boolean consent, MultipartFile photo, String staff) {
        if (!consent) throw new BadRequestException("Le consentement écrit du membre est obligatoire");
        Member m = members.findById(memberId).orElseThrow(() -> new NotFoundException("Membre introuvable"));

        float[] v = client.embed(photo);                    // the photo is discarded right after
        return store(m, v, staff);
    }

    @Transactional
    protected FaceStatus store(Member m, float[] v, String staff) {
        FaceTemplate t = faces.findByMemberId(m.getId()).orElseGet(FaceTemplate::new);
        t.setMember(m); t.setEmbedding(toBytes(v));
        t.setConsentAt(LocalDateTime.now()); t.setRecordedBy(staff);
        if (t.getCreatedAt() == null) t.setCreatedAt(LocalDateTime.now());
        faces.save(t);
        log.info("Visage enregistré pour le membre {} par {}", m.getId(), staff);   // never log the numbers
        return new FaceStatus(true, t.getConsentAt());
    }

    @Transactional(readOnly = true)
    public FaceStatus status(Long memberId) {
        return faces.findByMemberId(memberId).map(t -> new FaceStatus(true, t.getConsentAt()))
                .orElse(new FaceStatus(false, null));
    }

    @Transactional
    public void delete(Long memberId, String staff) {
        faces.deleteByMemberId(memberId);
        log.info("Visage supprimé pour le membre {} par {}", memberId, staff);
    }

    // ---------- identification ----------
    public FaceMatch identify(MultipartFile photo) {
        float[] probe = client.embed(photo);
        List<FaceTemplate> all = loadMatchable();

        double best = -1, second = -1;
        FaceTemplate bestT = null;
        for (FaceTemplate t : all) {
            double s = dot(probe, toFloats(t.getEmbedding()));
            if (s > best) { second = best; best = s; bestT = t; }
            else if (s > second) second = s;
        }

        // 3 conditions: a candidate exists, the score is high enough, and nobody else is close behind
        boolean ok = bestT != null && best >= threshold && (second < 0 || best - second >= margin);
        if (!ok) {
            log.info("Visage non reconnu (meilleur score {}, second {})", fmt(best), fmt(second));
            return new FaceMatch(false, null, null, 0, "Visage non reconnu. Utilisez le QR code ou la recherche.");
        }
        Member m = bestT.getMember();
        log.info("Visage proposé : membre {} (score {})", m.getId(), fmt(best));
        return new FaceMatch(true, m.getId(), m.getFirstName() + " " + m.getLastName(),
                (int) Math.round(Math.min(1.0, best) * 100), "Confirmez l'identité avant d'ouvrir.");
    }

    @Transactional(readOnly = true)
    protected List<FaceTemplate> loadMatchable() { return faces.findMatchable(); }

    // ---------- helpers ----------
    /** Both vectors have length 1, so the dot product IS the cosine similarity. */
    private static double dot(float[] a, float[] b) {
        double s = 0;
        for (int i = 0; i < a.length; i++) s += a[i] * b[i];
        return s;
    }
    private static byte[] toBytes(float[] v) {
        ByteBuffer b = ByteBuffer.allocate(v.length * 4);
        for (float f : v) b.putFloat(f);
        return b.array();
    }
    private static float[] toFloats(byte[] bytes) {
        ByteBuffer b = ByteBuffer.wrap(bytes);
        float[] v = new float[bytes.length / 4];
        for (int i = 0; i < v.length; i++) v[i] = b.getFloat();
        return v;
    }
    private static String fmt(double d) { return String.format("%.2f", d); }
}