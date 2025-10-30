<template>
  <div class="container mt-5">
    <h2 class="mb-4 text-primary fw-bold">Детали тикета</h2>

    <!-- Карточка с деталями тикета -->
    <div v-if="ticket" class="card mb-4 shadow-sm border-0">
      <div class="card-header bg-primary text-white">
        <h5 class="card-title mb-0">{{ ticket.descrption }}</h5>
      </div>
      <div class="card-body">
        <h6 class="card-subtitle mb-2 text-muted">
          Создан пользователем: {{ ticket.createdByUser }}
        </h6>
        <p class="card-text">
          <strong>Статус:</strong>
          {{ ticket.status === 0 ? 'Открытый' : ticket.status === 1 ? 'В процессе' : 'Закрытый' }}
        </p>
        <p class="card-text">
          <strong>Ссылка на проект:</strong>
          <a
              v-if="ticket.urlToProject"
              :href="ticket.urlToProject"
              target="_blank"
              class="text-primary"
          >
            {{ ticket.urlToProject }}
          </a>
          <span v-else class="text-muted">Не указана</span>
        </p>
        <div class="d-flex justify-content-between align-items-center">
          <div class="d-flex gap-2">
            <button
                v-if="ticket.createdByUser === getUserId && ticket.status !== 2"
                @click="completeTicket"
                class="btn btn-success"
            >
              <i class="bi bi-check-circle me-2"></i>Завершить
            </button>
            <button
                v-if="ticket.createdByUser === getUserId && ticket.status !== 2"
                @click="openEditModal"
                class="btn btn-primary"
            >
              <i class="bi bi-pencil me-2"></i>Редактировать
            </button>
            <button
                v-if="ticket.createdByUser === getUserId"
                @click="deleteTicket"
                class="btn btn-danger"
            >
              <i class="bi bi-trash me-2"></i>Удалить
            </button>
          </div>
          <button @click="goBack" class="btn btn-secondary">
            <i class="bi bi-arrow-left me-2"></i>Назад
          </button>
        </div>
      </div>
    </div>
    <div v-else class="alert alert-warning">
      Тикет не найден или не загружен.
    </div>

    <!-- Список комментариев -->
    <div class="mb-4">
      <h5 class="text-primary fw-bold">Комментарии</h5>
      <button
          class="btn btn-success mb-3 shadow-sm"
          data-bs-toggle="modal"
          data-bs-target="#addCommentModal"
      >
        <i class="bi bi-plus-circle me-2"></i>Добавить комментарий
      </button>
      <ul class="list-group">
        <li
            v-for="(comment, index) in comments"
            :key="index"
            class="list-group-item border-0 shadow-sm mb-2 rounded"
        >
          <p class="mb-1">{{ comment.description }}</p>
          <span class="text-muted">
            {{ new Date(comment.createdAt).toLocaleDateString() }}
            (Статус: {{ comment.status === 0 ? 'В процессе' : 'Завершено' }})
          </span>
        </li>
      </ul>
      <p v-if="!comments || !comments.length" class="text-muted mt-3">
        Комментариев пока нет.
      </p>
    </div>

    <!-- Модальное окно для добавления комментария -->
    <div
        class="modal fade"
        id="addCommentModal"
        tabindex="-1"
        aria-labelledby="addCommentModalLabel"
        aria-hidden="true"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-primary text-white">
            <h5 class="modal-title" id="addCommentModalLabel">Добавить комментарий</h5>
            <button
                type="button"
                class="btn-close btn-close-white"
                data-bs-dismiss="modal"
                aria-label="Закрыть"
            ></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="submitComment">
              <div class="mb-3">
                <label for="commentdescrption" class="form-label">Комментарий</label>
                <textarea
                    v-model="newComment.description"
                    id="commentdescrption"
                    class="form-control"
                    rows="3"
                    required
                ></textarea>
              </div>
              <div class="mb-3">
                <label for="commentStatus" class="form-label">Статус</label>
                <select
                    v-model="newComment.status"
                    id="commentStatus"
                    class="form-select"
                    required
                >
                  <option :value="0">В процессе</option>
                  <option :value="1">Завершено</option>
                </select>
              </div>
              <button type="submit" class="btn btn-primary w-100">Добавить</button>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- Модальное окно для редактирования тикета -->
    <div
        class="modal fade"
        id="editTicketModal"
        tabindex="-1"
        aria-labelledby="editTicketModalLabel"
        aria-hidden="true"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-primary text-white">
            <h5 class="modal-title" id="editTicketModalLabel">Редактировать тикет</h5>
            <button
                type="button"
                class="btn-close btn-close-white"
                data-bs-dismiss="modal"
                aria-label="Закрыть"
            ></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="updateTicket">
              <div class="mb-3">
                <label for="ticketDescription" class="form-label">Описание</label>
                <textarea
                    class="form-control"
                    id="ticketDescription"
                    v-model="editedTicket.descrption"
                    required
                ></textarea>
              </div>
              <div class="mb-3">
                <label for="projectUrl" class="form-label">Ссылка на проект</label>
                <input
                    type="url"
                    class="form-control"
                    id="projectUrl"
                    v-model="editedTicket.urlToProject"
                />
              </div>
              <div class="mb-3">
                <label for="statusSelect" class="form-label">Статус</label>
                <select
                    id="statusSelect"
                    class="form-select"
                    v-model="editedTicket.status"
                    required
                >
                  <option :value="0">Открытый</option>
                  <option :value="1">В процессе</option>
                  <option :value="2">Закрытый</option>
                </select>
              </div>
              <button type="submit" class="btn btn-primary w-100">Сохранить</button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import apiClient from '@/utils/apiClient';
import { mapGetters } from 'vuex';

export default {
  data() {
    return {
      ticket: null,
      comments: [],
      newComment: {
        description: '',
        status: 0,
        createdByUser: null,
      },
      editedTicket: {
        descrption: '',
        urlToProject: '',
        status: 0,
      },
    };
  },
  computed: {
    ...mapGetters(['getUserId']),
  },
  methods: {
    async fetchTicket() {
      try {
        const response = await apiClient.get(`/tickets/get/${this.$route.params.id}`);
        this.ticket = response.data || null;
        this.comments = this.ticket?.comments || [];
        console.log('Тикет загружен:', this.ticket);
      } catch (error) {
        console.error('Ошибка при загрузке тикета:', error);
        alert('Не удалось загрузить тикет. Попробуйте позже.');
      }
    },
    async submitComment() {
      if (this.newComment.description.trim() === '') return;
      try {
        this.newComment.createdByUser = this.getUserId;
        const response = await apiClient.post(
            `/tickets/comment/${this.ticket.ticketId}/add`,
            this.newComment
        );
        this.comments.push(response.data);
        this.newComment.description = '';
        this.newComment.status = 0;
        this.newComment.createdByUser = null;
        const modal = bootstrap.Modal.getInstance(
            document.getElementById('addCommentModal')
        );
        modal.hide();
        document.querySelectorAll('.modal-backdrop').forEach((backdrop) => backdrop.remove());
        await this.fetchTicket();
      } catch (error) {
        console.error('Ошибка при добавлении комментария:', error);
        alert('Не удалось добавить комментарий. Попробуйте позже.');
      }
    },
    async completeTicket() {
      try {
        await apiClient.post(`/tickets/update/${this.ticket.ticketId}`, {
          status: 3,
          descrption: this.ticket.descrption,
          urlToProject: this.ticket.urlToProject || null,
        });
        await this.fetchTicket();
        alert('Тикет успешно завершён.');
      } catch (error) {
        console.error('Ошибка при завершении тикета:', error);
        alert('Не удалось завершить тикет. Попробуйте позже.');
      }
    },
    openEditModal() {
      this.editedTicket = {
        descrption: this.ticket.descrption,
        urlToProject: this.ticket.urlToProject || '',
        status: this.ticket.status,
      };
      const modal = new bootstrap.Modal(document.getElementById('editTicketModal'));
      modal.show();
    },
    async updateTicket() {
      try {
        await apiClient.post(`/tickets/update/${this.ticket.ticketId}`, {
          status: parseInt(this.editedTicket.status),
          descrption: this.editedTicket.descrption,
          urlToProject: this.editedTicket.urlToProject || null,
        });
        const modal = bootstrap.Modal.getInstance(
            document.getElementById('editTicketModal')
        );
        modal.hide();
        document.querySelectorAll('.modal-backdrop').forEach((backdrop) => backdrop.remove());
        await this.fetchTicket();
        alert('Тикет успешно обновлён.');
      } catch (error) {
        console.error('Ошибка при обновлении тикета:', error);
        alert('Не удалось обновить тикет. Попробуйте позже.');
      }
    },
    async deleteTicket() {
      const confirmation = confirm('Вы уверены, что хотите удалить этот тикет?');
      if (confirmation) {
        try {
          await apiClient.delete(`/tickets/delete/${this.ticket.ticketId}`);
          this.goBack();
          alert('Тикет успешно удалён.');
        } catch (error) {
          console.error('Ошибка при удалении тикета:', error);
          alert('Не удалось удалить тикет. Попробуйте позже.');
        }
      }
    },
    goBack() {
      this.$router.push({ name: 'myTickets' });
    },
  },
  created() {
    if (this.getUserId) {
      this.fetchTicket();
    } else {
      console.warn('userId не определён');
      alert('Пользователь не аутентифицирован.');
    }
  },
};
</script>

<style scoped>
.container {
  margin-top: 40px;
  max-width: 1200px;
}

h2,
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
}

.card-body {
  padding: 1.5rem;
}

.list-group-item {
  border-radius: 8px;
  transition: background-color 0.2s ease;
}

.list-group-item:hover {
  background-color: #f8f9fa;
}

.btn-primary {
  background-color: #1a3c6d;
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
  transition: background-color 0.2s ease;
}

.btn-primary:hover {
  background-color: #153258;
}

.btn-danger {
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
}

.btn-secondary,
.btn-success {
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
}

.btn-success {
  background-color: #28a745;
  border: none;
  transition: background-color 0.2s ease;
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

.form-control,
.form-select {
  border-radius: 8px;
  border: 1px solid #ced4da;
  transition: border-color 0.2s ease;
}

.form-control:focus,
.form-select:focus {
  border-color: #1a3c6d;
  box-shadow: 0 0 5px rgba(26, 60, 109, 0.3);
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

.card,
.list-group-item {
  animation: fadeIn 0.3s ease-out;
}
</style>