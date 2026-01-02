import request from './request'

export interface ChatRequest {
  sessionId?: string
  message: string
}

export interface ChatResponse {
  sessionId: string
  message: string
  relatedProducts?: Product[]
  messageId: number
}

export interface Product {
  id: number
  name: string
  description?: string
  price: number
  imageUrl?: string
}

export interface Conversation {
  id: number
  sessionId: string
  title: string
  lastMessage?: string
  updateTime: string
}

/**
 * 发送消息（普通）
 */
export const chat = (data: ChatRequest) => {
  return request.post<ChatResponse>('/assistant/chat', data)
}

/**
 * 发送消息（流式 - SSE）
 */
export const chatStream = (data: ChatRequest, onMessage: (chunk: string) => void, onDone: () => void, onError: (error: Error) => void) => {
  const eventSource = new EventSource(`/api/assistant/chat/stream?${new URLSearchParams({
    sessionId: data.sessionId || '',
    message: data.message
  })}`)
  
  eventSource.addEventListener('message', (event) => {
    if (event.data === '[DONE]') {
      eventSource.close()
      onDone()
    } else {
      onMessage(event.data)
    }
  })
  
  eventSource.addEventListener('error', (error) => {
    eventSource.close()
    onError(new Error('流式对话连接错误'))
  })
  
  // 注意：由于SSE不支持POST请求体，这里使用GET方式
  // 实际应该使用POST + SSE，需要通过其他方式实现
  return eventSource
}

/**
 * 使用POST方式发送流式消息（使用fetch + ReadableStream）
 */
export const chatStreamPost = async (
  data: ChatRequest,
  onMessage: (chunk: string) => void,
  onDone: (relatedProducts?: Product[]) => void,
  onError: (error: Error) => void
) => {
  try {
    const token = localStorage.getItem('token')
    const userId = localStorage.getItem('userId') || '1'
    
    const response = await fetch('/api/assistant/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        'userId': userId
      },
      body: JSON.stringify(data)
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body?.getReader()
    const decoder = new TextDecoder()

    if (!reader) {
      throw new Error('无法获取响应流')
    }

    let buffer = ''
    let relatedProducts: Product[] | undefined
    
    while (true) {
      const { done, value } = await reader.read()
      
      if (done) {
        onDone(relatedProducts)
        break
      }

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const messageData = line.slice(6).trim()
          if (messageData === '[DONE]') {
            onDone(relatedProducts)
            return
          }
          
          // 尝试解析JSON（可能包含商品推荐信息）
          try {
            const jsonData = JSON.parse(messageData)
            if (jsonData.products) {
              relatedProducts = jsonData.products
            }
            if (jsonData.chunk) {
              onMessage(jsonData.chunk)
            }
          } catch {
            // 如果不是JSON，直接作为文本消息
            onMessage(messageData)
          }
        } else if (line.startsWith('event: ')) {
          // 处理事件类型
          const eventType = line.slice(7).trim()
          if (eventType === 'products') {
            // 可以在这里处理商品推荐事件
          }
        }
      }
    }
  } catch (error) {
    onError(error as Error)
  }
}

/**
 * 获取会话列表
 */
export const getConversations = (page = 1, size = 10) => {
  return request.get<{
    list: Conversation[]
    total: number
  }>('/assistant/conversations', {
    params: { page, size }
  })
}

/**
 * 获取会话历史消息
 */
export const getMessages = (sessionId: string) => {
  return request.get<Array<{
    id: number
    role: 'user' | 'assistant'
    content: string
    createTime: string
  }>>(`/assistant/conversation/${sessionId}/messages`)
}

/**
 * 删除会话
 */
export const deleteConversation = (sessionId: string) => {
  return request.delete(`/assistant/conversation/${sessionId}`)
}
