package ru.practicum.later.item.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class AddItemRequest {
    private String url;
    private Set<String> tags = new HashSet<>();
}
