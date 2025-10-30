<template>
  <div class="card project-card mb-4 shadow-sm">
    <div class="card-body">
      <h5 class="card-title text-primary">{{ project.title }}</h5>
      <p class="card-text text-muted">{{ project.descrption }}</p>
      <div class="d-flex justify-content-between align-items-center">
        <router-link
            :to="`/projects/${project.projectId}`"
            class="btn btn-primary btn-sm project-btn"
        >
          <i class="bi bi-eye me-1"></i> Детали проекта
        </router-link>
        <button
            class="btn btn-sm follow-btn"
            :class="{
              'btn-outline-primary': !project.isFollowed,
              'btn-success': project.isFollowed
            }"
            @click="toggleFollow"
        >
          <i
              :class="project.isFollowed ? 'bi bi-person-dash' : 'bi bi-person-plus'"
              class="me-1"
          ></i>
          {{ project.isFollowed ? 'Отписаться' : 'Подписаться' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import apiClient from '@/utils/apiClient';
import { mapState } from 'vuex';

export default {
  props: {
    project: {
      type: Object,
      required: true
    }
  },
  computed: {
    ...mapState(['userId'])
  },
  methods: {
    async toggleFollow() {
      const action = this.project.isFollowed ? 'unfollow' : 'follow';
      const projectId = this.project.projectId;

      try {
        const response = await apiClient.get(`/project/${action}/${projectId}/${this.userId}`);
        if (response.data === true) {
          this.$emit('followToggled', projectId);
        } else {
          throw new Error('Не удалось изменить статус подписки');
        }
      } catch (error) {
        console.error(`Ошибка при ${action} проекта:`, error);
        alert(`Не удалось ${action === 'follow' ? 'подписаться на' : 'отписаться от'} проект. Попробуйте позже.`);
      }
    }
  }
};
</script>

<style scoped>
.project-card {
  height: 100%;
  border: none;
  border-radius: 15px;
  transition: all 0.3s ease;
  background: #ffffff;
  overflow: hidden;
}

.project-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.15) !important;
}

.card-body {
  padding: 1.5rem;
}

.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: #1a3c6d;
}

.card-text {
  font-size: 0.95rem;
  line-height: 1.5;
  margin-bottom: 1.5rem;
  color: #6c757d;
}

.project-btn {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-weight: 500;
  background-color: #1a3c6d;
  border: none;
  transition: all 0.3s ease;
}

.project-btn:hover {
  background-color: #153258;
  transform: scale(1.05);
}

.follow-btn {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.follow-btn.btn-outline-primary {
  border-color: #17a2b8;
  color: #17a2b8;
}

.follow-btn.btn-outline-primary:hover {
  background-color: #17a2b8;
  color: white;
  transform: scale(1.05);
}

.follow-btn.btn-success {
  background-color: #28a745;
  border: none;
  color: white;
}

.follow-btn.btn-success:hover {
  background-color: #218838;
  transform: scale(1.05);
}
</style>