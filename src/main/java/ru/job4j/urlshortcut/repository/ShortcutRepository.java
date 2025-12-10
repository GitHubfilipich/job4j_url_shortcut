package ru.job4j.urlshortcut.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.urlshortcut.model.Shortcut;
import ru.job4j.urlshortcut.model.User;

import java.util.List;
import java.util.Optional;

public interface ShortcutRepository extends JpaRepository<Shortcut, Integer> {
    List<Shortcut> findByUser(User user);

    @Transactional
    @Query(value = """
        UPDATE shortcut
        SET total = total + 1
        WHERE shortcut = :shortcut
        RETURNING *
        """, nativeQuery = true)
    Optional<Shortcut> findByShortcutAndIncrementCounter(@Param("shortcut") String shortcut);
}
