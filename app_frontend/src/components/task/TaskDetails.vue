<template>
  <div class="container mt-5">
    <!-- Карточка задачи -->
    <div class="card shadow-sm border-0 mb-5">
      <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
        <h2 class="mb-0">{{ task.titleTask }}</h2>
        <div v-if="isManager" class="manager-actions">
          <button v-if="!task.status" class="btn btn-success btn-sm me-2" @click="completeTask">
            <i class="bi bi-check-circle me-2"></i>Завершить
          </button>
          <button class="btn btn-warning btn-sm me-2" data-bs-toggle="modal" data-bs-target="#editTaskModal">
            <i class="bi bi-pencil me-2"></i>Редактировать
          </button>
          <button class="btn btn-danger btn-sm" @click="confirmDelete">
            <i class="bi bi-trash me-2"></i>Удалить
          </button>
        </div>
      </div>
      <div class="card-body">
        <p class="lead">{{ task.descriptionTask || 'Описание отсутствует' }}</p>
        <div class="row">
          <div class="col-md-6">
            <p><strong>Создано:</strong> {{ new Date(task.createdAt).toLocaleDateString('ru-RU') }}</p>
            <p><strong>Обновлено:</strong> {{ new Date(task.updatedAt).toLocaleDateString('ru-RU') }}</p>
          </div>
          <div class="col-md-6">
            <p><strong>Статус:</strong> <span class="badge" :class="task.status ? 'bg-success' : 'bg-warning'">{{ task.status ? 'Завершено' : 'В процессе' }}</span></p>
            <p><strong>Проект:</strong> <router-link :to="{ name: 'projectDetails', params: { id: task.project } }" class="text-primary">Перейти к проекту</router-link></p>
          </div>
        </div>
        <router-link :to="{ name: 'usersTasks' }" class="btn btn-secondary mt-3">
          <i class="bi bi-arrow-left me-2"></i>Вернуться к задачам
        </router-link>
      </div>
    </div>

    <!-- Секция комментариев -->
    <div class="comments-section mt-5">
      <h4 class="mb-4 text-primary fw-bold">Комментарии</h4>
      <div v-if="task.comments && task.comments.length" class="comment-list">
        <div v-for="comment in task.comments" :key="comment.commentId" class="card shadow-sm border-0 mb-3">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <h5 class="mb-0 text-primary">Комментарий от пользователя {{ comment.createdByUser }}</h5>
              <span class="text-muted small">{{ new Date(comment.createdAt).toLocaleDateString('ru-RU') }}</span>
            </div>
            <p class="mb-0">{{ comment.description }}</p>
          </div>
        </div>
      </div>
      <p v-else class="text-muted">Комментариев пока нет.</p>
    </div>

    <!-- Кнопка для открытия модального окна добавления комментария -->
    <button class="btn btn-primary mt-3" data-bs-toggle="modal" data-bs-target="#addCommentModal">
      <i class="bi bi-plus-circle me-2"></i>Добавить комментарий
    </button>

    <!-- Модальное окно для добавления комментария -->
    <div class="modal fade" id="addCommentModal" tabindex="-1" aria-labelledby="addCommentModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-primary text-white">
            <h5 class="modal-title" id="addCommentModalLabel">Добавить комментарий</h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Закрыть"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="submitComment">
              <div class="mb-3">
                <label for="commentDescription" class="form-label">Комментарий</label>
                <textarea
                    v-model="newComment.description"
                    id="commentDescription"
                    class="form-control"
                    rows="4"
                    required
                    placeholder="Введите ваш комментарий"
                ></textarea>
              </div>
              <button type="submit" class="btn btn-primary w-100">Добавить</button>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- Модальное окно для редактирования задачи -->
    <div class="modal fade" id="editTaskModal" tabindex="-1" aria-labelledby="editTaskModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-primary text-white">
            <h5 class="modal-title" id="editTaskModalLabel">Редактировать задачу</h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Закрыть"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="updateTask">
              <div class="mb-3">
                <label for="taskTitle" class="form-label">Название задачи</label>
                <input
                    v-model="editTask.titleTask"
                    id="taskTitle"
                    class="form-control"
                    required
                />
              </div>
              <div class="mb-3">
                <label for="taskDescription" class="form-label">Описание</label>
                <textarea
                    v-model="editTask.descriptionTask"
                    id="taskDescription"
                    class="form-control"
                    rows="4"
                ></textarea>
              </div>
              <div class="mb-3">
                <label for="taskStatus" class="form-label">Статус</label>
                <select v-model="editTask.status" id="taskStatus" class="form-select">
                  <option :value="false">В процессе</option>
                  <option :value="true">Завершено</option>
                </select>
              </div>
              <button type="submit" class="btn btn-primary w-100">Сохранить</button>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- Модальное окно для сообщений и подтверждений -->
    <div class="modal fade" id="messageModal" tabindex="-1" aria-labelledby="messageModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header" :class="modalIsError ? 'bg-danger text-white' : 'bg-primary text-white'">
            <h5 class="modal-title" id="messageModalLabel">{{ modalTitle }}</h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Закрыть"></button>
          </div>
          <div class="modal-body">
            <p>{{ modalMessage }}</p>
          </div>
          <div class="modal-footer">
            <button v-if="showConfirmButton" class="btn btn-primary" @click="confirmAction">Подтвердить</button>
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Закрыть</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStore } from 'vuex';
import apiClient from '@/utils/apiClient';

const store = useStore();
const route = useRoute();
const router = useRouter();
const task = ref({});
const newComment = ref({
  description: '',
  createdByUser: null,
});
const editTask = ref({
  taskId: null,
  titleTask: '',
  descriptionTask: '',
  status: false,
  project: null,
});
const modalTitle = ref('');
const modalMessage = ref('');
const modalIsError = ref(false);
const showConfirmButton = ref(false);
const confirmAction = ref(() => {});

const userId = computed(() => store.getters.getUserId);
const userRole = computed(() => store.getters.getUserRole);
const isManager = computed(() => userRole.value === 'manager');

const fetchTask = async (taskId) => {
  try {
    const response = await apiClient.get(`/task/get/${taskId}`);
    task.value = response.data;
  } catch (error) {
    showModal('Ошибка', 'Не удалось загрузить задачу. Попробуйте позже.', true);
  }
};

const submitComment = async () => {
  if (!userId.value) {
    showModal('Ошибка', 'Пользователь не аутентифицирован.', true);
    return;
  }

  const taskId = task.value.taskId;
  newComment.value.createdByUser = userId.value;

  try {
    await apiClient.post(`/task/comment/${taskId}/add`, newComment.value);
    await fetchTask(taskId);
    newComment.value.description = '';
    const modal = bootstrap.Modal.getInstance(document.getElementById('addCommentModal'));
    modal.hide();
    showModal('Успех', 'Комментарий успешно добавлен.', false);
  } catch (error) {
    showModal('Ошибка', 'Не удалось добавить комментарий. Попробуйте позже.', true);
  }
};

const completeTask = async () => {
  if (!isManager.value) {
    showModal('Ошибка', 'Только менеджеры могут завершать задачи.', true);
    return;
  }

  try {
    const taskId = task.value.taskId;
    await apiClient.post(`/task/complete/${taskId}`);
    await fetchTask(taskId);
    showModal('Успех', 'Задача успешно завершена.', false);
  } catch (error) {
    showModal('Ошибка', 'Не удалось завершить задачу. Попробуйте позже.', true);
  }
};

const openEditTaskModal = () => {
  editTask.value = {
    taskId: task.value.taskId,
    titleTask: task.value.titleTask,
    descriptionTask: task.value.descriptionTask || '',
    status: task.value.status,
    project: task.value.project,
  };
};

const updateTask = async () => {
  try {
    await apiClient.post(`/task/update/${editTask.value.taskId}`, {
      titleTask: editTask.value.titleTask,
      descriptionTask: editTask.value.descriptionTask,
      status: editTask.value.status,
      project: editTask.value.project,
      acceptedForExecutionByUser: task.value.acceptedForExecutionByUser || 0,
    });
    await fetchTask(task.value.taskId);
    const modal = bootstrap.Modal.getInstance(document.getElementById('editTaskModal'));
    modal.hide();
    showModal('Успех', 'Задача успешно обновлена.', false);
  } catch (error) {
    showModal('Ошибка', 'Не удалось обновить задачу. Попробуйте позже.', true);
  }
};

const confirmDelete = () => {
  modalTitle.value = 'Подтверждение удаления';
  modalMessage.value = 'Вы уверены, что хотите удалить эту задачу?';
  modalIsError.value = false;
  showConfirmButton.value = true;
  confirmAction.value = async () => {
    try {
      await apiClient.delete(`/task/delete/${task.value.taskId}`);
      showModal('Успех', 'Задача успешно удалена.', false);
      router.push({ name: 'usersTasks' });
    } catch (error) {
      showModal('Ошибка', 'Не удалось удалить задачу. Попробуйте позже.', true);
    }
  };
  const modal = new bootstrap.Modal(document.getElementById('messageModal'));
  modal.show();
};

const showModal = (title, message, isError) => {
  modalTitle.value = title;
  modalMessage.value = message;
  modalIsError.value = isError;
  showConfirmButton.value = false;
  const modal = new bootstrap.Modal(document.getElementById('messageModal'));
  modal.show();
};

onMounted(() => {
  const taskId = route.params.id;
  fetchTask(taskId);
  if (isManager.value && task.value.taskId) {
    openEditTaskModal();
  }
});
</script>

<style scoped>
.container {
  max-width: 1200px;
  margin-top: 40px;
}

.card {
  border-radius: 12px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.card-header {
  border-radius: 12px 12px 0 0;
  background-color: #1a3c6d;
  color: white;
  font-weight: 500;
}

.card-body {
  padding: 20px;
}

.lead {
  font-size: 1.1rem;
  color: #333;
}

.badge {
  font-size: 0.85rem;
  padding: 0.5rem 1rem;
  border-radius: 15px;
}

.btn-primary {
  background-color: #1a3c6d;
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  transition: background-color 0.2s ease;
}

.btn-primary:hover {
  background-color: #153258;
}

.btn-secondary {
  border-radius: 8px;
  padding: 10px 20px;
  transition: background-color 0.2s ease;
}

.btn-secondary:hover {
  background-color: #5a6268;
}

.btn-warning {
  border-radius: 8px;
  padding: 8px 16px;
}

.btn-danger {
  border-radius: 8px;
  padding: 8px 16px;
}

.btn-success {
  border-radius: 8px;
  padding: 8px 16px;
}

.comments-section {
  margin-top: 40px;
}

.comment-list .card {
  border-radius: 10px;
  background-color: #f8f9fa;
}

.comment-list .card-body {
  padding: 15px;
}

.comment-list h5 {
  font-size: 1rem;
  font-weight: 600;
}

.form-control, .form-select {
  border-radius: 8px;
  border: 1px solid #ced4da;
  transition: border-color 0.2s ease;
}

.form-control:focus, .form-select:focus {
  border-color: #1a3c6d;
  box-shadow: 0 0 5px rgba(26, 60, 109, 0.3);
}

.modal-content {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-header {
  border-bottom: none;
}

.btn-close-white {
  filter: invert(1);
}

.text-primary {
  color: #1a3c6d !important;
}

.manager-actions {
  display: flex;
  gap: 10px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.comment-list .card {
  animation: fadeIn 0.3s ease-out;
}
</style>