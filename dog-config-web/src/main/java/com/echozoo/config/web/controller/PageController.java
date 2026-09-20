package com.echozoo.config.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.echozoo.config.common.ApiResponse;
import com.echozoo.config.domain.ConfigPage;
import com.echozoo.config.domain.ConfigStatus;
import com.echozoo.config.dto.PageRequest;
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

@RestController
@RequestMapping("/api/pages")
public class PageController {

    private final ConfigAdminService adminService;

    public PageController(ConfigAdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    public ApiResponse<ConfigPage> create(@Valid @RequestBody PageRequest req) {
        return ApiResponse.ok(adminService.createPage(req));
    }

    @GetMapping
    public ApiResponse<Page<ConfigPage>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ConfigStatus status,
            @RequestParam(defaultValue = "0") long page,
            @RequestParam(defaultValue = "20") long size) {
        return ApiResponse.ok(adminService.listPages(name, status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ConfigPage> get(@PathVariable Long id) {
        return ApiResponse.ok(adminService.getPage(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ConfigPage> update(@PathVariable Long id, @Valid @RequestBody PageRequest req) {
        return ApiResponse.ok(adminService.updatePage(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminService.deletePage(id);
        return ApiResponse.ok();
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<ConfigPage> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest req) {
        return ApiResponse.ok(adminService.updatePageStatus(id, req.getStatus()));
    }
}
