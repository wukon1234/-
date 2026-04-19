-- 客服回复系统数据库脚本

USE `mall_db`;

-- 1. 客服回复模板表
CREATE TABLE `customer_service_template` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `scene` VARCHAR(50) NOT NULL COMMENT '场景标识',
  `template_content` TEXT NOT NULL COMMENT '模板内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` BIGINT NOT NULL COMMENT '创建人ID',
  `is_common` TINYINT DEFAULT 0 COMMENT '是否常用，0-否，1-是',
  INDEX `idx_scene` (`scene`),
  INDEX `idx_is_common` (`is_common`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服回复模板表';

-- 2. 客服回复记录表
CREATE TABLE `customer_service_reply` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `admin_id` BIGINT NOT NULL COMMENT '管理员ID',
  `scene` VARCHAR(50) NOT NULL COMMENT '场景标识',
  `params` TEXT COMMENT '输入参数，JSON格式',
  `reply_content` TEXT NOT NULL COMMENT '回复内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_admin_id` (`admin_id`),
  INDEX `idx_scene` (`scene`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服回复记录表';

-- 插入默认模板数据
INSERT INTO `customer_service_template` (`template_name`, `scene`, `template_content`, `create_by`, `is_common`) VALUES
('退款同意模板', 'refund_agree', '尊敬的用户，您好！\n\n您的退款申请已审核通过，退款金额为¥{refundAmount}，我们已将退款原路返回至您的{refundChannel}，预计{refundTime}个工作日内到账，请您耐心等待。\n\n如有其他疑问，随时联系我们，祝您生活愉快！', 1, 1),
('退款拒绝模板', 'refund_reject', '尊敬的用户，您好！\n\n非常抱歉，您的退款申请未能通过审核，原因是：{rejectReason}。\n\n建议您：{suggestion}。\n\n如有其他疑问，随时联系我们，我们将尽力为您解决问题。', 1, 1),
('退款进度查询模板', 'refund_status', '尊敬的用户，您好！\n\n您的退款申请当前状态为：{currentStatus}，预计{expectedTime}个工作日内到账。\n\n如有其他疑问，随时联系我们，祝您生活愉快！', 1, 1),
('催发货安抚模板', 'order_urgent', '尊敬的用户，您好！\n\n非常感谢您的耐心等待，您的订单{orderNo}目前状态为{orderStatus}，我们正在加急处理中，预计{expectedShipTime}前为您发货。\n\n给您带来的不便，我们深表歉意，感谢您的理解与支持！', 1, 1),
('订单查询回复模板', 'order_query', '尊敬的用户，您好！\n\n您的订单{orderNo}详情如下：\n商品：{productName}\n物流状态：{logisticsStatus}\n预计收货时间：{expectedReceiveTime}\n\n如有其他疑问，随时联系我们，祝您生活愉快！', 1, 1),
('订单取消回复模板', 'order_cancel', '尊敬的用户，您好！\n\n您的订单{orderNo}已成功取消，取消原因：{cancelReason}。\n\n{refundInfo}\n\n如有其他疑问，随时联系我们，祝您生活愉快！', 1, 1),
('订单异常安抚模板', 'order_exception', '尊敬的用户，您好！\n\n非常抱歉，您的订单{orderNo}出现了{exceptionType}的情况，我们深表歉意。\n\n解决方案：{solution}。\n\n我们会尽快为您处理，给您带来的不便，敬请谅解！', 1, 1),
('退货咨询回复模板', 'return_consult', '尊敬的用户，您好！\n\n关于退货流程，您可以按照以下步骤操作：\n1. {step1}\n2. {step2}\n3. {step3}\n\n运费说明：{freightInfo}\n退款时效：{refundTimeframe}\n\n如有其他疑问，随时联系我们，祝您生活愉快！', 1, 1),
('换货咨询回复模板', 'exchange_consult', '尊敬的用户，您好！\n\n关于换货，需要满足以下条件：{exchangeConditions}\n\n换货流程：\n1. {step1}\n2. {step2}\n3. {step3}\n\n预计耗时：{expectedTime}\n\n如有其他疑问，随时联系我们，祝您生活愉快！', 1, 1),
('质量问题投诉回复模板', 'quality_complaint', '尊敬的用户，您好！\n\n非常抱歉给您带来了不好的购物体验，我们对您反映的质量问题高度重视。\n\n解决方案：{solution}\n\n补偿建议：{compensation}\n\n我们会持续改进产品质量，感谢您的监督与支持！', 1, 1),
('商品咨询回复模板', 'product_consult', '尊敬的用户，您好！\n\n关于{productName}的咨询，以下是详细信息：\n{productInfo}\n\n如有其他疑问，随时联系我们，祝您生活愉快！', 1, 1),
('物流咨询回复模板', 'logistics_consult', '尊敬的用户，您好！\n\n关于物流问题，以下是相关信息：\n{logisticsInfo}\n\n如有其他疑问，随时联系我们，祝您生活愉快！', 1, 1);
