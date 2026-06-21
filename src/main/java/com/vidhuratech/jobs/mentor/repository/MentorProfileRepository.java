package com.vidhuratech.jobs.mentor.repository;

import com.vidhuratech.jobs.mentor.entity.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {

    @Query("SELECT mp FROM MentorProfile mp JOIN FETCH mp.user u WHERE mp.active = true")
    List<MentorProfile> findAllActiveWithUser();

    @Query("SELECT mp FROM MentorProfile mp JOIN FETCH mp.user u")
    List<MentorProfile> findAllWithUser();

    @Query("SELECT mp FROM MentorProfile mp JOIN FETCH mp.user u WHERE mp.userId = :userId")
    Optional<MentorProfile> findByIdWithUser(@Param("userId") Long userId);

    @Query("SELECT mp FROM MentorProfile mp JOIN FETCH mp.user u " +
           "WHERE mp.active = true " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(mp.currentCompany) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(mp.currentRole) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(mp.skills) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(mp.languages) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<MentorProfile> searchActiveMentors(@Param("keyword") String keyword);
}
