package com.mall.service.assistant.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.entity.Order;
import com.mall.entity.OrderItem;
import com.mall.mapper.OrderItemMapper;
import com.mall.mapper.OrderMapper;
import com.mall.service.assistant.DataSource;
import com.mall.service.assistant.OrderChatIntent;
import com.mall.service.assistant.RAGDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 订单 RAG 数据源：仅返回当前用户（filters.userId）下的订单，用于助手回答状态/物流/单号等。
 */
@Component
public class OrderRAGDataSource implements RAGDataSource {

    private static final Pattern ORD_NO_PATTERN = Pattern.compile("(?i)\\b(ORD\\d+)\\b");
    private static final Pattern LABELED_ORDER_NO = Pattern.compile("订单号\\s*[:：]?\\s*([A-Za-z0-9\\-]+)");
    private static final Pattern ORD_PREFIX_BODY = Pattern.compile("(?i)^ORD\\d+$");

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public DataSource getType() {
        return DataSource.ORDER;
    }

    @Override
    public List<Map<String, Object>> search(String query, int topK, Map<String, Object> filters) {
        if (filters == null || !(filters.get("userId") instanceof Number)) {
            return Collections.emptyList();
        }
        long userId = ((Number) filters.get("userId")).longValue();
        if (userId <= 0) {
            return Collections.emptyList();
        }
        int safeTopK = topK <= 0 ? 5 : Math.min(topK, 50);

        String q = query == null ? "" : query.trim();
        if (!OrderChatIntent.shouldRetrieveOrderData(q)) {
            return Collections.emptyList();
        }

        LinkedHashSet<String> orderNos = extractOrderNumbers(q);
        if (!orderNos.isEmpty()) {
            List<Map<String, Object>> out = new ArrayList<>();
            for (String orderNo : orderNos) {
                String normalizedNo = normalizeOrderNo(orderNo);
                Order order = orderMapper.selectOne(
                        new LambdaQueryWrapper<Order>()
                                .eq(Order::getUserId, userId)
                                .eq(Order::getOrderNo, normalizedNo)
                                .last("LIMIT 1")
                );
                if (order != null) {
                    out.add(toOrderMap(order, userId));
                } else {
                    out.add(orderNotFoundMap(normalizedNo));
                }
            }
            return out.stream().limit(safeTopK).collect(Collectors.toList());
        }

        List<Order> recent = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreateTime)
                        .last("LIMIT " + safeTopK)
        );
        return recent.stream().map(o -> toOrderMap(o, userId)).collect(Collectors.toList());
    }

    @Override
    public String toText(Map<String, Object> data) {
        if (data == null) {
            return "";
        }
        Object type = data.get("type");
        if ("order_miss".equals(type)) {
            return "订单查询 " + data.get("orderNo") + " 未找到";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("订单 ").append(data.get("orderNo"));
        sb.append(" 状态:").append(data.get("statusText"));
        sb.append(" ").append(data.get("logisticsSummary"));
        return sb.toString();
    }

    @Override
    public String getDescription() {
        return "当前用户订单：订单号、金额、状态、时间节点与物流阶段说明（基于订单状态推导，非第三方快递轨迹）";
    }

    /**
     * 与下单生成的订单号对齐（ORD 前缀大写）；其它格式保持原样便于精确匹配。
     */
    private static String normalizeOrderNo(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return s;
        }
        if (ORD_PREFIX_BODY.matcher(s).matches()) {
            return s.toUpperCase(Locale.ROOT);
        }
        return s;
    }

    private LinkedHashSet<String> extractOrderNumbers(String query) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        Matcher m1 = ORD_NO_PATTERN.matcher(query);
        while (m1.find()) {
            set.add(normalizeOrderNo(m1.group(1)));
        }
        Matcher m2 = LABELED_ORDER_NO.matcher(query);
        while (m2.find()) {
            String labeled = normalizeOrderNo(m2.group(1));
            if (!labeled.isEmpty()) {
                set.add(labeled);
            }
        }
        return set;
    }

    private Map<String, Object> orderNotFoundMap(String orderNo) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "order_miss");
        map.put("orderNo", orderNo);
        map.put("hint", "未在您的账号下找到该订单号，请核对是否登录正确账号或订单号是否抄写完整。");
        return map;
    }

    private Map<String, Object> toOrderMap(Order order, long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "order");
        map.put("id", order.getId());
        map.put("orderNo", order.getOrderNo());
        map.put("userId", userId);
        map.put("totalAmount", order.getTotalAmount());
        map.put("status", order.getStatus());
        map.put("statusText", statusText(order.getStatus()));
        map.put("createTime", formatDt(order.getCreateTime()));
        map.put("payTime", formatDt(order.getPayTime()));
        map.put("deliveryTime", formatDt(order.getDeliveryTime()));
        map.put("completeTime", formatDt(order.getCompleteTime()));
        map.put("logisticsSummary", logisticsSummary(order));
        map.put("itemsSummary", buildItemsSummary(order.getId()));
        return map;
    }

    private String buildItemsSummary(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );
        if (items == null || items.isEmpty()) {
            return "（无明细）";
        }
        return items.stream()
                .map(i -> {
                    String name = i.getProductName() == null ? "商品" : i.getProductName();
                    int qty = i.getQuantity() == null ? 0 : i.getQuantity();
                    return name + "×" + qty;
                })
                .collect(Collectors.joining("；"));
    }

    private String formatDt(java.time.LocalDateTime t) {
        return t == null ? "" : t.format(DT_FMT);
    }

    private static String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "待支付";
            case 1:
                return "已支付（备货中）";
            case 2:
                return "已发货";
            case 3:
                return "已完成";
            case 4:
                return "已取消";
            default:
                return "未知(" + status + ")";
        }
    }

    private String logisticsSummary(Order order) {
        Integer s = order.getStatus();
        if (s == null) {
            return "状态未知，请到订单页查看。";
        }
        switch (s) {
            case 0:
                return "订单待支付，尚未进入发货与配送流程。支付成功后会进入备货。";
            case 1:
                return "订单已支付，仓库备货中，尚未发货；暂无在途物流信息。";
            case 2:
                String ship = order.getDeliveryTime() != null
                        ? "已于 " + formatDt(order.getDeliveryTime()) + " 标记发货，"
                        : "已标记发货，";
                return ship + "商品在配送途中（本系统未对接第三方快递轨迹，请以订单页或短信通知为准）。";
            case 3:
                return "订单已完成"
                        + (order.getCompleteTime() != null ? "（" + formatDt(order.getCompleteTime()) + "）" : "")
                        + "，如已收货则无需再跟踪物流。";
            case 4:
                return "订单已取消，不产生物流配送。";
            default:
                return "请前往「我的订单」查看最新状态。";
        }
    }
}
