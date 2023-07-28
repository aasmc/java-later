package ru.practicum.later.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.later.BaseIntegTest;
import ru.practicum.later.item.dto.AddItemRequest;
import ru.practicum.later.item.dto.ModifyItemRequest;
import ru.practicum.later.item.model.Item;
import ru.practicum.later.user.User;
import ru.practicum.later.user.UserRepository;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.later.testutil.TestDataProvider.getUnsavedItemOfUser;
import static ru.practicum.later.testutil.TestDataProvider.getUnsavedUser;


class ItemControllerTest extends BaseIntegTest {

    private static final DateTimeFormatter dtFormatter = DateTimeFormatter
            .ofPattern("yyyy.MM.dd hh:mm:ss")
            .withZone(ZoneOffset.UTC);

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    @Test
    @SneakyThrows
    void whenAddNewItem_returnsAddedItem() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        String url = "http://info.cern.ch/hypertext/WWW/TheProject.html";
        Set<String> tags = Set.of("tag1", "tag2");
        AddItemRequest req = new AddItemRequest()
                .setUrl(url)
                .setTags(tags);

        mvc.perform(post("/items")
                .header("X-Later-User-Id", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.normalUrl", is(url)))
                .andExpect(jsonPath("$.resolvedUrl", is(url)))
                .andExpect(jsonPath("$.mimeType", is("text")))
                .andExpect(jsonPath("$.title", is("")))
                .andExpect(jsonPath("$.hasImage", is(false)))
                .andExpect(jsonPath("$.hasVideo", is(false)))
                .andExpect(jsonPath("$.unread", is(true)));
    }

    @Test
    @SneakyThrows
    void whenModifyItem_returnsModifiedItem() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item item = getUnsavedItemOfUser(user);
        item = itemRepository.save(item);
        ModifyItemRequest req = ModifyItemRequest.of(
                item.getId(),
                true,
                null,
                false
        );
        mvc.perform(patch("/items")
                        .header("X-Later-User-Id", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.normalUrl", is(item.getUrl())))
                .andExpect(jsonPath("$.resolvedUrl", is(item.getResolvedUrl())))
                .andExpect(jsonPath("$.mimeType", is(item.getMimeType())))
                .andExpect(jsonPath("$.title", is(item.getTitle())))
                .andExpect(jsonPath("$.hasImage", is(item.isHasImage())))
                .andExpect(jsonPath("$.hasVideo", is(item.isHasVideo())))
                .andExpect(jsonPath("$.unread", is(!req.isRead())))
                .andExpect(jsonPath("$.dateResolved", is(dtFormatter.format(item.getDateResolved()))));

    }

    @SneakyThrows
    @Test
    void whenDeleteItem_deletesItem() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item item = getUnsavedItemOfUser(user);
        item = itemRepository.save(item);

        mvc.perform(delete("/items/{itemId}", item.getId())
                        .header("X-Later-User-Id", user.getId()))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    void whenGetItemsByLastName_returnsCorrectList() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item item = getUnsavedItemOfUser(user);
        item = itemRepository.save(item);

        mvc.perform(get("/items")
                        .param("lastName", user.getLastName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$[0].normalUrl", is(item.getUrl())))
                .andExpect(jsonPath("$[0].resolvedUrl", is(item.getResolvedUrl())))
                .andExpect(jsonPath("$[0].mimeType", is(item.getMimeType())))
                .andExpect(jsonPath("$[0].title", is(item.getTitle())))
                .andExpect(jsonPath("$[0].hasImage", is(item.isHasImage())))
                .andExpect(jsonPath("$[0].hasVideo", is(item.isHasVideo())))
                .andExpect(jsonPath("$[0].unread", is(item.isUnread())))
                .andExpect(jsonPath("$[0].dateResolved", is(dtFormatter.format(item.getDateResolved()))));
    }

    @SneakyThrows
    @Test
    void whenGetItemsByParams_returnsCorrectList() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item item = getUnsavedItemOfUser(user);
        item = itemRepository.save(item);

        mvc.perform(
                        get("/items")
                                .header("X-Later-User-Id", user.getId())
                                .param("state", "all")
                                .param("contentType", "article")
                                .param("sort", "title")
                                .param("limit", "2")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$[0].normalUrl", is(item.getUrl())))
                .andExpect(jsonPath("$[0].resolvedUrl", is(item.getResolvedUrl())))
                .andExpect(jsonPath("$[0].mimeType", is(item.getMimeType())))
                .andExpect(jsonPath("$[0].title", is(item.getTitle())))
                .andExpect(jsonPath("$[0].hasImage", is(item.isHasImage())))
                .andExpect(jsonPath("$[0].hasVideo", is(item.isHasVideo())))
                .andExpect(jsonPath("$[0].unread", is(item.isUnread())))
                .andExpect(jsonPath("$[0].dateResolved", is(dtFormatter.format(item.getDateResolved()))));

    }

}