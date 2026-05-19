<template>
  <div class="conversation-list">
    <div class="list-header">
      <h3>会话列表</h3>
      <el-button type="primary" size="small" @click="handleNew">
        <el-icon><Plus /></el-icon>
        新建会话
      </el-button>
    </div>
    <div class="list-content">
      <div
        v-for="item in conversations"
        :key="item.sessionId"
        :class="['conversation-item', { active: item.sessionId === currentSessionId }]"
        @click="handleSelect(item.sessionId)"
      >
        <div class="item-content">
          <div class="item-title">{{ item.title }}</div>
          <div class="item-preview" v-if="item.lastMessage">
            {{ item.lastMessage }}
          </div>
          <div class="item-time">{{ formatTime(item.updateTime) }}</div>
        </div>
        <el-button
          type="danger"
          size="small"
          text
          @click.stop="handleDelete(item.sessionId)"
        >
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
      <el-empty v-if="conversations.length === 0" description="暂无会话" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { Plus, Delete } from '@element-plus/icons-vue'
import type { Conversation } from '@/api/assistant'

interface Props {
  conversations: Conversation[]
  currentSessionId?: string
}

defineProps<Props>()

const emit = defineEmits<{
  select: [sessionId: string]
  new: []
  delete: [sessionId: string]
}>()

const handleSelect = (sessionId: string) => {
  emit('select', sessionId)
}

const handleNew = () => {
  emit('new')
}

const handleDelete = (sessionId: string) => {
  emit('delete', sessionId)
}

const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}
</script>

<style scoped lang="scss">
.conversation-list {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: rgba(7, 11, 22, 0.82);
  backdrop-filter: blur(14px);
  border-radius: var(--radius-lg, 12px) 0 0 var(--radius-lg, 12px);
  border: 1px solid rgba(34, 211, 238, 0.14);
  border-right: none;
  overflow: hidden;

  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid var(--color-border, #e5e7eb);

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 700;
      color: var(--color-text-bold, #222);
    }
  }

  .list-content {
    flex: 1;
    overflow-y: auto;
    padding: 12px;

    .conversation-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px;
      margin-bottom: 8px;
      border-radius: var(--radius-md, 8px);
      cursor: pointer;
      transition: var(--transition, all 0.28s ease-out);
      border: 1px solid transparent;

      &:hover {
        background-color: rgba(34, 211, 238, 0.07);
      }

      &.active {
        background: linear-gradient(
          117deg,
          rgba(34, 211, 238, 0.18) 0%,
          rgba(74, 222, 128, 0.1) 100%
        );
        border: 1px solid rgba(74, 222, 128, 0.35);
        box-shadow: 0 0 22px rgba(34, 211, 238, 0.1);
      }

      .item-content {
        flex: 1;
        min-width: 0;

        .item-title {
          font-weight: 500;
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .item-preview {
          font-size: 12px;
          color: var(--color-text-weak, #888);
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .item-time {
          font-size: 12px;
          color: var(--color-text-weak, #888);
        }
      }
    }
  }
}
</style>
