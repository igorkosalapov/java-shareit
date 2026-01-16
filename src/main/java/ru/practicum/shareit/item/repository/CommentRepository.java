package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByItem_IdOrderByCreatedDesc(Long itemId);

    @Query("select c " +
            "from Comment c " +
            "join fetch c.item i " +
            "join fetch c.author a " +
            "where i.id in ?1 " +
            "order by c.created desc")
    List<Comment> findAllByItemIdsWithItemAndAuthorOrderByCreatedDesc(List<Long> itemIds);
}
