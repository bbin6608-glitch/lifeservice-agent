package com.lifeservice.agent.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chunk_embeddings")
public class ChunkEmbeddingEntity {

    @Id
    private String chunkId;

    private String model;

    @Column(columnDefinition = "TEXT")
    private String embedding; // Will be upgraded to vector type later
}
