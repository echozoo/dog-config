-- 种子数据：订单配置 / 商品配置 / 物流配置
-- 对应规格：specs/v0.1/database-design.md §5

INSERT IGNORE INTO config_page (id, code, name, description, status, sort, created_at, updated_at) VALUES
(1, 'ORDER', '订单配置', '订单相关业务配置', 'ACTIVE', 1, NOW(), NOW()),
(2, 'PRODUCT', '商品配置', '商品相关业务配置', 'ACTIVE', 2, NOW(), NOW()),
(3, 'LOGISTICS', '物流配置', '物流相关业务配置', 'ACTIVE', 3, NOW(), NOW());

INSERT IGNORE INTO config_group (id, page_id, code, name, description, status, sort, created_at, updated_at) VALUES
(1, 1, 'BASIC', '基础设置', '订单基础设置', 'ACTIVE', 1, NOW(), NOW()),
(2, 1, 'AUTO_CANCEL', '自动取消', '订单自动取消策略', 'ACTIVE', 2, NOW(), NOW()),
(3, 1, 'LOGISTICS', '物流设置', '订单发货物流配置', 'ACTIVE', 3, NOW(), NOW()),
(4, 2, 'BASIC', '基础设置', '商品基础设置', 'ACTIVE', 1, NOW(), NOW()),
(5, 2, 'INVENTORY', '库存设置', '商品库存配置', 'ACTIVE', 2, NOW(), NOW()),
(6, 3, 'BASIC', '基础设置', '物流基础设置', 'ACTIVE', 1, NOW(), NOW());

INSERT IGNORE INTO config_item (id, group_id, `key`, name, value, default_value, value_type, component_type, options, description, required, status, sort, created_at, updated_at) VALUES
(1, 1, 'order.timeout', '订单超时时间', '30', '30', 'INTEGER', 'NUMBER', NULL, '订单超时时间（分钟）', 1, 'ACTIVE', 1, NOW(), NOW()),
(2, 1, 'order.allow.over.sell', '是否允许超卖', 'false', 'false', 'BOOLEAN', 'SWITCH', NULL, '是否允许库存超卖', 0, 'ACTIVE', 2, NOW(), NOW()),
(3, 2, 'order.auto.cancel.enabled', '自动取消开关', 'true', 'true', 'BOOLEAN', 'SWITCH', NULL, '是否开启订单自动取消', 0, 'ACTIVE', 1, NOW(), NOW()),
(4, 2, 'order.auto.cancel.minutes', '自动取消时间', '30', '30', 'INTEGER', 'NUMBER', NULL, '订单超过该分钟数自动取消', 1, 'ACTIVE', 2, NOW(), NOW()),
(5, 3, 'order.default.logistics', '默认物流商', 'SF', 'SF', 'STRING', 'SELECT', '[{"value":"SF","label":"顺丰"},{"value":"JD","label":"京东"},{"value":"YTO","label":"圆通"}]', '订单默认使用的物流商', 0, 'ACTIVE', 1, NOW(), NOW()),
(6, 4, 'product.max.buy.quantity', '单次最大购买数量', '10', '10', 'INTEGER', 'NUMBER', NULL, '单次下单最大购买数量', 0, 'ACTIVE', 1, NOW(), NOW()),
(7, 5, 'product.stock.warning', '库存预警值', '20', '20', 'INTEGER', 'NUMBER', NULL, '库存低于该值时预警', 0, 'ACTIVE', 1, NOW(), NOW()),
(8, 6, 'logistics.default.channel', '默认发货渠道', 'JD', 'JD', 'STRING', 'SELECT', '[{"value":"JD","label":"京东"},{"value":"SF","label":"顺丰"},{"value":"ZTO","label":"中通"}]', '默认发货渠道', 0, 'ACTIVE', 1, NOW(), NOW()),
(9, 6, 'logistics.allow.overnight', '允许次日达', 'true', 'true', 'BOOLEAN', 'SWITCH', NULL, '是否开启次日达', 0, 'ACTIVE', 2, NOW(), NOW());
