package ru.job4j.urlshortcut.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.urlshortcut.model.Shortcut;
import ru.job4j.urlshortcut.model.User;

import java.util.List;
import java.util.Optional;

public interface ShortcutRepository extends JpaRepository<Shortcut, Integer> {
    Optional<Shortcut> findByUrl(String url);

    Optional<Shortcut> findByShortcut(String shortcut);

    @Transactional
    @Modifying
    @Query("update Shortcut s set s.total = s.total + 1 where s.id = :id")
    void incrementCounter(@Param("id") int id);

    List<Shortcut> findByUser(User user);
}
