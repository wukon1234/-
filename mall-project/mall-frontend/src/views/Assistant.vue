<template>
  <div class="assistant-container">
    <el-row :gutter="20" class="assistant-layout">
      <!-- 左侧会话列表 -->
      <el-col :span="6" class="conversation-list-col">
        <ConversationList
          :conversations="conversations"
          :current-session-id="currentSessionId"
          @select="handleSelectConversation"
          @new="handleNewConversation"
          @delete="handleDeleteConversation"
        />
      </el-col>

      <!-- 中间对话窗口 -->
      <el-col :span="18" class="chat-window-col">
        <ChatWindow
          :session-id="currentSessionId"
          :messages="messages"
          :loading="loading"
          @send="handleSendMessage"
        />
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import ConversationList from '@/components/assistant/ConversationList.vue'
import ChatWindow from '@/components/assistant/ChatWindow.vue'
import { getConversations, getMessages, deleteConversation, type Conversation } from '@/api/assistant'
import { ElMessage } from 'element-plus'

const conversations = ref<Conversation[]>([])
const currentSessionId = ref<string>('')
const messages = ref<any[]>([])
const loading = ref(false)

// 加载会话列表
const loadConversations = async () => {
  try {
    const res = await getConversations()
    if (res.data?.list) {
      conversations.value = res.data.list
      // 如果没有当前会话，选择第一个
      if (!currentSessionId.value && res.data.list.length > 0) {
        currentSessionId.value = res.data.list[0].sessionId
      }
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

// 加载消息历史
const loadMessages = async (sessionId: string) => {
  if (!sessionId) return
  
  try {
    loading.value = true
    const res = await getMessages(sessionId)
    if (res.data) {
      // 转换消息格式，将role从数字转换为字符串
      messages.value = res.data.map((msg: any) => ({
        ...msg,
        role: msg.role === 1 ? 'user' : 'assistant',
        // 如果有relatedProducts，需要查询商品详情
        relatedProducts: msg.relatedProducts || []
      }))
    }
  } catch (error) {
    console.error('加载消息历史失败:', error)
  } finally {
    loading.value = false
  }
}

// 选择会话
const handleSelectConversation = (sessionId: string) => {
  currentSessionId.value = sessionId
}

// 新建会话
const handleNewConversation = () => {
  currentSessionId.value = ''
  messages.value = []
}

// 删除会话
const handleDeleteConversation = async (sessionId: string) => {
  try {
    await deleteConversation(sessionId)
    ElMessage.success('删除成功')
    await loadConversations()
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = ''
      messages.value = []
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 发送消息
const handleSendMessage = () => {
  // 消息发送由ChatWindow组件内部处理
  // 发送后重新加载消息
  if (currentSessionId.value) {
    loadMessages(currentSessionId.value)
  }
}

// 监听会话变化，加载消息
watch(currentSessionId, (newSessionId) => {
  if (newSessionId) {
    loadMessages(newSessionId)
  } else {
    messages.value = []
  }
})

onMounted(() => {
  loadConversations()
})
</script>

<style scoped lang="scss">
.assistant-container {
  height: calc(100vh - 140px);
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.assistant-layout {
  height: 100%;
}

.conversation-list-col {
  height: 100%;
  border-right: 1px solid #e4e7ed;
}

.chat-window-col {
  height: 100%;
}
</style>
