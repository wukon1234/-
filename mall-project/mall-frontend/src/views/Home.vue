<template>
  <div class="home page-fade">
    <section class="hero">
      <div class="hero-matrix" aria-hidden="true" />
      <div class="hero-scan" aria-hidden="true" />
      <div class="hero-wrap">
        <div class="hero-copy">
          <div class="hero-chyron">
            <span class="blink">OBS</span>
            <span>EARTH_LINK</span>
            <span>ASSIST ONLINE</span>
          </div>
          <h1>
            <span class="hero-title-muted">静默观测：</span><br />
            用<strong class="hero-accent">智策</strong>与算法，在低噪声里完成每一笔交易。
          </h1>
          <p class="hero-sub">
            像面壁者推演未来一样比价、选规格、追问库存；助手在链路末端为你给出可执行的回答。
          </p>
          <div class="hero-actions">
            <el-button type="primary" size="large" @click="$router.push('/assistant')">
              <el-icon><ChatDotRound /></el-icon>
              启动对话信道
            </el-button>
            <el-button size="large" class="btn-secondary" @click="$router.push('/products')">
              浏览商品阵列
            </el-button>
          </div>
        </div>
      </div>
    </section>

    <section class="quick-nav card-surface">
      <h2 class="block-title">快捷入口</h2>
      <div class="quick-grid">
        <button type="button" class="quick-item" @click="$router.push('/products')">
          <el-icon :size="26"><Shop /></el-icon>
          <span>商品</span>
        </button>
        <button type="button" class="quick-item" @click="$router.push('/cart')">
          <el-icon :size="26"><ShoppingCart /></el-icon>
          <span>购物车</span>
        </button>
        <button type="button" class="quick-item" @click="$router.push('/orders')">
          <el-icon :size="26"><List /></el-icon>
          <span>订单</span>
        </button>
        <button type="button" class="quick-item" @click="$router.push('/assistant')">
          <el-icon :size="26"><ChatDotRound /></el-icon>
          <span>智能助手</span>
        </button>
      </div>
    </section>

    <section class="assistant-cta card-surface" @click="$router.push('/assistant')">
      <div class="assistant-cta-text">
        <h3>智能购物助手</h3>
        <p>对话内可推荐商品、对比参数、查询订单，一站式完成选购。</p>
      </div>
      <el-button type="primary" round>立即体验</el-button>
    </section>

    <div class="section">
      <div class="section-title">
        <h2>热门商品</h2>
        <el-button link type="primary" @click="$router.push('/products')">查看更多</el-button>
      </div>
      <el-row :gutter="20" v-if="hot.length > 0">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="p in hot" :key="p.id">
          <el-card class="p-card" shadow="never" @click="$router.push(`/products/${p.id}`)">
            <div class="p-img-wrap">
              <el-image :src="p.imageUrl || '/placeholder.png'" class="p-img" fit="cover" />
            </div>
            <div class="p-name">{{ p.name }}</div>
            <div class="p-bottom">
              <div class="p-price">¥{{ Number(p.price).toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-else description="暂无热门商品" />
    </div>

    <div class="section">
      <div class="section-title">
        <h2>最新上架</h2>
        <el-button link type="primary" @click="$router.push('/products')">查看更多</el-button>
      </div>
      <el-row :gutter="20" v-if="news.length > 0">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="p in news" :key="p.id">
          <el-card class="p-card" shadow="never" @click="$router.push(`/products/${p.id}`)">
            <div class="p-img-wrap">
              <el-image :src="p.imageUrl || '/placeholder.png'" class="p-img" fit="cover" />
            </div>
            <div class="p-name">{{ p.name }}</div>
            <div class="p-bottom">
              <div class="p-price">¥{{ Number(p.price).toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-else description="暂无最新商品" />
    </div>

    <footer class="site-footer">
      <p>© {{ year }} MALL.OS · 「光锥之内，即是命运。」界面仅为风格致敬，与实际剧情无关。</p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ChatDotRound, Shop, ShoppingCart, List } from '@element-plus/icons-vue'
import { ref, onMounted } from 'vue'
import { getHotProducts, getNewProducts, type Product } from '@/api/product'

const hot = ref<Product[]>([])
const news = ref<Product[]>([])
const year = new Date().getFullYear()

const load = async () => {
  const [hotRes, newRes] = await Promise.all([getHotProducts(8), getNewProducts(8)])
  hot.value = hotRes.data || []
  news.value = newRes.data || []
}

onMounted(() => load())
</script>

<style scoped lang="scss">
.home {
  padding-bottom: 32px;
}

.page-fade {
  animation: pageIn 0.45s ease-out both;
}

@keyframes pageIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero {
  position: relative;
  margin-bottom: 24px;
  border-radius: var(--radius-lg, 12px);
  overflow: hidden;
  border: 1px solid rgba(34, 211, 238, 0.22);
  background:
    radial-gradient(ellipse 100% 100% at 10% -20%, rgba(34, 211, 238, 0.15), transparent 55%),
    radial-gradient(circle at 95% 10%, rgba(74, 222, 128, 0.1), transparent 45%),
    linear-gradient(160deg, #060b17 0%, #0b1328 52%, #04060d 100%);
  box-shadow: var(--shadow-lg);
  transition: var(--transition, all 0.28s ease-out);

  &:hover {
    border-color: rgba(74, 222, 128, 0.32);
    box-shadow: var(--shadow-hover);
  }

  .hero-matrix {
    position: absolute;
    inset: 0;
    opacity: 0.08;
    background-image:
      linear-gradient(rgba(34, 211, 238, 0.5) 1px, transparent 1px),
      linear-gradient(90deg, rgba(34, 211, 238, 0.5) 1px, transparent 1px);
    background-size: 38px 38px;
    pointer-events: none;
    mask-image: radial-gradient(circle at 50% 0%, black 0%, transparent 75%);
  }

  .hero-scan {
    position: absolute;
    inset: 0;
    pointer-events: none;
    background: linear-gradient(transparent 30%, rgba(34, 211, 238, 0.05) 50%, transparent 72%);
    animation: sweep 8s linear infinite;
    opacity: 0.65;
  }

  .hero-wrap {
    padding: 48px 40px;
    position: relative;
    z-index: 1;

    @media (max-width: 768px) {
      padding: 36px 24px;
    }
  }

  .hero-copy {
    color: var(--color-text-bold, #f1f5f9);
    max-width: 700px;
  }

  .hero-chyron {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    align-items: center;
    margin-bottom: 18px;
    font-family: var(--font-mono, monospace);
    font-size: 11px;
    letter-spacing: 0.18em;
    text-transform: uppercase;
    color: var(--color-accent-cyan, #22d3ee);

    span {
      padding: 4px 8px;
      border: 1px solid rgba(34, 211, 238, 0.35);
      border-radius: 4px;
      background: rgba(15, 23, 42, 0.45);

      &.blink {
        animation: chyron-blink 2.8s ease-in-out infinite;
      }
    }
  }

  h1 {
    margin: 0 0 18px 0;
    font-size: clamp(24px, 3.8vw, 34px);
    font-weight: 700;
    line-height: 1.35;
    letter-spacing: 0.02em;

    .hero-title-muted {
      font-weight: 500;
      color: var(--color-text-weak, #64748b);
      font-size: 0.55em;
      letter-spacing: 0.2em;
      text-transform: uppercase;
      font-family: var(--font-mono, monospace);
    }

    .hero-accent {
      color: var(--color-accent-cyan, #22d3ee);
      text-shadow: 0 0 24px rgba(34, 211, 238, 0.35);
    }
  }

  .hero-sub {
    margin: 0 0 30px 0;
    font-size: 15px;
    line-height: 1.85;
    color: var(--color-text, #cbd5e1);
    max-width: 620px;
  }

  .hero-actions {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;

    .btn-secondary {
      background: transparent !important;
      color: var(--color-accent-cyan, #22d3ee);
      border: 1px solid rgba(34, 211, 238, 0.55);
      font-weight: 700;

      &:hover {
        border-color: rgba(74, 222, 128, 0.85);
        color: var(--color-accent-green, #4ade80);
        box-shadow: 0 0 20px rgba(34, 211, 238, 0.15);
      }
    }
  }
}

@keyframes sweep {
  0% {
    transform: translateY(-100%);
  }
  100% {
    transform: translateY(100%);
  }
}

@keyframes chyron-blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.45;
  }
}

.card-surface {
  background: var(--surface-panel, rgba(15, 23, 42, 0.86));
  border: 1px solid var(--tech-panel-border, rgba(34, 211, 238, 0.22));
  border-radius: var(--radius-lg, 12px);
  box-shadow: var(--shadow-sm);
  padding: 24px;
  margin-bottom: 24px;
  backdrop-filter: blur(10px);
  transition: var(--transition, all 0.28s ease-out);
}

.quick-nav {
  .block-title {
    margin: 0 0 16px 0;
    font-size: 20px;
    font-weight: 700;
    color: var(--color-text-bold, #222);
  }

  .quick-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;

    @media (max-width: 768px) {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  .quick-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    padding: 20px 12px;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-lg, 12px);
    background: rgba(15, 23, 42, 0.55);
    cursor: pointer;
    font-size: 14px;
    font-weight: 600;
    color: var(--color-text);
    transition: var(--transition, all 0.28s ease-out);

    &:hover {
      border-color: rgba(34, 211, 238, 0.45);
      box-shadow: 0 0 24px rgba(34, 211, 238, 0.12);
      transform: translateY(-2px);
      color: var(--color-accent-cyan, #22d3ee);
    }
  }
}

.assistant-cta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  cursor: pointer;
  background: linear-gradient(
    117deg,
    rgba(34, 211, 238, 0.1) 0%,
    rgba(74, 222, 128, 0.08) 50%,
    rgba(59, 130, 246, 0.03) 100%
  );

  @media (max-width: 768px) {
    flex-direction: column;
    align-items: flex-start;
  }

  .assistant-cta-text h3 {
    margin: 0 0 8px 0;
    font-size: 18px;
    font-weight: 700;
    color: var(--color-text-bold, #222);
  }

  .assistant-cta-text p {
    margin: 0;
    font-size: 14px;
    color: var(--color-text-weak, #888);
    line-height: 1.5;
    max-width: 520px;
  }

  &:hover {
    border-color: rgba(34, 211, 238, 0.45);
    box-shadow: var(--shadow-md);
  }
}

.section {
  margin-bottom: 40px;

  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--color-border, #e5e7eb);

    h2 {
      margin: 0;
      color: var(--color-text-bold, #222);
      font-size: 22px;
      font-weight: 700;
      position: relative;

      &::after {
        content: '';
        position: absolute;
        bottom: -13px;
        left: 0;
        width: 40px;
        height: 3px;
        background: var(--color-primary-gradient);
        border-radius: 3px;
      }
    }
  }

  .p-card {
    margin-bottom: 20px;
    cursor: pointer;
    border-radius: var(--radius-lg, 12px);
    border: 1px solid var(--color-border, #e5e7eb);
    transition: var(--transition, all 0.28s ease-out);
    overflow: hidden;

    &:hover {
      transform: translateY(-4px);
      box-shadow: var(--shadow-hover);
      border-color: rgba(34, 211, 238, 0.42);
    }

    .p-img-wrap {
      overflow: hidden;
      border-radius: var(--radius-lg, 12px) var(--radius-lg, 12px) 0 0;
      background: var(--color-bg-200, #f1f3f5);
    }

    .p-img {
      width: 100%;
      height: 180px;
      display: block;
      transition: var(--transition, all 0.28s ease-out);
    }

    &:hover .p-img {
      transform: scale(1.04);
    }

    :deep(.el-card__body) {
      padding: 16px;
    }

    .p-name {
      margin: 0 0 12px 0;
      font-weight: 600;
      color: var(--color-text-bold, #222);
      font-size: 16px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      line-height: 1.5;
      min-height: 48px;
    }

    .p-bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .p-price {
        color: var(--color-danger, #ff4d4f);
        font-weight: 700;
        font-size: 18px;
      }
    }
  }
}

.site-footer {
  text-align: center;
  padding: 24px 16px 0;
  font-size: 12px;
  color: var(--color-text-weak, #888);
}
</style>
