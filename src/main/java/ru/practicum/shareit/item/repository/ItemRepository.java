package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository {

    Item create(Item item);

    Item update(Item item);

    Optional<Item> findItemById(Long id);

    List<Item> findByOwner(Long ownerId);

    List<Item> findAvailable(String text);
}
