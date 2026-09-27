-- 存量数据回填：为尚无版本记录的 config_item 各插入一条基于当前值的 CREATE 版本（幂等）
-- 可选执行；不执行也不影响功能（版本自首次值变更起记录）
INSERT INTO config_item_version (item_id, version_no, `value`, `default_value`, value_type, change_type, created_at)
SELECT i.id, 1, i.`value`, i.`default_value`, i.value_type, 'CREATE', NOW()
FROM config_item i
WHERE NOT EXISTS (
    SELECT 1 FROM config_item_version v WHERE v.item_id = i.id
);
