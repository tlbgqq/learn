<template>
  <div class="question-batch-form">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>批量录入题目（父题目+子题目）</span>
          <div>
            <el-button @click="handleBack">返回</el-button>
          </div>
        </div>
      </template>

      <el-divider content-position="left">
        <span class="divider-title">父题目</span>
      </el-divider>

      <el-form
        ref="parentFormRef"
        :model="parentForm"
        :rules="rules"
        label-width="120px"
        style="max-width: 800px; margin-bottom: 30px;"
      >
        <el-form-item label="题目类型" prop="type">
          <el-radio-group v-model="parentForm.type" @change="handleParentTypeChange">
            <el-radio value="选择">选择题</el-radio>
            <el-radio value="填空">填空题</el-radio>
            <el-radio value="解答">解答题</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="题目内容" prop="content">
          <el-input
            v-model="parentForm.content"
            :maxlength="5000"
            :rows="4"
            placeholder="请输入题目内容"
            show-word-limit
            type="textarea"
          />
        </el-form-item>

        <el-form-item
          v-if="parentForm.type === '选择'"
          label="选项"
          prop="options"
        >
          <el-input
            v-model="parentForm.options"
            :rows="4"
            placeholder="请输入选项，每行一个&#10;例如：&#10;A. 选项A&#10;B. 选项B&#10;C. 选项C&#10;D. 选项D"
            type="textarea"
          />
          <div class="form-tip">每行一个选项，格式如：A. xxx</div>
        </el-form-item>

        <el-form-item label="正确答案" prop="answer">
          <el-input
            v-model="parentForm.answer"
            :rows="2"
            placeholder="请输入正确答案（选择题为选项字母）"
            type="textarea"
          />
          <div v-if="parentForm.type === '选择'" class="form-tip">选择题请输入选项字母，如：A、B</div>
        </el-form-item>

        <el-form-item label="题目解析" prop="analysis">
          <el-input
            v-model="parentForm.analysis"
            :maxlength="3000"
            :rows="3"
            placeholder="请输入题目解析（选填）"
            show-word-limit
            type="textarea"
          />
        </el-form-item>

        <el-form-item label="所属学科" prop="subjectId">
          <el-select
            v-model="parentForm.subjectId"
            placeholder="请选择学科"
            style="width: 300px"
            @change="handleParentSubjectChange"
          >
            <el-option
              v-for="subject in subjectList"
              :key="subject.id"
              :label="subject.name"
              :value="subject.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="关联知识点" prop="knowledgePointIds">
          <el-tree-select
            v-model="parentSelectedKnowledgePoints"
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
          <el-rate v-model="parentForm.difficulty" :max="5" :texts="['1星','2星','3星','4星','5星']" show-text />
        </el-form-item>
      </el-form>

      <el-divider content-position="left">
        <span class="divider-title">子题目</span>
        <el-button size="small" style="margin-left: 10px;" type="primary" @click="addChildQuestion">
          <el-icon><Plus /></el-icon> 添加子题目
        </el-button>
        <el-button size="small" style="margin-left: 5px;" @click="inheritFromParent">
          <el-icon><CopyDocument /></el-icon> 继承父题目配置到所有子题目
        </el-button>
      </el-divider>

      <div v-if="childForms.length === 0" class="empty-tip">
        <el-empty description="暂无子题目，请点击上方按钮添加" />
      </div>

      <div v-for="(childForm, index) in childForms" :key="index" class="child-question-card">
        <el-card :shadow="'hover'">
          <template #header>
            <div class="child-card-header">
              <span class="child-title">子题目 {{ index + 1 }}</span>
              <div>
                <el-button size="small" type="primary" @click="inheritSingleFromParent(index)">
                  <el-icon><CopyDocument /></el-icon> 继承父题目
                </el-button>
                <el-button size="small" type="danger" @click="removeChildQuestion(index)">
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </div>
            </div>
          </template>

          <el-form
            :ref="el => setChildFormRef(el, index)"
            :model="childForm"
            :rules="rules"
            label-width="100px"
            style="max-width: 750px;"
          >
            <el-form-item label="题目类型" prop="type">
              <el-radio-group v-model="childForm.type" @change="handleChildTypeChange(index)">
                <el-radio value="选择">选择题</el-radio>
                <el-radio value="填空">填空题</el-radio>
                <el-radio value="解答">解答题</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="题目内容" prop="content">
              <el-input
                v-model="childForm.content"
                :maxlength="5000"
                :rows="3"
                placeholder="请输入题目内容"
                show-word-limit
                type="textarea"
              />
            </el-form-item>

            <el-form-item
              v-if="childForm.type === '选择'"
              label="选项"
              prop="options"
            >
              <el-input
                v-model="childForm.options"
                :rows="3"
                placeholder="请输入选项，每行一个"
                type="textarea"
              />
            </el-form-item>

            <el-form-item label="正确答案" prop="answer">
              <el-input
                v-model="childForm.answer"
                :rows="2"
                placeholder="请输入正确答案"
                type="textarea"
              />
            </el-form-item>

            <el-form-item label="题目解析" prop="analysis">
              <el-input
                v-model="childForm.analysis"
                :maxlength="3000"
                :rows="2"
                placeholder="请输入题目解析（选填）"
                show-word-limit
                type="textarea"
              />
            </el-form-item>

            <el-form-item label="所属学科" prop="subjectId">
              <el-select
                v-model="childForm.subjectId"
                placeholder="请选择学科"
                style="width: 250px"
                @change="handleChildSubjectChange(index)"
              >
                <el-option
                  v-for="subject in subjectList"
                  :key="subject.id"
                  :label="subject.name"
                  :value="subject.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="关联知识点" prop="knowledgePointIds">
              <el-tree-select
                v-model="childForm.selectedKnowledgePoints"
                :data="childForm.knowledgeTree || knowledgeTree"
                :props="{ value: 'id', label: 'label', children: 'children' }"
                check-strictly
                multiple
                placeholder="请选择知识点"
                show-checkbox
                style="width: 350px"
              />
            </el-form-item>

            <el-form-item label="难度等级" prop="difficulty">
              <el-rate v-model="childForm.difficulty" :max="5" :texts="['1星','2星','3星','4星','5星']" show-text />
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <el-divider />

      <div class="action-buttons">
        <el-button :loading="loading" size="large" type="primary" @click="handleSubmit">
          确认批量录入
        </el-button>
        <el-button size="large" @click="handleReset">重置</el-button>
        <el-button size="large" @click="handleBack">返回</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref, watch} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {CopyDocument, Delete, Plus} from '@element-plus/icons-vue'
import {knowledgePointApi, questionApi} from '@/api/admin'

const router = useRouter()

const parentFormRef = ref(null)
const childFormRefs = ref([])
const loading = ref(false)
const subjectList = ref([])
const knowledgeTree = ref([])
const parentSelectedKnowledgePoints = ref([])

const setChildFormRef = (el, index) => {
  if (el) {
    childFormRefs.value[index] = el
  }
}

const parentForm = reactive({
  type: '选择',
  content: '',
  options: '',
  answer: '',
  analysis: '',
  subjectId: null,
  knowledgePointIds: '',
  difficulty: 3
})

const childForms = ref([])

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

const createEmptyChildForm = () => ({
  type: '选择',
  content: '',
  options: '',
  answer: '',
  analysis: '',
  subjectId: parentForm.subjectId,
  knowledgePointIds: '',
  selectedKnowledgePoints: [],
  difficulty: parentForm.difficulty,
  knowledgeTree: knowledgeTree.value
})

const addChildQuestion = () => {
  childForms.value.push(createEmptyChildForm())
}

const removeChildQuestion = (index) => {
  childForms.value.splice(index, 1)
  childFormRefs.value.splice(index, 1)
}

const inheritFromParent = () => {
  if (!parentForm.subjectId) {
    ElMessage.warning('请先选择父题目的学科')
    return
  }
  childForms.value.forEach((child, index) => {
    inheritSingleFromParent(index)
  })
  ElMessage.success('已将父题目配置继承到所有子题目')
}

const inheritSingleFromParent = (index) => {
  if (!parentForm.subjectId) {
    ElMessage.warning('请先选择父题目的学科')
    return
  }
  const child = childForms.value[index]
  child.subjectId = parentForm.subjectId
  child.difficulty = parentForm.difficulty
  child.selectedKnowledgePoints = [...parentSelectedKnowledgePoints.value]
  child.knowledgePointIds = parentForm.knowledgePointIds
  child.knowledgeTree = knowledgeTree.value
}

watch(() => parentForm.type, (newVal) => {
  if (newVal !== '选择') {
    parentForm.options = ''
  }
})

watch(parentSelectedKnowledgePoints, (newVal) => {
  if (newVal && newVal.length > 0) {
    parentForm.knowledgePointIds = newVal.join(',')
  } else {
    parentForm.knowledgePointIds = ''
  }
}, { deep: true })

const handleParentTypeChange = (val) => {
  if (val !== '选择') {
    parentForm.options = ''
  }
}

const handleChildTypeChange = (index) => {
  if (childForms.value[index].type !== '选择') {
    childForms.value[index].options = ''
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

const handleParentSubjectChange = (val) => {
  parentSelectedKnowledgePoints.value = []
  parentForm.knowledgePointIds = ''
  fetchKnowledgeTree(val)
}

const handleChildSubjectChange = async (index) => {
  const child = childForms.value[index]
  child.selectedKnowledgePoints = []
  child.knowledgePointIds = ''
  
  if (child.subjectId) {
    try {
      const res = await knowledgePointApi.getTree({ subjectId: child.subjectId })
      if (res.success) {
        child.knowledgeTree = res.data
      }
    } catch (error) {
      console.error('获取知识点树失败', error)
    }
  } else {
    child.knowledgeTree = knowledgeTree.value
  }
}

const validateAllForms = async () => {
  let valid = true
  
  if (parentFormRef.value) {
    const parentValid = await parentFormRef.value.validate().catch(() => false)
    if (!parentValid) valid = false
  }
  
  if (parentSelectedKnowledgePoints.value.length === 0) {
    ElMessage.warning('父题目请至少选择一个知识点')
    valid = false
  }
  
  if (parentForm.type === '选择' && !parentForm.options) {
    ElMessage.warning('父题目（选择题）请输入选项')
    valid = false
  }
  
  for (let i = 0; i < childForms.value.length; i++) {
    const childForm = childForms.value[i]
    const childRef = childFormRefs.value[i]
    
    if (childRef) {
      const childValid = await childRef.validate().catch(() => false)
      if (!childValid) {
        ElMessage.warning(`子题目 ${i + 1} 验证失败`)
        valid = false
      }
    }
    
    if (!childForm.selectedKnowledgePoints || childForm.selectedKnowledgePoints.length === 0) {
      ElMessage.warning(`子题目 ${i + 1} 请至少选择一个知识点`)
      valid = false
    }
    
    if (childForm.type === '选择' && !childForm.options) {
      ElMessage.warning(`子题目 ${i + 1}（选择题）请输入选项`)
      valid = false
    }
  }
  
  return valid
}

const handleSubmit = async () => {
  const valid = await validateAllForms()
  if (!valid) return

  loading.value = true
  try {
    const data = {
      parentQuestion: {
        type: parentForm.type,
        content: parentForm.content,
        options: parentForm.options,
        answer: parentForm.answer,
        analysis: parentForm.analysis,
        subjectId: parentForm.subjectId,
        knowledgePointIds: parentSelectedKnowledgePoints.value.join(','),
        difficulty: parentForm.difficulty
      },
      childQuestions: childForms.value.map(child => ({
        type: child.type,
        content: child.content,
        options: child.options,
        answer: child.answer,
        analysis: child.analysis,
        subjectId: child.subjectId,
        knowledgePointIds: child.selectedKnowledgePoints ? child.selectedKnowledgePoints.join(',') : '',
        difficulty: child.difficulty
      }))
    }

    const res = await questionApi.batchCreate(data)

    if (res.success) {
      ElMessage.success(`批量录入成功！共创建 ${res.data.totalCreated} 道题目（父题目ID: ${res.data.parentQuestionId}）`)
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
  parentForm.type = '选择'
  parentForm.content = ''
  parentForm.options = ''
  parentForm.answer = ''
  parentForm.analysis = ''
  parentForm.difficulty = 3
  parentForm.subjectId = null
  parentSelectedKnowledgePoints.value = []
  parentForm.knowledgePointIds = ''
  childForms.value = []
  childFormRefs.value = []
}

const handleBack = () => {
  router.push('/admin/question')
}

onMounted(() => {
  fetchSubjects()
})
</script>

<style scoped>
.question-batch-form {
  height: 100%;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.divider-title {
  font-size: 16px;
  font-weight: bold;
  color: #409eff;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.child-question-card {
  margin-bottom: 20px;
}

.child-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.child-title {
  font-size: 14px;
  font-weight: bold;
  color: #606266;
}

.empty-tip {
  padding: 40px 0;
}

.action-buttons {
  text-align: center;
  padding: 20px 0;
}

.action-buttons .el-button {
  margin: 0 10px;
}
</style>
