package com.mall.service.assistant;

import java.util.regex.Pattern;

/**
 * 判断用户是否在咨询「本人订单/物流/单号」等应走订单库与 RAG 的意图（与 {@link com.mall.service.assistant.impl.OrderRAGDataSource} 保持一致）。
 */
public final class OrderChatIntent {

    private static final Pattern ORD_NO_PATTERN = Pattern.compile("(?i)\\b(ORD\\d+)\\b");
    private static final Pattern LABELED_ORDER_NO = Pattern.compile("订单号\\s*[:：]?\\s*([A-Za-z0-9\\-]+)");

    private OrderChatIntent() {
    }

    public static boolean isOrderHowToQuestion(String q) {
        if (q == null || q.isEmpty()) {
            return false;
        }
        return q.contains("如何下单") || q.contains("怎么下单") || q.contains("怎样下单")
                || q.contains("下单流程") || q.contains("订单流程")
                || q.matches("(?s).*(如何|怎么|怎样).{0,8}下单.*");
    }

    /**
     * 是否应检索当前用户订单数据（并应在模板模式下绕过固定话术）。
     */
    public static boolean shouldRetrieveOrderData(String query) {
        if (query == null) {
            return false;
        }
        String q = query.trim();
        if (q.isEmpty()) {
            return false;
        }
        if (isOrderHowToQuestion(q)) {
            return false;
        }
        if (ORD_NO_PATTERN.matcher(q).find()) {
            return true;
        }
        if (LABELED_ORDER_NO.matcher(q).find()) {
            return true;
        }
        if (q.contains("我的订单")) {
            return true;
        }
        if (q.contains("订单状态")) {
            return true;
        }
        if (q.contains("订单物流") || q.contains("物流") || q.contains("快递")) {
            return true;
        }
        if (q.contains("发货")) {
            return true;
        }
        if (q.contains("到哪了") || q.contains("到哪里了") || q.contains("到哪里")) {
            return true;
        }
        if (q.contains("订单号")) {
            return true;
        }
        if (q.contains("查订单") || q.contains("查一下订单")) {
            return true;
        }
        if (q.contains("订单详情") && !q.contains("如何") && !q.contains("怎么")) {
            return true;
        }
        return q.contains("订单") && (q.contains("哪") || q.contains("状态"));
    }
}
