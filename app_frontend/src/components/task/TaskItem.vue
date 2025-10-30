<template>
  <div class="task-row d-flex align-items-center shadow-sm mb-2 p-3 bg-white rounded">
    <div class="task-title flex-grow-1">
      <h6 class="mb-0 text-primary">{{ task.titleTask }}</h6>
      <p class="mb-0 text-muted text-truncate">{{ task.descriptionTask }}</p>
    </div>
    <div class="task-status mx-3">
      <span class="badge" :class="taskStatusClass">
        {{ taskStatusText }}
      </span>
    </div>
    <div class="task-actions d-flex gap-2">
      <button
          class="btn btn-info btn-sm"
          @click="viewDetails"
      >
        Подробнее
      </button>
      <button
          v-if="canAcceptTask"
          class="btn btn-outline-primary btn-sm"
          @click="acceptTask"
      >
        Взять на выполнение
      </button>
      <button
          v-if="task.acceptedForExecutionByUser === userId && !task.status"
          class="btn btn-success btn-sm"
          @click="completeTask"
      >
        Завершить
      </button>
    </div>
  </div>
</template>

<script>
import apiClient from '@/utils/apiClient';
import { mapGetters } from 'vuex';
import { useToast } from 'vue-toastification';

export default {
  props: {
    task: {
      type: Object,
      required: true,
    },
  },
  setup() {
    const toast = useToast();
    return { toast };
  },
  computed: {
    ...mapGetters(['getUserId']),
    userId() {
      return this.getUserId;
    },
    taskStatusText() {
      if (this.task.status) return 'Завершена';
      if (this.task.acceptedForExecutionByUser) return 'В работе';
      return 'Ожидает исполнителя';
    },
    taskStatusClass() {
      if (this.task.status) return 'bg-success';
      if (this.task.acceptedForExecutionByUser) return 'bg-warning';
      return 'bg-secondary';
    },
    canAcceptTask() {
      return !this.task.acceptedForExecutionByUser && !this.task.status;
    },
  },
  methods: {
    async acceptTask() {
      try {
        const response = await apiClient.post(`/task/assign/${this.task.taskId}`, {
          acceptedForExecutionByUser: this.userId
        });
        if (response.data) {
          this.toast.success('Задача взята на выполнение');
          this.$emit('taskUpdated');
        } else {
          throw new Error('Не удалось взять задачу');
        }
      } catch (error) {
        this.toast.error('Ошибка при принятии задачи: ' + error.message);
        console.error('Ошибка:', error);
      }
    },
    async completeTask() {
      try {
        const response = await apiClient.post(`/task/complete/${this.task.taskId}`);
        if (response.data) {
          this.toast.success('Задача завершена');
          this.$emit('taskUpdated');
        } else {
          throw new Error('Не удалось завершить задачу');
        }
      } catch (error) {
        this.toast.error('Ошибка при завершении задачи: ' + error.message);
        console.error('Ошибка:', error);
      }
    },
    viewDetails() {
      this.$emit('view-details', this.task.taskId);
    },
  },
};
</script>

<style scoped>
.task-row {
  border-radius: 10px;
  transition: all 0.3s ease;
}

.task-row:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.task-title {
  flex: 1;
  min-width: 0;
}

.task-title h6 {
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
}

.task-title p {
  font-size: 0.85rem;
  line-height: 1.4;
  color: #6c757d;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
}

.task-status {
  min-width: 120px;
  text-align: center;
}

.badge {
  font-size: 0.8rem;
  padding: 0.5rem 1rem;
  border-radius: 15px;
}

.task-actions {
  min-width: 250px;
  justify-content: flex-end;
}

.btn-sm {
  padding: 0.4rem 0.8rem;
  border-radius: 15px;
  font-size: 0.85rem;
}

.btn-info {
  background-color: #17a2b8;
  border: none;
}

.btn-info:hover {
  background-color: #138496;
}
</style>