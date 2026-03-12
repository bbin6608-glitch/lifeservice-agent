package com.lifeservice.agent.knowledge.build;

import com.lifeservice.agent.knowledge.model.RawSourceFile;
import java.util.List;

public interface SnapshotSourceLoader {
    List<RawSourceFile> loadAll();
}
