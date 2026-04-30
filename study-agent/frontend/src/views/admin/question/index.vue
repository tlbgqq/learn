<template>
  <div class="question-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>题目列表</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增题目
            </el-button>
            <el-button type="success" @click="handleImport">
              <el-icon><Upload /></el-icon>
              批量导入
            </el-button>
            <el-button type="warning" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出题目
            </el-button>
            <el-button type="info" @click="goToKnowledgePoint">
              <el-icon><Connection /></el-icon>
              知识点管理
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
        <el-form-item label="知识点">
          <el-tree-select
            v-model="searchForm.knowledgePointIds"
            :data="knowledgeTree"
            :props="{ value: 'id', label: 'label', children: 'children' }"
            check-strictly
            clearable
            multiple
            placeholder="请选择知识点"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="题目类型">
          <el-select v-model="searchForm.type" clearable placeholder="请选择类型">
            <el-option label="选择题" value="选择" />
            <el-option label="填空题" value="填空" />
            <el-option label="解答题" value="解答" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="searchForm.difficulty" clearable placeholder="请选择难度">
            <el-option v-for="i in 5" :key="i" :label="`${i}星`" :value="i" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" clearable placeholder="搜索题目内容" style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" :tree-props="{ children: 'children' }" default-expand-all row-key="id" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="层级" prop="parentId" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.parentId === 0" size="small" type="primary">主题目</el-tag>
            <el-tag v-else size="small" type="info">子题目</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="题目内容" min-width="200" prop="content">
          <template #default="{ row }">
            <el-tooltip :content="row.content" placement="top">
              <span>{{ row.content.length > 50 ? row.content.substring(0, 50) + '...' : row.content }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="类型" prop="type" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">{{ row.type }}题</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="学科" prop="subjectName" width="100" />
        <el-table-column label="知识点" min-width="150" prop="knowledgePointNames">
          <template #default="{ row }">
            <el-tooltip :content="row.knowledgePointNames" placement="top">
              <span>{{ row.knowledgePointNames?.length > 20 ? row.knowledgePointNames.substring(0, 20) + '...' : row.knowledgePointNames }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="难度" prop="difficulty" width="100">
          <template #default="{ row }">
            <el-rate v-model="row.difficulty" :max="5" disabled />
          </template>
        </el-table-column>
        <el-table-column label="出题频率" prop="frequency" width="100" />
        <el-table-column label="创建时间" prop="createTime" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="250">
          <template #default="{ row }">
            <el-button v-if="row.parentId === 0" link type="success" @click="handleAddSubQuestion(row)">
              <el-icon><Plus /></el-icon>
              添加子题
            </el-button>
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

      <el-button
        v-if="selectedIds.length > 0"
        style="margin-top: 10px"
        type="danger"
        @click="handleBatchDelete"
      >
        批量删除已选 ({{ selectedIds.length }})
      </el-button>
    </el-card>

    <el-dialog v-model="importDialogVisible" title="批量导入题目" width="600px">
      <el-alert
        :closable="false"
        style="margin-bottom: 20px"
        title="导入说明"
        type="info"
      >
        <template #default>
          <p>1. 请先下载导入模板，按照模板格式填写题目数据</p>
          <p>2. 支持的题目类型：选择、填空、解答</p>
          <p>3. 选择题的选项请用换行分隔，格式如：A. xxx</p>
          <p>4. 上传后将预览数据，确认无误后再导入</p>
        </template>
      </el-alert>

      <div style="margin-bottom: 20px">
        <el-button type="primary" @click="downloadTemplate">
          <el-icon><Download /></el-icon>
          下载导入模板
        </el-button>
      </div>

      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
        accept=".xlsx,.xls"
      >
        <el-button type="primary">
          <el-icon><Upload /></el-icon>
          选择文件
        </el-button>
        <template #tip>
          <div class="el-upload__tip">只能上传 .xlsx/.xls 文件</div>
        </template>
      </el-upload>

      <div v-if="uploadFile" style="margin-top: 10px">
        <el-tag>{{ uploadFile.name }}</el-tag>
      </div>

      <el-progress v-if="uploading" :percentage="uploadPercent" style="margin-top: 20px" />

      <el-alert
        v-if="importResult && importResult.errors?.length > 0"
        style="margin-top: 20px"
        title="导入错误"
        type="error"
      >
        <template #default>
          <ul>
            <li v-for="(error, index) in importResult.errors" :key="index">{{ error }}</li>
          </ul>
        </template>
      </el-alert>

      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button :disabled="!uploadFile" :loading="uploading" type="primary" @click="doImport">
          开始导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Connection, Download, Plus, Upload} from '@element-plus/icons-vue'
import {knowledgePointApi, questionApi} from '@/api/admin'

const router = useRouter()

const loading = ref(false)
const uploading = ref(false)
const uploadPercent = ref(0)
const importDialogVisible = ref(false)
const uploadFile = ref(null)
const importResult = ref(null)
const uploadRef = ref(null)

const tableData = ref([])
const subjectList = ref([])
const knowledgeTree = ref([])
const selectedIds = ref([])

const searchForm = reactive({
  subjectId: null,
  knowledgePointIds: [],
  type: null,
  difficulty: null,
  keyword: ''
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const getTypeTagType = (type) => {
  switch (type) {
    case '选择': return 'primary'
    case '填空': return 'success'
    case '解答': return 'warning'
    default: return 'info'
  }
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
    }
  } catch (error) {
    console.error('获取知识点树失败', error)
  }
}

const buildQuestionTree = async (questions) => {
  const parentQuestions = questions.filter(q => q.parentId === 0)
  const childQuestionsMap = new Map()

  for (const q of questions) {
    if (q.parentId > 0) {
      if (!childQuestionsMap.has(q.parentId)) {
        childQuestionsMap.set(q.parentId, [])
      }
      childQuestionsMap.get(q.parentId).push(q)
    }
  }

  const parentsWithChildren = []
  for (const pq of parentQuestions) {
    const pqWithChildren = { ...pq }
    if (childQuestionsMap.has(pq.id)) {
      pqWithChildren.children = childQuestionsMap.get(pq.id)
    }
    parentsWithChildren.push(pqWithChildren)
  }

  const orphanQuestions = questions.filter(q => 
    q.parentId > 0 && !questions.some(pq => pq.id === q.parentId)
  )
  for (const oq of orphanQuestions) {
    oq.children = []
    parentsWithChildren.push(oq)
  }

  return parentsWithChildren
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
    if (searchForm.knowledgePointIds?.length > 0) {
      params.knowledgePointIds = searchForm.knowledgePointIds.join(',')
    }
    if (searchForm.type) {
      params.type = searchForm.type
    }
    if (searchForm.difficulty) {
      params.difficulty = searchForm.difficulty
    }
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    const res = await questionApi.list(params)
    if (res.success) {
      tableData.value = await buildQuestionTree(res.data.records)
      pagination.total = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取题目列表失败')
  } finally {
    loading.value = false
  }
}

const handleSubjectChange = (val) => {
  searchForm.knowledgePointIds = []
  fetchKnowledgeTree(val)
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.subjectId = null
  searchForm.knowledgePointIds = []
  searchForm.type = null
  searchForm.difficulty = null
  searchForm.keyword = ''
  knowledgeTree.value = []
  handleSearch()
}

const handleAdd = () => {
  router.push('/admin/question/add')
}

const handleAddSubQuestion = (row) => {
  router.push({
    path: '/admin/question/add',
    query: { parentId: row.id, subjectId: row.subjectId }
  })
}

const handleEdit = (row) => {
  router.push(`/admin/question/edit/${row.id}`)
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该题目吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await questionApi.delete(row.id)
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

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 道题目吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await questionApi.batchDelete(selectedIds.value)
      if (res.success) {
        ElMessage.success('批量删除成功')
        fetchData()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleImport = () => {
  importDialogVisible.value = true
  uploadFile.value = null
  importResult.value = null
}

const downloadTemplate = async () => {
  try {
    const res = await questionApi.getTemplate()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '题目导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch (error) {
    ElMessage.error('下载模板失败')
  }
}

const handleFileChange = (file) => {
  uploadFile.value = file.raw
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

const doImport = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  uploading.value = true
  uploadPercent.value = 0
  importResult.value = null

  try {
    const formData = new FormData()
    formData.append('file', uploadFile.value)

    const res = await questionApi.import(formData, (progressEvent) => {
      uploadPercent.value = Math.round((progressEvent.loaded * 100) / progressEvent.total)
    })

    if (res.success) {
      importResult.value = res.data
      if (res.data.errors?.length > 0) {
        ElMessage.warning(`导入完成，成功 ${res.data.successCount} 条，失败 ${res.data.errors.length} 条`)
      } else {
        ElMessage.success(`导入成功，共 ${res.data.successCount} 条`)
        importDialogVisible.value = false
        fetchData()
      }
    } else {
      ElMessage.error(res.message || '导入失败')
    }
  } catch (error) {
    ElMessage.error('导入失败')
  } finally {
    uploading.value = false
  }
}

const handleExport = async () => {
  try {
    const params = {}
    if (searchForm.subjectId) {
      params.subjectId = searchForm.subjectId
    }
    if (searchForm.knowledgePointIds?.length > 0) {
      params.knowledgePointIds = searchForm.knowledgePointIds.join(',')
    }
    if (searchForm.type) {
      params.type = searchForm.type
    }
    if (searchForm.difficulty) {
      params.difficulty = searchForm.difficulty
    }
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }

    const res = await questionApi.export(params)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `题目列表_${new Date().toLocaleDateString('zh-CN').replace(/\//g, '-')}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const goToKnowledgePoint = () => {
  router.push('/admin/knowledge-point')
}

onMounted(() => {
  fetchSubjects()
  fetchData()
})
</script>

<style scoped>
.question-manage {
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
</style>
