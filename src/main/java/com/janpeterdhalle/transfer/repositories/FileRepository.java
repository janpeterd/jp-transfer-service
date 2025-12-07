package com.janpeterdhalle.transfer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.janpeterdhalle.transfer.models.FileEntity;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    @Query("SELECT COALESCE(SUM(f.fileSize), 0L) FROM FileEntity f WHERE f.uploaded = true AND f.user.id = :userId")
    Long getFilesizeByUserIdAndUploadedTrue(@Param("userId") Long userId);
}
