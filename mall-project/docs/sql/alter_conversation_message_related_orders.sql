-- 为对话消息增加「关联订单」JSON，供智能助手在聊天内展示订单卡片（执行一次即可）
ALTER TABLE `conversation_message`
  ADD COLUMN `related_orders` TEXT NULL COMMENT '关联订单摘要JSON(order/order_miss)' AFTER `related_products`;
