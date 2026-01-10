package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBooker_IdOrderByStartDesc(Long bookerId);

    List<Booking> findAllByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Booking> findAllByBooker_IdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime end);

    List<Booking> findAllByBooker_IdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime start);

    List<Booking> findAllByBooker_IdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    List<Booking> findAllByItem_Owner_IdOrderByStartDesc(Long ownerId);

    List<Booking> findAllByItem_Owner_IdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Booking> findAllByItem_Owner_IdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime end);

    List<Booking> findAllByItem_Owner_IdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime start);

    List<Booking> findAllByItem_Owner_IdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status);

    Optional<Booking> findFirstByItem_IdAndStatusAndEndBeforeOrderByEndDesc(
            Long itemId,
            BookingStatus status,
            LocalDateTime end
    );

    Optional<Booking> findFirstByItem_IdAndStatusAndStartAfterOrderByStartAsc(
            Long itemId,
            BookingStatus status,
            LocalDateTime start
    );

    Optional<Booking> findFirstByItem_IdAndBooker_IdAndStatusOrderByEndDesc(
            Long itemId, Long bookerId, BookingStatus status
    );

    @Query("select b " +
            "from Booking b " +
            "join fetch b.item i " +
            "join fetch b.booker u " +
            "where i.id in :itemIds " +
            "and b.status = :status " +
            "and b.end < :now " +
            "order by b.end desc")
    List<Booking> findLastBookingsForItems(
            @Param("itemIds") List<Long> itemIds,
            @Param("status") BookingStatus status,
            @Param("now") LocalDateTime now
    );

    @Query("select b " +
            "from Booking b " +
            "join fetch b.item i " +
            "join fetch b.booker u " +
            "where i.id in :itemIds " +
            "and b.status = :status " +
            "and b.start > :now " +
            "order by b.start asc")
    List<Booking> findNextBookingsForItems(
            @Param("itemIds") List<Long> itemIds,
            @Param("status") BookingStatus status,
            @Param("now") LocalDateTime now
    );
}
