<template>
  <div class="user-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <el-button v-if="adminStore.hasPermission('system:user:add')" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" clearable placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" clearable placeholder="请选择状态">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column type="selection" width="50" />
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="用户名" prop="username" width="150" />
        <el-table-column label="状态" prop="status" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" prop="roleNames" />
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <el-table-column label="最后登录" prop="loginDate" width="180" />
        <el-table-column fixed="right" label="操作" width="300">
          <template #default="{ row }">
            <el-button v-if="adminStore.hasPermission('system:user:edit')" link type="primary" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button v-if="adminStore.hasPermission('system:user:assignRole')" link type="warning" @click="handleAssignRole(row)">
              分配角色
            </el-button>
            <el-button v-if="adminStore.hasPermission('system:user:resetPwd')" link type="info" @click="handleResetPwd(row)">
              重置密码
            </el-button>
            <el-button v-if="adminStore.hasPermission('system:user:delete')" link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" placeholder="请输入密码" show-password type="password" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色">
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :loading="submitLoading" type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignRoleVisible" title="分配角色" width="400px">
      <el-form label-width="80px">
        <el-form-item label="用户名">
          <el-input :value="currentUser?.username" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="selectedRoles" multiple placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignRoleVisible = false">取消</el-button>
        <el-button :loading="submitLoading" type="primary" @click="submitAssignRole">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus} from '@element-plus/icons-vue'
import {useAdminStore} from '@/store/admin'
import {roleApi, userApi} from '@/api/admin'

const adminStore = useAdminStore()

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const assignRoleVisible = ref(false)
const formRef = ref(null)
const currentUser = ref(null)
const selectedRoles = ref([])
const isEdit = ref(false)

const tableData = ref([])
const roleList = ref([])

const searchForm = reactive({
  username: '',
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

const form = reactive({
  id: null,
  username: '',
  password: '',
  status: 1,
  roleIds: []
})

const validateUsername = (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请输入用户名'))
  }
  if (!/^[a-zA-Z][a-zA-Z0-9_]{5,19}$/.test(value)) {
    return callback(new Error('用户名6-20位，字母开头，支持字母数字下划线'))
  }
  callback()
}

const validatePassword = (rule, value, callback) => {
  if (isEdit.value) {
    return callback()
  }
  if (!value) {
    return callback(new Error('请输入密码'))
  }
  if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,20}$/.test(value)) {
    return callback(new Error('密码8-20位，须包含大小写字母和数字'))
  }
  callback()
}

const rules = {
  username: [{ validator: validateUsername, trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  roleIds: [{ required: true, message: '请至少选择一个角色', type: 'array' }]
}

const fetchRoleList = async () => {
  try {
    const res = await roleApi.listAll()
    if (res.success) {
      roleList.value = res.data
    }
  } catch (error) {
    console.error('获取角色列表失败', error)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    const res = await userApi.list(params)
    if (res.success) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.status = null
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  form.id = null
  form.username = ''
  form.password = ''
  form.status = 1
  form.roleIds = []
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  const res = await userApi.getById(row.id)
  if (res.success) {
    form.id = res.data.id
    form.username = res.data.username
    form.status = res.data.status
    form.roleIds = res.data.roleIds || []
    dialogVisible.value = true
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const data = { ...form }
        let res
        if (isEdit.value) {
          res = await userApi.update(data)
        } else {
          res = await userApi.add(data)
        }
        if (res.success) {
          ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
          dialogVisible.value = false
          fetchData()
        } else {
          ElMessage.error(res.message || '操作失败')
        }
      } catch (error) {
        ElMessage.error('操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await userApi.delete(row.id)
      if (res.success) {
        ElMessage.success('删除成功')
        fetchData()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleResetPwd = (row) => {
  ElMessageBox.confirm('确定要重置该用户的密码吗？重置后密码为：Admin@123456', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await userApi.resetPassword(row.id)
      if (res.success) {
        ElMessage.success('密码重置成功')
      } else {
        ElMessage.error(res.message || '重置失败')
      }
    } catch (error) {
      ElMessage.error('重置失败')
    }
  }).catch(() => {})
}

const handleAssignRole = async (row) => {
  currentUser.value = row
  const res = await userApi.getById(row.id)
  if (res.success) {
    selectedRoles.value = res.data.roleIds || []
    assignRoleVisible.value = true
  }
}

const submitAssignRole = async () => {
  submitLoading.value = true
  try {
    const res = await userApi.assignRoles(currentUser.value.id, selectedRoles.value)
    if (res.success) {
      ElMessage.success('分配角色成功')
      assignRoleVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '分配失败')
    }
  } catch (error) {
    ElMessage.error('分配失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchRoleList()
  fetchData()
})
</script>

<style scoped>
.user-manage {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}
</style>
