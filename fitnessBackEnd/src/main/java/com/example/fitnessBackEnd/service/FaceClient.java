package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.exception.BadRequestException;
import com.example.fitnessBackEnd.exception.ConflictException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Component
public class FaceClient {

    private static final Map<String, String> REASONS = Map.of(
            "no_face", "Aucun visage détecté. Placez-vous face à la caméra.",
            "several_faces", "Plusieurs visages détectés. Une seule personne à la fois.",
            "face_too_small", "Visage trop loin. Rapprochez-vous.",
            "too_blurry", "Image floue. Améliorez la lumière et restez immobile.");

    private final RestClient http;
    private final boolean enabled;

    public FaceClient(@Value("${app.face.enabled:false}") boolean enabled,
                      @Value("${app.face.url}") String url,
                      @Value("${app.face.api-key:}") String key) {
        this.enabled = enabled;
        if (enabled && key.isBlank()) throw new IllegalStateException("FACE_API_KEY manquante");
        var f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(2000);
        f.setReadTimeout(8000);
        this.http = RestClient.builder().baseUrl(url).requestFactory(f)
                .defaultHeader("X-Internal-Key", key).build();
    }

    /** Photo -> 128 numbers. Throws a clear business error when the photo is not usable. */
    public float[] embed(MultipartFile photo) {
        if (!enabled) throw new ConflictException("La reconnaissance faciale est désactivée");
        if (photo == null || photo.isEmpty() || photo.getSize() > 2 * 1024 * 1024)
            throw new BadRequestException("Photo invalide (2 Mo maximum)");
        String type = photo.getContentType();
        if (type == null || !(type.equals("image/jpeg") || type.equals("image/png")))
            throw new BadRequestException("Format accepté : JPEG ou PNG");

        try {
            var body = new LinkedMultiValueMap<String, Object>();
            body.add("image", new ByteArrayResource(photo.getBytes()) {
                @Override public String getFilename() { return "photo.jpg"; }
            });
            JsonNode r = http.post().uri("/embed").contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body).retrieve().body(JsonNode.class);

            if (r == null || !r.path("ok").asBoolean())
                throw new BadRequestException(REASONS.getOrDefault(r == null ? "" : r.path("reason").asText(),
                        "Photo non utilisable"));
            JsonNode e = r.path("embedding");
            float[] v = new float[e.size()];
            for (int i = 0; i < v.length; i++) v[i] = (float) e.get(i).asDouble();
            if (v.length != 128) throw new IllegalStateException("embedding inattendu");
            return v;
        } catch (BadRequestException | ConflictException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
        org.slf4j.LoggerFactory.getLogger(FaceClient.class)
                .error("Appel du service facial impossible : {}", e.toString());
        throw new ConflictException("Service de reconnaissance indisponible");
    }
    }
}