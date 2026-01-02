<template>
  <div class="message-input">
    <el-input
      v-model="inputMessage"
      type="textarea"
      :rows="3"
      placeholder="输入您的问题..."
      @keydown.ctrl.enter="handleSend"
      @keydown.meta.enter="handleSend"
      :disabled="loading"
    />
    <div class="input-actions">
      <div class="tips">按 Ctrl+Enter 或 Cmd+Enter 发送</div>
      <el-button
        type="primary"
        @click="handleSend"
        :loading="loading"
        :disabled="!inputMessage.trim()"
      >
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface Props {
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const emit = defineEmits<{
  send: [message: string]
}>()

const inputMessage = ref('')

const handleSend = () => {
  if (!inputMessage.value.trim() || props.loading) return
  
  emit('send', inputMessage.value)
  inputMessage.value = ''
}
</script>

<style scoped lang="scss">
.message-input {
  .input-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 10px;

    .tips {
      font-size: 12px;
      color: #909399;
    }
  }
}
</style>
