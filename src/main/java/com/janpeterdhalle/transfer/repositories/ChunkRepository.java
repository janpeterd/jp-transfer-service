package com.janpeterdhalle.transfer.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.janpeterdhalle.transfer.models.Chunk;
import com.janpeterdhalle.transfer.models.FileEntity;

public interface ChunkRepository extends JpaRepository<Chunk, Long> {
    List<Chunk> findByFileOrderByChunkIndexAsc(FileEntity file);
}
