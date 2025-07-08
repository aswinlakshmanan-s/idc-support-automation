package edu.northeastern;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class QdrantClient {
    private static final String QDRANT_URL = "http://localhost:6333";
    private static final String COLLECTION_NAME = "assignment_vectors";
    private static final int VECTOR_SIZE = 384;
    private static final ObjectMapper mapper = new ObjectMapper();

    public QdrantClient() throws Exception {
        ensureCollectionExists();
    }

    public void upload(List<Document> docs) throws Exception {
        List<Map<String, Object>> points = new ArrayList<>();
        for (Document doc : docs) {
            Map<String, Object> p = new HashMap<>();
            // Use a UUID string as the point ID:
            p.put("id", UUID.randomUUID().toString());
            p.put("vector", doc.vector);
            p.put("payload", Map.of("text", doc.text));
            points.add(p);
        }
        Map<String, Object> body = Map.of("points", points);
        sendPut("/collections/" + COLLECTION_NAME + "/points", body);
    }

    public List<String> search(float[] queryVector) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("vector", queryVector);
        body.put("top", 5);
        body.put("with_payload", true);

        String resp = sendPost("/collections/" + COLLECTION_NAME + "/points/search", body);
        Map<?,?> result = mapper.readValue(resp, Map.class);
        List<?> hits = (List<?>) result.get("result");

        List<String> texts = new ArrayList<>();
        for (Object o : hits) {
            Map<?,?> hit = (Map<?,?>) o;
            Map<?,?> payload = (Map<?,?>) hit.get("payload");
            texts.add((String) payload.get("text"));
        }
        return texts;
    }

    private String sendPut(String path, Object bodyObj) throws Exception {
        String json = mapper.writeValueAsString(bodyObj);
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPut put = new HttpPut(QDRANT_URL + path);
            put.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

            try (ClassicHttpResponse resp = client.executeOpen(null, put, null)) {
                int code = resp.getCode();
                String responseBody = readAll(resp.getEntity().getContent());
                if (code >= 200 && code < 300) {
                    return responseBody;
                } else {
                    System.err.println("Qdrant error (" + code + "): " + responseBody);
                    throw new RuntimeException("HTTP error code: " + code);
                }
            }
        }
    }

    private String sendPost(String path, Object bodyObj) throws Exception {
        String json = mapper.writeValueAsString(bodyObj);
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(QDRANT_URL + path);
            post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

            try (ClassicHttpResponse resp = client.executeOpen(null, post, null)) {
                int code = resp.getCode();
                String responseBody = readAll(resp.getEntity().getContent());
                if (code >= 200 && code < 300) {
                    return responseBody;
                } else {
                    System.err.println("Qdrant error (" + code + "): " + responseBody);
                    throw new RuntimeException("HTTP error code: " + code);
                }
            }
        }
    }

    private String readAll(InputStream in) throws Exception {
        byte[] bytes = in.readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private void ensureCollectionExists() throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPut put = new HttpPut(QDRANT_URL + "/collections/" + COLLECTION_NAME);
            String json = """
                {
                  "vectors": {
                    "size": %d,
                    "distance": "Cosine"
                  }
                }
                """.formatted(VECTOR_SIZE);
            put.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

            try (ClassicHttpResponse resp = client.executeOpen(null, put, null)) {
                int code = resp.getCode();
                if (code == 409) {
                    System.out.println("Collection already exists — skipping creation.");
                } else if (code < 200 || code >= 300) {
                    throw new RuntimeException("Failed to create Qdrant collection. Code: " + code);
                }
            }
        }
    }
}
