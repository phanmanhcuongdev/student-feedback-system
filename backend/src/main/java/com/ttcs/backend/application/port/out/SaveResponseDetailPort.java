package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.ResponseDetail;

import java.util.List;

public interface SaveResponseDetailPort {
    ResponseDetail save(ResponseDetail responseDetail);
    List<ResponseDetail> saveAll(List<ResponseDetail> responseDetails);
}