package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.ApproveStudentRequest;
import com.ttcs.backend.adapter.in.web.dto.ApproveStudentResponse;
import com.ttcs.backend.adapter.in.web.dto.PendingStudentResponse;
import com.ttcs.backend.adapter.in.web.dto.RejectStudentRequest;
import com.ttcs.backend.adapter.in.web.dto.RejectStudentResponse;
import com.ttcs.backend.application.port.in.admin.ApprovalActionResult;
import com.ttcs.backend.application.port.in.admin.ApproveStudentUseCase;
import com.ttcs.backend.application.port.in.admin.GetPendingStudentsUseCase;
import com.ttcs.backend.application.port.in.admin.PendingStudentResult;
import com.ttcs.backend.application.port.in.admin.RejectStudentUseCase;
import com.ttcs.backend.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@WebAdapter
@RequestMapping("/api/admin/students")
@RequiredArgsConstructor
public class AdminStudentController {

    private final GetPendingStudentsUseCase getPendingStudentsUseCase;
    private final ApproveStudentUseCase approveStudentUseCase;
    private final RejectStudentUseCase rejectStudentUseCase;
    private final CurrentStudentProvider currentStudentProvider;

    @GetMapping("/pending")
    public ResponseEntity<List<PendingStudentResponse>> getPendingStudents() {
        return ResponseEntity.ok(
                getPendingStudentsUseCase.getPendingStudents().stream()
                        .map(this::toPendingStudentResponse)
                        .toList()
        );
    }

    @PostMapping("/{studentId}/approve")
    public ResponseEntity<ApproveStudentResponse> approveStudent(
            @PathVariable Integer studentId,
            @RequestBody(required = false) ApproveStudentRequest request
    ) {
        ApprovalActionResult result = approveStudentUseCase.approve(
                studentId,
                request != null ? request.getReviewNotes() : null,
                currentStudentProvider.currentUserId()
        );
        return ResponseEntity.ok(new ApproveStudentResponse(result.success(), result.code(), result.message()));
    }

    @PostMapping("/{studentId}/reject")
    public ResponseEntity<RejectStudentResponse> rejectStudent(
            @PathVariable Integer studentId,
            @RequestBody(required = false) RejectStudentRequest request
    ) {
        ApprovalActionResult result = rejectStudentUseCase.reject(
                studentId,
                request != null ? request.getReviewReason() : null,
                request != null ? request.getReviewNotes() : null,
                currentStudentProvider.currentUserId()
        );
        return ResponseEntity.ok(new RejectStudentResponse(result.success(), result.code(), result.message()));
    }

    private PendingStudentResponse toPendingStudentResponse(PendingStudentResult result) {
        return new PendingStudentResponse(
                result.id(),
                result.name(),
                result.email(),
                result.studentCode(),
                result.departmentName(),
                result.status(),
                result.studentCardImageUrl(),
                result.nationalIdImageUrl(),
                result.reviewReason(),
                result.reviewNotes(),
                result.resubmissionCount()
        );
    }
}
