package ru.practicum.later.testutil;

import ru.practicum.later.item.UrlMetaDataRetriever;
import ru.practicum.later.item.UrlMetaDataRetrieverImpl;
import ru.practicum.later.item.dto.AddItemRequest;
import ru.practicum.later.item.model.Item;
import ru.practicum.later.user.User;
import ru.practicum.later.user.UserState;

import java.time.Instant;
import java.util.Set;

public class TestDataProvider {

    public static final Instant DATE_RESOLVED = Instant.now();

    public static AddItemRequest getDefaultAddItemRequest() {
        return getAddItemRequest("url", Set.of("tag"));
    }

    public static AddItemRequest getAddItemRequest(String url, Set<String> tags) {
        return new AddItemRequest()
                .setUrl(url)
                .setTags(tags);
    }

    public static UrlMetaDataRetriever.UrlMetadata getUrlMetadata(String url) {
        return UrlMetaDataRetrieverImpl.UrlMetadataImpl.builder()
                .normalUrl(url)
                .resolvedUrl(url)
                .mimeType("text")
                .title("title")
                .hasVideo(false)
                .hasImage(false)
                .dateResolved(DATE_RESOLVED)
                .build();
    }

    public static UrlMetaDataRetriever.UrlMetadata getUrlMetaData(String url,
                                                            String resolved,
                                                            String mime,
                                                            String title,
                                                            boolean hasImage,
                                                            boolean hasVideo) {
        return UrlMetaDataRetrieverImpl.UrlMetadataImpl .builder()
                .normalUrl(url)
                .resolvedUrl(resolved)
                .mimeType(mime)
                .title(title)
                .hasVideo(hasVideo)
                .hasImage(hasImage)
                .dateResolved(DATE_RESOLVED)
                .build();
    }

    public static Item getUnsavedItemOfUser(User user,
                                      String mime,
                                      String title,
                                      String resolvedUrl,
                                      Instant dateResolved,
                                      boolean unread) {
        return new Item()
                .setUser(user)
                .setResolvedUrl(resolvedUrl)
                .setMimeType(mime)
                .setTitle(title)
                .setHasImage(false)
                .setHasVideo(false)
                .setUnread(unread)
                .setDateResolved(dateResolved)
                .setTags(Set.of("tag1", "tag2"));
    }

    public static Item getUnsavedItemOfUser(User user) {
        return new Item()
                .setUser(user)
                .setResolvedUrl("http://resolved.com/text.html")
                .setMimeType("text")
                .setTitle("Title")
                .setHasImage(false)
                .setHasVideo(false)
                .setDateResolved(DATE_RESOLVED)
                .setTags(Set.of("tag1", "tag2"));
    }

    public static User getUnsavedUser() {
        return new User()
                .setEmail("user@email.com")
                .setFirstName("firstName")
                .setLastName("lastName")
                .setState(UserState.ACTIVE);
    }
}
