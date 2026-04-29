<template>
  <div class="menu-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>菜单列表</span>
          <el-button v-if="adminStore.hasPermission('system:menu:add')" type="primary" @click="handleAdd(0)">
            <el-icon><Plus /></el-icon>
            新增菜单
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        :tree-props="{ children: 'children' }"
        border
        default-expand-all
        row-key="id"
      >
        <el-table-column label="菜单名称" min-width="180" prop="name">
          <template #default="{ row }">
            <el-icon v-if="row.icon" style="margin-right: 5px"><component :is="row.icon" /></el-icon>
            <span>{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" prop="type" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeName(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="路由路径" min-width="150" prop="path" />
        <el-table-column label="权限标识" min-width="180" prop="permission" />
        <el-table-column label="排序" prop="sort" width="80" />
        <el-table-column label="显示状态" prop="isShow" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isShow === 1 ? 'success' : 'info'">
              {{ row.isShow === 1 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="启用状态" prop="isEnable" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isEnable === 1 ? 'success' : 'danger'">
              {{ row.isEnable === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <el-table-column fixed="right" label="操作" width="250">
          <template #default="{ row }">
            <el-button v-if="adminStore.hasPermission('system:menu:add') && row.type !== 3" link type="primary" @click="handleAdd(row.id)">
              新增子级
            </el-button>
            <el-button v-if="adminStore.hasPermission('system:menu:edit')" link type="warning" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button v-if="adminStore.hasPermission('system:menu:delete')" link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :clearable="true"
            :data="menuTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            check-strictly
            placeholder="请选择上级菜单"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">目录</el-radio>
            <el-radio :value="2">菜单</el-radio>
            <el-radio :value="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.type !== 3" label="路由路径" prop="path">
          <el-input v-model="form.path" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item v-if="form.type !== 3" label="菜单图标" prop="icon">
          <el-input v-model="form.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item v-if="form.type === 3" label="权限标识" prop="permission">
          <el-input v-model="form.permission" placeholder="如: system:user:list" />
        </el-form-item>
        <el-form-item label="显示状态" prop="isShow">
          <el-radio-group v-model="form.isShow">
            <el-radio :value="1">显示</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="启用状态" prop="isEnable">
          <el-radio-group v-model="form.isEnable">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :loading="submitLoading" type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus} from '@element-plus/icons-vue'
import {useAdminStore} from '@/store/admin'
import {menuApi} from '@/api/admin'

const adminStore = useAdminStore()

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const tableData = ref([])
const menuTree = ref([])

const dialogTitle = computed(() => isEdit.value ? '编辑菜单' : '新增菜单')

const form = reactive({
  id: null,
  parentId: 0,
  name: '',
  type: 2,
  path: '',
  icon: '',
  sort: 0,
  permission: '',
  isShow: 1,
  isEnable: 1
})

const rules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }]
}

const getTypeName = (type) => {
  const map = { 1: '目录', 2: '菜单', 3: '按钮' }
  return map[type] || '未知'
}

const getTypeTagType = (type) => {
  const map = { 1: 'primary', 2: 'success', 3: 'warning' }
  return map[type] || 'info'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await menuApi.getTree()
    if (res.success) {
      tableData.value = res.data
      menuTree.value = [
        { id: 0, name: '顶级菜单', children: res.data }
      ]
    }
  } catch (error) {
    ElMessage.error('获取菜单列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = (parentId) => {
  isEdit.value = false
  form.id = null
  form.parentId = parentId
  form.name = ''
  form.type = parentId === 0 ? 1 : 2
  form.path = ''
  form.icon = ''
  form.sort = 0
  form.permission = ''
  form.isShow = 1
  form.isEnable = 1
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.parentId = row.parentId
  form.name = row.name
  form.type = row.type
  form.path = row.path
  form.icon = row.icon
  form.sort = row.sort
  form.permission = row.permission
  form.isShow = row.isShow
  form.isEnable = row.isEnable
  dialogVisible.value = true
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
          res = await menuApi.update(data)
        } else {
          res = await menuApi.add(data)
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
  ElMessageBox.confirm('确定要删除该菜单吗？删除后会同时删除所有子菜单', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await menuApi.delete(row.id)
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

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.menu-manage {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
