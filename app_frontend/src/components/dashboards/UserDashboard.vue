<template>
  <div class="dashboard-container">
    <!-- Заголовок -->
    <h1 class="dashboard-title">Личный кабинет</h1>
    <p class="dashboard-subtitle">Ваша статистика и активность</p>

    <!-- Состояние загрузки -->
    <div v-if="isLoading" class="text-center my-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Загрузка...</span>
      </div>
      <p>Загрузка данных...</p>
    </div>

    <!-- Контент дашборда -->
    <div v-else>
      <!-- Общая статистика -->
      <div class="row g-4 mb-5">
        <div class="col-md-4">
          <div class="stat-card shadow-sm">
            <h3>{{ stats.projects.created }}</h3>
            <p>Созданных проектов</p>
            <i class="bi bi-folder stat-icon"></i>
          </div>
        </div>
        <div class="col-md-4">
          <div class="stat-card shadow-sm">
            <h3>{{ stats.tasks.total }}</h3>
            <p>Активных задач</p>
            <i class="bi bi-check-circle stat-icon"></i>
          </div>
        </div>
        <div class="col-md-4">
          <div class="stat-card shadow-sm">
            <h3>{{ stats.tickets.total }}</h3>
            <p>Активных тикетов</p>
            <i class="bi bi-ticket stat-icon"></i>
          </div>
        </div>
      </div>

      <!-- Графики -->
      <div class="row g-4 mb-5">
        <!-- График: Проекты по датам -->
        <div class="col-md-6">
          <div class="chart-card shadow-sm">
            <h4>Создание проектов</h4>
            <VChart
                v-if="isChartDataValid(projectChartData)"
                :option="projectChartData"
                style="height: 300px;"
                autoresize
            />
            <div v-else class="alert alert-secondary">
              Нет данных для графика
            </div>
          </div>
        </div>
        <!-- График: Статусы задач -->
        <div class="col-md-6">
          <div class="chart-card shadow-sm">
            <h4>Статусы задач</h4>
            <VChart
                v-if="isChartDataValid(taskChartData)"
                :option="taskChartData"
                style="height: 300px;"
                autoresize
            />
            <div v-else class="alert alert-secondary">
              Нет данных для графика
            </div>
          </div>
        </div>
        <!-- График: Активность тикетов -->
        <div class="col-md-12">
          <div class="chart-card shadow-sm">
            <h4>Активность тикетов</h4>
            <VChart
                v-if="isChartDataValid(ticketChartData)"
                :option="ticketChartData"
                style="height: 300px;"
                autoresize
            />
            <div v-else class="alert alert-secondary">
              Нет данных для графика
            </div>
          </div>
        </div>
      </div>

      <!-- Уведомления -->
      <div class="notifications-card shadow-sm">
        <h4>Последние уведомления</h4>
        <div v-if="notifications.length === 0" class="alert alert-secondary">
          Нет новых уведомлений
        </div>
        <div
            v-for="notification in notifications.slice(0, 5)"
            :key="notification.id"
            class="alert alert-info mb-2"
        >
          {{ notification.message }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import apiClient from '@/utils/apiClient';
import { mapGetters } from 'vuex';
import { useToast } from 'vue-toastification';
import VChart from 'vue-echarts';
import 'echarts';

export default {
  components: {
    VChart,
  },
  data() {
    return {
      isLoading: true,
      stats: {
        projects: { created: 0, followed: 0 },
        tasks: { total: 0, pending: 0, inProgress: 0, completed: 0 },
        tickets: { total: 0, open: 0, inProgress: 0, completed: 0 },
      },
      notifications: [],
      projects: [],
      tasks: [],
      tickets: [],
    };
  },
  computed: {
    ...mapGetters(['getUserId']),
    projectChartData() {
      try {
        const dates = [...new Set(this.projects.map((p) => new Date(p.createdAt).toLocaleDateString()))];
        const counts = dates.map((date) =>
            this.projects.filter((p) => new Date(p.createdAt).toLocaleDateString() === date).length
        );
        return {
          tooltip: {
            trigger: 'axis',
            backgroundColor: '#2c3e50',
          },
          legend: {
            top: '5%',
            data: ['Создано проектов'],
          },
          xAxis: {
            type: 'category',
            data: dates.length ? dates : ['Нет данных'],
          },
          yAxis: {
            type: 'value',
          },
          series: [
            {
              name: 'Создано проектов',
              type: 'bar',
              data: counts.length ? counts : [0],
              itemStyle: {
                color: '#007bff',
              },
            },
          ],
        };
      } catch (error) {
        console.error('Ошибка в projectChartData:', error);
        return {
          xAxis: { type: 'category', data: ['Нет данных'] },
          yAxis: { type: 'value' },
          series: [{ name: 'Создано проектов', type: 'bar', data: [0], itemStyle: { color: '#007bff' } }],
        };
      }
    },
    taskChartData() {
      try {
        const data = [
          { value: this.stats.tasks.pending || 0, name: 'Ожидают' },
          { value: this.stats.tasks.inProgress || 0, name: 'В работе' },
          { value: this.stats.tasks.completed || 0, name: 'Завершены' },
        ].filter((item) => item.value > 0);
        return {
          tooltip: {
            trigger: 'item',
            backgroundColor: '#2c3e50',
          },
          legend: {
            top: '5%',
            left: 'center',
          },
          series: [
            {
              name: 'Статусы задач',
              type: 'pie',
              radius: '50%',
              data: data.length ? data : [{ value: 0, name: 'Нет данных' }],
              itemStyle: {
                color: (params) => ['#6c757d', '#ffc107', '#28a745'][params.dataIndex] || '#6c757d',
              },
            },
          ],
        };
      } catch (error) {
        console.error('Ошибка в taskChartData:', error);
        return {
          series: [
            {
              name: 'Статусы задач',
              type: 'pie',
              radius: '50%',
              data: [{ value: 0, name: 'Нет данных' }],
              itemStyle: { color: '#6c757d' },
            },
          ],
        };
      }
    },
    ticketChartData() {
      try {
        const dates = [...new Set(this.tickets.map((t) => new Date(t.createdAt).toLocaleDateString()))];
        const createdCounts = dates.map((date) =>
            this.tickets.filter((t) => new Date(t.createdAt).toLocaleDateString() === date && t.status === 0).length
        );
        const acceptedCounts = dates.map((date) =>
            this.tickets.filter((t) => new Date(t.createdAt).toLocaleDateString() === date && t.status === 1).length
        );
        return {
          tooltip: {
            trigger: 'axis',
            backgroundColor: '#2c3e50',
          },
          legend: {
            top: '5%',
            data: ['Создано тикетов', 'Принято тикетов'],
          },
          xAxis: {
            type: 'category',
            data: dates.length ? dates : ['Нет данных'],
          },
          yAxis: {
            type: 'value',
          },
          series: [
            {
              name: 'Создано тикетов',
              type: 'line',
              data: createdCounts.length ? createdCounts : [0],
              itemStyle: {
                color: '#007bff',
              },
            },
            {
              name: 'Принято тикетов',
              type: 'line',
              data: acceptedCounts.length ? acceptedCounts : [0],
              itemStyle: {
                color: '#ffc107',
              },
            },
          ],
        };
      } catch (error) {
        console.error('Ошибка в ticketChartData:', error);
        return {
          xAxis: { type: 'category', data: ['Нет данных'] },
          yAxis: { type: 'value' },
          series: [{ name: 'Активность тикетов', type: 'line', data: [0], itemStyle: { color: '#007bff' } }],
        };
      }
    },
  },
  setup() {
    const toast = useToast();
    return { toast };
  },
  created() {
    console.log('User ID:', this.getUserId);
    if (this.getUserId && !isNaN(this.getUserId)) {
      this.fetchData();
    } else {
      this.toast.error('Пользователь не аутентифицирован');
      this.isLoading = false;
    }
  },
  methods: {
    formatNotification(rawMessage) {
      // Пример: "Обновлена задача [TaskForm(...)]"
      try {
        // Ищем JSON-подобный фрагмент в квадратных скобках
        const match = rawMessage.match(/\[TaskForm\((.*)\)\]/);
        if (!match) return rawMessage; // если формат не совпал — возвращаем как есть

        const content = match[1]; // всё внутри скобок

        // Разбиваем на поля, например: "taskId=1, titleTask=Разработка главной страницы, ..."
        const fieldsArray = content.split(/,(?=\s*\w+=)/); // разделяем по ", ", но только перед key=value

        // Собираем объект из пар key=value
        const task = {};
        fieldsArray.forEach(field => {
          const [key, ...rest] = field.split('=');
          const value = rest.join('=').trim();

          // Убираем кавычки, если есть
          task[key.trim()] = value.replace(/^"|"$/g, '').trim();
        });

        // Форматируем поля
        const title = task.titleTask || 'Без названия';
        const description = task.descriptionTask || '';
        const status = task.status === 'true' || task.status === true ? 'Завершена' : 'В работе';

        // Преобразуем дату обновления к читаемому виду
        let updatedAt = task.updatedAt || '';
        if (updatedAt) {
          // Попробуем распарсить и отформатировать дату
          let date = new Date(updatedAt);
          if (!isNaN(date)) {
            updatedAt = date.toLocaleDateString('ru-RU', { day: '2-digit', month: 'long', year: 'numeric' }) + ', ' + date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
          }
        }

        // Вытаскиваем комментарий из comments
        let commentsText = '';
        if (task.comments) {
          const commentMatch = task.comments.match(/TaskCommentForm\(.*description=(.*?),/);
          if (commentMatch) {
            commentsText = commentMatch[1];
          } else {
            // Более общий вариант — просто берем всё после description= до закрывающей скобки
            const descMatch = task.comments.match(/description=([^,^\)]+)/);
            if (descMatch) commentsText = descMatch[1];
          }
        }

        // Собираем финальный текст
        let result = `Обновлена задача: "${title}"\n`;
        if (description) result += `Описание: ${description}\n`;
        result += `Статус: ${status}\n`;
        if (updatedAt) result += `Обновлено: ${updatedAt}\n`;
        if (commentsText) result += `Комментарии: ${commentsText}`;

        return result.trim();

      } catch (e) {
        // если что-то пошло не так, возвращаем исходное сообщение
        return rawMessage;
      }
    },

    isChartDataValid(chartData) {
      return (
          chartData &&
          chartData.series &&
          Array.isArray(chartData.series) &&
          chartData.series.every(
              (series) =>
                  series.data &&
                  (Array.isArray(series.data) || // Для bar и line
                      (series.type === 'pie' && Array.isArray(series.data) && series.data.every((item) => item.value !== undefined))) // Для pie
          ) &&
          (chartData.xAxis ? Array.isArray(chartData.xAxis.data) : true) // Проверка xAxis для bar и line
      );
    },
    async fetchData() {
      this.isLoading = true;
      try {
        await Promise.all([
          this.fetchProjects(),
          this.fetchTasks(),
          this.fetchTickets(),
          this.fetchNotifications(),
        ]);
        console.log('Данные загружены:', {
          projects: this.projects,
          tasks: this.tasks,
          tickets: this.tickets,
          notifications: this.notifications,
        });
      } catch (error) {
        this.toast.error('Ошибка при загрузке данных: ' + error.message);
      } finally {
        this.isLoading = false;
      }
    },
    async fetchProjects() {
      try {
        const [createdRes, followedRes] = await Promise.all([
          apiClient.get(`/project/list/by/${this.getUserId}`),
          apiClient.get(`/project/follow/${this.getUserId}`),
        ]);
        this.projects = createdRes.data || [];
        this.stats.projects.created = createdRes.data?.length || 0;
        this.stats.projects.followed = followedRes.data?.length || 0;
      } catch (error) {
        console.error('Ошибка при получении проектов:', error);
        this.toast.error('Не удалось загрузить проекты');
        this.projects = [];
        this.stats.projects = { created: 0, followed: 0 };
      }
    },
    async fetchTasks() {
      try {
        const response = await apiClient.get(`/task/list/by/${this.getUserId}`);
        this.tasks = response.data || [];
        this.stats.tasks.total = this.tasks.length || 0;
        this.stats.tasks.pending = this.tasks.filter((t) => !t.acceptedForExecutionByUser && !t.status).length || 0;
        this.stats.tasks.inProgress = this.tasks.filter((t) => t.acceptedForExecutionByUser && !t.status).length || 0;
        this.stats.tasks.completed = this.tasks.filter((t) => t.status).length || 0;
      } catch (error) {
        console.error('Ошибка при получении задач:', error);
        this.toast.error('Не удалось загрузить задачи');
        this.tasks = [];
        this.stats.tasks = { total: 0, pending: 0, inProgress: 0, completed: 0 };
      }
    },
    async fetchTickets() {
      try {
        const [myRes, acceptedRes, completedRes] = await Promise.all([
          apiClient.get(`/tickets/my/${this.getUserId}`),
          apiClient.get(`/tickets/my/accepted/${this.getUserId}`),
          apiClient.get(`/tickets/completed`),
        ]);
        this.tickets = [...(myRes.data || []), ...(acceptedRes.data || [])];
        this.stats.tickets.total = this.tickets.length || 0;
        this.stats.tickets.open = this.tickets.filter((t) => t.status === 0).length || 0;
        this.stats.tickets.inProgress = this.tickets.filter((t) => t.status === 1).length || 0;
        this.stats.tickets.completed = (completedRes.data || []).length || 0;
      } catch (error) {
        console.error('Ошибка при получении тикетов:', error);
        this.toast.error('Не удалось загрузить тикеты');
        this.tickets = [];
        this.stats.tickets = { total: 0, open: 0, inProgress: 0, completed: 0 };
      }
    },
    async fetchNotifications() {
      const token = localStorage.getItem('token');

      try {
        const response = await fetch(`http://localhost:8085/api/test/${this.getUserId}`, {
          method: 'GET',
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        });

        if (!response.ok) {
          throw new Error(`Ошибка HTTP: ${response.status}`);
        }

        const text = await response.text();

        if (!text) {
          // Сервер вернул пустой ответ
          this.notifications = [];
          return;
        }

        const data = JSON.parse(text);

        this.notifications = data.map((notification) => ({
          ...notification,
          message: this.formatNotification(notification.message),
          progress: 100,
        }));

      } catch (error) {
        console.error('Ошибка при получении уведомлений:', error);
        this.toast?.error?.('Не удалось загрузить уведомления');
        this.notifications = [];
      }
    }

  },
};
</script>

<style scoped>
.dashboard-container {
  padding: 2rem;
  background: linear-gradient(135deg, #f5f6fa 0%, #e9ecef 100%);
  min-height: 100vh;
}

.dashboard-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 0.5rem;
  text-align: center;
}

.dashboard-subtitle {
  font-size: 1.2rem;
  color: #6c757d;
  margin-bottom: 2rem;
  text-align: center;
}

.stat-card {
  background: #ffffff;
  border-radius: 15px;
  padding: 1.5rem;
  text-align: center;
  position: relative;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.stat-card h3 {
  font-size: 2rem;
  color: #007bff;
  margin-bottom: 0.5rem;
}

.stat-card p {
  font-size: 1rem;
  color: #6c757d;
}

.stat-icon {
  position: absolute;
  top: 1rem;
  right: 1rem;
  font-size: 2rem;
  color: #007bff;
  opacity: 0.2;
}

.chart-card {
  background: #ffffff;
  border-radius: 15px;
  padding: 1.5rem;
  height: 400px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.chart-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.chart-card h4 {
  font-size: 1.25rem;
  color: #2c3e50;
  margin-bottom: 1rem;
}

.notifications-card {
  background: #ffffff;
  border-radius: 15px;
  padding: 1.5rem;
  margin-top: 2rem;
}

.notifications-card h4 {
  font-size: 1.25rem;
  color: #2c3e50;
  margin-bottom: 1rem;
}

.alert {
  border-radius: 8px;
  font-size: 0.9rem;
  padding: 0.75rem;
}

.alert-info {
  background-color: #e7f3ff;
  border-color: #b8daff;
  color: #004085;
}

.alert-secondary {
  background-color: #f8f9fa;
  border-color: #dee2e6;
  color: #6c757d;
}

@media (max-width: 768px) {
  .dashboard-title {
    font-size: 2rem;
  }

  .stat-card h3 {
    font-size: 1.5rem;
  }

  .chart-card {
    height: 300px;
  }
}
</style>