package com.janpeterdhalle.transfer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.janpeterdhalle.transfer.models.FileEntity;
import com.janpeterdhalle.transfer.models.User;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    @Query("SELECT COALESCE(SUM(f.fileSize), 0L) FROM FileEntity f WHERE f.uploaded = true AND f.user = :user")
    Long getFilesizeByUserAndUploadedTrue(@Param("user") User user);
}
