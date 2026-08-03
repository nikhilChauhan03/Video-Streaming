package com.example.videostreaming.repository;

import com.example.videostreaming.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    Optional<Channel> findByOwnerId(Long ownerId);
    boolean existsByName(String name);
    boolean existsByOwnerId(Long ownerId);
}
