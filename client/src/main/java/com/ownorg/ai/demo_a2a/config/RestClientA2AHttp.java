package com.ownorg.ai.demo_a2a.config;

import io.a2a.client.http.A2AHttpClient;
import io.a2a.client.http.A2AHttpResponse;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class RestClientA2AHttp implements A2AHttpClient {
    private final RestClient restClient;
    private final Executor executor;

    public RestClientA2AHttp(RestClient restClient, Executor executor) {
        this.restClient = restClient;
        this.executor = executor;
    }

    public A2AHttpClient.GetBuilder createGet() {
        return new SpringGetBuilder();
    }

    public A2AHttpClient.PostBuilder createPost() {
        return new SpringPostBuilder();
    }

    public A2AHttpClient.DeleteBuilder createDelete() {
        return new SpringDeleteBuilder();
    }

    private static boolean isSuccessStatus(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private abstract class SpringBuilder<T extends A2AHttpClient.Builder<T>> implements A2AHttpClient.Builder<T> {
        protected String url = "";
        protected Map<String, String> headers = new HashMap<>();

        public T url(String url) {
            this.url = url;
            return self();
        }

        public T addHeader(String name, String value) {
            this.headers.put(name, value);
            return self();
        }

        public T addHeaders(Map<String, String> headers) {
            if (headers != null && !headers.isEmpty()) {
                this.headers.putAll(headers);
            }
            return self();
        }

        @SuppressWarnings("unchecked")
        T self() {
            return (T) this;
        }

        protected void applyHeaders(RestClient.RequestHeadersSpec<?> spec) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                spec.header(entry.getKey(), entry.getValue());
            }
        }

        protected CompletableFuture<Void> asyncSSERequest(
                RestClient.RequestHeadersSpec<?> requestSpec,
                Consumer<String> messageConsumer,
                Consumer<Throwable> errorConsumer,
                Runnable completeRunnable) {

            return CompletableFuture.runAsync(() -> {
                try {
                    requestSpec
                            .accept(MediaType.TEXT_EVENT_STREAM)
                            .exchange((request, response) -> {
                                int statusCode = response.getStatusCode().value();

                                if (statusCode == 401) {
                                    errorConsumer.accept(new IOException("Authentication failed: Client credentials are missing or invalid"));
                                    return null;
                                }
                                if (statusCode == 403) {
                                    errorConsumer.accept(new IOException("Authorization failed: Client does not have permission for the operation"));
                                    return null;
                                }
                                if (!isSuccessStatus(statusCode)) {
                                    errorConsumer.accept(new IOException("Request failed with status " + statusCode));
                                    return null;
                                }

                                try (InputStream inputStream = response.getBody();
                                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        if (line.startsWith("data:")) {
                                            String data = line.substring(5).trim();
                                            if (!data.isEmpty()) {
                                                messageConsumer.accept(data);
                                            }
                                        }
                                    }
                                }
                                completeRunnable.run();
                                return null;
                            });
                } catch (Exception e) {
                    errorConsumer.accept(e);
                }
            }, executor);
        }
    }

    private class SpringGetBuilder extends SpringBuilder<GetBuilder> implements A2AHttpClient.GetBuilder {

        public A2AHttpResponse get() throws IOException, InterruptedException {
            try {
                RestClient.RequestHeadersSpec<?> spec = restClient.get().uri(url);
                applyHeaders(spec);

                return spec.exchange((request, response) -> {
                    int statusCode = response.getStatusCode().value();
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    return new SpringHttpResponse(statusCode, body);
                });
            } catch (Exception e) {
                if (e instanceof IOException) {
                    throw (IOException) e;
                }
                throw new IOException(e);
            }
        }

        public CompletableFuture<Void> getAsyncSSE(Consumer<String> messageConsumer, Consumer<Throwable> errorConsumer, Runnable completeRunnable) throws IOException, InterruptedException {
            RestClient.RequestHeadersSpec<?> spec = restClient.get().uri(url);
            applyHeaders(spec);
            return asyncSSERequest(spec, messageConsumer, errorConsumer, completeRunnable);
        }
    }

    private class SpringDeleteBuilder extends SpringBuilder<DeleteBuilder> implements A2AHttpClient.DeleteBuilder {

        public A2AHttpResponse delete() throws IOException, InterruptedException {
            try {
                RestClient.RequestHeadersSpec<?> spec = restClient.delete().uri(url);
                applyHeaders(spec);

                return spec.exchange((request, response) -> {
                    int statusCode = response.getStatusCode().value();
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    return new SpringHttpResponse(statusCode, body);
                });
            } catch (Exception e) {
                if (e instanceof IOException) {
                    throw (IOException) e;
                }
                throw new IOException(e);
            }
        }
    }

    private class SpringPostBuilder extends SpringBuilder<PostBuilder> implements A2AHttpClient.PostBuilder {
        private String body = "";

        public A2AHttpClient.PostBuilder body(String body) {
            this.body = body;
            return self();
        }

        public A2AHttpResponse post() throws IOException, InterruptedException {
            try {
                RestClient.RequestBodySpec spec = restClient.post().uri(url);
                applyHeaders(spec);

                return spec
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .exchange((request, response) -> {
                            int statusCode = response.getStatusCode().value();
                            String responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);

                            if (statusCode == 401) {
                                throw new IOException("Authentication failed: Client credentials are missing or invalid");
                            }
                            if (statusCode == 403) {
                                throw new IOException("Authorization failed: Client does not have permission for the operation");
                            }

                            return new SpringHttpResponse(statusCode, responseBody);
                        });
            } catch (Exception e) {
                if (e instanceof IOException) {
                    throw (IOException) e;
                }
                throw new IOException(e);
            }
        }

        public CompletableFuture<Void> postAsyncSSE(Consumer<String> messageConsumer, Consumer<Throwable> errorConsumer, Runnable completeRunnable) throws IOException, InterruptedException {
            RestClient.RequestBodySpec spec = restClient.post().uri(url);
            applyHeaders(spec);
            spec.contentType(MediaType.APPLICATION_JSON).body(body);
            return asyncSSERequest(spec, messageConsumer, errorConsumer, completeRunnable);
        }
    }

    private record SpringHttpResponse(int statusCode, String responseBody) implements A2AHttpResponse {
        public int status() {
            return statusCode;
        }

        public boolean success() {
            return isSuccessStatus(statusCode);
        }

        public String body() {
            return responseBody;
        }
    }
}
