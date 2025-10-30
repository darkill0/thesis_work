<template>
  <div class="projects-manager">
    <div class="sidebar">
      <h1>Управление проектами</h1>
      <div class="controls mb-3">
        <input
            type="text"
            v-model="searchQuery"
            class="form-control"
            placeholder="Поиск..."
            @input="filterProjects"
        />
        <button @click="openAddProjectModal" class="btn btn-primary mt-2">Добавить проект</button>
        <button @click="printReport" class="btn btn-secondary mt-2">Печать отчета</button>
        <div class="barp">
          <button @click="filterByStatus('active')" class="btn btn-info mt-2">Активные</button>
          <button @click="filterByStatus('completed')" class="btn btn-success mt-2">Завершенные</button>
          <button @click="filterByStatus('all')" class="btn btn-light mt-2">Все</button>
        </div>
        <button @click="drawChart" class="btn btn-info mt-2">Показать график</button>
      </div>
    </div>
    <div class="content">
      <table class="table table-striped">
        <thead>
        <tr>
          <th @click="sortProjects('title')" style="cursor: pointer;">Название</th>
          <th @click="sortProjects('createdAt')" style="cursor: pointer;">Дата создания</th>
          <th>Действия</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="project in filteredProjects" :key="project.projectId">
          <td>{{ project.title }}</td>
          <td>{{ new Date(project.createdAt).toLocaleDateString() }}</td>
          <td>
            <button @click="openEditProjectModal(project)" class="btn btn-warning btn-sm">Редактировать</button>
            <button @click="deleteProject(project.projectId)" class="btn btn-danger btn-sm">Удалить</button>
          </td>
        </tr>
        </tbody>
      </table>

      <div>
        <canvas id="myChart" ref="myChart"></canvas>
      </div>
    </div>
    <!-- Модальное окно для создания и редактирования проекта -->
    <div class="modal fade" id="projectModal" tabindex="-1" aria-labelledby="projectModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="projectModalLabel">{{ modalTitle }}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label for="projectTitle" class="form-label">Название проекта</label>
              <input type="text" v-model="currentProject.title" class="form-control" id="projectTitle" placeholder="Название проекта" />
            </div>
            <div class="mb-3">
              <label for="projectDescription" class="form-label">Описание проекта</label>
              <textarea v-model="currentProject.descrption" class="form-control" id="projectDescription" placeholder="Описание проекта"></textarea>
            </div>
            <div class="mb-3">
              <label for="projectUrl" class="form-label">Ссылка на проект</label>
              <input type="url" v-model="currentProject.urlToProject" class="form-control" id="projectUrl" placeholder="Ссылка на проект" />
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" @click="closeModal">Закрыть</button>
            <button type="button" class="btn btn-primary" @click="saveProject">{{ modalButtonText }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import apiClient from "@/utils/apiClient";
import Chart from 'chart.js/auto';

export default {
  data() {
    return {
      projects: [],
      filteredProjects: [],
      searchQuery: '',
      currentProject: { title: '', descrption: '', urlToProject: '', createdByUser: null },
      showModal: false,
      modalTitle: '',
      modalButtonText: 'Сохранить',
      chart: null,
    };
  },
  created() {
    this.fetchProjects();
  },
  methods: {
    async fetchProjects() {
      try {
        const response = await apiClient.get('/project/list/by/' + this.$store.getters.getUserId);
        this.projects = response.data;
        this.filteredProjects = this.projects;
        this.renderChart();
      } catch (error) {
        console.error('Ошибка при загрузке проектов:', error);
      }
    },
    filterProjects() {
      this.filteredProjects = this.projects.filter(project =>
          project.title.toLowerCase().includes(this.searchQuery.toLowerCase())
      );
    },
    filterByStatus(status) {
      if (status === 'all') {
        this.filteredProjects = this.projects;
      } else {
        this.filteredProjects = this.projects.filter(project => project.status === status);
      }
    },
    sortProjects(field) {
      this.filteredProjects.sort((a, b) => {
        return a[field] > b[field] ? 1 : -1;
      });
    },
    openAddProjectModal() {
      this.currentProject = {
        title: '',
        descrption: '',
        urlToProject: '',
        createdByUser: this.$store.getters.getUserId // Устанавливаем ID пользователя
      };
      this.modalTitle = 'Добавить проект';
      this.modalButtonText = 'Добавить';
      this.showModal = true;
      this.showModalInBootstrap();
    },
    openEditProjectModal(project) {
      this.currentProject = { ...project, createdByUser: this.$store.getters.getUserId };
      this.modalTitle = 'Редактировать проект';
      this.modalButtonText = 'Сохранить изменения';
      this.showModal = true;
      this.showModalInBootstrap();
    },
    async saveProject() {
      if (this.modalButtonText === 'Добавить') {
        await this.createProject();
      } else {
        await this.updateProject(this.currentProject.projectId);
      }
      this.closeModal();
      await this.fetchProjects();
    },
    async createProject() {
      try {
        await apiClient.post('/project/create', {
          title: this.currentProject.title,
          descrption: this.currentProject.descrption,
          urlToProject: this.currentProject.urlToProject,
          createdByUser: this.currentProject.createdByUser
        });
      } catch (error) {
        console.error('Ошибка при создании проекта:', error);
        throw error;
      }
    },
    async updateProject(id) {
      try {
        await apiClient.post('/project/update/' + id, {
          title: this.currentProject.title,
          descrption: this.currentProject.descrption,
          urlToProject: this.currentProject.urlToProject,
          createdByUser: this.currentProject.createdByUser
        });
      } catch (error) {
        console.error('Ошибка при обновлении проекта:', error);
        throw error;
      }
    },
    async deleteProject(id) {
      try {
        await apiClient.delete('/project/delete/' + id);
        await this.fetchProjects();
      } catch (error) {
        console.error('Ошибка при удалении проекта:', error);
      }
    },
    closeModal() {
      this.showModal = false;
      const modal = new bootstrap.Modal(document.getElementById('projectModal'));
      modal.hide();
    },
    printReport() {
      const reportContent = this.filteredProjects.map(project => `${project.title} - ${project.createdAt}`).join('\n');
      const printWindow = window.open('', '_blank');
      printWindow.document.write('<pre>' + reportContent + '</pre>');
      printWindow.document.close();
      printWindow.print();
    },
    showModalInBootstrap() {
      const modal = new bootstrap.Modal(document.getElementById('projectModal'));
      modal.show();
    },
    drawChart() {
      this.renderChart();
    },
    renderChart() {
      const ctx = this.$refs.myChart.getContext('2d');
      const projectStatusCounts = this.projects.reduce((acc, project) => {
        acc[project.status] = (acc[project.status] || 0) + 1;
        return acc;
      }, {});

      const chartData = {
        labels: Object.keys(projectStatusCounts),
        datasets: [{
          label: 'Количество проектов по статусам',
          data: Object.values(projectStatusCounts),
          backgroundColor: ['#ff6384', '#36a2eb', '#ffce56'],
        }]
      };

      if (this.chart) {
        this.chart.destroy();
      }

      this.chart = new Chart(ctx, {
        type: 'bar',
        data: chartData,
        options: {
          responsive: true,
          scales: {
            y: {
              beginAtZero: true
            }
          }
        }
      });
    },
  }
};
</script>

<style scoped>
.projects-manager {
  display: flex;
}

.sidebar {
  width: 250px;
  background-color: #f8f9fa;
  padding: 20px;
  border-right: 1px solid #dee2e6;
}

.content {
  flex: 1;
  padding: 20px;
}

.controls {
  margin-bottom: 20px;
}

.barp {
  display: flex;
  flex-direction: column;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th, .table td {
  padding: 10px;
  text-align: left;
}

.table th {
  cursor: pointer;
}

.btn {
  width: 100%;
}

.btn:not(:last-child) {
  margin-bottom: 10px;
}
</style>