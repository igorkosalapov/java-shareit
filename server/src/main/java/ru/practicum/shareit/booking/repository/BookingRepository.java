package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    @Query("select b " +
            "from Booking b " +
            "join fetch b.item i " +
            "join fetch b.booker u " +
            "where i.id in ?1 " +
            "and b.status = ?2 " +
            "and b.end < ?3 " +
            "order by b.end desc")
    List<Booking> findLastBookingsForItems(
            List<Long> itemIds,
            BookingStatus status,
            LocalDateTime now
    );

    @Query("select b " +
            "from Booking b " +
            "join fetch b.item i " +
            "join fetch b.booker u " +
            "where i.id in ?1 " +
            "and b.status = ?2 " +
            "and b.start > ?3 " +
            "order by b.start asc")
    List<Booking> findNextBookingsForItems(
            List<Long> itemIds,
            BookingStatus status,
            LocalDateTime now
    );

    boolean existsByItem_IdAndBooker_IdAndStatusAndEndLessThanEqual(
            Long itemId,
            Long bookerId,
            BookingStatus status,
            LocalDateTime now
    );
}
