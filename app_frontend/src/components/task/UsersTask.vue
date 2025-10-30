<template>
  <div class="container mt-5">
    <h2 class="mb-4 text-primary fw-bold">Мои Задачи</h2>
    <div class="row">
      <div class="col-md-9">
        <!-- Фильтры, поиск, сортировка и экспорт -->
        <div class="card shadow-sm border-0 mb-4">
          <div class="card-header bg-primary text-white">
            <h5 class="mb-0">Фильтры и управление</h5>
          </div>
          <div class="card-body">
            <div class="row g-3">
              <div class="col-md-4">
                <label for="statusFilter" class="form-label">Фильтр по статусу</label>
                <select
                    id="statusFilter"
                    class="form-select"
                    v-model="statusFilter"
                    @change="applyFilters"
                >
                  <option value="">Все</option>
                  <option value="inProgress">В процессе</option>
                  <option value="completed">Завершено</option>
                </select>
              </div>
              <div class="col-md-4">
                <label for="sortBy" class="form-label">Сортировка</label>
                <select
                    id="sortBy"
                    class="form-select"
                    v-model="sortBy"
                    @change="applyFilters"
                >
                  <option value="createdAt">По дате создания</option>
                  <option value="titleTask">По названию</option>
                </select>
              </div>
              <div class="col-md-4">
                <label for="searchQuery" class="form-label">Поиск</label>
                <input
                    id="searchQuery"
                    type="text"
                    class="form-control"
                    v-model="searchQuery"
                    placeholder="Поиск по названию или описанию"
                    @input="applyFilters"
                />
              </div>
            </div>
            <div class="mt-3">
              <button
                  class="btn btn-success w-100"
                  @click="exportToCSV"
              >
                <i class="bi bi-file-earmark-arrow-down me-2"></i>Экспорт в CSV
              </button>
            </div>
          </div>
        </div>
        <!-- Список задач -->
        <div class="task-list">
          <TaskItem
              v-for="task in filteredTasks"
              :key="task.taskId"
              :task="task"
              :isUserTasks="true"
              @edit-task="openEditTaskModal"
              @delete-task="deleteTask"
              @view-details="viewTaskDetails"
              class="mb-3"
          />
          <p v-if="!filteredTasks.length" class="text-muted text-center mt-4">
            Нет задач, соответствующих фильтрам.
          </p>
        </div>
      </div>
      <!-- Уведомления -->
      <div class="col-md-3">
        <div class="notifications">
          <h4 class="text-primary fw-bold">Уведомления</h4>
          <transition-group name="fade">
            <div
                v-for="notification in filteredNotifications"
                :key="notification.id"
                class="notification-popup"
            >
              <p>{{ notification.message }}</p>
              <div class="progress-bar">
                <div :style="{ width: notification.progress + '%' }"></div>
              </div>
            </div>
          </transition-group>
          <p v-if="!filteredNotifications.length" class="text-muted mt-3">
            Нет уведомлений.
          </p>
        </div>
      </div>
    </div>

    <!-- Модальное окно для редактирования задачи -->
    <div
        class="modal fade"
        id="editTaskModal"
        tabindex="-1"
        aria-labelledby="editTaskModalLabel"
        aria-hidden="true"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-primary text-white">
            <h5 class="modal-title" id="editTaskModalLabel">Редактировать задачу</h5>
            <button
                type="button"
                class="btn-close btn-close-white"
                data-bs-dismiss="modal"
                aria-label="Закрыть"
            ></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="updateTask">
              <div class="mb-3">
                <label for="taskTitle" class="form-label">Название задачи</label>
                <input
                    type="text"
                    class="form-control"
                    id="taskTitle"
                    v-model="editTask.titleTask"
                    required
                />
              </div>
              <div class="mb-3">
                <label for="taskDescription" class="form-label">Описание</label>
                <textarea
                    class="form-control"
                    id="taskDescription"
                    v-model="editTask.descriptionTask"
                    rows="4"
                ></textarea>
              </div>
              <div class="mb-3">
                <label for="taskStatus" class="form-label">Статус</label>
                <select
                    id="taskStatus"
                    class="form-select"
                    v-model="editTask.status"
                >
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

    <!-- Модальное окно для сообщений -->
    <div
        class="modal fade"
        id="messageModal"
        tabindex="-1"
        aria-labelledby="messageModalLabel"
        aria-hidden="true"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header" :class="modalIsError ? 'bg-danger text-white' : 'bg-primary text-white'">
            <h5 class="modal-title" id="messageModalLabel">{{ modalTitle }}</h5>
            <button
                type="button"
                class="btn-close btn-close-white"
                data-bs-dismiss="modal"
                aria-label="Закрыть"
            ></button>
          </div>
          <div class="modal-body">
            <p>{{ modalMessage }}</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Закрыть</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router'; // Исправленный импорт
import apiClient from '@/utils/apiClient';
import TaskItem from '@/components/task/TaskItem.vue';
import WebSocketService from '@/utils/WebSocketService';

// Данные
const store = useStore();
const router = useRouter();
const tasks = ref([]);
const notifications = ref([]);
const webSocketService = ref(null);
const statusFilter = ref('');
const sortBy = ref('createdAt');
const searchQuery = ref('');
const editTask = ref({
  taskId: null,
  titleTask: '',
  descriptionTask: '',
  status: false,
});
const modalTitle = ref('');
const modalMessage = ref('');
const modalIsError = ref(false);

// Вычисляемые свойства
const userId = computed(() => store.getters.getUserId);
const filteredNotifications = computed(() => {
  return notifications.value.filter((notification) => {
    const isTaskRelated = notification.message.includes('TaskForm');
    let isUserRelated = false;
    if (isTaskRelated) {
      try {
        const taskData = notification.message.match(/TaskForm\((.+)\)/)[1];
        const fields = taskData.split(', ').reduce((acc, field) => {
          const [key, value] = field.split('=');
          acc[key] = value;
          return acc;
        }, {});
        isUserRelated =
            fields.createdByUser === userId.value.toString() ||
            fields.acceptedForExecutionByUser === userId.value.toString();
      } catch (e) {
        console.error('Ошибка при проверке уведомления:', e);
      }
    }
    return isTaskRelated && isUserRelated;
  });
});
const filteredTasks = computed(() => {
  let filtered = [...tasks.value];

  // Фильтрация по статусу
  if (statusFilter.value === 'inProgress') {
    filtered = filtered.filter((task) => !task.status);
  } else if (statusFilter.value === 'completed') {
    filtered = filtered.filter((task) => task.status);
  }

  // Поиск
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(
        (task) =>
            task.titleTask.toLowerCase().includes(query) ||
            (task.descriptionTask && task.descriptionTask.toLowerCase().includes(query))
    );
  }

  // Сортировка
  if (sortBy.value === 'createdAt') {
    filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  } else if (sortBy.value === 'titleTask') {
    filtered.sort((a, b) => a.titleTask.localeCompare(b.titleTask));
  }

  return filtered;
});

// Методы
const fetchUserTasks = async () => {
  try {
    const response = await apiClient.get(`/task/list/by/${userId.value}`);
    tasks.value = response.data || [];
    console.log('Мои задачи:', tasks.value);
  } catch (error) {
    console.error('Ошибка при получении задач:', error);
    showModal('Ошибка', 'Не удалось загрузить задачи. Попробуйте позже.', true);
  }
};

const fetchNotifications = async () => {
  const token = localStorage.getItem('token');
  try {
    const response = await fetch(`http://localhost:8085/api/test/${userId.value}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    });
    if (!response.ok) throw new Error(`Ошибка HTTP: ${response.status}`);
    const data = await response.json();
    notifications.value = data.map((notification) => ({
      ...notification,
      message: formatNotification(notification.message),
      progress: 100,
    }));
  } catch (error) {
    console.error('Ошибка при получении уведомлений:', error);
    showModal('Ошибка', 'Не удалось загрузить уведомления. Попробуйте позже.', true);
  }
};

const addNotification = (notification) => {
  const id = Date.now();
  const formattedMessage = formatNotification(notification.message);
  const newNotification = { id, message: formattedMessage, progress: 100 };

  const isTaskRelated = formattedMessage.includes('TaskForm');
  let isUserRelated = false;
  if (isTaskRelated) {
    try {
      const taskData = notification.message.match(/TaskForm\((.+)\)/)[1];
      const fields = taskData.split(', ').reduce((acc, field) => {
        const [key, value] = field.split('=');
        acc[key] = value;
        return acc;
      }, {});
      isUserRelated =
          fields.createdByUser === userId.value.toString() ||
          fields.acceptedForExecutionByUser === userId.value.toString();
    } catch (e) {
      console.error('Ошибка при проверке уведомления:', e);
    }
  }

  if (isTaskRelated && isUserRelated) {
    notifications.value.push(newNotification);
    const interval = setInterval(() => {
      newNotification.progress -= 5;
      if (newNotification.progress <= 0) {
        notifications.value = notifications.value.filter((n) => n.id !== id);
        clearInterval(interval);
      }
    }, 300);
  }
};

const formatNotification = (notification) => {
  const rawMessage = notification.message || 'Новое уведомление';

  // Форматирование даты для удобного отображения
  const formatDate = (dateStr) => {
    const date = new Date(dateStr);
    if (isNaN(date)) return dateStr;
    return date.toLocaleDateString('ru-RU', { day: '2-digit', month: 'long', year: 'numeric' }) + ', ' +
        date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
  };

  // Форматирование сообщения для обновлённой задачи
  const formatTaskUpdate = (rawMsg) => {
    try {
      const match = rawMsg.match(/TaskForm\((.+)\)/);
      if (!match) return rawMsg;

      const taskData = match[1];
      // Разбиваем поля с учётом того, что внутри могут быть запятые (упрощённо)
      // Для надёжности можно улучшить парсер, сейчас простая реализация
      const fieldsArray = taskData.split(/,(?=\s*\w+=)/);
      const fields = fieldsArray.reduce((acc, field) => {
        const [key, ...rest] = field.split('=');
        acc[key.trim()] = rest.join('=').trim();
        return acc;
      }, {});

      const title = fields.titleTask || 'Без названия';
      const description = fields.descriptionTask || 'Без описания';
      const status = (fields.status === 'true' || fields.status === true) ? 'Завершена' : 'В работе';
      const updatedAt = fields.updatedAt ? formatDate(fields.updatedAt) : 'неизвестно';

      // Можно достать комментарий, если есть
      let comment = '';
      if (fields.comments) {
        const commentMatch = fields.comments.match(/description=([^,\)]+)/);
        comment = commentMatch ? commentMatch[1] : '';
      }

      return `Задача "${title}" обновлена.\nОписание: ${description}\nСтатус: ${status}\nОбновлено: ${updatedAt}` +
          (comment ? `\nКомментарий: ${comment}` : '');
    } catch (e) {
      console.error('Ошибка при форматировании уведомления:', e);
      return rawMsg;
    }
  };

  switch (notification.type) {
    case 'CREATE_PROJECT':
      return `Создан новый проект: ${rawMessage}`;
    case 'UPDATE_PROJECT':
      return `Проект обновлён: ${rawMessage}`;
    case 'CLOSE_PROJECT':
      return `Проект закрыт: ${rawMessage}`;
    case 'CREATE_TASK':
      return `Создана новая задача: ${rawMessage}`;
    case 'UPDATE_TASK':
      return formatTaskUpdate(rawMessage);
    case 'CLOSE_TASK':
      return `Задача закрыта: ${rawMessage}`;
    case 'COMPLETE_TASK':
      return `Задача завершена: ${rawMessage}`;
    default:
      return rawMessage;
  }
};


const applyFilters = () => {
  tasks.value = [...tasks.value]; // Триггерим реактивность
};

const openEditTaskModal = (task) => {
  editTask.value = {
    taskId: task.taskId,
    titleTask: task.titleTask,
    descriptionTask: task.descriptionTask || '',
    status: task.status,
  };
  const modal = new bootstrap.Modal(document.getElementById('editTaskModal'));
  modal.show();
};

const updateTask = async () => {
  try {
    await apiClient.post(`/task/update/${editTask.value.taskId}`, {
      titleTask: editTask.value.titleTask,
      descriptionTask: editTask.value.descriptionTask,
      status: editTask.value.status,
    });
    const modal = bootstrap.Modal.getInstance(document.getElementById('editTaskModal'));
    modal.hide();
    document.querySelectorAll('.modal-backdrop').forEach((backdrop) => backdrop.remove());
    await fetchUserTasks();
    showModal('Успех', 'Задача успешно обновлена.', false);
  } catch (error) {
    console.error('Ошибка при обновлении задачи:', error);
    showModal('Ошибка', 'Не удалось обновить задачу. Попробуйте позже.', true);
  }
};

const deleteTask = async (taskId) => {
  showModal('Подтверждение', 'Вы уверены, что хотите удалить эту задачу?', false, async () => {
    try {
      await apiClient.delete(`/task/delete/${taskId}`);
      await fetchUserTasks();
      showModal('Успех', 'Задача успешно удалена.', false);
    } catch (error) {
      console.error('Ошибка при удалении задачи:', error);
      showModal('Ошибка', 'Не удалось удалить задачу. Попробуйте позже.', true);
    }
  });
};

const exportToCSV = () => {
  const headers = ['ID', 'Название', 'Описание', 'Статус', 'Дата создания'];
  const rows = filteredTasks.value.map((task) => [
    task.taskId,
    `"${task.titleTask.replace(/"/g, '""')}"`,
    `"${(task.descriptionTask || '').replace(/"/g, '""')}"`,
    task.status ? 'Завершено' : 'В процессе',
    new Date(task.createdAt).toLocaleDateString('ru-RU'),
  ]);
  const csvContent = [
    headers.join(','),
    ...rows.map((row) => row.join(',')),
  ].join('\n');
  const bom = '\uFEFF'; // Добавляем BOM для UTF-8
  const blob = new Blob([bom + csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = 'tasks.csv';
  link.click();
  URL.revokeObjectURL(link.href);
};

const viewTaskDetails = (taskId) => {
  router.push({ name: 'TaskDetails', params: { id: taskId } });
};

const showModal = (title, message, isError, confirmCallback = null) => {
  modalTitle.value = title;
  modalMessage.value = message;
  modalIsError.value = isError;
  const modal = new bootstrap.Modal(document.getElementById('messageModal'));
  modal.show();
  if (confirmCallback) {
    const confirmButton = document.createElement('button');
    confirmButton.type = 'button';
    confirmButton.className = 'btn btn-primary';
    confirmButton.innerText = 'Подтвердить';
    confirmButton.onclick = async () => {
      await confirmCallback();
      modal.hide();
    };
    const modalFooter = document.querySelector('#messageModal .modal-footer');
    modalFooter.insertBefore(confirmButton, modalFooter.firstChild);
  }
};

// Загрузка данных при монтировании
onMounted(async () => {
  if (userId.value) {
    await fetchUserTasks();
    await fetchNotifications();
    webSocketService.value = new WebSocketService(userId.value, addNotification);
  } else {
    console.warn('userId не определён');
    showModal('Ошибка', 'Пользователь не аутентифицирован.', true);
  }
});
</script>

<style scoped>
.container {
  margin-top: 40px;
  max-width: 1200px;
}

h2,
h4,
h5 {
  color: #1a3c6d;
}

.card {
  border-radius: 10px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.card-header {
  border-radius: 10px 10px 0 0;
  background-color: #1a3c6d;
  color: white;
  font-weight: 500;
}

.task-list {
  margin-top: 20px;
}

.form-select,
.form-control {
  border-radius: 8px;
  border: 1px solid #ced4da;
  transition: border-color 0.2s ease;
}

.form-select:focus,
.form-control:focus {
  border-color: #1a3c6d;
  box-shadow: 0 0 5px rgba(26, 60, 109, 0.3);
}

.btn-primary,
.btn-success {
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
  transition: background-color 0.2s ease;
}

.btn-primary {
  background-color: #1a3c6d;
  border: none;
}

.btn-primary:hover {
  background-color: #153258;
}

.btn-success {
  background-color: #28a745;
  border: none;
}

.btn-success:hover {
  background-color: #218838;
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

.notifications {
  position: sticky;
  top: 20px;
  width: 100%;
  z-index: 1000;
}

.notification-popup {
  background: #17a2b8;
  color: white;
  padding: 15px;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  margin-bottom: 10px;
  animation: fadeIn 0.3s ease-out;
}

.progress-bar {
  height: 5px;
  background: rgba(255, 255, 255, 0.5);
  margin-top: 10px;
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar div {
  height: 100%;
  background: white;
  transition: width 0.3s linear;
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

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-enter,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>