<template>
  <div class="container">
    <h1>Администрирование задач</h1>

    <!-- Поиск по названию задачи -->
    <div class="mb-3">
      <input type="text" v-model="searchQuery" class="form-control" placeholder="Поиск задачи" @input="fetchTasks" />
    </div>

    <!-- Кнопки сортировки -->
    <div class="d-flex justify-content-end mb-3">
      <button class="btn btn-outline-secondary me-2" @click="sortTasks('title')">Сортировать по названию</button>
      <button class="btn btn-outline-secondary me-2" @click="sortTasks('status')">Сортировать по статусу</button>
      <button class="btn btn-outline-secondary" @click="sortTasks('priority')">Сортировать по приоритету</button>
    </div>

    <!-- Таблица задач -->
    <table class="table table-striped">
      <thead>
      <tr>
        <th>ID</th>
        <th>Название</th>
        <th>Статус</th>
        <th>Приоритет</th>
        <th>Проект</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="task in filteredTasks" :key="task.id">
        <td>{{ task.taskId }}</td>
        <td>{{ task.titleTask }}</td>
        <td>{{ task.status }}</td>
        <td>{{ task.project }}</td>
        <td>
          <button class="btn btn-warning btn-sm me-2" @click="openEditTaskModal(task)">Редактировать</button>
          <button class="btn btn-danger btn-sm" @click="deleteTask(task.id)">Удалить</button>
        </td>
      </tr>
      </tbody>
    </table>

    <!-- Пагинация -->
    <nav>
      <ul class="pagination justify-content-center">
        <li class="page-item" :class="{ disabled: currentPage === 1 }">
          <button class="page-link" @click="changePage(currentPage - 1)">Предыдущая</button>
        </li>
        <li class="page-item" v-for="page in totalPages" :key="page" :class="{ active: currentPage === page }">
          <button class="page-link" @click="changePage(page)">{{ page }}</button>
        </li>
        <li class="page-item" :class="{ disabled: currentPage === totalPages }">
          <button class="page-link" @click="changePage(currentPage + 1)">Следующая</button>
        </li>
      </ul>
    </nav>

    <!-- Модальное окно редактирования задачи -->
    <div class="modal fade" id="editTaskModal" tabindex="-1" role="dialog" aria-labelledby="editTaskModalLabel" aria-hidden="true">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="editTaskModalLabel">Редактировать задачу</h5>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close" @click="resetCurrentTask">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <form @submit.prevent="updateTask">
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label">Название</label>
                <input type="text" class="form-control" v-model="currentTask.titleTask" required />
              </div>
              <div class="mb-3">
                <label class="form-label">Статус</label>
                <input type="text" class="form-control" v-model="currentTask.status" required />
              </div>
              <div class="mb-3">
                <label class="form-label">Приоритет</label>
                <input type="number" class="form-control" v-model="currentTask.project" required />
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-dismiss="modal" @click="resetCurrentTask">Закрыть</button>
              <button type="submit" class="btn btn-primary">Сохранить</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import apiClient from "@/utils/apiClient";

export default {
  data() {
    return {
      tasks: [],
      searchQuery: "",
      sortKey: "",
      currentPage: 1,
      perPage: 5,
      totalPages: 0,
      currentTask: {}
    };
  },
  computed: {
    filteredTasks() {
      let filtered = this.tasks;
      if (this.searchQuery) {
        filtered = filtered.filter(task => task.title.toLowerCase().includes(this.searchQuery.toLowerCase()));
      }
      if (this.sortKey) {
        filtered = filtered.sort((a, b) => (a[this.sortKey] > b[this.sortKey] ? 1 : -1));
      }
      this.totalPages = Math.ceil(filtered.length / this.perPage);
      const start = (this.currentPage - 1) * this.perPage;
      const end = start + this.perPage;
      return filtered.slice(start, end);
    }
  },
  methods: {
    fetchTasks() {
      apiClient.get("/task/list")
          .then(response => {
            this.tasks = response.data;
          })
          .catch(error => {
            console.error("Ошибка при получении списка задач:", error);
          });
    },
    sortTasks(key) {
      this.sortKey = key;
      this.currentPage = 1;
    },
    changePage(page) {
      if (page > 0 && page <= this.totalPages) {
        this.currentPage = page;
      }
    },
    openEditTaskModal(task) {
      this.currentTask = { ...task };
      const modalElement = document.getElementById("editTaskModal");
      this.editTaskModalInstance = new window.bootstrap.Modal(modalElement);
      this.editTaskModalInstance.show();
    },
    updateTask() {
      apiClient.put(`/task/update/${this.currentTask.id}`, this.currentTask)
          .then(() => {
            this.fetchTasks();
            this.editTaskModalInstance.hide();
            this.resetCurrentTask();
          })
          .catch(error => {
            console.error("Ошибка при обновлении задачи:", error);
          });
    },
    deleteTask(id) {
      apiClient.delete(`/task/delete/${id}`)
          .then(() => {
            this.fetchTasks();
          })
          .catch(error => {
            console.error("Ошибка при удалении задачи:", error);
          });
    },
    resetCurrentTask() {
      this.currentTask = {};
      this.editTaskModalInstance.hide();

    }
  },
  created() {
    this.fetchTasks();
  }
};
</script>

<style scoped>
.progress {
  height: 20px;
}
</style>
