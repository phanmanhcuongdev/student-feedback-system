package com.ttcs.backend.adapter.in.web.dto;

public record QuestionItemResponse(
        Integer id,
        String content,
        String type
) {}