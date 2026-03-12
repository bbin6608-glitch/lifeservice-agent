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
@Table(name = "chunk_metadata")
public class ChunkMetadataEntity {
    @Id
    private String chunkId;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(columnDefinition = "TEXT")
    private String scenarioTags; // 新增：场景化标签

    @Column(columnDefinition = "TEXT")
    private String usageScope;

    @Column(columnDefinition = "TEXT")
    private String classes;

    @Column(columnDefinition = "TEXT")
    private String methods;

    @Column(columnDefinition = "TEXT")
    private String endpoints;

    @Column(columnDefinition = "TEXT")
    private String middleware;

    @Column(columnDefinition = "TEXT")
    private String sourceFiles;
}
