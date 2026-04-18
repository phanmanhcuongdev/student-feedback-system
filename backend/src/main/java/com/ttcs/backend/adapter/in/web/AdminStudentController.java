package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.ApproveStudentRequest;
import com.ttcs.backend.adapter.in.web.dto.ApproveStudentResponse;
import com.ttcs.backend.adapter.in.web.dto.PendingStudentPageResponse;
import com.ttcs.backend.adapter.in.web.dto.PendingStudentResponse;
import com.ttcs.backend.adapter.in.web.dto.RejectStudentRequest;
import com.ttcs.backend.adapter.in.web.dto.RejectStudentResponse;
import com.ttcs.backend.application.port.in.admin.ApprovalActionResult;
import com.ttcs.backend.application.port.in.admin.ApproveStudentUseCase;
import com.ttcs.backend.application.port.in.admin.GetPendingStudentsQuery;
import com.ttcs.backend.application.port.in.admin.GetStudentDocumentUseCase;
import com.ttcs.backend.application.port.in.admin.GetPendingStudentsUseCase;
import com.ttcs.backend.application.port.in.admin.PendingStudentPageResult;
import com.ttcs.backend.application.port.in.admin.PendingStudentResult;
import com.ttcs.backend.application.port.in.admin.RejectStudentUseCase;
import com.ttcs.backend.application.port.in.admin.StudentDocumentResult;
import com.ttcs.backend.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@WebAdapter
@RequestMapping("/api/admin/students")
@RequiredArgsConstructor
public class AdminStudentController {

    private final GetPendingStudentsUseCase getPendingStudentsUseCase;
    private final ApproveStudentUseCase approveStudentUseCase;
    private final RejectStudentUseCase rejectStudentUseCase;
    private final GetStudentDocumentUseCase getStudentDocumentUseCase;
    private final CurrentStudentProvider currentStudentProvider;

    @GetMapping("/pending")
    public ResponseEntity<PendingStudentPageResponse> getPendingStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) String submissionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "resubmissionCount") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        PendingStudentPageResult result = getPendingStudentsUseCase.getPendingStudents(new GetPendingStudentsQuery(
                keyword,
                departmentId,
                submissionType,
                page,
                size,
                sortBy,
                sortDir
        ));

        return ResponseEntity.ok(new PendingStudentPageResponse(
                result.items().stream().map(this::toPendingStudentResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        ));
    }

    @GetMapping("/{studentId}/documents/{documentType}")
    public ResponseEntity<ByteArrayResource> getStudentDocument(
            @PathVariable Integer studentId,
            @PathVariable String documentType
    ) {
        StudentDocumentResult result = getStudentDocumentUseCase.getDocument(studentId, documentType);
        MediaType contentType = MediaType.parseMediaType(result.contentType());

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(result.filename()).build().toString()
                )
                .body(new ByteArrayResource(result.content()));
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
