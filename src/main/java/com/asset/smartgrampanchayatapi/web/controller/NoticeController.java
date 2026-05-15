package com.asset.smartgrampanchayatapi.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.jpa.model.NoticeType;
import com.asset.smartgrampanchayatapi.district.service.notice.NoticeService;
import com.asset.smartgrampanchayatapi.web.dto.NoticeCreateRequest;
import com.asset.smartgrampanchayatapi.web.dto.NoticeDeleteRequest;
import com.asset.smartgrampanchayatapi.web.dto.NoticeDto;
import com.asset.smartgrampanchayatapi.web.filter.TenantCodeHeaderFilter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notices")
@Tag(name = "Notices", description = "Gram panchayat notice board (requires " + TenantCodeHeaderFilter.HEADER_TENANT_CODE + ")")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    @Operation(summary = "List notices for the tenant", description = "Optional noticeType filter: NOTICE, MEETING, MEMBER, URGENT.")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    @ApiResponse(
            responseCode = "200",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = NoticeDto.class)))
    )
    public ResponseEntity<List<NoticeDto>> list(
            @RequestParam(value = "noticeType", required = false) NoticeType noticeType,
            @RequestParam(value = "includeExpired", defaultValue = "false") boolean includeExpired
    ) {
        return noticeService
                .listNotices(noticeType, includeExpired)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant code."
                ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one notice by id")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    public ResponseEntity<NoticeDto> getById(
            @PathVariable("id") UUID id,
            @RequestParam(value = "includeExpired", defaultValue = "false") boolean includeExpired
    ) {
        return noticeService
                .getNotice(id, includeExpired)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notice not found."));
    }

    @PostMapping
    @Operation(summary = "Publish a new notice (logged-in staff user id required)")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = NoticeDto.class)))
    public ResponseEntity<NoticeDto> create(@Valid @RequestBody NoticeCreateRequest body) {
        NoticeDto saved = noticeService.createNotice(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notice (logged-in staff user id in body)")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    public ResponseEntity<Void> delete(
            @PathVariable("id") UUID id,
            @Valid @RequestBody NoticeDeleteRequest body
    ) {
        noticeService.deleteNotice(id, body);
        return ResponseEntity.noContent().build();
    }
}
