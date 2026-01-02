-- 商城智能助手系统数据库脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `mall_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `mall_db`;

-- 1. 用户表
CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-禁用',
  `role` TINYINT DEFAULT 1 COMMENT '角色：1-普通用户 2-管理员',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 商品分类表
CREATE TABLE `category` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
  `level` TINYINT DEFAULT 1 COMMENT '分类层级',
  `sort_order` INT DEFAULT 0 COMMENT '排序序号',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 3. 商品表
CREATE TABLE `product` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `description` TEXT COMMENT '商品描述，用于RAG检索',
  `category_id` BIGINT COMMENT '分类ID',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `stock` INT DEFAULT 0 COMMENT '库存',
  `image_url` VARCHAR(500) COMMENT '主图URL',
  `detail_images` TEXT COMMENT '详情图片，JSON格式',
  `specs` TEXT COMMENT '商品规格，JSON格式',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-上架 0-下架',
  `vector_id` VARCHAR(100) COMMENT '向量数据库ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_category` (`category_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 4. 购物车表
CREATE TABLE `cart` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `quantity` INT DEFAULT 1 COMMENT '数量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 5. 订单表
CREATE TABLE `order` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待支付 1-已支付 2-已发货 3-已完成 4-已取消',
  `address_id` BIGINT COMMENT '收货地址ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 6. 订单项表
CREATE TABLE `order_item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_name` VARCHAR(200) COMMENT '商品名称（快照）',
  `quantity` INT NOT NULL COMMENT '数量',
  `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项表';

-- 6.1 收货地址表
CREATE TABLE `address` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人手机号',
  `province` VARCHAR(50) NOT NULL COMMENT '省份',
  `city` VARCHAR(50) NOT NULL COMMENT '城市',
  `district` VARCHAR(50) NOT NULL COMMENT '区/县',
  `detail` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认：1-默认 0-非默认',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- 7. 对话会话表
CREATE TABLE `conversation` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `session_id` VARCHAR(100) NOT NULL COMMENT '会话ID',
  `title` VARCHAR(200) COMMENT '会话标题',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话会话表';

-- 8. 对话消息表
CREATE TABLE `conversation_message` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
  `role` TINYINT NOT NULL COMMENT '角色：1-用户 2-助手',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `related_products` TEXT COMMENT '相关商品ID，JSON格式',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话消息表';

-- 插入测试数据

-- 插入分类数据
INSERT INTO `category` (`name`, `parent_id`, `level`, `sort_order`) VALUES
('电子产品', 0, 1, 1),
('服装配饰', 0, 1, 2),
('食品饮料', 0, 1, 3),
('手机', 1, 2, 1),
('电脑', 1, 2, 2),
('男装', 2, 2, 1),
('女装', 2, 2, 2);

-- 插入测试用户（密码：123456，实际使用时需要加密）
INSERT INTO `user` (`username`, `password`, `email`, `phone`) VALUES
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pXDS', 'test@example.com', '13800138000');

-- 插入测试地址
INSERT INTO `address` (`user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `district`, `detail`, `is_default`) VALUES
(1, '测试用户', '13800138000', '广东省', '深圳市', '南山区', '科技园xx路xx号', 1);

-- 插入测试商品
INSERT INTO `product` (`name`, `description`, `category_id`, `price`, `stock`, `image_url`, `status`) VALUES
('iPhone 15 Pro', 'Apple iPhone 15 Pro 256GB 钛金属色，A17 Pro芯片，支持5G网络', 4, 8999.00, 100, '/images/iphone15.jpg', 1),
('MacBook Pro', 'Apple MacBook Pro 14英寸 M3芯片 18GB内存 512GB存储', 5, 14999.00, 50, '/images/macbook.jpg', 1),
('休闲T恤', '纯棉休闲T恤，舒适透气，多色可选', 6, 99.00, 200, '/images/tshirt.jpg', 1);