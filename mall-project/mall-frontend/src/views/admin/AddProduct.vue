<template>
  <div class="admin-add-product">
    <div class="page-header">
      <h2 class="page-title">添加商品</h2>
      <el-button @click="$router.push('/admin/products')">
        <el-icon><ArrowLeft /></el-icon> 返回列表
      </el-button>
    </div>
    
    <el-card shadow="hover" class="content-card">
      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="productRules"
        label-width="120px"
        class="product-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="productForm.name" placeholder="请输入商品名称" />
            </el-form-item>
            
            <el-form-item label="商品分类" prop="categoryId">
              <el-select v-model="productForm.categoryId" placeholder="请选择商品分类">
                <el-option label="手机" value="1" />
                <el-option label="电脑" value="2" />
                <el-option label="服装" value="3" />
                <el-option label="家居" value="4" />
                <el-option label="电子产品" value="5" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="商品价格" prop="price">
              <el-input v-model="productForm.price" placeholder="请输入商品价格" type="number" />
            </el-form-item>
            
            <el-form-item label="商品库存" prop="stock">
              <el-input v-model="productForm.stock" placeholder="请输入商品库存" type="number" />
            </el-form-item>
            
            <el-form-item label="商品状态" prop="status">
              <el-switch v-model="productForm.status" active-value="1" inactive-value="0" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="商品图片">
              <el-upload
                class="upload-demo"
                action="#"
                :on-preview="handlePreview"
                :on-remove="handleRemove"
                :file-list="fileList"
                multiple
                :limit="3"
                :on-exceed="handleExceed"
                :before-upload="beforeUpload"
                :auto-upload="false"
              >
                <el-button type="primary">
                  <el-icon><Upload /></el-icon> 点击上传
                </el-button>
                <template #tip>
                  <div class="el-upload__tip">只能上传jpg/png文件，且不超过500kb</div>
                </template>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="productForm.description"
            type="textarea"
            :rows="5"
            placeholder="请输入商品描述"
          />
          <el-button
            type="primary"
            :icon="ChatLineRound"
            @click="handleAIGenerate"
            style="margin-top: 10px"
          >
            AI自动生成商品描述
          </el-button>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">提交</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Upload, ChatLineRound } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, UploadFile } from 'element-plus'
import { generateProductContent, type AIProductRequest, type AIProductResponse } from '@/api/product'

const router = useRouter()
const productFormRef = ref<FormInstance>()
const loading = ref(false)

// 商品表单
const productForm = reactive({
  name: '',
  categoryId: '',
  price: 0,
  stock: 0,
  status: 1,
  description: '',
  imageUrl: ''
})

// 商品表单规则
const productRules = reactive({
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 100, message: '商品名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (typeof value === 'number' && value > 0) {
          callback()
        } else {
          callback(new Error('商品价格必须大于0'))
        }
      },
      trigger: 'blur'
    }
  ],
  stock: [
    { required: true, message: '请输入商品库存', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (typeof value === 'number' && value >= 0) {
          callback()
        } else {
          callback(new Error('商品库存必须大于等于0'))
        }
      },
      trigger: 'blur'
    }
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && value.length >= 10) {
          callback()
        } else {
          callback(new Error('商品描述长度不能少于10个字符'))
        }
      },
      trigger: 'blur'
    }
  ]
})

// 文件列表
const fileList = ref<UploadFile[]>([])

// 处理文件上传前
const beforeUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt500K = file.size / 1024 < 500

  if (!isJPG) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片')
  }
  if (!isLt500K) {
    ElMessage.error('图片大小不能超过 500KB')
  }
  return isJPG && isLt500K
}

// 处理文件超出数量限制
const handleExceed = (files: File[], fileList: UploadFile[]) => {
  ElMessage.error(`只能上传 ${3} 个文件`)
}

// 处理文件移除
const handleRemove = (file: UploadFile, fileList: UploadFile[]) => {
  console.log('文件移除', file, fileList)
}

// 处理文件预览
const handlePreview = (file: UploadFile) => {
  console.log('文件预览', file)
}

// 提交表单
const handleSubmit = async () => {
  if (!productFormRef.value) return
  
  await productFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 这里可以添加提交逻辑
        setTimeout(() => {
          ElMessage.success('商品添加成功')
          loading.value = false
          router.push('/admin/products')
        }, 1000)
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '商品添加失败')
        loading.value = false
      }
    }
  })
}

// 重置表单
const handleReset = () => {
  if (productFormRef.value) {
    productFormRef.value.resetFields()
    fileList.value = []
  }
}

// AI生成商品描述
const handleAIGenerate = async () => {
  try {
    // 检查商品名称是否已填写
    if (!productForm.name) {
      ElMessage.warning('请先填写商品名称')
      return
    }

    // 显示加载状态
    loading.value = true

    // 准备生成参数，使用表单中已有的信息
    const generateParams = {
      productName: productForm.name,
      keywords: getCategoryName(productForm.categoryId) || '高品质,热销',
      targetAudience: '所有用户'
    }

    // 调用API生成商品文案
    const response = await generateProductContent(generateParams)

    // 回填到表单
    const data = response.data
    if (data.title) productForm.name = data.title
    if (data.description) productForm.description = data.description

    // 显示成功消息
    ElMessage.success('商品文案生成成功')

    // 可以选择是否显示生成的所有内容
    ElMessageBox.alert(
      `
        <div style="padding: 20px;">
          <h3 style="margin-bottom: 15px;">生成的商品文案</h3>
          <div style="margin-bottom: 15px;">
            <strong>商品标题：</strong>
            <p>${data.title || '无'}</p>
          </div>
          <div style="margin-bottom: 15px;">
            <strong>商品简介：</strong>
            <p>${data.summary || '无'}</p>
          </div>
          <div style="margin-bottom: 15px;">
            <strong>商品详细描述：</strong>
            <p>${data.description || '无'}</p>
          </div>
          <div style="margin-bottom: 15px;">
            <strong>营销卖点：</strong>
            <p>${data.sellingPoints || '无'}</p>
          </div>
        </div>
      `,
      '生成结果',
      {
        confirmButtonText: '确定',
        dangerouslyUseHTMLString: true
      }
    )
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('生成商品文案失败，请稍后重试')
      console.error('生成商品文案失败:', error)
    }
  } finally {
    loading.value = false
  }
}

// 根据分类ID获取分类名称
const getCategoryName = (categoryId: string | number) => {
  const categoryMap: Record<string | number, string> = {
    1: '手机,数码,科技',
    2: '电脑,办公,科技',
    3: '服装,时尚,潮流',
    4: '家居,生活,舒适',
    5: '电子产品,数码,科技'
  }
  return categoryMap[categoryId] || ''
}
</script>

<style scoped lang="scss">
.admin-add-product {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .page-title {
      margin: 0;
      font-size: 24px;
      font-weight: 700;
      color: #333;
    }
  }
  
  .content-card {
    
    .product-form {
      margin-top: 20px;
      
      :deep(.el-form-item) {
        margin-bottom: 20px;
      }
    }
  }
}
</style>