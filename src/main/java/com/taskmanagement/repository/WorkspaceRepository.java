package com.taskmanagement.repository;

import com.taskmanagement.entity.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    Page<Workspace> findByOwnerId(Long ownerId, Pageable pageable);

    @Query("SELECT w FROM Workspace w JOIN w.members m WHERE m.user.id = :userId")
    Page<Workspace> findByMemberId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT w FROM Workspace w JOIN w.members m WHERE m.user.id = :userId AND w.name LIKE %:searchTerm%")
    List<Workspace> findByMemberIdAndNameContaining(@Param("userId") Long userId,
                                                    @Param("searchTerm") String searchTerm);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    @Query("SELECT COUNT(w) FROM Workspace w WHERE w.owner.id = :userId")
    long countByOwnerId(@Param("userId") Long userId);
}
