<template>
  <div class="container">
    <h2>Task Manager</h2>
    <div class="row">
      <div class="col-md-3">
        <div class="sidebar">
          <h5>Управление задачами</h5>
          <button @click="sortTasks('title')" class="btn btn-primary w-100 mb-2">Сортировать по названию</button>
          <button @click="sortTasks('createdAt')" class="btn btn-secondary w-100 mb-2">Сортировать по дате создания</button>
          <button @click="printReport" class="btn btn-info w-100">Печать отчета</button>
        </div>
      </div>
      <div class="col-md-9">
        <div class="controls mb-3">
          <input
              type="text"
              class="form-control"
              v-model="searchQuery"
              placeholder="Поиск по названию задачи"
          />
        </div>

        <table class="table table-bordered">
          <thead>
          <tr>
            <th>Название</th>
            <th>Описание</th>
            <th>Статус</th>
            <th>Дата создания</th>
            <th>Действия</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="task in filteredTasks" :key="task.taskId">
            <td>{{ task.titleTask }}</td>
            <td>{{ task.descriptionTask }}</td>
            <td>{{ task.status ? 'Завершено' : 'В процессе' }}</td>
            <td>{{ new Date(task.createdAt).toLocaleDateString() }}</td>
            <td>
              <button @click="openEditModal(task)" class="btn btn-warning" data-bs-toggle="modal" data-bs-target="#editTaskModal">Редактировать</button>
              <button @click="deleteTask(task.taskId)" class="btn btn-danger">Удалить</button>
            </td>
          </tr>
          </tbody>
        </table>

        <!-- Модальное окно для редактирования задачи -->
        <div class="modal fade" id="editTaskModal" tabindex="-1" aria-labelledby="editTaskModalLabel" aria-hidden="true">
          <div class="modal-dialog">
            <div class="modal-content">
              <div class="modal-header">
                <h5 class="modal-title" id="editTaskModalLabel">Редактирование задачи</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Закрыть"></button>
              </div>
              <div class="modal-body">
                <form @submit.prevent="updateTask">
                  <div class="mb-3">
                    <label for="taskTitle" class="form-label">Название задачи</label>
                    <input type="text" class="form-control" id="taskTitle" v-model="currentTask.titleTask" required />
                  </div>
                  <div class="mb-3">
                    <label for="taskDescription" class="form-label">Описание задачи</label>
                    <textarea class="form-control" id="taskDescription" v-model="currentTask.descriptionTask" required></textarea>
                  </div>
                  <div class="mb-3">
                    <label for="taskStatus" class="form-label">Статус</label>
                    <select class="form-select" id="taskStatus" v-model="currentTask.status">
                      <option :value="true">Завершено</option>
                      <option :value="false">В процессе</option>
                    </select>
                  </div>
                  <button type="submit" class="btn btn-primary">Сохранить изменения</button>
                </form>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import apiClient from '@/utils/apiClient';

export default {
  data() {
    return {
      tasks: [],
      searchQuery: '',
      sortBy: '',
      currentTask: {
        taskId: null,
        titleTask: '',
        descriptionTask: '',
        status: false,
      },
    };
  },
  computed: {
    filteredTasks() {
      let filtered = this.tasks.filter(task =>
          task.titleTask.toLowerCase().includes(this.searchQuery.toLowerCase())
      );
      if (this.sortBy) {
        filtered.sort((a, b) => {
          if (this.sortBy === 'title') {
            return a.titleTask.localeCompare(b.titleTask);
          } else if (this.sortBy === 'createdAt') {
            return new Date(b.createdAt) - new Date(a.createdAt);
          }
          return 0;
        });
      }
      return filtered;
    },
  },
  methods: {
    async fetchTasks() {
      const response = await apiClient.get('/task/list');
      this.tasks = response.data;
    },
    sortTasks(criteria) {
      this.sortBy = criteria;
    },
    openEditModal(task) {
      this.currentTask = { ...task }; // Копируем данные задачи для редактирования
    },
    async updateTask() {
      try {
        await apiClient.post(`/task/update/${this.currentTask.taskId}`, this.currentTask);
        await this.fetchTasks(); // Обновляем список задач
        const modal = bootstrap.Modal.getInstance(document.getElementById('editTaskModal'));
        modal.hide(); // Закрываем модальное окно
      } catch (error) {
        console.error("Ошибка при обновлении задачи:", error);
      }
    },
    async deleteTask(taskId) {
      await apiClient.delete(`/task/delete/${taskId}`);
      await this.fetchTasks(); // Обновляем список задач
    },
    printReport() {
      const reportWindow = window.open('', '_blank');
      reportWindow.document.write('<html><head><title>Отчет</title></head><body>');
      reportWindow.document.write('<h2>Отчет по задачам</h2>');
      reportWindow.document.write(this.generateReportTable());
      reportWindow.document.write('</body></html>');
      reportWindow.document.close();
      reportWindow.print();
    },
    generateReportTable() {
      let tableHtml = '<table border="1" style="width:100%; border-collapse:collapse;"><thead><tr><th>Название</th><th>Описание</th><th>Статус</th><th>Дата создания</th></tr></thead><tbody>';
      this.tasks.forEach(task => {
        tableHtml += `<tr>
                        <td>${task.titleTask}</td>
                        <td>${task.descriptionTask}</td>
                        <td>${task.status ? 'Завершено' : 'В процессе'}</td>
                        <td>${new Date(task.createdAt).toLocaleDateString()}</td>
                      </tr>`;
      });
      tableHtml += '</tbody></table>';
      return tableHtml;
    },
  },
  created() {
    this.fetchTasks();
  },
};
</script>

<style scoped>
.sidebar {
  background-color: #f8f9fa; /* Цвет фона боковой панели */
  padding: 15px;
  border-right: 1px solid #dee2e6; /* Граница правой стороны */
  height: 100%; /* Высота панели */
}

.sidebar h5 {
  margin-bottom: 15px; /* Отступ для заголовка */
}

.sidebar .btn {
  width: 100%; /* Ширина кнопок на всю панель */
  margin-bottom: 10px; /* Отступ между кнопками */
}

.table {
  margin-top: 20px;
}
</style>
