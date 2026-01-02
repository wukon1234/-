<template>
  <div class="address-page">
    <div class="header">
      <h2>收货地址</h2>
      <el-button type="primary" @click="openCreate">新增地址</el-button>
    </div>

    <el-card v-if="list.length > 0" class="list-card">
      <div v-for="addr in list" :key="addr.id" class="addr-item">
        <div class="addr-main">
          <div class="row1">
            <span class="name">{{ addr.receiverName }}</span>
            <span class="phone">{{ addr.receiverPhone }}</span>
            <el-tag v-if="addr.isDefault === 1" type="success" size="small">默认</el-tag>
          </div>
          <div class="row2">{{ addr.fullAddress || formatFull(addr) }}</div>
        </div>

        <div class="addr-actions">
          <el-button v-if="addr.isDefault !== 1" @click="handleSetDefault(addr.id)">设为默认</el-button>
          <el-button type="primary" @click="openEdit(addr)">编辑</el-button>
          <el-button type="danger" @click="handleDelete(addr.id)">删除</el-button>
        </div>
      </div>
    </el-card>

    <el-empty v-else description="暂无地址，快去新增一个吧~" />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="form.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="form.province" placeholder="如：广东省" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="form.city" placeholder="如：深圳市" />
        </el-form-item>
        <el-form-item label="区/县" prop="district">
          <el-input v-model="form.district" placeholder="如：南山区" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="form.detail" placeholder="如：xx路xx号xx室" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="defaultChecked">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getAddressList,
  createAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress,
  type Address,
  type SaveAddressRequest
} from '@/api/address'

const list = ref<Address[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref<FormInstance>()

const form = ref<SaveAddressRequest>({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detail: ''
})
const defaultChecked = ref(false)

const rules: FormRules = {
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  receiverPhone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区/县', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const dialogTitle = computed(() => (editingId.value ? '编辑地址' : '新增地址'))

const load = async () => {
  const res = await getAddressList()
  list.value = res.data || []
}

const resetForm = () => {
  form.value = {
    receiverName: '',
    receiverPhone: '',
    province: '',
    city: '',
    district: '',
    detail: ''
  }
  defaultChecked.value = false
  editingId.value = null
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (addr: Address) => {
  editingId.value = addr.id
  form.value = {
    receiverName: addr.receiverName,
    receiverPhone: addr.receiverPhone,
    province: addr.province,
    city: addr.city,
    district: addr.district,
    detail: addr.detail
  }
  defaultChecked.value = addr.isDefault === 1
  dialogVisible.value = true
}

const handleSave = async () => {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload: SaveAddressRequest = {
      ...form.value,
      isDefault: defaultChecked.value ? 1 : 0
    }
    if (editingId.value) {
      await updateAddress(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createAddress(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAddress(id)
    ElMessage.success('删除成功')
    await load()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

const handleSetDefault = async (id: number) => {
  await setDefaultAddress(id)
  ElMessage.success('已设为默认')
  await load()
}

const formatFull = (a: Address) => `${a.province}${a.city}${a.district}${a.detail}`

onMounted(() => {
  load()
})
</script>

<style scoped lang="scss">
.address-page {
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h2 {
      margin: 0;
      color: #333;
    }
  }
}

.list-card {
  .addr-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .addr-main {
      .row1 {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 6px;

        .name {
          font-weight: 600;
          color: #333;
        }
        .phone {
          color: #666;
        }
      }
      .row2 {
        color: #888;
      }
    }

    .addr-actions {
      display: flex;
      gap: 10px;
    }
  }
}
</style>


