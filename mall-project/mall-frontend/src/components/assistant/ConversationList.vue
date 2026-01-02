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

  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px;
    border-bottom: 1px solid #e4e7ed;

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
    }
  }

  .list-content {
    flex: 1;
    overflow-y: auto;
    padding: 10px;

    .conversation-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px;
      margin-bottom: 8px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background-color: #f5f7fa;
      }

      &.active {
        background-color: #e6f7ff;
        border: 1px solid #91d5ff;
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
          color: #909399;
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .item-time {
          font-size: 12px;
          color: #c0c4cc;
        }
      }
    }
  }
}
</style>
