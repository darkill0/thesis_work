<template>
  <div class="container py-5">
    <h1 class="display-4 text-primary mb-5 animate__animated animate__fadeInDown">
      Все проекты
    </h1>
    <!-- Поиск -->
    <div class="mb-4">
      <div class="input-group">
        <span class="input-group-text bg-primary text-white">
          <i class="bi bi-search"></i>
        </span>
        <input
            type="text"
            class="form-control"
            placeholder="Поиск по названию проекта"
            v-model="searchQuery"
        />
      </div>
    </div>
    <!-- Список проектов -->
    <div class="row g-4">
      <div
          class="col-lg-4 col-md-6 animate__animated animate__fadeInUp"
          v-for="project in filteredProjects"
          :key="project.projectId"
      >
        <ProjectCard :project="project" @followToggled="updateFollowStatus" />
      </div>
      <div v-if="!filteredProjects.length" class="col-12 text-center text-muted">
        <p class="fs-5">Проекты не найдены.</p>
      </div>
    </div>
    <!-- Пагинация -->
    <nav class="mt-5" v-if="filteredProjects.length">
      <ul class="pagination justify-content-center">
        <li class="page-item" :class="{ disabled: currentPage === 1 }">
          <button class="page-link" @click="currentPage--">
            <i class="bi bi-chevron-left"></i>
          </button>
        </li>
        <li
            class="page-item"
            v-for="page in totalPages"
            :key="page"
            :class="{ active: currentPage === page }"
        >
          <button class="page-link" @click="currentPage = page">{{ page }}</button>
        </li>
        <li class="page-item" :class="{ disabled: currentPage === totalPages }">
          <button class="page-link" @click="currentPage++">
            <i class="bi bi-chevron-right"></i>
          </button>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script>
import apiClient from '@/utils/apiClient';
import ProjectCard from '@/components/project/ProjectCard.vue';
import { mapState } from 'vuex';

export default {
  components: {
    ProjectCard
  },
  data() {
    return {
      projects: [],
      searchQuery: '',
      currentPage: 1,
      projectsPerPage: 9 // 3 ряда по 3 карточки
    };
  },
  computed: {
    ...mapState(['userId']),
    filteredProjects() {
      const query = this.searchQuery.toLowerCase().trim();
      const filtered = query
          ? this.projects.filter(project =>
              project.title.toLowerCase().includes(query)
          )
          : this.projects;
      const start = (this.currentPage - 1) * this.projectsPerPage;
      return filtered.slice(start, start + this.projectsPerPage);
    },
    totalPages() {
      const filtered = this.searchQuery.toLowerCase().trim()
          ? this.projects.filter(project =>
              project.title.toLowerCase().includes(this.searchQuery.toLowerCase())
          )
          : this.projects;
      return Math.ceil(filtered.length / this.projectsPerPage);
    }
  },
  async created() {
    await this.fetchProjects();
  },
  methods: {
    async fetchProjects() {
      try {
        // Получаем все проекты
        const projectsResponse = await apiClient.get('/project/list');
        // Получаем проекты, на которые подписан пользователь
        const followedResponse = await apiClient.get(`/project/follow/${this.userId}`);
        const followedProjectIds = followedResponse.data.map(p => p.projectId);
        // Объединяем данные
        this.projects = projectsResponse.data.map(project => ({
          ...project,
          isFollowed: followedProjectIds.includes(project.projectId)
        }));
      } catch (error) {
        console.error('Ошибка при получении проектов:', error);
        alert('Не удалось загрузить проекты. Попробуйте позже.');
      }
    },
    updateFollowStatus(projectId) {
      const project = this.projects.find(p => p.projectId === projectId);
      if (project) {
        project.isFollowed = !project.isFollowed;
      }
    }
  }
};
</script>

<style scoped>
.container {
  max-width: 1200px;
  margin-top: 40px;
}

h1 {
  font-weight: 700;
  color: #1a3c6d;
  text-align: center;
}

.input-group {
  max-width: 500px;
  margin: 0 auto;
}

.input-group-text {
  border: none;
  border-radius: 8px 0 0 8px;
}

.form-control {
  border: 1px solid #ced4da;
  border-radius: 0 8px 8px 0;
  padding: 10px;
  transition: border-color 0.2s ease;
}

.form-control:focus {
  border-color: #1a3c6d;
  box-shadow: 0 0 5px rgba(26, 60, 109, 0.3);
}

.pagination .page-link {
  color: #1a3c6d;
  border: none;
  margin: 0 5px;
  border-radius: 8px;
  transition: background-color 0.2s ease;
}

.pagination .page-link:hover {
  background-color: #e9ecef;
}

.pagination .page-item.active .page-link {
  background-color: #1a3c6d;
  color: white;
}

.pagination .page-item.disabled .page-link {
  color: #6c757d;
}
</style>