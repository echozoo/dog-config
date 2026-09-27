package com.echozoo.config.web.controller;

import com.echozoo.config.common.ApiResponse;
import com.echozoo.config.domain.ConfigItem;
import com.echozoo.config.domain.ConfigItemVersion;
import com.echozoo.config.domain.ConfigStatus;
import com.echozoo.config.dto.ItemRequest;
import com.echozoo.config.dto.StatusRequest;
import com.echozoo.config.dto.ValueRequest;
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
public class ItemController {

    private final ConfigAdminService adminService;

    public ItemController(ConfigAdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/groups/{groupId}/items")
    public ApiResponse<ConfigItem> create(@PathVariable Long groupId, @Valid @RequestBody ItemRequest req) {
        return ApiResponse.ok(adminService.createItem(groupId, req));
    }

    @GetMapping("/groups/{groupId}/items")
    public ApiResponse<List<ConfigItem>> listByGroup(
            @PathVariable Long groupId,
            @RequestParam(required = false) ConfigStatus status) {
        return ApiResponse.ok(adminService.listItems(groupId, status));
    }

    @GetMapping("/items/{id}")
    public ApiResponse<ConfigItem> get(@PathVariable Long id) {
        return ApiResponse.ok(adminService.getItem(id));
    }

    @PutMapping("/items/{id}")
    public ApiResponse<ConfigItem> update(@PathVariable Long id, @Valid @RequestBody ItemRequest req) {
        return ApiResponse.ok(adminService.updateItem(id, req));
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminService.deleteItem(id);
        return ApiResponse.ok();
    }

    @PatchMapping("/items/{id}/status")
    public ApiResponse<ConfigItem> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest req) {
        return ApiResponse.ok(adminService.updateItemStatus(id, req.getStatus()));
    }

    @PatchMapping("/items/{id}/value")
    public ApiResponse<ConfigItem> updateValue(@PathVariable Long id, @Valid @RequestBody ValueRequest req) {
        return ApiResponse.ok(adminService.updateItemValue(id, req.getValue()));
    }

    @GetMapping("/items/{id}/versions")
    public ApiResponse<List<ConfigItemVersion>> listVersions(@PathVariable Long id) {
        return ApiResponse.ok(adminService.listVersions(id));
    }

    @GetMapping("/items/{id}/versions/{versionNo}")
    public ApiResponse<ConfigItemVersion> getVersion(@PathVariable Long id, @PathVariable Integer versionNo) {
        return ApiResponse.ok(adminService.getVersion(id, versionNo));
    }

    @PostMapping("/items/{id}/versions/{versionNo}/rollback")
    public ApiResponse<ConfigItem> rollback(@PathVariable Long id, @PathVariable Integer versionNo) {
        return ApiResponse.ok(adminService.rollback(id, versionNo));
    }

    @PostMapping("/items/{id}/restore")
    public ApiResponse<ConfigItem> restore(@PathVariable Long id) {
        return ApiResponse.ok(adminService.restore(id));
    }
}
