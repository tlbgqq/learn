<template>
  <div class="question-form">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑题目' : (form.parentId > 0 ? '新增子题目' : '新增题目') }}</span>
          <div>
            <el-button @click="handleBack">返回</el-button>
          </div>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        style="max-width: 800px"
      >
        <el-form-item label="所属父题目" prop="parentId">
          <el-select
            v-model="form.parentId"
            :disabled="isEdit && currentParentId > 0"
            clearable
            placeholder="请选择父题目（不选则为主题目）"
            style="width: 500px"
          >
            <el-option :value="0" label="无（作为主题目）" />
            <el-option
              v-for="parent in parentQuestionList"
              :key="parent.id"
              :label="`[${parent.id}] ${parent.content.length > 30 ? parent.content.substring(0, 30) + '...' : parent.content}`"
              :value="parent.id"
            />
          </el-select>
          <div class="form-tip">选择父题目后，此题将作为子题目存在</div>
        </el-form-item>

        <el-form-item label="题目类型" prop="type">
          <el-radio-group v-model="form.type" @change="handleTypeChange">
            <el-radio value="选择">选择题</el-radio>
            <el-radio value="填空">填空题</el-radio>
            <el-radio value="解答">解答题</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="题目内容" prop="content">
          <el-input
            v-model="form.content"
            :maxlength="5000"
            :rows="4"
            placeholder="请输入题目内容"
            show-word-limit
            type="textarea"
          />
        </el-form-item>

        <el-form-item
          v-if="form.type === '选择'"
          label="选项"
          prop="options"
        >
          <el-input
            v-model="form.options"
            :rows="4"
            placeholder="请输入选项，每行一个&#10;例如：&#10;A. 选项A&#10;B. 选项B&#10;C. 选项C&#10;D. 选项D"
            type="textarea"
          />
          <div class="form-tip">每行一个选项，格式如：A. xxx</div>
        </el-form-item>

        <el-form-item label="正确答案" prop="answer">
          <el-input
            v-model="form.answer"
            :rows="2"
            placeholder="请输入正确答案（选择题为选项字母）"
            type="textarea"
          />
          <div v-if="form.type === '选择'" class="form-tip">选择题请输入选项字母，如：A、B</div>
        </el-form-item>

        <el-form-item label="题目解析" prop="analysis">
          <el-input
            v-model="form.analysis"
            :maxlength="3000"
            :rows="3"
            placeholder="请输入题目解析（选填）"
            show-word-limit
            type="textarea"
          />
        </el-form-item>

        <el-form-item label="所属学科" prop="subjectId">
          <el-select
            v-model="form.subjectId"
            placeholder="请选择学科"
            style="width: 300px"
            @change="handleSubjectChange"
          >
            <el-option
              v-for="subject in subjectList"
              :key="subject.id"
              :label="subject.name"
              :value="subject.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="所属年级" prop="gradeId">
          <el-select
            v-model="form.gradeId"
            placeholder="请选择年级"
            style="width: 300px"
          >
            <el-option
              v-for="grade in gradeList"
              :key="grade.id"
              :label="grade.name"
              :value="grade.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="关联知识点" prop="knowledgePointIds">
          <el-tree-select
            v-model="selectedKnowledgePoints"
            :data="knowledgeTree"
            :props="{ value: 'id', label: 'label', children: 'children' }"
            check-strictly
            multiple
            placeholder="请选择知识点"
            show-checkbox
            style="width: 400px"
          />
          <div class="form-tip">必选至少一个知识点</div>
        </el-form-item>

        <el-form-item label="难度等级" prop="difficulty">
          <el-rate v-model="form.difficulty" :max="5" :texts="['1星','2星','3星','4星','5星']" show-text />
        </el-form-item>

        <el-form-item>
          <el-button :loading="loading" type="primary" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '确认录入' }}
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {gradeApi, knowledgePointApi, questionApi} from '@/api/admin'

const route = useRoute()
const router = useRouter()

const formRef = ref(null)
const loading = ref(false)
const subjectList = ref([])
const gradeList = ref([])
const knowledgeTree = ref([])
const selectedKnowledgePoints = ref([])
const parentQuestionList = ref([])
const currentParentId = ref(0)

const isEdit = computed(() => route.path.includes('/edit/'))
const questionId = computed(() => {
  if (isEdit.value) {
    return route.params.id ? parseInt(route.params.id) : null
  }
  return null
})

const form = reactive({
  id: null,
  type: '选择',
  content: '',
  options: '',
  answer: '',
  analysis: '',
  subjectId: null,
  gradeId: null,
  knowledgePointIds: '',
  difficulty: 3,
  parentId: 0
})

const rules = {
  type: [{ required: true, message: '请选择题目类型', trigger: 'change' }],
  content: [
    { required: true, message: '请输入题目内容', trigger: 'blur' },
    { max: 5000, message: '题目内容最多5000字符', trigger: 'blur' }
  ],
  answer: [{ required: true, message: '请输入正确答案', trigger: 'blur' }],
  subjectId: [{ required: true, message: '请选择学科', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度等级', trigger: 'change' }]
}

const validateKnowledgePoints = (rule, value, callback) => {
  if (selectedKnowledgePoints.value.length === 0) {
    callback(new Error('请至少选择一个知识点'))
  } else {
    callback()
  }
}

watch(() => form.type, (newVal) => {
  if (newVal !== '选择') {
    form.options = ''
  }
})

watch(selectedKnowledgePoints, (newVal) => {
  if (newVal && newVal.length > 0) {
    form.knowledgePointIds = newVal.join(',')
  } else {
    form.knowledgePointIds = ''
  }
}, { deep: true })

const handleTypeChange = (val) => {
  if (val !== '选择') {
    form.options = ''
  }
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

const fetchGrades = async () => {
  try {
    const res = await gradeApi.list()
    if (res) {
      gradeList.value = res.data
    }
  } catch (error) {
    console.error('获取年级列表失败', error)
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

const handleSubjectChange = (val) => {
  selectedKnowledgePoints.value = []
  form.knowledgePointIds = ''
  fetchKnowledgeTree(val)
  fetchParentQuestions(val)
}

const fetchParentQuestions = async (subjectId, excludeId = null) => {
  try {
    const params = {}
    if (subjectId && subjectId > 0) {
      params.subjectId = subjectId
    }
    if (excludeId && excludeId > 0) {
      params.excludeId = excludeId
    }
    const res = await questionApi.getParents(params)
    if (res.success) {
      parentQuestionList.value = res.data
    }
  } catch (error) {
    console.error('获取父题目列表失败', error)
  }
}

const fetchQuestionDetail = async () => {
  if (!questionId.value) return

  try {
    const res = await questionApi.getById(questionId.value)
    if (res.success) {
      const data = res.data
      form.id = data.id
      form.type = data.type || '选择'
      form.content = data.content || ''
      form.options = data.options || ''
      form.answer = data.answer || ''
      form.analysis = data.analysis || ''
      form.subjectId = data.subjectId
      form.gradeId = data.gradeId
      form.difficulty = data.difficulty || 3
      form.parentId = data.parentId || 0
      currentParentId.value = data.parentId || 0

      if (data.knowledgePointIds) {
        const kpIds = data.knowledgePointIds.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id))
        selectedKnowledgePoints.value = kpIds
      }

      if (data.subjectId) {
        fetchKnowledgeTree(data.subjectId)
        fetchParentQuestions(data.subjectId, data.id)
      }
    } else {
      ElMessage.error(res.message || '获取题目详情失败')
    }
  } catch (error) {
    ElMessage.error('获取题目详情失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (selectedKnowledgePoints.value.length === 0) {
    ElMessage.warning('请至少选择一个知识点')
    return
  }

  if (form.type === '选择' && !form.options) {
    ElMessage.warning('选择题请输入选项')
    return
  }

  loading.value = true
  try {
    const data = {
      ...form,
      knowledgePointIds: selectedKnowledgePoints.value.join(',')
    }

    let res
    if (isEdit.value) {
      res = await questionApi.update(data)
    } else {
      res = await questionApi.add(data)
    }

    if (res.success) {
      ElMessage.success(isEdit.value ? '保存成功' : '录入成功')
      router.push('/admin/question')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  if (isEdit.value) {
    fetchQuestionDetail()
  } else {
    form.type = '选择'
    form.content = ''
    form.options = ''
    form.answer = ''
    form.analysis = ''
    form.difficulty = 3
    selectedKnowledgePoints.value = []
    form.knowledgePointIds = ''
    
    const queryParentId = route.query.parentId ? parseInt(route.query.parentId) : 0
    const querySubjectId = route.query.subjectId ? parseInt(route.query.subjectId) : null
    form.parentId = queryParentId || 0
    form.subjectId = querySubjectId
  }
}

const handleBack = () => {
  router.push('/admin/question')
}

onMounted(() => {
  fetchSubjects()
  fetchGrades()
  
  const queryParentId = route.query.parentId ? parseInt(route.query.parentId) : 0
  const querySubjectId = route.query.subjectId ? parseInt(route.query.subjectId) : null
  
  if (isEdit.value) {
    fetchQuestionDetail()
  } else {
    if (queryParentId > 0) {
      form.parentId = queryParentId
    }
    if (querySubjectId > 0) {
      form.subjectId = querySubjectId
      fetchKnowledgeTree(querySubjectId)
      fetchParentQuestions(querySubjectId)
    }
  }
})
</script>

<style scoped>
.question-form {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
