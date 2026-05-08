<template>
  <div v-if="orders && orders.length" class="order-rag-cards">
    <div v-for="(row, idx) in orders" :key="idx" class="order-card">
      <template v-if="row.type === 'order_miss'">
        <div class="order-card-title">订单查询</div>
        <p class="order-miss">{{ row.hint || '未找到该订单。' }}</p>
        <p v-if="row.orderNo" class="order-no">单号：{{ row.orderNo }}</p>
      </template>
      <template v-else-if="row.type === 'order'">
        <div class="order-card-title">订单 {{ row.orderNo }}</div>
        <ul class="order-meta">
          <li><span>状态</span>{{ row.statusText || '—' }}</li>
          <li><span>金额</span>{{ row.totalAmount != null ? row.totalAmount + ' 元' : '—' }}</li>
          <li v-if="row.createTime"><span>下单</span>{{ row.createTime }}</li>
          <li v-if="row.payTime && row.payTime !== '—'"><span>支付</span>{{ row.payTime }}</li>
          <li v-if="row.deliveryTime && row.deliveryTime !== '—'"><span>发货</span>{{ row.deliveryTime }}</li>
        </ul>
        <p v-if="row.itemsSummary" class="order-items">商品：{{ row.itemsSummary }}</p>
        <p class="order-logistics">{{ row.logisticsSummary }}</p>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  orders: Record<string, unknown>[]
}>()
</script>

<style scoped lang="scss">
.order-rag-cards {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.order-card {
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.75);
  border: 1px solid rgba(34, 211, 238, 0.28);
  font-size: 13px;
  line-height: 1.55;
}

.order-card-title {
  font-weight: 700;
  color: #22d3ee;
  margin-bottom: 6px;
  letter-spacing: 0.04em;
}

.order-meta {
  list-style: none;
  padding: 0;
  margin: 0 0 6px;
  li {
    display: flex;
    gap: 8px;
    margin-bottom: 2px;
    color: #e2e8f0;
    span {
      min-width: 36px;
      color: #94a3b8;
      font-size: 12px;
    }
  }
}

.order-items {
  margin: 4px 0;
  color: #cbd5e1;
  font-size: 12px;
}

.order-logistics {
  margin: 0;
  color: #a7f3d0;
  font-size: 12px;
}

.order-miss {
  margin: 0 0 4px;
  color: #fecaca;
}

.order-no {
  margin: 0;
  font-family: ui-monospace, monospace;
  color: #e2e8f0;
  font-size: 12px;
}
</style>
