package com.echozoo.config.web.controller;

import com.echozoo.config.common.ApiResponse;
import com.echozoo.config.domain.ConfigGroup;
import com.echozoo.config.domain.ConfigStatus;
import com.echozoo.config.dto.GroupRequest;
import com.echozoo.config.dto.StatusRequest;
import com.echozoo.config.service.ConfigAdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GroupController {

    private final ConfigAdminService adminService;

    public GroupController(ConfigAdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/pages/{pageId}/groups")
    public ApiResponse<ConfigGroup> create(@PathVariable Long pageId, @Valid @RequestBody GroupRequest req) {
        return ApiResponse.ok(adminService.createGroup(pageId, req));
    }

    @GetMapping("/pages/{pageId}/groups")
    public ApiResponse<List<ConfigGroup>> listByPage(
            @PathVariable Long pageId,
            @RequestParam(required = false) ConfigStatus status) {
        return ApiResponse.ok(adminService.listGroups(pageId, status));
    }

    @GetMapping("/groups/{id}")
    public ApiResponse<ConfigGroup> get(@PathVariable Long id) {
        return ApiResponse.ok(adminService.getGroup(id));
    }

    @PutMapping("/groups/{id}")
    public ApiResponse<ConfigGroup> update(@PathVariable Long id, @Valid @RequestBody GroupRequest req) {
        return ApiResponse.ok(adminService.updateGroup(id, req));
    }

    @DeleteMapping("/groups/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminService.deleteGroup(id);
        return ApiResponse.ok();
    }

    @PatchMapping("/groups/{id}/status")
    public ApiResponse<ConfigGroup> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest req) {
        return ApiResponse.ok(adminService.updateGroupStatus(id, req.getStatus()));
    }
}
