package com.focusflow.data.repository;

import com.focusflow.domain.repository.ModelRepository;
import com.focusflow.ml.ModelManager;

public class ModelRepositoryImpl implements ModelRepository {

    private final ModelManager modelManager;

    public ModelRepositoryImpl(ModelManager modelManager) {
        this.modelManager = modelManager;
    }

    @Override
    public String activeModelVersion() {
        return modelManager.loadedVersion();
    }
}
