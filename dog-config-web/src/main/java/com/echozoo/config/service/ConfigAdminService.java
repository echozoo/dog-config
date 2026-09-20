package com.echozoo.config.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.echozoo.config.common.ApiException;
import com.echozoo.config.common.ErrorCode;
import com.echozoo.config.domain.ConfigGroup;
import com.echozoo.config.domain.ConfigItem;
import com.echozoo.config.domain.ConfigPage;
import com.echozoo.config.domain.ConfigStatus;
import com.echozoo.config.dto.GroupRequest;
import com.echozoo.config.dto.ItemRequest;
import com.echozoo.config.dto.PageRequest;
import com.echozoo.config.mapper.ConfigGroupMapper;
import com.echozoo.config.mapper.ConfigItemMapper;
import com.echozoo.config.mapper.ConfigPageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConfigAdminService {

    private final ConfigPageMapper pageMapper;
    private final ConfigGroupMapper groupMapper;
    private final ConfigItemMapper itemMapper;

    public ConfigAdminService(ConfigPageMapper pageMapper, ConfigGroupMapper groupMapper, ConfigItemMapper itemMapper) {
        this.pageMapper = pageMapper;
        this.groupMapper = groupMapper;
        this.itemMapper = itemMapper;
    }

    // ---------- Page ----------

    @Transactional
    public ConfigPage createPage(PageRequest req) {
        checkPageCodeUnique(null, req.getCode());
        ConfigPage page = new ConfigPage();
        page.setCode(req.getCode());
        page.setName(req.getName());
        page.setDescription(req.getDescription());
        page.setSort(req.getSort() == null ? 0 : req.getSort());
        page.setStatus(ConfigStatus.ACTIVE);
        fillTimestamps(page);
        pageMapper.insert(page);
        return page;
    }

    public Page<ConfigPage> listPages(String name, ConfigStatus status, long page, long size) {
        LambdaQueryWrapper<ConfigPage> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(ConfigPage::getName, name);
        }
        if (status != null) {
            wrapper.eq(ConfigPage::getStatus, status);
        }
        wrapper.orderByAsc(ConfigPage::getSort).orderByDesc(ConfigPage::getId);
        return pageMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public ConfigPage getPage(Long id) {
        return requirePage(id);
    }

    @Transactional
    public ConfigPage updatePage(Long id, PageRequest req) {
        ConfigPage page = requirePage(id);
        checkPageCodeUnique(id, req.getCode());
        page.setCode(req.getCode());
        page.setName(req.getName());
        page.setDescription(req.getDescription());
        if (req.getSort() != null) {
            page.setSort(req.getSort());
        }
        page.setUpdatedAt(LocalDateTime.now());
        pageMapper.updateById(page);
        return page;
    }

    @Transactional
    public void deletePage(Long id) {
        ConfigPage page = requirePage(id);
        pageMapper.deleteById(page.getId());
    }

    @Transactional
    public ConfigPage updatePageStatus(Long id, ConfigStatus status) {
        ConfigPage page = requirePage(id);
        page.setStatus(status);
        page.setUpdatedAt(LocalDateTime.now());
        pageMapper.updateById(page);
        return page;
    }

    // ---------- Group ----------

    @Transactional
    public ConfigGroup createGroup(Long pageId, GroupRequest req) {
        requirePage(pageId);
        checkGroupCodeUnique(null, pageId, req.getCode());
        ConfigGroup group = new ConfigGroup();
        group.setPageId(pageId);
        group.setCode(req.getCode());
        group.setName(req.getName());
        group.setDescription(req.getDescription());
        group.setSort(req.getSort() == null ? 0 : req.getSort());
        group.setStatus(ConfigStatus.ACTIVE);
        fillTimestamps(group);
        groupMapper.insert(group);
        return group;
    }

    public List<ConfigGroup> listGroups(Long pageId, ConfigStatus status) {
        LambdaQueryWrapper<ConfigGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConfigGroup::getPageId, pageId);
        if (status != null) {
            wrapper.eq(ConfigGroup::getStatus, status);
        }
        wrapper.orderByAsc(ConfigGroup::getSort).orderByAsc(ConfigGroup::getId);
        return groupMapper.selectList(wrapper);
    }

    public ConfigGroup getGroup(Long id) {
        return requireGroup(id);
    }

    @Transactional
    public ConfigGroup updateGroup(Long id, GroupRequest req) {
        ConfigGroup group = requireGroup(id);
        checkGroupCodeUnique(id, group.getPageId(), req.getCode());
        group.setCode(req.getCode());
        group.setName(req.getName());
        group.setDescription(req.getDescription());
        if (req.getSort() != null) {
            group.setSort(req.getSort());
        }
        group.setUpdatedAt(LocalDateTime.now());
        groupMapper.updateById(group);
        return group;
    }

    @Transactional
    public void deleteGroup(Long id) {
        requireGroup(id);
        groupMapper.deleteById(id);
    }

    @Transactional
    public ConfigGroup updateGroupStatus(Long id, ConfigStatus status) {
        ConfigGroup group = requireGroup(id);
        group.setStatus(status);
        group.setUpdatedAt(LocalDateTime.now());
        groupMapper.updateById(group);
        return group;
    }

    // ---------- Item ----------

    @Transactional
    public ConfigItem createItem(Long groupId, ItemRequest req) {
        requireGroup(groupId);
        checkItemKeyUnique(null, req.getKey());
        ConfigItem item = new ConfigItem();
        item.setGroupId(groupId);
        applyItemRequest(item, req);
        item.setStatus(ConfigStatus.ACTIVE);
        fillTimestamps(item);
        itemMapper.insert(item);
        return item;
    }

    public List<ConfigItem> listItems(Long groupId, ConfigStatus status) {
        LambdaQueryWrapper<ConfigItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConfigItem::getGroupId, groupId);
        if (status != null) {
            wrapper.eq(ConfigItem::getStatus, status);
        }
        wrapper.orderByAsc(ConfigItem::getSort).orderByAsc(ConfigItem::getId);
        return itemMapper.selectList(wrapper);
    }

    public ConfigItem getItem(Long id) {
        return requireItem(id);
    }

    @Transactional
    public ConfigItem updateItem(Long id, ItemRequest req) {
        ConfigItem item = requireItem(id);
        checkItemKeyUnique(id, req.getKey());
        applyItemRequest(item, req);
        item.setUpdatedAt(LocalDateTime.now());
        itemMapper.updateById(item);
        return item;
    }

    @Transactional
    public void deleteItem(Long id) {
        requireItem(id);
        itemMapper.deleteById(id);
    }

    @Transactional
    public ConfigItem updateItemStatus(Long id, ConfigStatus status) {
        ConfigItem item = requireItem(id);
        item.setStatus(status);
        item.setUpdatedAt(LocalDateTime.now());
        itemMapper.updateById(item);
        return item;
    }

    @Transactional
    public ConfigItem updateItemValue(Long id, String value) {
        ConfigItem item = requireItem(id);
        item.setValue(value);
        item.setUpdatedAt(LocalDateTime.now());
        itemMapper.updateById(item);
        return item;
    }

    // ---------- helpers ----------

    private void applyItemRequest(ConfigItem item, ItemRequest req) {
        item.setKey(req.getKey());
        item.setName(req.getName());
        item.setValue(req.getValue());
        item.setDefaultValue(req.getDefaultValue());
        item.setValueType(req.getValueType());
        item.setComponentType(req.getComponentType());
        item.setOptions(req.getOptions());
        item.setDescription(req.getDescription());
        item.setRequired(req.getRequired() != null && req.getRequired());
        if (req.getSort() != null) {
            item.setSort(req.getSort());
        }
    }

    private void fillTimestamps(ConfigPage page) {
        LocalDateTime now = LocalDateTime.now();
        page.setCreatedAt(now);
        page.setUpdatedAt(now);
    }

    private void fillTimestamps(ConfigGroup group) {
        LocalDateTime now = LocalDateTime.now();
        group.setCreatedAt(now);
        group.setUpdatedAt(now);
    }

    private void fillTimestamps(ConfigItem item) {
        LocalDateTime now = LocalDateTime.now();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
    }

    private ConfigPage requirePage(Long id) {
        ConfigPage page = pageMapper.selectById(id);
        if (page == null) {
            throw new ApiException(ErrorCode.NOT_FOUND, "Page 不存在: " + id);
        }
        return page;
    }

    private ConfigGroup requireGroup(Long id) {
        ConfigGroup group = groupMapper.selectById(id);
        if (group == null) {
            throw new ApiException(ErrorCode.NOT_FOUND, "Group 不存在: " + id);
        }
        return group;
    }

    private ConfigItem requireItem(Long id) {
        ConfigItem item = itemMapper.selectById(id);
        if (item == null) {
            throw new ApiException(ErrorCode.NOT_FOUND, "Item 不存在: " + id);
        }
        return item;
    }

    private void checkPageCodeUnique(Long excludeId, String code) {
        ConfigPage existing = pageMapper.selectOne(new LambdaQueryWrapper<ConfigPage>()
                .eq(ConfigPage::getCode, code)
                .last("LIMIT 1"));
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw new ApiException(ErrorCode.CONFLICT, "Page code 已存在: " + code);
        }
    }

    private void checkGroupCodeUnique(Long excludeId, Long pageId, String code) {
        ConfigGroup existing = groupMapper.selectOne(new LambdaQueryWrapper<ConfigGroup>()
                .eq(ConfigGroup::getPageId, pageId)
                .eq(ConfigGroup::getCode, code)
                .last("LIMIT 1"));
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw new ApiException(ErrorCode.CONFLICT, "Group code 已存在: " + code);
        }
    }

    private void checkItemKeyUnique(Long excludeId, String key) {
        ConfigItem existing = itemMapper.selectOne(new LambdaQueryWrapper<ConfigItem>()
                .eq(ConfigItem::getKey, key)
                .last("LIMIT 1"));
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw new ApiException(ErrorCode.CONFLICT, "Item key 已存在: " + key);
        }
    }
}
