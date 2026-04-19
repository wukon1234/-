-- 商城系统新增功能数据库脚本
-- 包含：评价系统、收藏列表、优惠券系统、物流追踪

USE `mall_db`;

-- 1. 商品评价表
CREATE TABLE IF NOT EXISTS `review` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `order_id` BIGINT COMMENT '订单ID',
  `rating` TINYINT NOT NULL COMMENT '评分：1-5星',
  `content` TEXT COMMENT '评价内容',
  `images` TEXT COMMENT '评价图片，JSON格式',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-隐藏',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_product_id` (`product_id`),
  INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

-- 2. 商品收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品收藏表';

-- 3. 优惠券表
CREATE TABLE IF NOT EXISTS `coupon` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
  `type` TINYINT NOT NULL COMMENT '类型：1-满减券 2-折扣券 3-无门槛券',
  `value` DECIMAL(10,2) NOT NULL COMMENT '优惠值（满减金额或折扣比例）',
  `min_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '最低消费金额',
  `total_count` INT DEFAULT 0 COMMENT '发放总量',
  `used_count` INT DEFAULT 0 COMMENT '已使用数量',
  `per_limit` INT DEFAULT 1 COMMENT '每人限领数量',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-有效 0-无效',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 4. 用户优惠券表
CREATE TABLE IF NOT EXISTS `user_coupon` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
  `order_id` BIGINT COMMENT '使用的订单ID',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-未使用 1-已使用 2-已过期',
  `use_time` DATETIME COMMENT '使用时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_coupon_id` (`coupon_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- 5. 物流信息表
CREATE TABLE IF NOT EXISTS `logistics` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `company` VARCHAR(50) COMMENT '物流公司',
  `tracking_no` VARCHAR(100) COMMENT '物流单号',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待发货 1-已发货 2-运输中 3-派送中 4-已签收',
  `current_location` VARCHAR(200) COMMENT '当前位置',
  `estimated_time` DATETIME COMMENT '预计到达时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_tracking_no` (`tracking_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流信息表';

-- 6. 物流轨迹表
CREATE TABLE IF NOT EXISTS `logistics_trace` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `logistics_id` BIGINT NOT NULL COMMENT '物流ID',
  `location` VARCHAR(200) COMMENT '所在地点',
  `description` VARCHAR(500) COMMENT '描述信息',
  `operator` VARCHAR(50) COMMENT '操作人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_logistics_id` (`logistics_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流轨迹表';

-- 插入测试数据

-- 插入测试优惠券
INSERT INTO `coupon` (`name`, `type`, `value`, `min_amount`, `total_count`, `per_limit`, `start_time`, `end_time`, `status`) VALUES
('新人专享券', 1, 50.00, 100.00, 1000, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
('满200减30', 1, 30.00, 200.00, 500, 2, NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), 1),
('全场9折券', 2, 0.90, 0.00, 300, 1, NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY), 1),
('无门槛10元券', 3, 10.00, 0.00, 200, 1, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1);

-- 插入测试评价
INSERT INTO `review` (`user_id`, `product_id`, `rating`, `content`, `status`) VALUES
(1, 1, 5, '非常好用，物流很快，包装完好，推荐购买！', 1),
(1, 2, 4, '性能很强，就是价格有点贵', 1);

-- 插入测试收藏
INSERT INTO `favorite` (`user_id`, `product_id`) VALUES
(1, 1),
(1, 2);

-- 插入测试物流信息
INSERT INTO `logistics` (`order_id`, `company`, `tracking_no`, `status`, `current_location`, `estimated_time`) VALUES
(1, '顺丰速运', 'SF1234567890', 1, '深圳转运中心', DATE_ADD(NOW(), INTERVAL 3 DAY));

-- 插入测试物流轨迹
INSERT INTO `logistics_trace` (`logistics_id`, `location`, `description`, `operator`) VALUES
(1, '深圳市南山区', '已发货，正在揽收', '快递员小王'),
(1, '深圳转运中心', '已到达深圳转运中心', '系统自动'),
(1, '深圳转运中心', '正在发往下一站', '系统自动');
