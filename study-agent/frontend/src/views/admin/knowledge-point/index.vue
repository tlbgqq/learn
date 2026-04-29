<template>
  <div class="knowledge-point-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>知识点管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增知识点
            </el-button>
            <el-button @click="goToQuestion">
              <el-icon><Document /></el-icon>
              返回题库
            </el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="学科">
          <el-select v-model="searchForm.subjectId" clearable placeholder="请选择学科" @change="handleSubjectChange">
            <el-option
              v-for="subject in subjectList"
              :key="subject.id"
              :label="subject.name"
              :value="subject.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" clearable placeholder="搜索知识点名称/编码" style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="hover" style="height: 600px; overflow: auto">
            <template #header>
              <span>知识点树形结构</span>
            </template>
            <el-tree
              ref="treeRef"
              :data="knowledgeTree"
              :props="{ label: 'label', children: 'children' }"
              default-expand-all
              highlight-current
              node-key="id"
              @node-click="handleNodeClick"
            >
              <template #default="{ node, data }">
                <div class="tree-node">
                  <span>{{ node.label }}</span>
                  <span class="tree-node-code">{{ data.code }}</span>
                </div>
              </template>
            </el-tree>
          </el-card>
        </el-col>

        <el-col :span="16">
          <el-card shadow="hover" style="height: 600px; display: flex; flex-direction: column">
            <template #header>
              <span>知识点列表</span>
            </template>

            <el-table
              v-loading="loading"
              :data="tableData"
              stripe
              style="flex: 1; overflow: auto"
            >
              <el-table-column label="ID" prop="id" width="80" />
              <el-table-column label="知识点名称" min-width="150" prop="name">
                <template #default="{ row }">
                  <span v-if="row.parentName" style="color: #909399; margin-right: 5px">
                    {{ row.parentName }} →
                  </span>
                  <span>{{ row.name }}</span>
                </template>
              </el-table-column>
              <el-table-column label="编码" prop="code" width="150" />
              <el-table-column label="学科" prop="subjectName" width="100" />
              <el-table-column label="难度" prop="difficulty" width="100">
                <template #default="{ row }">
                  <el-rate v-model="row.difficulty" :max="5" disabled />
                </template>
              </el-table-column>
              <el-table-column label="排序" prop="sortOrder" width="80" />
              <el-table-column label="创建时间" prop="createTime" width="180">
                <template #default="{ row }">
                  {{ formatTime(row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column fixed="right" label="操作" width="150">
                <template #default="{ row }">
                  <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
                  <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
        </el-col>
      </el-row>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="知识点名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入知识点名称" />
        </el-form-item>

        <el-form-item label="知识点编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入知识点编码" />
        </el-form-item>

        <el-form-item label="所属学科" prop="subjectId">
          <el-select
            v-model="form.subjectId"
            placeholder="请选择学科"
            style="width: 100%"
            @change="handleFormSubjectChange"
          >
            <el-option
              v-for="subject in subjectList"
              :key="subject.id"
              :label="subject.name"
              :value="subject.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="父知识点" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="formKnowledgeTree"
            :props="{ value: 'id', label: 'label', children: 'children' }"
            clearable
            placeholder="请选择父知识点（不选则为顶级）"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="难度等级" prop="difficulty">
          <el-rate v-model="form.difficulty" :max="5" :texts="['1星','2星','3星','4星','5星']" show-text />
        </el-form-item>

        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :max="1000" :min="0" />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            :rows="3"
            placeholder="请输入知识点描述"
            type="textarea"
          />
        </el-form-item>

        <el-form-item label="常见错误" prop="commonErrors">
          <el-input
            v-model="form.commonErrors"
            :rows="2"
            placeholder="请输入常见错误类型"
            type="textarea"
          />
        </el-form-item>

        <el-form-item label="关联概念" prop="relatedConcepts">
          <el-input
            v-model="form.relatedConcepts"
            :rows="2"
            placeholder="请输入关联知识点"
            type="textarea"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :loading="submitLoading" type="primary" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Document, Plus} from '@element-plus/icons-vue'
import {knowledgePointApi} from '@/api/admin'

const router = useRouter()

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const treeRef = ref(null)

const subjectList = ref([])
const knowledgeTree = ref([])
const formKnowledgeTree = ref([])
const tableData = ref([])

const searchForm = reactive({
  subjectId: null,
  keyword: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const isEdit = ref(false)

const dialogTitle = computed(() => isEdit.value ? '编辑知识点' : '新增知识点')

const form = reactive({
  id: null,
  name: '',
  code: '',
  subjectId: null,
  parentId: null,
  difficulty: 1,
  sortOrder: 0,
  description: '',
  commonErrors: '',
  relatedConcepts: ''
})

const rules = {
  name: [{ required: true, message: '请输入知识点名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入知识点编码', trigger: 'blur' }],
  subjectId: [{ required: true, message: '请选择学科', trigger: 'change' }]
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const fetchSubjects = async () => {
  try {
    const res = await knowledgePointApi.getSubjects()
    if (res.success) {
      subjectList.value = res.data
    }
  } catch (error) {
    console.error('获取学科列表失败', error)
  }
}

const fetchKnowledgeTree = async (subjectId) => {
  try {
    const params = {}
    if (subjectId) {
      params.subjectId = subjectId
    }
    const res = await knowledgePointApi.getTree(params)
    if (res.success) {
      knowledgeTree.value = res.data
      formKnowledgeTree.value = JSON.parse(JSON.stringify(res.data))
    }
  } catch (error) {
    console.error('获取知识点树失败', error)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }
    if (searchForm.subjectId) {
      params.subjectId = searchForm.subjectId
    }
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    const res = await knowledgePointApi.list(params)
    if (res.success) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取知识点列表失败')
  } finally {
    loading.value = false
  }
}

const handleSubjectChange = (val) => {
  fetchKnowledgeTree(val)
  handleSearch()
}

const handleFormSubjectChange = (val) => {
  form.parentId = null
  const params = {}
  if (val) {
    params.subjectId = val
  }
  knowledgePointApi.getTree(params).then(res => {
    if (res.success) {
      formKnowledgeTree.value = res.data
    }
  }).catch(error => {
    console.error('获取知识点树失败', error)
  })
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.subjectId = null
  searchForm.keyword = ''
  knowledgeTree.value = []
  fetchKnowledgeTree()
  handleSearch()
}

const handleNodeClick = (data) => {
  if (data && data.id) {
    searchForm.keyword = ''
    fetchData()
  }
}

const handleAdd = () => {
  isEdit.value = false
  form.id = null
  form.name = ''
  form.code = ''
  form.subjectId = null
  form.parentId = null
  form.difficulty = 1
  form.sortOrder = 0
  form.description = ''
  form.commonErrors = ''
  form.relatedConcepts = ''
  formKnowledgeTree.value = JSON.parse(JSON.stringify(knowledgeTree.value))
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  try {
    const res = await knowledgePointApi.getById(row.id)
    if (res.success) {
      const data = res.data
      form.id = data.id
      form.name = data.name
      form.code = data.code
      form.subjectId = data.subjectId
      form.parentId = data.parentId === 0 ? null : data.parentId
      form.difficulty = data.difficulty || 1
      form.sortOrder = data.sortOrder || 0
      form.description = data.description || ''
      form.commonErrors = data.commonErrors || ''
      form.relatedConcepts = data.relatedConcepts || ''

      const params = {}
      if (data.subjectId) {
        params.subjectId = data.subjectId
      }
      const treeRes = await knowledgePointApi.getTree(params)
      if (treeRes.success) {
        formKnowledgeTree.value = treeRes.data
      }

      dialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取知识点详情失败')
    }
  } catch (error) {
    ElMessage.error('获取知识点详情失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该知识点吗？\n注意：有子知识点的无法删除。', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await knowledgePointApi.delete(row.id)
      if (res.success) {
        ElMessage.success('删除成功')
        fetchData()
        fetchKnowledgeTree(searchForm.subjectId)
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = {
      ...form,
      parentId: form.parentId || 0
    }

    let res
    if (isEdit.value) {
      res = await knowledgePointApi.update(data)
    } else {
      res = await knowledgePointApi.add(data)
    }

    if (res.success) {
      ElMessage.success(isEdit.value ? '保存成功' : '新增成功')
      dialogVisible.value = false
      fetchData()
      fetchKnowledgeTree(searchForm.subjectId)
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const goToQuestion = () => {
  router.push('/admin/question')
}

onMounted(() => {
  fetchSubjects()
  fetchKnowledgeTree()
  fetchData()
})
</script>

<style scoped>
.knowledge-point-manage {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-form {
  margin-bottom: 20px;
}

.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.tree-node-code {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}
</style>
