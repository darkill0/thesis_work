<template>
  <div class="container">
    <h1>Администрирование проектов</h1>

    <!-- Поиск по названию проекта -->
    <div class="mb-3">
      <input type="text" v-model="searchQuery" class="form-control" placeholder="Поиск проекта" @input="fetchProjects" />
    </div>

    <!-- Кнопки сортировки -->
    <div class="d-flex justify-content-end mb-3">
      <button class="btn btn-outline-secondary me-2" @click="sortProjects('title')">Сортировать по названию</button>
      <button class="btn btn-outline-secondary me-2" @click="sortProjects('status')">Сортировать по статусу</button>
      <button class="btn btn-outline-secondary" @click="sortProjects('completedTasks')">Сортировать по завершённым задачам</button>
    </div>

    <!-- Таблица проектов -->
    <table class="table table-striped">
      <thead>
      <tr>
        <th>ID</th>
        <th>Название</th>
        <th>Статус</th>
        <th>Завершение</th>
        <th>Действия</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="project in filteredProjects" :key="project.projectId">
        <td>{{ project.projectId }}</td>
        <td>{{ project.title }}</td>
        <td>{{ project.status || 'Не указан' }}</td>
        <td>{{ project.completedTasks }}/{{ project.totalTasks }}</td>
        <td>
          <button class="btn btn-warning btn-sm me-2" @click="openEditProjectModal(project)">Редактировать</button>
          <button class="btn btn-danger btn-sm" @click="deleteProject(project.projectId)">Удалить</button>
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

    <!-- Модальное окно редактирования проекта -->
    <div class="modal fade" id="editProjectModal" tabindex="-1" role="dialog" aria-labelledby="editProjectModalLabel" aria-hidden="true">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="editProjectModalLabel">Редактировать проект</h5>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close" @click="resetCurrentProject">
              <span aria-hidden="true">×</span>
            </button>
          </div>
          <form @submit.prevent="updateProject">
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label">Название</label>
                <input type="text" class="form-control" v-model="currentProject.title" required />
              </div>
              <div class="mb-3">
                <label class="form-label">Описание</label>
                <textarea class="form-control" v-model="currentProject.descrption"></textarea>
              </div>
              <div class="mb-3">
                <label class="form-label">URL проекта</label>
                <input type="text" class="form-control" v-model="currentProject.urlToProject" />
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-dismiss="modal" @click="resetCurrentProject">Закрыть</button>
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
      projects: [],
      searchQuery: "",
      sortKey: "",
      sortOrder: 1, // 1 для asc, -1 для desc
      currentPage: 1,
      perPage: 5,
      totalPages: 0,
      currentProject: {},
    };
  },
  computed: {
    filteredProjects() {
      // Копия проектов с вычисленными completedTasks и totalTasks
      let filtered = this.projects.map(project => {
        const { completedTasks, totalTasks } = this.calculateTaskCounts(project);
        console.log(`Project ${project.projectId}: completed=${completedTasks}, total=${totalTasks}`);
        return {
          ...project,
          completedTasks,
          totalTasks,
        };
      });

      // Фильтрация по поисковому запросу
      if (this.searchQuery) {
        filtered = filtered.filter(project =>
            project.title.toLowerCase().includes(this.searchQuery.toLowerCase())
        );
      }

      // Сортировка
      if (this.sortKey) {
        filtered.sort((a, b) => {
          let valueA = a[this.sortKey];
          let valueB = b[this.sortKey];

          // Обработка числовых значений для completedTasks
          if (this.sortKey === "completedTasks") {
            valueA = valueA || 0;
            valueB = valueB || 0;
            return (valueA - valueB) * this.sortOrder;
          }

          // Для строк (title, status)
          if (!valueA) valueA = "";
          if (!valueB) valueB = "";
          return valueA.localeCompare(valueB) * this.sortOrder;
        });
      }

      // Пагинация
      this.totalPages = Math.ceil(filtered.length / this.perPage);
      const start = (this.currentPage - 1) * this.perPage;
      const end = start + this.perPage;
      return filtered.slice(start, end);
    },
  },
  methods: {
    fetchProjects() {
      apiClient
          .get("/project/list")
          .then(response => {
            this.projects = response.data || [];
            console.log("Fetched projects:", this.projects);
          })
          .catch(error => {
            console.error("Ошибка при получении списка проектов:", error);
          });
    },
    calculateTaskCounts(project) {
      if (!project.tasks || !Array.isArray(project.tasks)) {
        return { completedTasks: 0, totalTasks: 0 };
      }
      const totalTasks = project.tasks.length;
      const completedTasks = project.tasks.filter(task => task.status === true).length;
      return { completedTasks, totalTasks };
    },
    sortProjects(key) {
      if (this.sortKey === key) {
        this.sortOrder *= -1; // Переключение порядка
      } else {
        this.sortKey = key;
        this.sortOrder = 1;
      }
      this.currentPage = 1;
      console.log(`Sorting by ${key}, order: ${this.sortOrder}`);
    },
    changePage(page) {
      if (page > 0 && page <= this.totalPages) {
        this.currentPage = page;
      }
    },
    openEditProjectModal(project) {
      this.currentProject = {
        projectId: project.projectId,
        title: project.title,
        descrption: project.descrption || "",
        urlToProject: project.urlToProject || "",
        createdByUser: project.createdByUser,
      };
      const modalElement = document.getElementById("editProjectModal");
      this.editProjectModalInstance = new window.bootstrap.Modal(modalElement);
      this.editProjectModalInstance.show();
    },
    updateProject() {
      apiClient
          .post(`/project/update/${this.currentProject.projectId}`, this.currentProject)
          .then(() => {
            this.fetchProjects();
            this.editProjectModalInstance.hide();
            this.resetCurrentProject();
          })
          .catch(error => {
            console.error("Ошибка при обновлении проекта:", error);
          });
    },
    deleteProject(id) {
      apiClient
          .delete(`/project/delete/${id}`)
          .then(() => {
            this.fetchProjects();
          })
          .catch(error => {
            console.error("Ошибка при удалении проекта:", error);
          });
    },
    resetCurrentProject() {
      this.currentProject = {};
      if (this.editProjectModalInstance) {
        this.editProjectModalInstance.hide();
      }
    },
  },
  created() {
    this.fetchProjects();
  },
};
</script>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  margin-bottom: 20px;
  color: #1a3c6d;
}

.table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.table thead {
  background-color: #1a3c6d;
  color: white;
}

.table th,
.table td {
  vertical-align: middle;
}

.btn-outline-secondary {
  border-radius: 8px;
  transition: background-color 0.2s ease;
}

.btn-outline-secondary:hover {
  background-color: #1a3c6d;
  color: white;
}

.btn-warning,
.btn-danger {
  border-radius: 8px;
  padding: 5px 10px;
}

.pagination .page-link {
  border-radius: 5px;
  margin: 0 5px;
  color: #1a3c6d;
}

.pagination .page-item.active .page-link {
  background-color: #1a3c6d;
  border-color: #1a3c6d;
  color: white;
}

.modal-content {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-header {
  background-color: #1a3c6d;
  color: white;
  border-bottom: none;
}

.modal-footer {
  border-top: none;
}

.form-control,
.form-select {
  border-radius: 8px;
  border: 1px solid #ced4da;
}

.form-control:focus,
.form-select:focus {
  border-color: #1a3c6d;
  box-shadow: 0 0 5px rgba(26, 60, 109, 0.3);
}
</style>