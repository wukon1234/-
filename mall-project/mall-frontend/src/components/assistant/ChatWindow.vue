<template>
  <div class="chat-window">
    <div class="chat-header">
      <h3>智能购物助手</h3>
      <el-switch
        v-model="useStream"
        active-text="流式对话"
        inactive-text="普通对话"
      />
    </div>
    <div class="chat-messages" ref="messagesContainer">
      <div v-if="displayMessages.length === 0" class="empty-state">
        <el-empty description="开始对话吧~" />
      </div>
      <div
        v-for="msg in displayMessages"
        :key="msg.id"
        :class="['message-item', msg.role === 'user' ? 'user-message' : 'assistant-message']"
      >
        <div class="message-avatar">
          <el-avatar :icon="msg.role === 'user' ? User : ChatDotRound" />
        </div>
        <div class="message-content">
          <div class="message-text" v-html="formatMessage(msg.content)"></div>
          
          <!-- 商品推荐 -->
          <ProductRecommendation
            v-if="msg.relatedProducts && msg.relatedProducts.length > 0"
            :products="msg.relatedProducts"
          />
          
          <div class="message-time">{{ formatTime(msg.createTime) }}</div>
        </div>
      </div>
      <!-- 流式输出中的临时消息 -->
      <div v-if="streamingMessage" class="message-item assistant-message">
        <div class="message-avatar">
          <el-avatar :icon="ChatDotRound" />
        </div>
        <div class="message-content">
          <div class="message-text">{{ streamingMessage }}</div>
          <div class="streaming-indicator">
            <el-icon class="is-loading"><Loading /></el-icon>
          </div>
        </div>
      </div>
      <div v-if="loading && !useStream" class="message-item assistant-message">
        <div class="message-avatar">
          <el-avatar :icon="ChatDotRound" />
        </div>
        <div class="message-content">
          <div class="message-text">
            <el-icon class="is-loading"><Loading /></el-icon>
            正在思考...
          </div>
        </div>
      </div>
    </div>
    <div class="chat-input">
      <MessageInput @send="handleSend" :loading="loading || streaming" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, nextTick, watch } from 'vue'
import { User, ChatDotRound, Loading } from '@element-plus/icons-vue'
import MessageInput from './MessageInput.vue'
import ProductRecommendation from './ProductRecommendation.vue'
import { chat, chatStreamPost, type ChatRequest } from '@/api/assistant'
import { ElMessage } from 'element-plus'

interface Props {
  sessionId?: string
  messages?: any[]
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  messages: () => [],
  loading: false
})

const emit = defineEmits<{
  send: []
}>()

const messagesContainer = ref<HTMLElement>()
const useStream = ref(true) // 默认使用流式对话
const streaming = ref(false)
const streamingMessage = ref('')
const currentStreamAbortController = ref<AbortController | null>(null)
const optimisticMessages = ref<any[]>([])
let tempMessageId = 0

const displayMessages = computed(() => [...(props.messages || []), ...optimisticMessages.value])

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 监听消息变化，自动滚动
watch(() => props.messages, () => {
  if (optimisticMessages.value.length > 0) {
    optimisticMessages.value = []
  }
  scrollToBottom()
}, { deep: true })

watch(streamingMessage, () => {
  scrollToBottom()
})

// 格式化消息（支持换行）
const formatMessage = (content: string) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br/>')
}

// 发送消息
const handleSend = async (message: string) => {
  if (!message.trim()) return

  const request: ChatRequest = {
    sessionId: props.sessionId || undefined,
    message: message.trim()
  }

  optimisticMessages.value.push({
    id: `tmp-user-${Date.now()}-${tempMessageId++}`,
    role: 'user',
    content: request.message,
    createTime: new Date().toISOString(),
    relatedProducts: []
  })
  scrollToBottom()

  try {
    if (useStream.value) {
      // 流式对话
      await handleStreamChat(request)
    } else {
      // 普通对话
      await handleNormalChat(request)
    }
  } catch (error) {
    const errorMessage = error instanceof Error ? error.message : '发送失败，请重试'
    ElMessage.error(errorMessage)
    console.error('发送消息失败:', error)
    streaming.value = false
    streamingMessage.value = ''
  }
}

// 普通对话
const handleNormalChat = async (request: ChatRequest) => {
  try {
    const res = await chat(request)
    
    if (res.data) {
      ElMessage.success('发送成功')
      emit('send')
    }
  } catch (error) {
    throw error
  }
}

// 流式对话
const handleStreamChat = async (request: ChatRequest) => {
  streaming.value = true
  streamingMessage.value = ''
  
  // 如果有之前的流，先取消
  if (currentStreamAbortController.value) {
    currentStreamAbortController.value.abort()
  }
  
  const abortController = new AbortController()
  currentStreamAbortController.value = abortController

  try {
    await chatStreamPost(
      request,
      (chunk: string) => {
        if (abortController.signal.aborted) return
        streamingMessage.value += chunk
        scrollToBottom()
      },
      (_relatedProducts) => {
        if (abortController.signal.aborted) return
        streaming.value = false
        streamingMessage.value = ''
        currentStreamAbortController.value = null
        emit('send') // 触发重新加载消息（包含商品推荐）
      },
      (error: Error) => {
        if (abortController.signal.aborted) return
        streaming.value = false
        streamingMessage.value = ''
        currentStreamAbortController.value = null
        throw error
      },
      abortController
    )
  } catch (error) {
    streaming.value = false
    streamingMessage.value = ''
    currentStreamAbortController.value = null
    throw error
  }
}

const formatTime = (time: string) => {
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped lang="scss">
.chat-window {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: rgba(7, 11, 22, 0.65);
  border-radius: 0 var(--radius-lg, 12px) var(--radius-lg, 12px) 0;
  overflow: hidden;

  .chat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid rgba(34, 211, 238, 0.15);
    background: rgba(7, 11, 22, 0.88);
    backdrop-filter: blur(12px);

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 700;
      font-family: var(--font-mono, monospace);
      letter-spacing: 0.14em;
      text-transform: uppercase;
      color: var(--color-accent-cyan, #22d3ee);
      text-shadow: 0 0 14px rgba(34, 211, 238, 0.35);
    }
  }

  .chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    background-color: rgba(4, 6, 12, 0.85);

    .empty-state {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .message-item {
      display: flex;
      margin-bottom: 20px;
      animation: fadeIn 0.3s;

      &.user-message {
        flex-direction: row-reverse;

        .message-content {
          background: var(--color-primary-gradient);
          color: #031018;
          margin-right: 10px;
          border-radius: 16px 16px 4px 16px;
          box-shadow: 0 0 24px rgba(34, 211, 238, 0.18);

          .message-time {
            color: rgba(3, 16, 24, 0.65);
          }
        }
      }

      &.assistant-message {
        .message-content {
          background: rgba(15, 23, 42, 0.94);
          color: var(--color-text-bold, #f1f5f9);
          margin-left: 10px;
          border-radius: 16px 16px 16px 4px;
          border: 1px solid rgba(74, 222, 128, 0.18);
          box-shadow: inset 0 0 48px rgba(34, 211, 238, 0.04);
        }
      }

      .message-avatar {
        flex-shrink: 0;
      }

      .message-content {
        max-width: 70%;
        padding: 12px 16px;
        box-shadow: var(--shadow-sm, 0 2px 8px rgba(0, 0, 0, 0.04));
        transition: var(--transition, all 0.28s ease-out);

        .message-text {
          word-wrap: break-word;
          line-height: 1.6;
          margin-bottom: 4px;
        }

        .streaming-indicator {
          display: inline-block;
          margin-left: 8px;
        }

        .message-time {
          font-size: 12px;
          color: var(--color-text-weak, #64748b);
          margin-top: 4px;
        }
      }
    }
  }

  .chat-input {
    padding: 16px;
    border-top: 1px solid rgba(34, 211, 238, 0.12);
    background: rgba(7, 11, 22, 0.92);
    backdrop-filter: blur(14px);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
