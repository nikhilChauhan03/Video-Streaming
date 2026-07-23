package com.example.videostreaming.repository;

import com.example.videostreaming.entity.Video;
import com.example.videostreaming.entity.UploadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for database operations on the 'Video' entity.
 */
@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    /**
     * Finds a video record by its unique object storage key.
     *
     * @param objectKey the unique storage key
     * @return an Optional containing the Video metadata if found
     */
    Optional<Video> findByObjectKey(String objectKey);

    /**
     * Finds all video records owned by a specific user with pagination support.
     *
     * @param userId owner user's ID
     * @param pageable pagination details
     * @return a Page of matching Video records
     */
    Page<Video> findAllByUserId(Long userId, Pageable pageable);

    /**
     * Finds all video records owned by a specific user matching a given upload status with pagination support.
     *
     * @param userId owner user's ID
     * @param uploadStatus the status to filter by
     * @param pageable pagination details
     * @return a Page of matching Video records
     */
    Page<Video> findAllByUserIdAndUploadStatus(Long userId, UploadStatus uploadStatus, Pageable pageable);
}
