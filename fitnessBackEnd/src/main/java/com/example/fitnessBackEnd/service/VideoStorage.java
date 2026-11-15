package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.exception.BadRequestException;
import com.example.fitnessBackEnd.exception.ConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Slf4j
public class VideoStorage {

    private static final Pattern NAME = Pattern.compile("^[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}\\.(mp4|webm|jpg)$");
    private static final int MAX_THUMB = 600 * 1024;

    private final Path dir;
    private final long maxBytes;

    public VideoStorage(@Value("${app.videos.dir}") String dir, @Value("${app.videos.max-mb:200}") long maxMb) {
        this.dir = Path.of(dir).toAbsolutePath().normalize();
        this.maxBytes = maxMb * 1024 * 1024;
        try { Files.createDirectories(this.dir); }
        catch (IOException e) { throw new IllegalStateException("Dossier vidéos inaccessible : " + this.dir, e); }
    }

    public String saveVideo(MultipartFile f) {
        if (f == null || f.isEmpty()) throw new BadRequestException("Choisissez un fichier vidéo");
        if (f.getSize() > maxBytes)
            throw new BadRequestException("Vidéo trop lourde (" + (maxBytes / 1024 / 1024) + " Mo maximum)");
        byte[] h = head(f);
        String ext = isMp4(h) ? "mp4" : isWebm(h) ? "webm" : null;
        if (ext == null) throw new BadRequestException("Format accepté : MP4 ou WebM");
        return write(f, UUID.randomUUID() + "." + ext);
    }

    /** The poster is optional: an invalid one is ignored instead of failing the upload. */
    public String saveThumb(MultipartFile f) {
        if (f == null || f.isEmpty() || f.getSize() > MAX_THUMB) return null;
        byte[] h = head(f);
        boolean jpeg = h.length >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF;
        if (!jpeg) return null;
        try { return write(f, UUID.randomUUID() + ".jpg"); } catch (RuntimeException e) { return null; }
    }

    public void delete(String name) {
        if (name == null || !NAME.matcher(name).matches()) return;
        try { Files.deleteIfExists(dir.resolve(name)); }
        catch (IOException e) { log.warn("Suppression impossible : {}", name); }
    }

    /** Null unless the name is one of OUR generated names and the file exists (blocks path traversal). */
    public Path resolve(String name) {
        if (name == null || !NAME.matcher(name).matches()) return null;
        Path p = dir.resolve(name).normalize();
        return p.startsWith(dir) && Files.isRegularFile(p) ? p : null;
    }

    private String write(MultipartFile f, String name) {
        try { f.transferTo(dir.resolve(name)); return name; }
        catch (IOException e) { log.error("Écriture impossible : {}", e.toString()); throw new ConflictException("Enregistrement impossible"); }
    }

    private static byte[] head(MultipartFile f) {
        try (InputStream in = f.getInputStream()) { return in.readNBytes(12); } catch (IOException e) { return new byte[0]; }
    }
    private static boolean isMp4(byte[] h) { return h.length >= 12 && h[4] == 'f' && h[5] == 't' && h[6] == 'y' && h[7] == 'p'; }
    private static boolean isWebm(byte[] h) {
        return h.length >= 4 && (h[0] & 0xFF) == 0x1A && (h[1] & 0xFF) == 0x45 && (h[2] & 0xFF) == 0xDF && (h[3] & 0xFF) == 0xA3;
    }
}