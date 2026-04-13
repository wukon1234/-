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
            <el-switch v-model="assistantSettings.enabled" />
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
        
        <el-table :data="templates" stripe style="width: 100%" border>
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
import { ref, reactive } from 'vue'
import { Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 助手设置
const assistantSettings = reactive({
  name: '商城智能助手',
  enabled: true,
  responseMode: 'intelligent',
  timeout: 30
})

// 回复模板
const templates = ref([
  { id: 1, keyword: '您好', response: '您好！欢迎来到我们的商城，有什么可以帮助您的吗？' },
  { id: 2, keyword: '订单', response: '您可以在"我的订单"页面查看您的订单状态。' },
  { id: 3, keyword: '退款', response: '关于退款问题，请联系客服处理，客服电话：400-123-4567。' }
])

// 模板对话框
const templateDialogVisible = ref(false)
const templateDialogTitle = ref('添加模板')
const currentTemplate = reactive({
  id: '',
  keyword: '',
  response: ''
})

// 保存设置
const saveSettings = () => {
  console.log('保存助手设置', assistantSettings)
  ElMessage.success('助手设置保存成功')
  // 这里可以添加实际的保存逻辑
  // 例如：assistantService.saveSettings(assistantSettings)
}

// 添加模板
const addTemplate = () => {
  templateDialogTitle.value = '添加模板'
  currentTemplate.id = ''
  currentTemplate.keyword = ''
  currentTemplate.response = ''
  templateDialogVisible.value = true
}

// 编辑模板
const editTemplate = (row: any) => {
  templateDialogTitle.value = '编辑模板'
  currentTemplate.id = row.id
  currentTemplate.keyword = row.keyword
  currentTemplate.response = row.response
  templateDialogVisible.value = true
}

// 保存模板
const saveTemplate = () => {
  console.log('保存模板', currentTemplate)
  if (currentTemplate.id) {
    // 编辑现有模板
    const index = templates.value.findIndex(t => t.id === currentTemplate.id)
    if (index !== -1) {
      templates.value[index] = { ...currentTemplate }
      ElMessage.success('模板编辑成功')
    }
  } else {
    // 添加新模板
    const newTemplate = {
      id: Date.now(),
      keyword: currentTemplate.keyword,
      response: currentTemplate.response
    }
    templates.value.push(newTemplate)
    ElMessage.success('模板添加成功')
  }
  templateDialogVisible.value = false
  // 这里可以添加实际的保存逻辑
  // 例如：assistantService.saveTemplate(currentTemplate)
}

// 删除模板
const deleteTemplate = (id: number) => {
  const index = templates.value.findIndex(t => t.id === id)
  if (index !== -1) {
    templates.value.splice(index, 1)
    ElMessage.success('模板删除成功')
  }
  // 这里可以添加实际的删除逻辑
  // 例如：assistantService.deleteTemplate(id)
}
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