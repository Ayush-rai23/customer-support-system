package com.support.backend.controller;

import com.support.backend.dto.AssignRequest;
import com.support.backend.dto.ReplyRequest;
import com.support.backend.dto.TicketDetail;
import com.support.backend.dto.TicketSummary;
import com.support.backend.dto.UpdateCategoryRequest;
import com.support.backend.dto.UpdateStatusRequest;
import com.support.backend.enums.TicketStatus;
import com.support.backend.security.AdminPrincipal;
import com.support.backend.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public ResponseEntity<Page<TicketSummary>> list(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return ResponseEntity.ok(ticketService.page(status, categoryId, assigneeId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDetail> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.detail(id));
    }

    @PostMapping("/{id}/replies")
    public ResponseEntity<TicketDetail> reply(
            @PathVariable Long id,
            @Valid @RequestBody ReplyRequest request,
            @AuthenticationPrincipal AdminPrincipal principal) {
        return ResponseEntity.ok(ticketService.addAdminReply(id, request.body(), principal.getAdmin()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketDetail> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(ticketService.changeStatus(id, request.status()));
    }

    @PatchMapping("/{id}/category")
    public ResponseEntity<TicketDetail> updateCategory(
            @PathVariable Long id,
            @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(ticketService.overrideCategory(id, request.categoryId()));
    }

    @PatchMapping("/{id}/assignee")
    public ResponseEntity<TicketDetail> updateAssignee(
            @PathVariable Long id,
            @RequestBody AssignRequest request) {
        return ResponseEntity.ok(ticketService.assign(id, request.adminId()));
    }
}
