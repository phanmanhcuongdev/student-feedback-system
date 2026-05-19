package com.ttcs.backend.application.port.out;

import java.util.List;

public interface GenerateTextEmbeddingPort {
    TextEmbeddingResult embed(List<String> texts);

    record TextEmbeddingResult(
            String modelName,
            List<List<Double>> vectors
    ) {
    }
}
