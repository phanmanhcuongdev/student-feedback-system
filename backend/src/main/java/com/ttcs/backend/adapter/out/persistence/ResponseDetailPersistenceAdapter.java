package com.ttcs.backend.adapter.out.persistence;

import com.ttcs.backend.application.domain.model.ResponseDetail;
import com.ttcs.backend.application.port.out.SaveResponseDetailPort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class ResponseDetailPersistenceAdapter implements SaveResponseDetailPort {

    private final ResponseDetailRepository responseDetailRepository;

    @Override
    public ResponseDetail save(ResponseDetail responseDetail) {
        ResponseDetailEntity savedEntity =
                responseDetailRepository.save(ResponseDetailMapper.toEntity(responseDetail));
        return ResponseDetailMapper.toDomain(savedEntity);
    }

    @Override
    public List<ResponseDetail> saveAll(List<ResponseDetail> responseDetails) {
        List<ResponseDetailEntity> entities = responseDetails.stream()
                .map(ResponseDetailMapper::toEntity)
                .toList();

        List<ResponseDetailEntity> savedEntities = responseDetailRepository.saveAll(entities);

        return savedEntities.stream()
                .map(ResponseDetailMapper::toDomain)
                .toList();
    }
}