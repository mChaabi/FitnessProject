package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.VideoRequest;
import com.example.fitnessBackEnd.dto.VideoResponse;
import com.example.fitnessBackEnd.entity.Video;
import com.example.fitnessBackEnd.enums.VideoCategory;
import com.example.fitnessBackEnd.exception.BadRequestException;
import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class VideoService {

    private static final Pattern YOUTUBE = Pattern.compile(
            "^https://(?:www\\.|m\\.)?(?:youtube\\.com/(?:watch\\?(?:.*&)?v=|shorts/|embed/)|youtu\\.be/)"
                    + "([A-Za-z0-9_-]{11})(?:[?&#].*)?$");
    private static final Pattern FILE = Pattern.compile("^/videos/[A-Za-z0-9._-]{1,100}\\.(?:mp4|webm)$");

    private final VideoRepository repo;
    private final VideoStorage storage;

    @Transactional(readOnly = true)
    public List<VideoResponse> published() {
        return repo.findByActiveTrueOrderByCreatedAtDesc().stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public List<VideoResponse> all() {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream().map(this::map).toList();
    }

    @Transactional
    public VideoResponse create(VideoRequest r) {
        Video v = new Video();
        v.setCreatedAt(LocalDateTime.now());
        return map(repo.save(fill(v, r)));
    }

    @Transactional
    public VideoResponse update(Long id, VideoRequest r) {
        Video v = repo.findById(id).orElseThrow(() -> new NotFoundException("Vidéo introuvable"));
        return map(fill(v, r));
    }

    @Transactional
    public VideoResponse upload(MultipartFile file, MultipartFile thumb, String title, String description,
                                VideoCategory category, Integer durationMin, boolean active) {
        String t = title == null ? "" : title.trim();
        if (t.isEmpty() || t.length() > 120) throw new BadRequestException("Titre obligatoire (120 caractères maximum)");
        if (category == null) throw new BadRequestException("Catégorie obligatoire");
        if (description != null && description.length() > 255) throw new BadRequestException("Description trop longue");
        if (durationMin != null && (durationMin < 1 || durationMin > 600)) durationMin = null;

        String name = storage.saveVideo(file);              // checks size and real format
        String poster = storage.saveThumb(thumb);
        try {
            Video v = new Video();
            v.setTitle(t); v.setDescription(description == null || description.isBlank() ? null : description.trim());
            v.setCategory(category); v.setType("UPLOAD"); v.setSource(name); v.setThumb(poster);
            v.setDurationMin(durationMin); v.setActive(active); v.setCreatedAt(LocalDateTime.now());
            return map(repo.save(v));
        } catch (RuntimeException e) {                      // no orphan files if the database refuses
            storage.delete(name); storage.delete(poster);
            throw e;
        }
    }

    @Transactional
    public void delete(Long id) {
        Video v = repo.findById(id).orElseThrow(() -> new NotFoundException("Vidéo introuvable"));
        repo.delete(v);
        if ("UPLOAD".equals(v.getType())) { storage.delete(v.getSource()); storage.delete(v.getThumb()); }
    }

    private Video fill(Video v, VideoRequest r) {
        if (!"UPLOAD".equals(v.getType())) {                // an uploaded file keeps its source; only metadata changes
            String url = r.url() == null ? "" : r.url().trim();
            Matcher yt = YOUTUBE.matcher(url);
            if (yt.matches()) { v.setType("YOUTUBE"); v.setSource(yt.group(1)); }
            else if (FILE.matcher(url).matches()) { v.setType("FILE"); v.setSource(url); }
            else throw new BadRequestException("Lien invalide : utilisez un lien YouTube ou un fichier /videos/nom.mp4");
        }
        v.setTitle(r.title().trim()); v.setDescription(r.description());
        v.setCategory(r.category()); v.setDurationMin(r.durationMin()); v.setActive(r.active());
        return v;
    }

    private VideoResponse map(Video v) {
        return new VideoResponse(v.getId(), v.getTitle(), v.getDescription(), v.getCategory(),
                v.getType(), v.getSource(), v.getThumb(), v.getDurationMin(), v.isActive());
    }
}