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
  :deep(.el-textarea__inner) {
    border-radius: var(--radius-lg, 12px);
    border: 1px solid var(--color-border, #e5e7eb);
    background: rgba(255, 255, 255, 0.95);
    box-shadow: var(--shadow-sm, 0 2px 8px rgba(0, 0, 0, 0.04));
    transition: var(--transition, all 0.28s ease-out);

    &:focus {
      border-color: var(--color-primary-start, #667eea);
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.12);
    }
  }

  .input-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12px;

    .tips {
      font-size: 12px;
      color: var(--color-text-weak, #888);
    }
  }
}
</style>
