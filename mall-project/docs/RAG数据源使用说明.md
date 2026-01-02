# RAG数据源使用说明

## 概述

本系统支持RAG（检索增强生成）的多数据源功能，可以灵活配置和扩展不同类型的数据源，实现跨数据源的语义搜索。

## 功能特性

1. **多数据源支持**：支持注册多个不同类型的数据源（商品、订单、用户等）
2. **灵活检索**：可以指定单个数据源或同时检索多个数据源
3. **可扩展性**：通过实现`RAGDataSource`接口轻松添加新的数据源
4. **配置化管理**：通过配置文件管理数据源的启用状态

## 数据源类型

系统内置了以下数据源类型（定义在`DataSource`枚举中）：

- `PRODUCT`：商品数据源
- `ORDER`：订单数据源
- `USER`：用户数据源
- `CUSTOM`：自定义数据源

## 使用方法

### 1. 配置数据源

在`application.yml`中配置：

```yaml
rag:
  # 启用的数据源列表
  enabled-sources:
    - product
    - order
  # 默认检索的数据源
  default-source: product
  # 是否启用多数据源检索
  multi-source-enabled: true
```

### 2. 使用单个数据源检索

```java
@Autowired
private RAGService ragService;

// 从商品数据源检索
List<Map<String, Object>> products = ragService.search(
    "手机", 
    DataSource.PRODUCT, 
    5, 
    null
);

// 带过滤条件的检索
Map<String, Object> filters = new HashMap<>();
filters.put("categoryId", 1L);
filters.put("minPrice", new BigDecimal("1000"));
filters.put("maxPrice", new BigDecimal("5000"));

List<Map<String, Object>> filteredProducts = ragService.search(
    "手机", 
    DataSource.PRODUCT, 
    5, 
    filters
);
```

### 3. 使用多个数据源检索

```java
// 同时从多个数据源检索
List<DataSource> sources = Arrays.asList(
    DataSource.PRODUCT, 
    DataSource.ORDER
);

Map<DataSource, List<Map<String, Object>>> results = ragService.searchMulti(
    "手机订单", 
    sources, 
    5, 
    null
);

// 处理结果
List<Map<String, Object>> products = results.get(DataSource.PRODUCT);
List<Map<String, Object>> orders = results.get(DataSource.ORDER);
```

### 4. 注册自定义数据源

#### 步骤1：实现RAGDataSource接口

```java
@Component
public class CustomRAGDataSource implements RAGDataSource {
    
    @Override
    public DataSource getType() {
        return DataSource.CUSTOM;
    }
    
    @Override
    public List<Map<String, Object>> search(String query, int topK, Map<String, Object> filters) {
        // 实现搜索逻辑
        List<Map<String, Object>> results = new ArrayList<>();
        // ... 搜索代码
        return results;
    }
    
    @Override
    public String toText(Map<String, Object> data) {
        // 将数据转换为用于向量化的文本
        return data.get("name") + " " + data.get("description");
    }
    
    @Override
    public String getDescription() {
        return "自定义数据源描述";
    }
}
```

#### 步骤2：注册数据源

数据源会在Spring容器启动时自动注册，也可以手动注册：

```java
@Autowired
private RAGService ragService;

@PostConstruct
public void init() {
    RAGDataSource customDataSource = new CustomRAGDataSource();
    ragService.registerDataSource(customDataSource);
}
```

### 5. 查看已注册的数据源

```java
@Autowired
private RAGService ragService;

List<RAGDataSource> dataSources = ragService.getRegisteredDataSources();
for (RAGDataSource ds : dataSources) {
    System.out.println("数据源: " + ds.getType() + " - " + ds.getDescription());
}
```

## 内置数据源说明

### 商品数据源（ProductRAGDataSource）

**功能**：从商品表中检索商品信息

**支持的过滤条件**：
- `categoryId`：分类ID
- `minPrice`：最低价格
- `maxPrice`：最高价格

**示例**：
```java
Map<String, Object> filters = new HashMap<>();
filters.put("categoryId", 1L);
filters.put("minPrice", new BigDecimal("100"));

List<Map<String, Object>> products = ragService.search(
    "手机", 
    DataSource.PRODUCT, 
    10, 
    filters
);
```

**返回数据格式**：
```json
{
  "id": 1,
  "type": "product",
  "name": "iPhone 15 Pro",
  "description": "Apple iPhone 15 Pro 256GB",
  "price": 8999.00,
  "categoryId": 4,
  "stock": 100,
  "imageUrl": "/images/iphone15.jpg",
  "specs": "{\"color\": \"钛金属色\"}"
}
```

## 扩展更多数据源

### 订单数据源示例

```java
@Component
public class OrderRAGDataSource implements RAGDataSource {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Override
    public DataSource getType() {
        return DataSource.ORDER;
    }
    
    @Override
    public List<Map<String, Object>> search(String query, int topK, Map<String, Object> filters) {
        // 实现订单搜索逻辑
        // ...
    }
    
    @Override
    public String toText(Map<String, Object> data) {
        return "订单号: " + data.get("orderNo") + " 金额: " + data.get("totalAmount");
    }
    
    @Override
    public String getDescription() {
        return "订单数据源，包含订单号、金额、状态等信息";
    }
}
```

## 与向量数据库集成

当前实现使用的是关键词匹配，后续可以集成向量数据库（如Chroma、Milvus）实现真正的语义搜索：

```java
@Override
public List<Map<String, Object>> search(String query, int topK, Map<String, Object> filters) {
    // 1. 将查询文本向量化
    List<Float> queryVector = embeddingService.embed(query);
    
    // 2. 在向量数据库中搜索相似向量
    List<String> similarIds = vectorDB.search(queryVector, topK);
    
    // 3. 从MySQL获取完整数据
    List<Map<String, Object>> results = fetchDataByIds(similarIds);
    
    return results;
}
```

## 注意事项

1. 数据源类需要使用`@Component`注解，才能被Spring自动扫描和注册
2. 确保每个数据源的`getType()`返回值唯一
3. 数据源的`search`方法应该处理null和空查询的情况
4. 建议为数据源添加适当的日志记录，便于调试
5. 如果使用向量数据库，需要在数据变更时同步更新向量

## 最佳实践

1. **数据源职责单一**：每个数据源只负责一种类型的数据检索
2. **统一数据格式**：所有数据源返回的数据都应该包含`id`和`type`字段
3. **合理使用过滤**：利用过滤条件减少不必要的搜索，提高性能
4. **缓存优化**：对于频繁查询的数据，可以考虑添加缓存层
5. **异步处理**：大数据量的检索可以考虑异步处理，提升响应速度

## 示例代码

完整示例请参考：
- `ProductRAGDataSource`：商品数据源实现示例
- `SimpleRAGServiceImpl`：RAG服务实现
- `AssistantServiceImpl`：智能助手服务中使用RAG的示例

