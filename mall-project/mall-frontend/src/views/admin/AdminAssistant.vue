<template>
  <div class="admin-assistant">
    <h2 class="page-title">智能助手管理</h2>
    
    <el-card shadow="hover" class="content-card">
      <div class="assistant-settings">
        <h3>助手设置</h3>
        <el-form :model="assistantSettings" label-width="120px">
          <el-form-item label="助手名称">
            <el-input v-model="assistantSettings.name" />
          </el-form-item>
          <el-form-item label="助手状态">
            <el-switch
              v-model="assistantSettings.enabled"
              :active-value="1"
              :inactive-value="0"
            />
          </el-form-item>
          <el-form-item label="回复模式">
            <el-select v-model="assistantSettings.responseMode">
              <el-option label="智能回复" value="intelligent" />
              <el-option label="模板回复" value="template" />
            </el-select>
          </el-form-item>
          <el-form-item label="回复超时">
            <el-input-number v-model="assistantSettings.timeout" :min="1" :max="60" />
            <span class="unit">秒</span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveSettings">保存设置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="template-management">
        <div class="template-header">
          <h3>回复模板管理</h3>
          <el-button type="primary" @click="addTemplate">添加模板</el-button>
        </div>
        
        <el-table v-loading="loading" :data="templates" stripe style="width: 100%" border>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="keyword" label="关键词" width="150" />
          <el-table-column prop="response" label="回复内容" min-width="300" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="editTemplate(scope.row)">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-button type="danger" size="small" @click="deleteTemplate(scope.row.id)">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
    
    <!-- 添加/编辑模板对话框 -->
    <el-dialog
      v-model="templateDialogVisible"
      :title="templateDialogTitle"
      width="500px"
    >
      <el-form :model="currentTemplate" label-width="80px">
        <el-form-item label="关键词">
          <el-input v-model="currentTemplate.keyword" />
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input
            v-model="currentTemplate.response"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="templateDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveTemplate">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  createAdminAssistantTemplate,
  deleteAdminAssistantTemplate,
  getAdminAssistantSettings,
  getAdminAssistantTemplates,
  updateAdminAssistantSettings,
  updateAdminAssistantTemplate,
  type AssistantTemplate
} from '@/api/assistant'

// 助手设置
const assistantSettings = reactive({
  name: '商城智能助手',
  enabled: 1,
  responseMode: 'intelligent',
  timeout: 30
})

// 回复模板
const templates = ref<AssistantTemplate[]>([])
const loading = ref(false)

// 模板对话框
const templateDialogVisible = ref(false)
const templateDialogTitle = ref('添加模板')
const currentTemplate = reactive({
  id: 0,
  keyword: '',
  response: ''
})

const loadAssistantData = async () => {
  loading.value = true
  try {
    const [settingsRes, templatesRes] = await Promise.all([
      getAdminAssistantSettings(),
      getAdminAssistantTemplates()
    ])
    Object.assign(assistantSettings, settingsRes.data)
    templates.value = templatesRes.data || []
  } catch (error) {
    ElMessage.error('加载助手管理数据失败')
    console.error('加载助手管理数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 保存设置
const saveSettings = async () => {
  if (!assistantSettings.name.trim()) {
    ElMessage.warning('请输入助手名称')
    return
  }
  loading.value = true
  try {
    await updateAdminAssistantSettings({
      ...assistantSettings,
      name: assistantSettings.name.trim()
    })
    ElMessage.success('助手设置保存成功')
  } catch (error) {
    ElMessage.error('助手设置保存失败')
    console.error('助手设置保存失败:', error)
  } finally {
    loading.value = false
  }
}

// 添加模板
const addTemplate = () => {
  templateDialogTitle.value = '添加模板'
  currentTemplate.id = 0
  currentTemplate.keyword = ''
  currentTemplate.response = ''
  templateDialogVisible.value = true
}

// 编辑模板
const editTemplate = (row: AssistantTemplate) => {
  templateDialogTitle.value = '编辑模板'
  currentTemplate.id = row.id
  currentTemplate.keyword = row.keyword
  currentTemplate.response = row.response
  templateDialogVisible.value = true
}

// 保存模板
const saveTemplate = async () => {
  const keyword = currentTemplate.keyword.trim()
  const response = currentTemplate.response.trim()
  if (!keyword || !response) {
    ElMessage.warning('请完整填写关键词和回复内容')
    return
  }

  loading.value = true
  if (currentTemplate.id) {
    try {
      await updateAdminAssistantTemplate(currentTemplate.id, { keyword, response })
      ElMessage.success('模板编辑成功')
      await loadAssistantData()
    } catch (error) {
      ElMessage.error('模板编辑失败')
      console.error('模板编辑失败:', error)
    } finally {
      loading.value = false
    }
  } else {
    try {
      await createAdminAssistantTemplate({ keyword, response })
      ElMessage.success('模板添加成功')
      await loadAssistantData()
    } catch (error) {
      ElMessage.error('模板添加失败')
      console.error('模板添加失败:', error)
    } finally {
      loading.value = false
    }
  }
  templateDialogVisible.value = false
}

// 删除模板
const deleteTemplate = async (id: number) => {
  loading.value = true
  try {
    await deleteAdminAssistantTemplate(id)
    ElMessage.success('模板删除成功')
    await loadAssistantData()
  } catch (error) {
    ElMessage.error('模板删除失败')
    console.error('模板删除失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAssistantData()
})
</script>

<style scoped lang="scss">
.admin-assistant {
  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 700;
    color: #333;
  }
  
  .content-card {
    .assistant-settings {
      margin-bottom: 30px;
      padding-bottom: 20px;
      border-bottom: 1px solid #e4e7ed;
      
      h3 {
        margin-bottom: 20px;
        color: #333;
      }
    }
    
    .template-management {
      .template-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        
        h3 {
          margin: 0;
          color: #333;
        }
      }
    }
  }
  
  .unit {
    margin-left: 10px;
    color: #909399;
  }
}
</style>