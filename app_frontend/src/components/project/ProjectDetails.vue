<template>
  <div class="d-flex project-details">
    <!-- Боковая панель с фильтрами -->
    <aside class="sidebar shadow-sm">
      <h5 class="sidebar-title">Фильтры задач</h5>
      <button @click="filterTasks('all')" class="btn btn-light mb-2 filter-btn">Все задачи</button>
      <button @click="filterTasks('unassigned')" class="btn btn-light mb-2 filter-btn">Без исполнителя</button>
      <button @click="filterTasks('assigned')" class="btn btn-light mb-2 filter-btn">Назначенные</button>
      <button @click="filterTasks('completed')" class="btn btn-light mb-2 filter-btn">Завершенные</button>
    </aside>

    <!-- Основное содержимое -->
    <div class="container main-content">
      <div class="project-header" v-if="project.title">
        <h1 class="project-title">{{ project.title }}</h1>
        <p class="project-description">{{ project.descrption || 'Описание отсутствует' }}</p>
        <div class="project-meta">
          <p><strong>Создан:</strong> {{ formatDate(project.createdAt) }}</p>
          <p><strong>Обновлен:</strong> {{ formatDate(project.updatedAt) }}</p>
        </div>
        <div class="project-actions">
          <a :href="project.urlToProject" target="_blank" class="btn btn-info action-btn">Перейти к проекту</a>
          <button
              class="btn btn-outline-secondary action-btn"
              @click="toggleFollow(project.projectId)"
              :class="{ 'followed': project.isFollowed }"
          >
            {{ project.isFollowed ? 'Отписаться' : 'Подписаться' }}
          </button>
          <button v-if="isManager" class="btn btn-primary action-btn" @click="showAddTaskModal">Добавить задание</button>
        </div>
      </div>
      <div v-else class="text-center">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Загрузка...</span>
        </div>
      </div>

      <div class="tasks-section mt-5" v-if="project.tasks">
        <h5 class="section-title">Задачи проекта</h5>
        <div class="task-list">
          <TaskItem
              v-for="task in filteredTasks"
              :key="task.taskId"
              :task="task"
              @taskUpdated="fetchProject(project.projectId)"
          />
        </div>
      </div>
    </div>

    <!-- Боковая панель Git -->
    <aside class="sidebar-right shadow-sm" v-if="project.title">
      <h5 class="sidebar-title">Текущая ветка: {{ currentBranch }}</h5>
      <p v-if="!isRepoCreated" class="text-warning">Репозиторий не создан</p>
      <button @click="createRepository" class="btn btn-light mb-2 git-btn" :disabled="isRepoCreated">Создать репозиторий</button>
      <button @click="showBranchesModal" class="btn btn-light mb-2 git-btn" :disabled="!isRepoCreated">Показать ветки</button>
      <button @click="showAddFileModal" class="btn btn-light mb-2 git-btn" :disabled="!isRepoCreated">Добавить файл</button>
      <button @click="showCommitModal" class="btn btn-light mb-2 git-btn" :disabled="!isRepoCreated">Коммит</button>
      <button @click="showMergeModal" class="btn btn-light mb-2 git-btn" :disabled="!isRepoCreated">Слить ветку</button>
      <button @click="showCheckoutModal" class="btn btn-light mb-2 git-btn" :disabled="!isRepoCreated">Переключиться на ветку</button>
      <button @click="showStatusModal" class="btn btn-light mb-2 git-btn" :disabled="!isRepoCreated">Показать статус</button>
      <button @click="showLogModal" class="btn btn-light mb-2 git-btn" :disabled="!isRepoCreated">Показать логи</button>
      <button @click="showInfoModal" class="btn btn-light mb-2 git-btn" :disabled="!isRepoCreated">Информация</button>
      <button @click="showCreateBranchModal" class="btn btn-light mb-2 git-btn" :disabled="!isRepoCreated">Создать ветку</button>
      <button @click="showDownloadModal" class="btn btn-light mb-2 git-btn" :disabled="!isRepoCreated">Выгрузить файлы</button>
    </aside>

    <!-- Модальное окно для добавления задания -->
    <div class="modal fade" id="addTaskModal" tabindex="-1" aria-labelledby="addTaskModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="addTaskModalLabel">Добавить задание</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="createTask">
              <div class="mb-3">
                <label for="taskTitle" class="form-label">Название задания</label>
                <input
                    type="text"
                    class="form-control"
                    id="taskTitle"
                    v-model="newTask.titleTask"
                    required
                    placeholder="Введите название задания"
                />
              </div>
              <div class="mb-3">
                <label for="taskDescription" class="form-label">Описание задания</label>
                <textarea
                    class="form-control"
                    id="taskDescription"
                    v-model="newTask.descriptionTask"
                    required
                    placeholder="Введите описание задания"
                    rows="4"
                ></textarea>
              </div>
              <button type="submit" class="btn btn-primary w-100">Создать</button>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- Модальное окно для добавления файла -->
    <div class="modal fade" id="addFileModal" tabindex="-1" aria-labelledby="addFileModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="addFileModalLabel">Добавить файл</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="addFile">
              <div class="mb-3">
                <label for="fileInput" class="form-label">Выберите файлы</label>
                <input
                    type="file"
                    class="form-control"
                    id="fileInput"
                    multiple
                    @change="handleFileUpload"
                />
              </div>
              <button type="submit" class="btn btn-primary w-100">Добавить</button>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- Модальное окно для коммита -->
    <div class="modal fade" id="commitModal" tabindex="-1" aria-labelledby="commitModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="commitModalLabel">Создать коммит</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="commitChanges">
              <div class="mb-3">
                <label for="commitMessage" class="form-label">Сообщение коммита</label>
                <input
                    type="text"
                    class="form-control"
                    id="commitMessage"
                    v-model="commitMessage"
                    required
                    placeholder="Введите сообщение коммита"
                />
              </div>
              <div class="mb-3">
                <label for="commitBranch" class="form-label">Ветка</label>
                <select
                    class="form-control"
                    id="commitBranch"
                    v-model="commitBranch"
                    required
                >
                  <option v-for="branch in branches" :key="branch" :value="branch">{{ branch }}</option>
                </select>
              </div>
              <div class="mb-3">
                <label for="commitFiles" class="form-label">Выберите файлы</label>
                <input
                    type="file"
                    class="form-control"
                    id="commitFiles"
                    multiple
                    @change="handleFileUpload"
                />
              </div>
              <button type="submit" class="btn btn-primary w-100">Коммит</button>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- Модальное окно для создания ветки -->
    <div class="modal fade" id="createBranchModal" tabindex="-1" aria-labelledby="createBranchModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="createBranchModalLabel">Создать ветку</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="createBranch">
              <div class="mb-3">
                <label for="branchName" class="form-label">Имя ветки</label>
                <input
                    type="text"
                    class="form-control"
                    id="branchName"
                    v-model="branchName"
                    required
                    placeholder="Введите имя ветки"
                />
              </div>
              <button type="submit" class="btn btn-primary w-100">Создать</button>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- Модальное окно для слияния веток -->
    <div class="modal fade" id="mergeModal" tabindex="-1" aria-labelledby="mergeModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="mergeModalLabel">Слить ветку</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="mergeBranches">
              <div class="mb-3">
                <label for="mergeBranch" class="form-label">Ветка для слияния</label>
                <select
                    class="form-control"
                    id="mergeBranch"
                    v-model="branchToMerge"
                    required
                >
                  <option v-for="branch in branches" :key="branch" :value="branch">{{ branch }}</option>
                </select>
              </div>
              <button type="submit" class="btn btn-primary w-100">Слить</button>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- Модальное окно для переключения ветки -->
    <div class="modal fade" id="checkoutModal" tabindex="-1" aria-labelledby="checkoutModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="checkoutModalLabel">Переключиться на ветку</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="checkoutBranch">
              <div class="mb-3">
                <label for="checkoutBranch" class="form-label">世界 ветку</label>
                <select
                    class="form-control"
                    id="checkoutBranch"
                    v-model="branchToCheckout"
                    required
                >
                  <option v-for="branch in branches" :key="branch" :value="branch">{{ branch }}</option>
                </select>
              </div>
              <button type="submit" class="btn btn-primary w-100">Переключиться</button>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- Модальное окно для показа веток -->
    <div class="modal fade" id="branchesModal" tabindex="-1" aria-labelledby="branchesModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="branchesModalLabel">Список веток</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <ul class="list-group">
              <li class="list-group-item" v-for="branch in branches" :key="branch">{{ branch }}</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- Модальное окно для статуса -->
    <div class="modal fade" id="statusModal" tabindex="-1" aria-labelledby="statusModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="statusModalLabel">Статус репозитория</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <pre>{{ statusOutput }}</pre>
          </div>
        </div>
      </div>
    </div>

    <!-- Модальное окно для логов -->
    <div class="modal fade" id="logModal" tabindex="-1" aria-labelledby="logModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="logModalLabel">Логи репозитория</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <pre>{{ logOutput }}</pre>
          </div>
        </div>
      </div>
    </div>

    <!-- Модальное окно для информации -->
    <div class="modal fade" id="infoModal" tabindex="-1" aria-labelledby="infoModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="infoModalLabel">Информация о репозитории</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <p><strong>Название:</strong> {{ repoInfo.name }}</p>
            <p><strong>Путь:</strong> {{ repoInfo.path }}</p>
            <p><strong>Существует:</strong> {{ repoInfo.exists ? 'Да' : 'Нет' }}</p>
            <p v-if="repoInfo.exists"><strong>Текущая ветка:</strong> {{ repoInfo.currentBranch }}</p>
            <p v-if="repoInfo.exists"><strong>Ветки:</strong> {{ repoInfo.branches.join(', ') }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Модальное окно для выгрузки файлов -->
    <div class="modal fade" id="downloadModal" tabindex="-1" aria-labelledby="downloadModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="downloadModalLabel">Выгрузить файлы</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="downloadFiles">
              <div class="mb-3">
                <label for="downloadBranch" class="form-label">Выберите ветку</label>
                <select
                    class="form-control"
                    id="downloadBranch"
                    v-model="branchToDownload"
                    required
                >
                  <option v-for="branch in branches" :key="branch" :value="branch">{{ branch }}</option>
                </select>
              </div>
              <button type="submit" class="btn btn-primary w-100" :disabled="isDownloading || !branchToDownload">
                <span v-if="isDownloading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                Скачать
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useStore } from 'vuex';
import { useToast } from 'vue-toastification';
import apiClient from '@/utils/apiClient';
import TaskItem from '@/components/task/TaskItem.vue';

// Реактивные данные
const route = useRoute();
const store = useStore();
const toast = useToast();

const project = ref({});
const currentBranch = ref('main');
const branches = ref([]);
const filter = ref('all');
const isRepoCreated = ref(false);
const repoInfo = ref({
  name: '',
  path: '',
  exists: false,
  currentBranch: '',
  branches: [],
});
const newTask = ref({
  titleTask: '',
  descriptionTask: '',
});
const commitMessage = ref('');
const commitBranch = ref('');
const branchName = ref('');
const branchToMerge = ref('');
const branchToCheckout = ref('');
const branchToDownload = ref('');
const filesToCommit = ref([]);
const statusOutput = ref('');
const logOutput = ref('');
const isDownloading = ref(false);

// Вычисляемые свойства
const userId = computed(() => store.state.userId);
const isManager = computed(() => store.state.userRole === 'manager');

const filteredTasks = computed(() => {
  if (!project.value.tasks) return [];
  switch (filter.value) {
    case 'unassigned':
      return project.value.tasks.filter(
          task => !task.acceptedForExecutionByUser && (task.status === false || task.status === null)
      );
    case 'assigned':
      return project.value.tasks.filter(
          task => task.acceptedForExecutionByUser && (task.status === false || task.status === null)
      );
    case 'completed':
      return project.value.tasks.filter(task => task.status === true);
    case 'all':
    default:
      return project.value.tasks;
  }
});

// Методы
const formatDate = (date) => {
  return date ? new Date(date).toLocaleDateString('ru-RU') : 'Не указано';
};

const fetchProject = async (projectId) => {
  try {
    const response = await apiClient.get(`/project/get/${projectId}`);
    project.value = response.data;
    const followResponse = await apiClient.get(`/project/follow/${userId.value}`);
    project.value.isFollowed = followResponse.data.some(p => p.projectId === projectId);
    await checkRepository();
  } catch (error) {
    toast.error('Ошибка при получении проекта: ' + error.message);
  }
};

const checkRepository = async () => {
  if (!project.value.title) {
    isRepoCreated.value = false;
    return;
  }
  try {
    const response = await apiClient.get(`/git/info/${project.value.title}`);
    repoInfo.value = response.data || { name: '', path: '', exists: false, currentBranch: '', branches: [] };
    isRepoCreated.value = repoInfo.value.exists;
    if (isRepoCreated.value) {
      currentBranch.value = repoInfo.value.currentBranch || 'main';
      branches.value = repoInfo.value.branches || [];
    } else {
      currentBranch.value = 'main';
      branches.value = [];
    }
  } catch (error) {
    console.error('Error checking repository:', error);
    isRepoCreated.value = false;
    currentBranch.value = 'main';
    branches.value = [];
    await fallbackCheckRepository();
  }
};

const fallbackCheckRepository = async () => {
  try {
    const response = await apiClient.get(`/git/repositories`);
    if (response.data.includes(project.value.title)) {
      isRepoCreated.value = true;
      repoInfo.value.exists = true;
      repoInfo.value.name = project.value.title;
      repoInfo.value.path = `/repos/project_${project.value.title}`;
      await fetchBranches();
      toast.info('Репозиторий найден через альтернативную проверку');
    }
  } catch (error) {
    console.error('Error in fallback repository check:', error);
    toast.error('Не удалось проверить существование репозитория: ' + error.message);
  }
};

const toggleFollow = async (projectId) => {
  const action = project.value.isFollowed ? 'unfollow' : 'follow';
  try {
    const response = await apiClient.get(`/project/${action}/${projectId}/${userId.value}`);
    if (response.data) {
      project.value.isFollowed = !project.value.isFollowed;
      toast.success(project.value.isFollowed ? 'Подписка оформлена' : 'Подписка отменена');
    } else {
      throw new Error('Не удалось изменить статус подписки');
    }
  } catch (error) {
    toast.error('Ошибка при переключении подписки: ' + error.message);
  }
};

const filterTasks = (type) => {
  filter.value = type;
};

const showAddTaskModal = () => {
  if (!isManager.value) {
    toast.error('Только менеджеры могут добавлять задания');
    return;
  }
  newTask.value = { titleTask: '', descriptionTask: '' };
  const modal = new bootstrap.Modal(document.getElementById('addTaskModal'));
  modal.show();
};

const createTask = async () => {
  if (!isManager.value) {
    toast.error('Только менеджеры могут добавлять задания');
    return;
  }
  try {
    await apiClient.post('/task/create', {
      titleTask: newTask.value.titleTask,
      descriptionTask: newTask.value.descriptionTask,
      project: project.value.projectId,
      createdByUser: userId.value,
    });
    toast.success('Задание успешно создано');
    newTask.value = { titleTask: '', descriptionTask: '' };
    await fetchProject(project.value.projectId);
    bootstrap.Modal.getInstance(document.getElementById('addTaskModal')).hide();
  } catch (error) {
    toast.error('Ошибка при создании задания: ' + error.message);
  }
};

const fetchBranches = async () => {
  if (!project.value.title || !isRepoCreated.value) {
    branches.value = [];
    currentBranch.value = 'main';
    return;
  }
  try {
    const response = await apiClient.get(`/git/branches/${project.value.title}`);
    branches.value = response.data.map(branch => branch.replace('refs/heads/', ''));
    if (!branches.value.includes(currentBranch.value)) {
      currentBranch.value = branches.value[0] || 'main';
    }
  } catch (error) {
    toast.error('Ошибка при получении веток: ' + error.message);
    branches.value = [];
    currentBranch.value = 'main';
  }
};

const createRepository = async () => {
  if (!project.value.title) {
    toast.error('Название проекта не определено');
    return;
  }
  try {
    const response = await apiClient.post(`/git/create/${project.value.title}`);
    toast.success(response.data);
    await checkRepository();
  } catch (error) {
    toast.error('Ошибка: ' + (error.response?.data || error.message));
    await checkRepository();
  }
};

const showBranchesModal = async () => {
  if (!isRepoCreated.value) {
    toast.error('Репозиторий не создан');
    return;
  }
  await fetchBranches();
  const modal = new bootstrap.Modal(document.getElementById('branchesModal'));
  modal.show();
};

const showAddFileModal = () => {
  if (!isRepoCreated.value) {
    toast.error('Репозиторий не создан');
    return;
  }
  filesToCommit.value = [];
  const modal = new bootstrap.Modal(document.getElementById('addFileModal'));
  modal.show();
};

const addFile = async () => {
  if (!filesToCommit.value || filesToCommit.value.length === 0) {
    toast.error('Выберите хотя бы один файл для загрузки');
    return;
  }

  const formData = new FormData();
  Array.from(filesToCommit.value).forEach(file => {
    formData.append('file', file);
  });

  try {
    const response = await apiClient.post(`/git/${encodeURIComponent(project.value.title)}/add`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    toast.success(response.data);
    bootstrap.Modal.getInstance(document.getElementById('addFileModal')).hide();
  } catch (error) {
    console.error('Error adding file:', error.response || error);
    toast.error('Ошибка при добавлении файла: ' + (error.response?.data || error.message));
  }
};

const showCommitModal = async () => {
  if (!isRepoCreated.value) {
    toast.error('Репозиторий не создан');
    return;
  }
  commitMessage.value = '';
  commitBranch.value = currentBranch.value;
  filesToCommit.value = [];
  await fetchBranches();
  if (branches.value.length === 0) {
    toast.error('Нет доступных веток для коммита');
    return;
  }
  const modal = new bootstrap.Modal(document.getElementById('commitModal'));
  modal.show();
};

const commitChanges = async () => {
  if (!isRepoCreated.value) {
    toast.error('Репозиторий не создан');
    return;
  }
  const formData = new FormData();
  formData.append('commitRequest', new Blob([JSON.stringify({
    message: commitMessage.value,
    branch: commitBranch.value,
  })], { type: 'application/json' }));
  Array.from(filesToCommit.value).forEach(file => {
    formData.append('files', file);
  });

  try {
    const response = await apiClient.post(`/git/${project.value.title}/commit`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    toast.success(response.data);
    bootstrap.Modal.getInstance(document.getElementById('commitModal')).hide();
  } catch (error) {
    console.error('Commit error:', error.response || error);
    toast.error('Ошибка при коммите: ' + (error.response?.data || error.message));
  }
};

const showCreateBranchModal = () => {
  if (!isRepoCreated.value) {
    toast.error('Репозиторий не создан');
    return;
  }
  branchName.value = '';
  const modal = new bootstrap.Modal(document.getElementById('createBranchModal'));
  modal.show();
};

const createBranch = async () => {
  try {
    const response = await apiClient.post(`/git/${project.value.title}/branch`, { branchName: branchName.value });
    toast.success(response.data);
    await fetchBranches();
    bootstrap.Modal.getInstance(document.getElementById('createBranchModal')).hide();
  } catch (error) {
    toast.error('Ошибка: ' + (error.response?.data || error.message));
  }
};

const showMergeModal = async () => {
  if (!isRepoCreated.value) {
    toast.error('Репозиторий не создан');
    return;
  }
  branchToMerge.value = '';
  await fetchBranches();
  const modal = new bootstrap.Modal(document.getElementById('mergeModal'));
  modal.show();
};

const mergeBranches = async () => {
  try {
    const response = await apiClient.post(`/git/${project.value.title}/merge`, { branchName: branchToMerge.value });
    toast.success(response.data);
    bootstrap.Modal.getInstance(document.getElementById('mergeModal')).hide();
  } catch (error) {
    toast.error('Ошибка: ' + (error.response?.data || error.message));
  }
};

const showCheckoutModal = async () => {
  if (!isRepoCreated.value) {
    toast.error('Репозиторий не создан');
    return;
  }
  branchToCheckout.value = '';
  await fetchBranches();
  const modal = new bootstrap.Modal(document.getElementById('checkoutModal'));
  modal.show();
};

const checkoutBranch = async () => {
  try {
    const response = await apiClient.post(`/git/${project.value.title}/checkout`, { branchName: branchToCheckout.value });
    currentBranch.value = branchToCheckout.value;
    toast.success(response.data);
    bootstrap.Modal.getInstance(document.getElementById('checkoutModal')).hide();
    await checkRepository();
  } catch (error) {
    toast.error('Ошибка: ' + (error.response?.data || error.message));
  }
};

const showStatusModal = async () => {
  if (!isRepoCreated.value) {
    toast.error('Репозиторий не создан');
    return;
  }
  try {
    const response = await apiClient.get(`/git/${project.value.title}/status`);
    statusOutput.value = response.data;
    const modal = new bootstrap.Modal(document.getElementById('statusModal'));
    modal.show();
  } catch (error) {
    toast.error('Ошибка: ' + (error.response?.data || error.message));
  }
};

const showLogModal = async () => {
  if (!isRepoCreated.value) {
    toast.error('Репозиторий не создан');
    return;
  }
  try {
    const response = await apiClient.get(`/git/${project.value.title}/log`);
    logOutput.value = response.data;
    const modal = new bootstrap.Modal(document.getElementById('logModal'));
    modal.show();
  } catch (error) {
    toast.error('Ошибка: ' + (error.response?.data || error.message));
  }
};

const showInfoModal = async () => {
  if (!isRepoCreated.value) {
    toast.error('Репозиторий не создан');
    return;
  }
  await checkRepository();
  const modal = new bootstrap.Modal(document.getElementById('infoModal'));
  modal.show();
};

const showDownloadModal = async () => {
  if (!isRepoCreated.value) {
    toast.error('Репозиторий не создан');
    return;
  }
  branchToDownload.value = currentBranch.value;
  await fetchBranches();
  const modal = new bootstrap.Modal(document.getElementById('downloadModal'));
  modal.show();
};

const downloadFiles = async () => {
  isDownloading.value = true;
  try {
    const response = await apiClient.get(`/git/${project.value.title}/download?branch=${branchToDownload.value}`, {
      responseType: 'blob',
    });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `${project.value.title}_${branchToDownload.value}.zip`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    toast.success('Файлы успешно скачаны');
    bootstrap.Modal.getInstance(document.getElementById('downloadModal')).hide();
  } catch (error) {
    toast.error('Ошибка при скачивании файлов: ' + (error.response?.data || error.message));
  } finally {
    isDownloading.value = false;
  }
};

const handleFileUpload = (event) => {
  filesToCommit.value = event.target.files;
};

// Инициализация
onMounted(() => {
  const projectId = route.params.id;
  fetchProject(projectId);
});
</script>

<style scoped>
.project-details {
  background: #f5f6fa;
  min-height: 100vh;
}

.sidebar,
.sidebar-right {
  width: 250px;
  padding: 2rem;
  background: #ffffff;
  border-radius: 15px;
  margin: 1rem;
}

.sidebar-title {
  color: #2c3e50;
  font-weight: 600;
  margin-bottom: 1.5rem;
}

.filter-btn,
.git-btn {
  width: 100%;
  text-align: left;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.filter-btn:hover,
.git-btn:hover {
  background: #e9ecef;
  transform: translateX(5px);
}

.git-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.main-content {
  flex-grow: 1;
  padding: 2rem;
}

.project-header {
  background: #ffffff;
  padding: 2rem;
  border-radius: 15px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.project-title {
  color: #2c3e50;
  font-weight: 700;
  margin-bottom: 1rem;
}

.project-description {
  color: #6c757d;
  font-size: 1.1rem;
  line-height: 1.6;
}

.project-meta {
  color: #6c757d;
  margin: 1rem 0;
}

.project-actions {
  display: flex;
  gap: 1rem;
}

.action-btn {
  padding: 0.6rem 1.5rem;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: scale(1.05);
}

.action-btn.followed {
  background: #f8f9fa;
  border-color: #dee2e6;
}

.tasks-section {
  background: #ffffff;
  padding: 2rem;
  border-radius: 15px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.section-title {
  color: #2c3e50;
  font-weight: 600;
  margin-bottom: 1.5rem;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.modal-content {
  border-radius: 15px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.modal-header {
  background: #2c3e50;
  color: #fff;
  border-top-left-radius: 15px;
  border-top-right-radius: 15px;
}

.modal-title {
  font-weight: 600;
}

.btn-close {
  filter: invert(1);
}

.form-control {
  border-radius: 10px;
  transition: all 0.3s ease;
}

.form-control:focus {
  box-shadow: 0 0 10px rgba(0, 123, 255, 0.3);
  border-color: #007bff;
}

.btn-primary {
  border-radius: 10px;
  transition: all 0.3s ease;
}

.btn-primary:hover {
  transform: scale(1.05);
  background: #0056b3;
}

.list-group-item {
  border-radius: 10px;
  margin-bottom: 0.5rem;
}

pre {
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 10px;
  max-height: 300px;
  overflow-y: auto;
}

.text-warning {
  color: #ffc107;
  font-size: 0.9rem;
  margin-bottom: 1rem;
}
</style>