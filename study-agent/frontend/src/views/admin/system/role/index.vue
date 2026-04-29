<template>
  <div class="role-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色列表</span>
          <el-button v-if="adminStore.hasPermission('system:role:add')" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增角色
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="角色名称">
          <el-input v-model="searchForm.name" clearable placeholder="请输入角色名称" />
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
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="角色编码" prop="code" width="200" />
        <el-table-column label="角色名称" prop="name" width="150" />
        <el-table-column label="排序" prop="sort" width="80" />
        <el-table-column label="状态" prop="status" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="描述" prop="description" />
        <el-table-column label="用户数" prop="userCount" width="80" />
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <el-table-column fixed="right" label="操作" width="250">
          <template #default="{ row }">
            <el-button v-if="adminStore.hasPermission('system:role:edit')" link type="primary" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button v-if="adminStore.hasPermission('system:role:assignMenu')" link type="warning" @click="handleAssignMenu(row)">
              分配菜单
            </el-button>
            <el-button v-if="adminStore.hasPermission('system:role:delete')" link type="danger" @click="handleDelete(row)">
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
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" :disabled="isEdit" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" :rows="3" placeholder="请输入描述" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :loading="submitLoading" type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignMenuVisible" title="分配菜单权限" width="600px">
      <el-tree
        ref="treeRef"
        :data="menuTree"
        :default-checked-keys="checkedMenus"
        :default-expand-all="true"
        :props="treeProps"
        node-key="id"
        show-checkbox
      />
      <template #footer>
        <el-button @click="assignMenuVisible = false">取消</el-button>
        <el-button :loading="submitLoading" type="primary" @click="submitAssignMenu">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus} from '@element-plus/icons-vue'
import {useAdminStore} from '@/store/admin'
import {menuApi, roleApi} from '@/api/admin'

const adminStore = useAdminStore()

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const assignMenuVisible = ref(false)
const formRef = ref(null)
const treeRef = ref(null)
const isEdit = ref(false)
const currentRole = ref(null)

const tableData = ref([])
const menuTree = ref([])
const checkedMenus = ref([])

const searchForm = reactive({
  name: '',
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const dialogTitle = computed(() => isEdit.value ? '编辑角色' : '新增角色')

const form = reactive({
  id: null,
  code: '',
  name: '',
  sort: 0,
  status: 1,
  description: ''
})

const treeProps = {
  label: 'name',
  children: 'children'
}

const validateCode = (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请输入角色编码'))
  }
  callback()
}

const rules = {
  code: [{ validator: validateCode, trigger: 'blur' }],
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const fetchMenuTree = async () => {
  try {
    const res = await menuApi.getTree()
    if (res.success) {
      menuTree.value = res.data
    }
  } catch (error) {
    console.error('获取菜单树失败', error)
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
    const res = await roleApi.list(params)
    if (res.success) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.status = null
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  form.id = null
  form.code = ''
  form.name = ''
  form.sort = 0
  form.status = 1
  form.description = ''
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  const res = await roleApi.getById(row.id)
  if (res.success) {
    form.id = res.data.id
    form.code = res.data.code
    form.name = res.data.name
    form.sort = res.data.sort
    form.status = res.data.status
    form.description = res.data.description
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
          res = await roleApi.update(data)
        } else {
          res = await roleApi.add(data)
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
  ElMessageBox.confirm('确定要删除该角色吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await roleApi.delete(row.id)
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

const handleAssignMenu = async (row) => {
  currentRole.value = row
  const res = await roleApi.getMenus(row.id)
  if (res.success) {
    checkedMenus.value = res.data || []
    assignMenuVisible.value = true
  }
}

const submitAssignMenu = async () => {
  if (!treeRef.value) return
  
  const checkedKeys = treeRef.value.getCheckedKeys()
  const halfCheckedKeys = treeRef.value.getHalfCheckedKeys()
  const allKeys = [...checkedKeys, ...halfCheckedKeys]
  
  submitLoading.value = true
  try {
    const res = await roleApi.assignMenus(currentRole.value.id, allKeys)
    if (res.success) {
      ElMessage.success('分配菜单成功')
      assignMenuVisible.value = false
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
  fetchMenuTree()
  fetchData()
})
</script>

<style scoped>
.role-manage {
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
