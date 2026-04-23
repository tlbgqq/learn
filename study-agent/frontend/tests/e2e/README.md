# E2E Tests - Sprint Mode

## Prerequisites

1. **Backend must be running** on `http://localhost:8080`
2. **Frontend must be running** on `http://localhost:3000` (or use the webServer config in playwright.config.js)

## Setup

```bash
cd frontend

# Install Playwright
npm install -D @playwright/test

# Install Chromium browser
npx playwright install chromium

# Install other browsers (optional)
npx playwright install --with-deps
```

## Running Tests

### Run all E2E tests
```bash
cd frontend
npx playwright test
```

### Run with UI (headed mode)
```bash
npx playwright test --headed
```

### Run specific test file
```bash
npx playwright test tests/e2e/sprint.spec.js
```

### Run with debug
```bash
npx playwright test --debug
```

### Run and show report
```bash
npx playwright test
npx playwright show-report
```

## Test Coverage

### TC-001: 新用户注册并登录，进入首页
- Register a new account through API
- Login via UI
- Verify redirect to home page
- Verify nickname displayed

### TC-002: 从首页进入冲刺页面
- Navigate to home
- Click on sprint mode card
- Verify URL changes to /student/sprint
- Verify sprint start page elements

### TC-003: 开始冲刺 - API 触发 + UI 验证答题界面
- Start sprint via backend API (bypasses 60s timer)
- Set sprint_session in localStorage
- Verify playing UI renders

### TC-004: 完整冲刺流程 - API 答题 + UI 结算页面验证
- Start sprint via API
- Answer 5 questions via API
- Finish sprint via API
- Navigate to sprint page
- Verify result UI

### TC-005: 冲刺完成后金币和经验值增长
- Record gold/exp before sprint
- Complete sprint
- Verify gold/exp increased (or at least tracked)

### TC-006: 冲刺中的错题被记录到错题本
- Start sprint
- Submit wrong answers
- Finish sprint
- Verify wrong answers API returns records

### TC-007: 选择英语科目开始冲刺
- Verify default English subject selected
- Start sprint with subjectId=3
- Verify session created with correct subject

### TC-008: 冲刺页面加载历史最佳记录
- Complete a sprint
- Navigate to sprint page
- Verify best records section loads

### TC-009: 提前结束冲刺，验证结算页面
- Start sprint
- Answer 2 questions
- Finish early (USER_END)
- Verify result returned

### TC-010: 未登录用户访问冲刺页面重定向到登录页
- Clear auth state
- Navigate to /student/sprint
- Verify redirect to /login

### TC-011: 冲刺页面 UI 组件完整性
- Navigate to sprint page
- Verify all UI elements present

### TC-012: 结算页面正确展示金币、经验、钻石奖励
- Complete sprint
- Verify rewards structure in API response

## Architecture

```
tests/e2e/
├── playwright.config.js   # Playwright configuration
├── helpers.js              # Test utilities (API calls, auth)
└── sprint.spec.js          # Sprint mode E2E tests
```

## Test Strategy

- **Hybrid approach**: Use direct API calls to drive backend state, use UI for verification
- **Why**: The 60-second timer in sprint mode makes pure UI testing impractical
- **API-first**: Register, login, start sprint, answer questions, finish sprint via API
- **UI verification**: Verify pages render correctly, elements are visible, navigation works

## Reports

- HTML report: `test-results/html/index.html`
- Screenshots: `test-results/screenshots/` (on failure)
- Videos: `test-results/videos/` (on failure)
- Traces: `test-results/trace.zip` (on first retry)

## Cleanup

Test accounts are created with unique timestamps. To clean up old test data:
```sql
DELETE FROM student WHERE username LIKE 'e2e_test_%';
```
