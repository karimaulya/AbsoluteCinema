package com.oop.absolutecinema.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

/**
 * Upload gambar ke ImageKit.io (image CDN) supaya dapat URL publik yang
 * disimpan di Tayangan.gambarUrl. Pendekatan ini Railway-safe: tidak menulis
 * ke filesystem lokal (yang ephemeral di Railway).
 *
 * API: POST https://upload.imagekit.io/api/v1/files/upload
 * Auth: HTTP Basic, username = private key, password kosong.
 * Multipart: field "file" (binary) + "fileName".
 */
@Service
public class ImageKitService {

    private static final String UPLOAD_URL = "https://upload.imagekit.io/api/v1/files/upload";

    @Value("${app.imagekit.private-key:}")
    private String privateKey;

    private final RestClient restClient;

    public ImageKitService() {
        this.restClient = RestClient.builder()
                .baseUrl(UPLOAD_URL)
                .build();
    }

    /**
     * Upload file dan kembalikan hosted URL dari response ImageKit.
     *
     * @throws RuntimeException bila kredensial kosong, file kosong, atau
     *                          ImageKit mengembalikan error / response tak terduga.
     */
    public String upload(MultipartFile file) {
        if (privateKey == null || privateKey.isBlank()) {
            throw new RuntimeException("ImageKit belum dikonfigurasi (IMAGEKIT_PRIVATE_KEY kosong).");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File gambar tidak boleh kosong!");
        }

        String basicAuth = Base64.getEncoder()
                .encodeToString((privateKey + ":").getBytes());

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        try {
            byte[] bytes = file.getBytes();
            String contentType = file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
            String fileName = deriveFileName(file);

            builder.part("file", new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            }).contentType(MediaType.parseMediaType(Objects.requireNonNull(contentType)));
            builder.part("fileName", Objects.requireNonNull(fileName))
                    .contentType(Objects.requireNonNull(MediaType.TEXT_PLAIN));
        } catch (IOException e) {
            throw new RuntimeException("Gagal membaca file gambar: " + e.getMessage(), e);
        }

        MultiValueMap<String, org.springframework.http.HttpEntity<?>> parts = builder.build();

        JsonNode response;
        try {
            response = restClient.post()
                    .header("Authorization", "Basic " + basicAuth)
                    .contentType(Objects.requireNonNull(MediaType.MULTIPART_FORM_DATA))
                    .body(parts)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (org.springframework.web.client.HttpClientErrorException |
                 org.springframework.web.client.HttpServerErrorException e) {
            throw new RuntimeException("ImageKit menolak upload (HTTP " + e.getStatusCode() + "): "
                    + e.getResponseBodyAsString(), e);
        } catch (org.springframework.web.client.RestClientException e) {
            throw new RuntimeException("Gagal menghubungi ImageKit: " + e.getMessage(), e);
        }

        if (response == null || response.path("url").isMissingNode()) {
            throw new RuntimeException("Response ImageKit tidak berisi field 'url'.");
        }
        return response.get("url").asText();
    }

    private String deriveFileName(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            return "tayangan-" + UUID.randomUUID();
        }
        // keep extension, randomize base name to avoid collisions in ImageKit
        int dot = original.lastIndexOf('.');
        String ext = (dot >= 0) ? original.substring(dot) : "";
        return "tayangan-" + UUID.randomUUID() + ext;
    }
}
