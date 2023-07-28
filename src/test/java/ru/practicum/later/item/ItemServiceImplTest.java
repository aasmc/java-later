package ru.practicum.later.item;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.later.BaseIntegTest;
import ru.practicum.later.common.InsufficientPermissionException;
import ru.practicum.later.common.NotFoundException;
import ru.practicum.later.item.dto.AddItemRequest;
import ru.practicum.later.item.dto.GetItemRequest;
import ru.practicum.later.item.dto.ItemDto;
import ru.practicum.later.item.dto.ModifyItemRequest;
import ru.practicum.later.item.model.Item;
import ru.practicum.later.user.User;
import ru.practicum.later.user.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.later.testutil.TestDataProvider.*;

class ItemServiceImplTest extends BaseIntegTest {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemService itemService;
    @MockBean
    private UrlMetaDataRetriever urlMetaDataRetriever;

    @Test
    void changeItem_itemBelongsToUserReplaceTags_changesItem() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item one = getUnsavedItemOfUser(user,
                "text",
                "ATitle",
                "bUrl",
                DATE_RESOLVED.plusMillis(3000),
                false);
        one = itemRepository.save(one);
        Set<String> newTags = Set.of("newTag1", "newTag2");
        ModifyItemRequest req = ModifyItemRequest.of(
                one.getId(),
                false,
                newTags,
                true
        );

        ItemDto dto = itemService.changeItem(user.getId(), req);
        assertThat(dto.isUnread()).isEqualTo(!req.isRead());
        assertThat(dto.getTags()).isEqualTo(newTags);
    }

    @Test
    void changeItem_itemBelongsToUserNotReplaceTags_changesItem() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item one = getUnsavedItemOfUser(user,
                "text",
                "ATitle",
                "bUrl",
                DATE_RESOLVED.plusMillis(3000),
                false);
        one = itemRepository.save(one);
        Set<String> newTags = Set.of("newTag1", "newTag2");
        ModifyItemRequest req = ModifyItemRequest.of(
                one.getId(),
                false,
                newTags,
                false
        );
        Set<String> expectedTags = new HashSet<>(one.getTags());
        expectedTags.addAll(newTags);

        ItemDto dto = itemService.changeItem(user.getId(), req);
        assertThat(dto.isUnread()).isEqualTo(!req.isRead());
        assertThat(dto.getTags()).isEqualTo(expectedTags);
    }

    @Test
    void changeItem_itemNotBelongsToUser_throws() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item item = getUnsavedItemOfUser(user,
                "text",
                "ATitle",
                "bUrl",
                DATE_RESOLVED.plusMillis(3000),
                false);
        Item saved = itemRepository.save(item);
        assertThrows(InsufficientPermissionException.class,
                ()-> itemService.changeItem(2,
                        ModifyItemRequest.of(saved.getId(), false, Set.of(), false))
        );
    }

    @Test
    void changeItem_userNotFound_throws() {
        assertThrows(NotFoundException.class,
                ()-> itemService.changeItem(1,
                        ModifyItemRequest.of(1, false, Set.of(), false))
        );
    }

    @Test
    void getItemsByRequestTagsMatch_returnsCorrectList() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item one = getUnsavedItemOfUser(user,
                "text",
                "ATitle",
                "bUrl",
                DATE_RESOLVED.plusMillis(3000),
                false);
        one = itemRepository.save(one);
        Item two = getUnsavedItemOfUser(user,
                "image",
                "BTitle",
                "aUrl",
                DATE_RESOLVED.plusMillis(2000),
                true);
        two = itemRepository.save(two);
        GetItemRequest req = GetItemRequest.of(user.getId(),
                "all",
                "all",
                "newest",
                2,
                List.of("tag1", "tag2"));

        List<ItemDto> items = itemService.getItems(req);
        assertThat(items.size()).isEqualTo(2);
        ItemDto dtoOne = items.get(0);
        ItemDto dtoTwo = items.get(1);

        assertThat(dtoOne.getId()).isEqualTo(one.getId());
        assertThat(dtoTwo.getId()).isEqualTo(two.getId());
    }

    @Test
    void getItemsByRequestTagsNotMatch_returnsEmptyList() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item one = getUnsavedItemOfUser(user,
                "text",
                "ATitle",
                "bUrl",
                DATE_RESOLVED.plusMillis(3000),
                false);
        one = itemRepository.save(one);
        Item two = getUnsavedItemOfUser(user,
                "image",
                "BTitle",
                "aUrl",
                DATE_RESOLVED.plusMillis(2000),
                true);
        two = itemRepository.save(two);
        GetItemRequest req = GetItemRequest.of(user.getId(),
                "all",
                "all",
                "newest",
                2,
                List.of("1", "2"));

        List<ItemDto> items = itemService.getItems(req);
        assertThat(items).isEmpty();
    }

    @Test
    void getItemsByRequestContentType_returnsCorrectList() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item one = getUnsavedItemOfUser(user,
                "text",
                "ATitle",
                "bUrl",
                DATE_RESOLVED.plusMillis(3000),
                false);
        one = itemRepository.save(one);
        Item two = getUnsavedItemOfUser(user,
                "image",
                "BTitle",
                "aUrl",
                DATE_RESOLVED.plusMillis(2000),
                true);
        two = itemRepository.save(two);
        GetItemRequest req = GetItemRequest.of(user.getId(),
                "all",
                "image",
                "newest",
                2,
                null);

        List<ItemDto> items = itemService.getItems(req);
        assertThat(items.size()).isEqualTo(1);
        ItemDto dtoOne = items.get(0);

        assertThat(dtoOne.getId()).isEqualTo(two.getId());
    }

    @Test
    void getItemsByRequestUnread_returnsCorrectList() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item one = getUnsavedItemOfUser(user,
                "text",
                "ATitle",
                "bUrl",
                DATE_RESOLVED.plusMillis(3000),
                false);
        one = itemRepository.save(one);
        Item two = getUnsavedItemOfUser(user,
                "text",
                "BTitle",
                "aUrl",
                DATE_RESOLVED.plusMillis(2000),
                true);
        two = itemRepository.save(two);
        GetItemRequest req = GetItemRequest.of(user.getId(),
                "unread",
                "all",
                "newest",
                2,
                null);

        List<ItemDto> items = itemService.getItems(req);
        assertThat(items.size()).isEqualTo(1);
        ItemDto dtoOne = items.get(0);

        assertThat(dtoOne.getId()).isEqualTo(two.getId());
    }

    @Test
    void getItemsByRequestLimit1_returnsCorrectList() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item one = getUnsavedItemOfUser(user,
                "text",
                "ATitle",
                "bUrl",
                DATE_RESOLVED.plusMillis(3000),
                false);
        one = itemRepository.save(one);
        Item two = getUnsavedItemOfUser(user,
                "text",
                "BTitle",
                "aUrl",
                DATE_RESOLVED.plusMillis(2000),
                true);
        two = itemRepository.save(two);
        GetItemRequest req = GetItemRequest.of(user.getId(),
                "all",
                "all",
                "newest",
                1,
                null);

        List<ItemDto> items = itemService.getItems(req);
        assertThat(items.size()).isEqualTo(1);
        ItemDto dtoOne = items.get(0);

        assertThat(dtoOne.getId()).isEqualTo(one.getId());
    }

    @Test
    void getItemsByRequestSortByNewest_returnsCorrectList() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item one = getUnsavedItemOfUser(user,
                "text",
                "ATitle",
                "bUrl",
                DATE_RESOLVED.plusMillis(3000),
                false);
        one = itemRepository.save(one);
        Item two = getUnsavedItemOfUser(user,
                "text",
                "BTitle",
                "aUrl",
                DATE_RESOLVED.plusMillis(2000),
                true);
        two = itemRepository.save(two);
        GetItemRequest req = GetItemRequest.of(user.getId(),
                "all",
                "all",
                "newest",
                2,
                null);

        List<ItemDto> items = itemService.getItems(req);
        assertThat(items.size()).isEqualTo(2);
        ItemDto dtoOne = items.get(0);
        ItemDto dtoTwo = items.get(1);

        assertThat(dtoOne.getId()).isEqualTo(one.getId());
        assertThat(dtoTwo.getId()).isEqualTo(two.getId());
    }

    @Test
    void getItemsByRequestSortByOldest_returnsCorrectList() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item one = getUnsavedItemOfUser(user,
                "text",
                "ATitle",
                "bUrl",
                DATE_RESOLVED.plusMillis(3000),
                false);
        one = itemRepository.save(one);
        Item two = getUnsavedItemOfUser(user,
                "text",
                "BTitle",
                "aUrl",
                DATE_RESOLVED.plusMillis(2000),
                true);
        two = itemRepository.save(two);
        GetItemRequest req = GetItemRequest.of(user.getId(),
                "all",
                "all",
                "oldest",
                2,
                null);

        List<ItemDto> items = itemService.getItems(req);
        assertThat(items.size()).isEqualTo(2);
        ItemDto dtoOne = items.get(0);
        ItemDto dtoTwo = items.get(1);

        assertThat(dtoOne.getId()).isEqualTo(two.getId());
        assertThat(dtoTwo.getId()).isEqualTo(one.getId());
    }

    @Test
    void getItemsByRequestSortBySite_returnsCorrectList() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item one = getUnsavedItemOfUser(user,
                "text",
                "ATitle",
                "bUrl",
                DATE_RESOLVED.plusMillis(1000),
                false);
        one = itemRepository.save(one);
        Item two = getUnsavedItemOfUser(user,
                "text",
                "BTitle",
                "aUrl",
                DATE_RESOLVED.plusMillis(2000),
                true);
        two = itemRepository.save(two);
        GetItemRequest req = GetItemRequest.of(user.getId(),
                "all",
                "all",
                "site",
                2,
                null);

        List<ItemDto> items = itemService.getItems(req);
        assertThat(items.size()).isEqualTo(2);
        ItemDto dtoOne = items.get(0);
        ItemDto dtoTwo = items.get(1);

        assertThat(dtoOne.getId()).isEqualTo(two.getId());
        assertThat(dtoTwo.getId()).isEqualTo(one.getId());
    }

    @Test
    void getItemsByRequestSortByTitle_returnsCorrectList() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item one = getUnsavedItemOfUser(user,
                "text",
                "ATitle",
                "aUrl",
                DATE_RESOLVED.plusMillis(1000),
                false);
        one = itemRepository.save(one);
        Item two = getUnsavedItemOfUser(user,
                "text",
                "BTitle",
                "bUrl",
                DATE_RESOLVED.plusMillis(2000),
                true);
        two = itemRepository.save(two);
        GetItemRequest req = GetItemRequest.of(user.getId(),
                "all",
                "all",
                "title",
                2,
                null);

        List<ItemDto> items = itemService.getItems(req);
        assertThat(items.size()).isEqualTo(2);
        ItemDto dtoOne = items.get(0);
        ItemDto dtoTwo = items.get(1);

        assertThat(dtoOne.getId()).isEqualTo(one.getId());
        assertThat(dtoTwo.getId()).isEqualTo(two.getId());
    }

    @Test
    void getUserItems_whenNoUserWithItems_returnsEmptyList() {
        List<ItemDto> dtos = itemService.getUserItems("lastname");
        assertThat(dtos).isEmpty();
    }

    @Test
    void getUserItems_whenUserWithItemsExists_returnsCorrectList() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item item = getUnsavedItemOfUser(user);
        item = itemRepository.save(item);

        List<ItemDto> dtos = itemService.getUserItems(user.getLastName());
        assertThat(dtos.size()).isEqualTo(1);
        ItemDto dto = dtos.get(0);
        assertThat(dto.getId()).isEqualTo(item.getId());
        assertThat(dto.getTitle()).isEqualTo(item.getTitle());
        assertThat(dto.getNormalUrl()).isEqualTo(item.getUrl());
        assertThat(dto.getResolvedUrl()).isEqualTo(item.getResolvedUrl());
        assertThat(dto.isHasImage()).isEqualTo(item.isHasImage());
        assertThat(dto.isHasVideo()).isEqualTo(item.isHasVideo());
        assertThat(dto.getMimeType()).isEqualTo(item.getMimeType());
        assertThat(dto.isUnread()).isEqualTo(item.isUnread());
        assertThat(dto.getTags()).isEqualTo(item.getTags());
    }

    @Test
    void deleteItem_deletesItemOfUser() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item item = getUnsavedItemOfUser(user);
        item = itemRepository.save(item);

        itemService.deleteItem(user.getId(), item.getId());

        List<ItemDto> items = itemService.getItems(user.getId());
        assertThat(items).isEmpty();
    }

    @Test
    void addNewItem_whenItemExists_newTagsAreAdded() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item item = getUnsavedItemOfUser(user);
        item = itemRepository.save(item);
        Set<String> tags = Set.of("tag3", "tag4");
        AddItemRequest req = getAddItemRequest(item.getResolvedUrl(), tags);
        UrlMetaDataRetriever.UrlMetadata metadata = getUrlMetaData(item.getUrl(),
                item.getResolvedUrl(),
                item.getMimeType(),
                item.getTitle(),
                item.isHasImage(),
                item.isHasVideo());

        Mockito
                .when(urlMetaDataRetriever.retrieve(item.getResolvedUrl()))
                .thenReturn(metadata);
        Set<String> expectedTags = new HashSet<>(req.getTags());
        expectedTags.addAll(item.getTags());

        ItemDto dto = itemService.addNewItem(user.getId(), req);
        assertThat(dto.getTitle()).isEqualTo(item.getTitle());
        assertThat(dto.getTags()).isEqualTo(expectedTags);
        assertThat(dto.getNormalUrl()).isEqualTo(item.getUrl());
        assertThat(dto.getResolvedUrl()).isEqualTo(item.getResolvedUrl());
        assertThat(dto.isHasVideo()).isEqualTo(item.isHasVideo());
        assertThat(dto.isHasImage()).isEqualTo(item.isHasImage());
        assertThat(dto.isUnread()).isEqualTo(item.isUnread());
    }

    @Test
    void addNewItem_whenItemNotExists_savesNewItem() {
        String url = "http://test.com";
        Set<String> tags = Set.of("tag1", "tag2");
        AddItemRequest req = getAddItemRequest(url, tags);
        UrlMetaDataRetriever.UrlMetadata metadata = getUrlMetadata(url);
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Mockito
                .when(urlMetaDataRetriever.retrieve(url))
                .thenReturn(metadata);

        ItemDto dto = itemService.addNewItem(user.getId(), req);
        assertThat(dto.getTitle()).isEqualTo(metadata.getTitle());
        assertThat(dto.getTags()).isEqualTo(req.getTags());
        assertThat(dto.getNormalUrl()).isEqualTo(metadata.getNormalUrl());
        assertThat(dto.getResolvedUrl()).isEqualTo(metadata.getResolvedUrl());
        assertThat(dto.isHasVideo()).isEqualTo(metadata.isHasVideo());
        assertThat(dto.isHasImage()).isEqualTo(metadata.isHasImage());
        assertThat(dto.isUnread()).isTrue();
    }

    @Test
    void addNewItem_whenUserNotFound_throws() {
        AddItemRequest req = getDefaultAddItemRequest();
        Long unknownUserId = 1000L;
        assertThrows(InsufficientPermissionException.class,
                () -> itemService.addNewItem(unknownUserId, req));
    }

    @Test
    void getItems_whenHasItems_returnsListOfItems() {
        User user = getUnsavedUser();
        user = userRepository.save(user);
        Item item = getUnsavedItemOfUser(user);
        itemRepository.save(item);

        List<ItemDto> items = itemService.getItems(user.getId());
        assertThat(items.size()).isEqualTo(1);

        ItemDto dto = items.get(0);
        assertThat(dto.getId()).isEqualTo(item.getId());
        assertThat(dto.getTitle()).isEqualTo(item.getTitle());
        assertThat(dto.getNormalUrl()).isEqualTo(item.getUrl());
        assertThat(dto.getResolvedUrl()).isEqualTo(item.getResolvedUrl());
        assertThat(dto.isHasImage()).isEqualTo(item.isHasImage());
        assertThat(dto.isHasVideo()).isEqualTo(item.isHasVideo());
        assertThat(dto.getMimeType()).isEqualTo(item.getMimeType());
        assertThat(dto.isUnread()).isEqualTo(item.isUnread());
        assertThat(dto.getTags()).isEqualTo(item.getTags());
    }

    @Test
    void getItems_whenNoItems_returnsEmptyList() {
        List<ItemDto> items = itemService.getItems(1);
        assertThat(items).isEmpty();
    }

}