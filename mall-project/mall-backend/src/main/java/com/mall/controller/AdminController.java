package com.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.Result;
import com.mall.dto.LoginRequest;
import com.mall.dto.LoginResponse;
import com.mall.entity.Order;
import com.mall.entity.OrderItem;
import com.mall.entity.Product;
import com.mall.entity.User;
import com.mall.mapper.OrderItemMapper;
import com.mall.mapper.OrderMapper;
import com.mall.mapper.UserMapper;
import com.mall.service.ProductService;
import com.mall.service.UserService;
import com.mall.service.assistant.LLMService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 管理端控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LLMService llmService;
    
    // ========== 商品管理 ==========
    
    /**
     * 获取所有商品
     */
    @GetMapping("/products")
    public Result<List<Product>> getAllProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        List<Product> productList = productService.getProductList(page, size);
        return Result.success(productList);
    }
    
    /**
     * 添加商品
     */
    @PostMapping("/products")
    public Result<?> addProduct(@RequestBody Product product) {
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        boolean result = productService.save(product);
        if (!result) {
            return Result.error("商品添加失败");
        }
        return Result.success("商品添加成功");
    }
    
    /**
     * 更新商品
     */
    @PutMapping("/products/{id}")
    public Result<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Product existing = productService.getById(id);
        if (existing == null) {
            return Result.error("商品不存在");
        }
        product.setId(id);
        product.setUpdateTime(LocalDateTime.now());
        boolean result = productService.updateById(product);
        if (!result) {
            return Result.error("商品更新失败");
        }
        return Result.success("商品更新成功");
    }
    
    /**
     * 删除商品
     */
    @DeleteMapping("/products/{id}")
    public Result<?> deleteProduct(@PathVariable Long id) {
        boolean result = productService.removeById(id);
        if (!result) {
            return Result.error("商品删除失败");
        }
        return Result.success("商品删除成功");
    }
    
    // ========== 用户管理 ==========
    
    /**
     * 获取所有用户
     */
    @GetMapping("/users")
    public Result<List<User>> getAllUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreateTime);
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(User::getUsername, kw).or().like(User::getEmail, kw));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> pageParam =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> userPage = userService.page(pageParam, wrapper);
        return Result.success(userPage.getRecords());
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/users/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        User existingUser = userService.getById(id);
        if (existingUser == null) {
            return Result.error("用户不存在");
        }
        user.setId(id);
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);
        log.info("更新用户信息成功: id={}", id);
        return Result.success("用户信息更新成功");
    }
    
    /**
     * 禁用/启用用户
     */
    @PutMapping("/users/{id}/status")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean result = userService.updateUserStatus(id, status);
        if (result) {
            return Result.success("用户状态更新成功");
        } else {
            return Result.error("用户状态更新失败");
        }
    }
    
    /**
     * 设置用户角色
     */
    @PutMapping("/users/{id}/role")
    public Result<?> updateUserRole(@PathVariable Long id, @RequestParam Integer role) {
        boolean result = userService.updateUserRole(id, role);
        if (result) {
            return Result.success("用户角色更新成功");
        } else {
            return Result.error("用户角色更新失败");
        }
    }

    /**
     * 管理员登录
     * @param request
     * @return
     */
    @PostMapping("/login")
    public Result<LoginResponse> adminLogin(@Validated @RequestBody LoginRequest request) {
        LoginResponse response = userService.adminLogin(request);
        return Result.success(response);
    }

    /**
     * 仪表盘数据
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData() {
        Map<String, Object> result = new HashMap<>();

        long productTotal = productService.count();
        long orderTotal = orderMapper.selectCount(new LambdaQueryWrapper<>());
        long userTotal = userService.count();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        Long monthNewProducts = productService.count(new LambdaQueryWrapper<Product>()
                .ge(Product::getCreateTime, monthStart));
        Long todayNewOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, now.toLocalDate().atStartOfDay()));
        Long monthNewUsers = userService.count(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, monthStart));

        List<Order> recentOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .orderByDesc(Order::getCreateTime)
                .last("LIMIT 5"));
        Set<Long> userIds = recentOrders.stream().map(Order::getUserId).collect(Collectors.toSet());
        Map<Long, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, userIds));
            userNameMap = users.stream().collect(Collectors.toMap(User::getId, User::getUsername));
        }

        List<Map<String, Object>> recentOrderViews = new ArrayList<>();
        for (Order order : recentOrders) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", order.getId());
            row.put("orderNo", order.getOrderNo());
            row.put("userName", userNameMap.getOrDefault(order.getUserId(), "未知用户"));
            row.put("totalAmount", order.getTotalAmount());
            row.put("status", order.getStatus());
            row.put("createTime", order.getCreateTime());
            recentOrderViews.add(row);
        }

        List<OrderItem> orderItems = orderItemMapper.selectList(new LambdaQueryWrapper<>());
        Map<Long, Integer> salesMap = new HashMap<>();
        for (OrderItem item : orderItems) {
            Long productId = item.getProductId();
            Integer qty = item.getQuantity() == null ? 0 : item.getQuantity();
            salesMap.put(productId, salesMap.getOrDefault(productId, 0) + qty);
        }
        List<Map.Entry<Long, Integer>> topSales = salesMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .collect(Collectors.toList());
        List<Map<String, Object>> hotProductViews = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : topSales) {
            Product product = productService.getById(entry.getKey());
            if (product == null) {
                continue;
            }
            Map<String, Object> row = new HashMap<>();
            row.put("id", product.getId());
            row.put("name", product.getName());
            row.put("categoryId", product.getCategoryId());
            row.put("price", product.getPrice());
            row.put("sales", entry.getValue());
            hotProductViews.add(row);
        }

        result.put("productTotal", productTotal);
        result.put("orderTotal", orderTotal);
        result.put("userTotal", userTotal);
        result.put("monthSalesAmount", orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, monthStart)).stream()
                .map(o -> o.getTotalAmount() == null ? java.math.BigDecimal.ZERO : o.getTotalAmount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        result.put("monthNewProducts", monthNewProducts);
        result.put("todayNewOrders", todayNewOrders);
        result.put("monthNewUsers", monthNewUsers);
        result.put("recentOrders", recentOrderViews);
        result.put("hotProducts", hotProductViews);
        return Result.success(result);
    }

    /**
     * 订单分析数据
     */
    @GetMapping("/orders/analysis")
    public Result<Map<String, Object>> getOrderAnalysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "date") String type) {
        LocalDateTime start = (startDate == null || startDate.trim().isEmpty())
                ? LocalDate.now().minusDays(29).atStartOfDay()
                : LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = (endDate == null || endDate.trim().isEmpty())
                ? LocalDateTime.now()
                : LocalDate.parse(endDate).atTime(LocalTime.MAX);

        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, start)
                .le(Order::getCreateTime, end)
                .orderByDesc(Order::getCreateTime));

        long totalOrders = orders.size();
        BigDecimal totalAmount = orders.stream()
                .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgAmount = totalOrders == 0
                ? BigDecimal.ZERO
                : totalAmount.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP);
        long paidCount = orders.stream().filter(o -> o.getStatus() != null && o.getStatus() > 0 && o.getStatus() != 4).count();
        BigDecimal conversionRate = totalOrders == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(paidCount * 100.0 / totalOrders).setScale(2, java.math.RoundingMode.HALF_UP);

        List<Map<String, Object>> chartData = new ArrayList<>();
        int targetYear = year != null ? year : start.getYear();

        if ("product".equalsIgnoreCase(type)) {
            buildProductAnalysisChart(chartData, orders);
        } else if ("user".equalsIgnoreCase(type)) {
            buildUserAnalysisChart(chartData, orders);
        } else {
            buildDateAnalysisChart(chartData, orders, targetYear);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalOrders", totalOrders);
        result.put("totalAmount", totalAmount);
        result.put("avgAmount", avgAmount);
        result.put("conversionRate", conversionRate);
        result.put("chartData", chartData);
        return Result.success(result);
    }

    /**
     * 管理端订单列表（支持筛选和分页）
     */
    @GetMapping("/orders")
    public Result<Map<String, Object>> getAdminOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .orderByDesc(Order::getCreateTime);
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            wrapper.like(Order::getOrderNo, orderNo.trim());
        }
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        if (userName != null && !userName.trim().isEmpty()) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .like(User::getUsername, userName.trim()));
            if (users.isEmpty()) {
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("list", new ArrayList<>());
                emptyResult.put("total", 0);
                return Result.success(emptyResult);
            }
            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            wrapper.in(Order::getUserId, userIds);
        }

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Order> pageParam =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);

        Set<Long> userIdsInPage = orderPage.getRecords().stream().map(Order::getUserId).collect(Collectors.toSet());
        Map<Long, String> userNameMap = new HashMap<>();
        if (!userIdsInPage.isEmpty()) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, userIdsInPage));
            userNameMap = users.stream().collect(Collectors.toMap(User::getId, User::getUsername));
        }
        final Map<Long, String> finalUserNameMap = userNameMap;

        List<Map<String, Object>> rows = orderPage.getRecords().stream().map(order -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", order.getId());
            row.put("orderNo", order.getOrderNo());
            row.put("userId", order.getUserId());
            row.put("userName", finalUserNameMap.getOrDefault(order.getUserId(), "未知用户"));
            row.put("totalAmount", order.getTotalAmount());
            row.put("status", order.getStatus());
            row.put("createTime", order.getCreateTime());
            row.put("payTime", order.getPayTime());
            row.put("deliveryTime", order.getDeliveryTime());
            row.put("completeTime", order.getCompleteTime());
            return row;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", rows);
        result.put("total", orderPage.getTotal());
        return Result.success(result);
    }

    /**
     * 管理端订单详情
     */
    @GetMapping("/orders/{orderId}")
    public Result<Map<String, Object>> getAdminOrderDetail(@PathVariable Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        User user = userMapper.selectById(order.getUserId());
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));
        List<Map<String, Object>> itemRows = items.stream().map(item -> {
            Map<String, Object> row = new HashMap<>();
            row.put("productName", item.getProductName());
            row.put("price", item.getPrice());
            row.put("quantity", item.getQuantity());
            BigDecimal subtotal = (item.getPrice() == null || item.getQuantity() == null)
                    ? BigDecimal.ZERO
                    : item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            row.put("subtotal", subtotal);
            return row;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("id", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("userId", order.getUserId());
        result.put("userName", user == null ? "未知用户" : user.getUsername());
        result.put("totalAmount", order.getTotalAmount());
        result.put("status", order.getStatus());
        result.put("createTime", order.getCreateTime());
        result.put("payTime", order.getPayTime());
        result.put("deliveryTime", order.getDeliveryTime());
        result.put("completeTime", order.getCompleteTime());
        result.put("items", itemRows);
        return Result.success(result);
    }

    /**
     * 管理端发货
     */
    @PutMapping("/orders/{orderId}/ship")
    public Result<?> shipOrder(@PathVariable Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != 1) {
            return Result.error("只有已支付订单可以发货");
        }
        order.setStatus(2);
        order.setDeliveryTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return Result.success("发货成功");
    }

    /**
     * 管理端取消订单
     */
    @PutMapping("/orders/{orderId}/cancel")
    public Result<?> cancelOrderByAdmin(@PathVariable Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != 0) {
            return Result.error("只有待支付订单可以取消");
        }
        order.setStatus(4);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return Result.success("订单已取消");
    }

    /**
     * 订单 AI 分析
     */
    @PostMapping("/orders/analysis/ai")
    public Result<Map<String, Object>> getOrderAnalysisByAI(@RequestBody Map<String, Object> payload) {
        try {
            Object totalOrders = payload.getOrDefault("totalOrders", 0);
            Object totalAmount = payload.getOrDefault("totalAmount", 0);
            Object avgAmount = payload.getOrDefault("avgAmount", 0);
            Object conversionRate = payload.getOrDefault("conversionRate", 0);
            Object type = payload.getOrDefault("type", "date");
            Object chartData = payload.getOrDefault("chartData", new ArrayList<>());

            String prompt = "你是商城后台运营分析助手，请基于以下订单分析数据给出结论和建议。\n"
                    + "要求：使用简体中文，输出结构为【结论】【问题】【建议】三部分，每部分2-4条，简洁可执行。\n\n"
                    + "分析维度：" + type + "\n"
                    + "订单总数：" + totalOrders + "\n"
                    + "总销售额：" + totalAmount + "\n"
                    + "平均订单金额：" + avgAmount + "\n"
                    + "订单转化率：" + conversionRate + "%\n"
                    + "图表数据：" + com.alibaba.fastjson2.JSON.toJSONString(chartData) + "\n";

            String aiContent = llmService.chat(prompt, null);
            Map<String, Object> result = new HashMap<>();
            result.put("content", aiContent == null ? "" : aiContent);
            return Result.success(result);
        } catch (Exception e) {
            log.error("订单AI分析失败", e);
            return Result.error("订单AI分析失败：" + e.getMessage());
        }
    }

    private void buildDateAnalysisChart(List<Map<String, Object>> chartData,
                                        List<Order> orders,
                                        int targetYear) {
        Map<Integer, Long> countByMonth = orders.stream()
                .filter(o -> o.getCreateTime() != null && o.getCreateTime().getYear() == targetYear)
                .collect(Collectors.groupingBy(o -> o.getCreateTime().getMonthValue(), Collectors.counting()));
        for (int month = 1; month <= 12; month++) {
            Map<String, Object> row = new HashMap<>();
            row.put("label", month + "月");
            row.put("value", countByMonth.getOrDefault(month, 0L));
            chartData.add(row);
        }
    }

    private void buildProductAnalysisChart(List<Map<String, Object>> chartData, List<Order> orders) {
        if (orders.isEmpty()) {
            return;
        }
        Set<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toSet());
        if (orderIds.isEmpty()) {
            return;
        }
        List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds)
        );
        Map<String, Integer> salesByProduct = new HashMap<>();
        for (OrderItem item : orderItems) {
            String name = item.getProductName() == null ? "未知商品" : item.getProductName();
            int qty = item.getQuantity() == null ? 0 : item.getQuantity();
            salesByProduct.put(name, salesByProduct.getOrDefault(name, 0) + qty);
        }
        salesByProduct.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .forEach(e -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("label", e.getKey());
                    row.put("value", e.getValue());
                    chartData.add(row);
                });
    }

    private void buildUserAnalysisChart(List<Map<String, Object>> chartData, List<Order> orders) {
        Map<Long, Long> countByUserId = orders.stream()
                .collect(Collectors.groupingBy(Order::getUserId, Collectors.counting()));
        if (countByUserId.isEmpty()) {
            return;
        }
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, countByUserId.keySet()));
        Map<Long, String> userNameMap = users.stream().collect(Collectors.toMap(User::getId, User::getUsername));
        countByUserId.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .forEach(e -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("label", userNameMap.getOrDefault(e.getKey(), "用户" + e.getKey()));
                    row.put("value", e.getValue());
                    chartData.add(row);
                });
    }
}
