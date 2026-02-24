package com.institute.Institue.service;

import com.institute.Institue.dto.*;

import java.util.List;
import java.util.UUID;

public interface BatchService {

    BatchResponse createBatch(UUID orgId, BatchRequest request);

    List<BatchResponse> listBatches(UUID orgId);

    BatchResponse getBatch(UUID batchId);

    BatchResponse updateBatch(UUID batchId, BatchRequest request);

    void deleteBatch(UUID batchId);

    List<StudentResponse> getStudentsInBatch(UUID batchId);
}
