# MVVM 架構與資料流 (Architecture)

## 架構概述

本專案採用 **MVVM (Model-View-ViewModel)** 架構模式，搭配 **Repository Pattern** 實現單一資料來源（Single Source of Truth, SSOT）原則，確保程式碼的可測試性、可維護性與職責分離。

![MVVM 行為交互圖](../picture/MVVM%20行為交互圖%20(Sequence%20Diagram).png)

---

## 各層職責說明

### 1. View Layer (Composable UI)

**職責：**
- 純粹的 UI 渲染層，不包含任何業務邏輯
- 訂閱 ViewModel 的 `StateFlow<UiState>`，根據狀態變化更新畫面
- 將使用者操作（如點擊按鈕）轉換為 ViewModel 的 Event

**範例：**
```kotlin
@Composable
fun TemplateListScreen(viewModel: TemplateViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn {
        items(uiState.templates) { template ->
            TemplateCard(
                template = template,
                onDelete = { viewModel.deleteTemplate(template) }
            )
        }
    }
}
```

---

### 2. ViewModel Layer

**職責：**
- 持有 UI 狀態（`StateFlow<TemplateUiState>`）
- 處理 UI 事件（如儲存、刪除、同步）
- 呼叫 Repository 執行資料操作，並將結果轉換為 UI 狀態
- 管理 Coroutine 生命週期（透過 `viewModelScope`）

**核心實作：**
```kotlin
class TemplateViewModel(private val repository: TemplateRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TemplateUiState())
    val uiState: StateFlow<TemplateUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            repository.allTemplates.collect { list ->
                _uiState.update { it.copy(templates = list) }
            }
        }
    }
    
    fun saveTemplate(template: ServiceTemplate) {
        viewModelScope.launch {
            repository.insertTemplate(template)
        }
    }
    
    fun syncWithCloud() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.uploadTemplates(_uiState.value.templates)
                repository.downloadTemplates()
                _uiState.update { it.copy(connectionStatus = "Synced") }
            } catch (e: Exception) {
                _uiState.update { it.copy(connectionStatus = "Error: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
```

---

### 3. Data Layer (Repository)

**職責：**
- 作為單一資料來源（SSOT），統一管理本地與雲端資料
- 決定資料來源優先順序（本地優先策略）
- 封裝 Room DAO 與 Supabase API 呼叫
- 處理網路狀態檢查與錯誤處理

**核心邏輯：**
```kotlin
class TemplateRepository(
    private val templateDao: TemplateDao,
    private val supabaseClient: SupabaseClient,
    private val context: Context
) {
    val allTemplates: Flow<List<ServiceTemplate>> = templateDao.getAllTemplates()
    
    suspend fun insertTemplate(template: ServiceTemplate) {
        // 1. 本地優先：先寫入 Room
        templateDao.insertTemplate(template)
        
        // 2. 雲端同步：網路可用時上傳
        if (NetworkUtils.isNetworkAvailable(context)) {
            try {
                supabaseClient.postgrest["service_templates"].upsert(template)
            } catch (e: Exception) {
                Log.e(TAG, "Cloud sync failed: ${e.message}")
                // 不拋出異常，本地儲存已成功
            }
        }
    }
    
    suspend fun downloadTemplates(): List<ServiceTemplate> {
        val result = supabaseClient.postgrest["service_templates"]
            .select()
            .decodeList<ServiceTemplate>()
        
        // 全量覆蓋策略
        templateDao.deleteAll()
        templateDao.insertAll(result)
        return result
    }
}
```
