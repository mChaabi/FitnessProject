package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.VideoRequest;
import com.example.fitnessBackEnd.dto.VideoResponse;
import com.example.fitnessBackEnd.entity.Video;
import com.example.fitnessBackEnd.exception.BadRequestException;
import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new NotFoundException("Vidéo introuvable");
        repo.deleteById(id);
    }

    private Video fill(Video v, VideoRequest r) {
        String url = r.url().trim();
        Matcher yt = YOUTUBE.matcher(url);
        if (yt.matches()) { v.setType("YOUTUBE"); v.setSource(yt.group(1)); }          // only the 11-char ID is kept
        else if (FILE.matcher(url).matches()) { v.setType("FILE"); v.setSource(url); }
        else throw new BadRequestException("Lien invalide : utilisez un lien YouTube ou un fichier /videos/nom.mp4");

        v.setTitle(r.title().trim()); v.setDescription(r.description());
        v.setCategory(r.category()); v.setDurationMin(r.durationMin()); v.setActive(r.active());
        return v;
    }

    private VideoResponse map(Video v) {
        return new VideoResponse(v.getId(), v.getTitle(), v.getDescription(), v.getCategory(),
                v.getType(), v.getSource(), v.getDurationMin(), v.isActive());
    }
}
