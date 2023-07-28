package ru.practicum.later.item;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UrlMetaDataRetrieverImplTest {
    private static final String HTML_NO_VIDEO_NO_IMAGES = " <!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "  <title>Title</title>\n" +
            "</head>" +
            "<body>\n" +
            "\n" +
            "<h1>Header</h1>\n" +
            "<p>Paragraph</p>\n" +
            "\n" +
            "</body>\n" +
            "</html> ";
    private static final String VALID_IMAGE_URL = "http://test.com/image.jpg";
    private static final String VALID_TEXT_URL = "http://test.com/text.html";
    private static final String VALID_VIDEO_URL = "http://test.com/video.mpg";
    private static final String INVALID_URL = "https://www.test.com/ java-%%$^&& iuyi";

    @Mock
    private HttpClient client;

    @InjectMocks
    private UrlMetaDataRetrieverImpl retriever;

    @SneakyThrows
    @Test
    void retrieve_whenClientTrowsInterruptedException_thenThrowsRuntimeException() {
        Mockito
                .when(client.send(getRequest(VALID_IMAGE_URL, "HEAD"), HttpResponse.BodyHandlers.discarding()))
                .thenThrow(InterruptedException.class);

        assertThrows(RuntimeException.class, () -> retriever.retrieve(VALID_IMAGE_URL));
    }

    @SneakyThrows
    @Test
    void retrieve_whenClientTrowsIOException_thenThrowsItemRetrieverException() {
        Mockito
                .when(client.send(getRequest(VALID_IMAGE_URL, "HEAD"), HttpResponse.BodyHandlers.discarding()))
                .thenThrow(IOException.class);

        assertThrows(ItemRetrieverException.class, () -> retriever.retrieve(VALID_IMAGE_URL));
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("getInvalidStatus")
    void retrieve_whenStatusIncorrect_throwsItemRetrieverException(int status) {
        String resolvedUrl = "file:/image.jpg";
        String mimeType = "image/jpg";
        Mockito
                .when(client.send(getRequest(VALID_IMAGE_URL, "HEAD"), HttpResponse.BodyHandlers.discarding()))
                .thenReturn(getHEADResponse(mimeType, resolvedUrl, status));

        assertThrows(ItemRetrieverException.class, () -> retriever.retrieve(VALID_IMAGE_URL));

    }

    private static Stream<Integer> getInvalidStatus() {
        return Stream.of(
                401,
                403,
                404,
                500
        );
    }

    @SneakyThrows
    @Test
    void retrieve_whenTextType_resultContainsTextInfo() {
        String mimeType = "text/html";
        Mockito
                .when(client.send(getRequest(VALID_TEXT_URL, "HEAD"), HttpResponse.BodyHandlers.discarding()))
                .thenReturn(getHEADResponse(mimeType, VALID_TEXT_URL, 200));

        Mockito
                .when(client.send(getRequest(VALID_TEXT_URL, "GET"), HttpResponse.BodyHandlers.ofString()))
                .thenReturn(getTextResponse());

        UrlMetaDataRetriever.UrlMetadata urlMetadata = retriever.retrieve(VALID_TEXT_URL);
        assertThat(urlMetadata.getResolvedUrl()).isEqualTo(VALID_TEXT_URL);
        assertThat(urlMetadata.getNormalUrl()).isEqualTo(VALID_TEXT_URL);
        assertThat(urlMetadata.getMimeType()).isEqualTo("text");
        assertThat(urlMetadata.getTitle()).isEqualTo("Title");
        assertThat(urlMetadata.isHasImage()).isFalse();
        assertThat(urlMetadata.isHasVideo()).isFalse();
    }

    @SneakyThrows
    @Test
    void retrieve_whenVideoType_resultContainsVideoInfo() {
        String resolvedUrl = "file:/video.mpg";
        String mimeType = "video/mpg";
        Mockito
                .when(client.send(getRequest(VALID_VIDEO_URL, "HEAD"), HttpResponse.BodyHandlers.discarding()))
                .thenReturn(getHEADResponse(mimeType, resolvedUrl, 200));

        UrlMetaDataRetriever.UrlMetadata urlMetadata = retriever.retrieve(VALID_VIDEO_URL);
        assertThat(urlMetadata.getResolvedUrl()).isEqualTo(resolvedUrl);
        assertThat(urlMetadata.getNormalUrl()).isEqualTo(VALID_VIDEO_URL);
        assertThat(urlMetadata.getMimeType()).isEqualTo("video");
        assertThat(urlMetadata.getTitle()).isEqualTo("video.mpg");
        assertThat(urlMetadata.isHasImage()).isFalse();
        assertThat(urlMetadata.isHasVideo()).isTrue();
    }

    @SneakyThrows
    @Test
    void retrieve_whenImageType_resultContainsImageInfo() {
        String resolvedUrl = "file:/image.jpg";
        String mimeType = "image/jpg";
        Mockito
                .when(client.send(getRequest(VALID_IMAGE_URL, "HEAD"), HttpResponse.BodyHandlers.discarding()))
                .thenReturn(getHEADResponse(mimeType, resolvedUrl, 200));

        UrlMetaDataRetriever.UrlMetadata urlMetadata = retriever.retrieve(VALID_IMAGE_URL);
        assertThat(urlMetadata.getResolvedUrl()).isEqualTo(resolvedUrl);
        assertThat(urlMetadata.getNormalUrl()).isEqualTo(VALID_IMAGE_URL);
        assertThat(urlMetadata.getMimeType()).isEqualTo("image");
        assertThat(urlMetadata.getTitle()).isEqualTo("image.jpg");
        assertThat(urlMetadata.isHasImage()).isTrue();
        assertThat(urlMetadata.isHasVideo()).isFalse();
    }

    @Test
    void retrieve_whenInvalidUrl_throwsItemRetrieverException() {
        assertThrows(ItemRetrieverException.class, () -> retriever.retrieve(INVALID_URL));
    }

    @SneakyThrows
    private HttpRequest getRequest(String url, String method) {
        URI uri = new URI(url);
        return HttpRequest.newBuilder()
                .uri(uri)
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build();
    }

    private HttpResponse<String> getTextResponse() {
        return new HttpResponse<String>() {
            @Override
            public int statusCode() {
                return 200;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<String>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return null;
            }

            @Override
            public String body() {
                return HTML_NO_VIDEO_NO_IMAGES;
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return null;
            }

            @Override
            public HttpClient.Version version() {
                return null;
            }
        };
    }

    private HttpResponse<Void> getHEADResponse(String mediaType, String uri, int status) {
        return new HttpResponse<Void>() {
            @Override
            public int statusCode() {
                return status;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<Void>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return HttpHeaders.of(
                        Map.of("Content-Type", List.of(mediaType)),
                        (s1, s2) -> true
                );
            }

            @Override
            public Void body() {
                return null;
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @SneakyThrows
            @Override
            public URI uri() {
                return new URI(uri);
            }

            @Override
            public HttpClient.Version version() {
                return null;
            }
        };
    }

}