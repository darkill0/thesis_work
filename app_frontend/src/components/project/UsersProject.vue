<template>
  <div class="container">
    <h2 class="mb-4 text-primary fw-bold">Мои Проекты</h2>
    <div class="row">
      <div class="col-md-9">
        <div class="project-list">
          <ProjectCard
              v-for="project in projects"
              :key="project.id"
              :project="project"
              @click="viewProjectDetails(project.id)"
          />
        </div>
      </div>
      <div class="col-md-3">
        <div class="notifications">
          <h4 class="text-primary fw-bold">Уведомления</h4>
          <input
              type="text"
              class="form-control mb-3"
              v-model="notificationSearch"
              placeholder="Поиск уведомлений"
              @input="filterNotifications"
          />
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
  </div>
</template>

<script>
import apiClient from '@/utils/apiClient';
import { mapState } from 'vuex';
import ProjectCard from "@/components/project/ProjectCard.vue";
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { useToast } from 'vue-toastification';

export default {
  components: {
    ProjectCard,
  },
  data() {
    return {
      projects: [],
      notifications: [],
      filteredNotifications: [],
      notificationSearch: '',
      stompClient: null,
    };
  },
  computed: {
    ...mapState(['userId']),
  },
  setup() {
    const toast = useToast();
    return { toast };
  },
  methods: {
    async fetchUserProjects() {
      try {
        const response = await apiClient.get(`/project/follow/${this.userId}`);
        this.projects = response.data || [];
        console.log('Проекты:', this.projects);
      } catch (error) {
        console.error('Ошибка при получении проектов пользователя:', error);
        this.toast.error('Не удалось загрузить проекты: ' + error.message);
      }
    },
    async fetchNotifications() {
      const token = localStorage.getItem('token');
      try {
        const response = await fetch(`http://localhost:8085/api/test/${this.userId}`, {
          method: 'GET',
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        });
        if (!response.ok) throw new Error(`Ошибка HTTP: ${response.status}`);
        const data = await response.json();
        this.notifications = data.map((notification) => ({
          id: notification.id,
          message: this.formatNotification(notification),
          type: notification.type || 'UNKNOWN',
          progress: 100,
        }));
        this.filteredNotifications = this.notifications;
        console.log('Уведомления:', this.notifications);
      } catch (error) {
        console.error('Ошибка при получении уведомлений:', error);
        this.toast.error('Не удалось загрузить уведомления: ' + error.message);
      }
    },
    formatNotification(notification) {
      const message = notification.message || 'Новое уведомление';

      // Вспомогательная функция для форматирования даты из строки
      function formatDate(dateStr) {
        const date = new Date(dateStr);
        if (isNaN(date)) return dateStr;
        return date.toLocaleDateString('ru-RU', { day: '2-digit', month: 'long', year: 'numeric' }) + ', ' +
            date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
      }

      // Детальный парсер для UPDATE_TASK
      function formatUpdateTask(rawMessage) {
        try {
          const match = rawMessage.match(/\[TaskForm\((.*)\)\]/);
          if (!match) return rawMessage;

          const content = match[1];
          const fieldsArray = content.split(/,(?=\s*\w+=)/);
          const task = {};
          fieldsArray.forEach(field => {
            const [key, ...rest] = field.split('=');
            task[key.trim()] = rest.join('=').trim().replace(/^"|"$/g, '');
          });

          const title = task.titleTask || 'Без названия';
          const description = task.descriptionTask || '';
          const status = (task.status === 'true' || task.status === true) ? 'Завершена' : 'В работе';
          const updatedAt = task.updatedAt ? formatDate(task.updatedAt) : '';

          let commentsText = '';
          if (task.comments) {
            const commentMatch = task.comments.match(/description=([^,\)]+)/);
            if (commentMatch) commentsText = commentMatch[1];
          }

          let result = `Задача обновлена: "${title}"\n`;
          if (description) result += `Описание: ${description}\n`;
          result += `Статус: ${status}\n`;
          if (updatedAt) result += `Обновлено: ${updatedAt}\n`;
          if (commentsText) result += `Комментарий: ${commentsText}`;

          return result.trim();
        } catch {
          return rawMessage;
        }
      }

      switch (notification.type) {
        case 'CREATE_PROJECT':
          return `Создан новый проект: ${message}`;
        case 'UPDATE_PROJECT':
          return `Проект обновлён: ${message}`;
        case 'CLOSE_PROJECT':
          return `Проект закрыт: ${message}`;
        case 'CREATE_TASK':
          return `Создана новая задача: ${message}`;
        case 'UPDATE_TASK':
          return formatUpdateTask(message);
        case 'CLOSE_TASK':
          return `Задача закрыта: ${message}`;
        case 'COMPLETE_TASK':
          return `Задача завершена: ${message}`;
        default:
          return message;
      }
    },
    connectWebSocket() {
      const socket = new SockJS('http://localhost:8085/ws');
      this.stompClient = Stomp.over(socket);
      this.stompClient.connect({}, () => {
        this.stompClient.subscribe('/topic/messages', (message) => {
          const notification = JSON.parse(message.body);
          const projectTaskEvents = [
            'CREATE_PROJECT', 'UPDATE_PROJECT', 'CLOSE_PROJECT',
            'CREATE_TASK', 'UPDATE_TASK', 'CLOSE_TASK', 'COMPLETE_TASK'
          ];
          if (notification.to === this.userId && projectTaskEvents.includes(notification.type)) {
            const newNotification = {
              id: notification.id || Date.now(),
              type: notification.type,
              message: this.formatNotification(notification),
              progress: 100,
            };
            this.notifications.push(newNotification);
            this.filterNotifications();
            this.toast.info(`Новое уведомление: ${notification.message}`);
            const interval = setInterval(() => {
              newNotification.progress -= 5;
              if (newNotification.progress <= 0) {
                this.notifications = this.notifications.filter((n) => n.id !== newNotification.id);
                this.filterNotifications();
                clearInterval(interval);
              }
            }, 300);
          }
        });
      }, (error) => {
        console.error('Ошибка подключения к WebSocket:', error);
        this.toast.error('Не удалось подключиться к WebSocket');
      });
    },
    filterNotifications() {
      if (this.notificationSearch.trim() === '') {
        this.filteredNotifications = this.notifications;
      } else {
        this.filteredNotifications = this.notifications.filter((notification) =>
            notification.message.toLowerCase().includes(this.notificationSearch.toLowerCase())
        );
      }
    },
    viewProjectDetails(projectId) {
      this.$router.push({ name: 'projectDetails', params: { id: projectId } });
    },
  },
  created() {
    if (this.userId) {
      this.fetchUserProjects();
      this.fetchNotifications();
      this.connectWebSocket();
    } else {
      console.warn('userId не определен');
      this.toast.error('Пользователь не аутентифицирован');
    }
  },
  beforeUnmount() {
    if (this.stompClient) {
      this.stompClient.disconnect();
    }
  },
};
</script>

<style scoped>
.container {
  padding: 20px;
  max-width: 1200px;
  margin-top: 40px;
}

h2,
h4 {
  color: #1a3c6d;
}

.project-list {
  padding: 15px;
}

.notifications {
  position: sticky;
  top: 20px;
  width: 100%;
  padding: 15px;
  background-color: #e9ecef;
  border-radius: 10px;
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

.form-control {
  border-radius: 8px;
  border: 1px solid #ced4da;
  transition: border-color 0.2s ease;
}

.form-control:focus {
  border-color: #1a3c6d;
  box-shadow: 0 0 5px rgba(26, 60, 109, 0.3);
}
</style>