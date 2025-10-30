<template>
  <div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="text-primary fw-bold">Тикеты</h2>
      <button @click="openCreateTicketModal" class="btn btn-success shadow-sm">
        <i class="bi bi-plus-circle me-2"></i>Создать тикет
      </button>
    </div>
    <div class="row">
      <!-- Фильтры -->
      <div class="col-md-3">
        <div class="card shadow-sm border-0 mb-4">
          <div class="card-header bg-primary text-white">
            <h5 class="mb-0">Фильтры</h5>
          </div>
          <div class="card-body">
            <div class="mb-3">
              <label for="searchInput" class="form-label">Поиск</label>
              <input
                  type="text"
                  id="searchInput"
                  class="form-control"
                  v-model="searchTerm"
                  @input="filterTickets"
                  placeholder="Введите описание..."
              />
            </div>
            <div class="mb-3">
              <label for="statusSelect" class="form-label">Статус</label>
              <select
                  id="statusSelect"
                  class="form-select"
                  v-model="selectedStatus"
                  @change="filterTickets"
              >
                <option value="">Все статусы</option>
                <option value="2">Открытый</option>
                <option value="3">Закрытый</option>
                <option value="1">В процессе</option>
              </select>
            </div>
            <div class="mb-3">
              <label for="sortSelect" class="form-label">Сортировка</label>
              <select
                  id="sortSelect"
                  class="form-select"
                  v-model="selectedSort"
                  @change="sortTickets"
              >
                <option value="date-asc">По дате (по возрастанию)</option>
                <option value="date-desc">По дате (по убыванию)</option>
                <option value="status-asc">По статусу (1-3)</option>
                <option value="status-desc">По статусу (3-1)</option>
              </select>
            </div>
          </div>
        </div>
      </div>
      <!-- Список тикетов -->
      <div class="col-md-9">
        <div class="row">
          <TicketItem
              v-for="ticket in filteredTickets"
              :key="ticket.ticketId"
              :ticket="ticket"
              @taskUpdated="fetchTickets"
              @viewDetails="viewTicketDetails(ticket.ticketId)"
          />
          <p v-if="!filteredTickets.length" class="text-muted text-center mt-4">
            Нет доступных тикетов, соответствующих фильтрам.
          </p>
        </div>
      </div>
    </div>

    <!-- Модальное окно для создания тикета -->
    <div class="modal fade" id="createTicketModal" tabindex="-1" aria-labelledby="createTicketModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-primary text-white">
            <h5 class="modal-title" id="createTicketModalLabel">Создание нового тикета</h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Закрыть"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="createTicket">
              <div class="mb-3">
                <label for="ticketDescription" class="form-label">Описание</label>
                <textarea
                    class="form-control"
                    id="ticketDescription"
                    v-model="newTicket.descrption"
                    required
                ></textarea>
              </div>
              <div class="mb-3">
                <label for="projectUrl" class="form-label">Ссылка на проект</label>
                <input
                    type="url"
                    class="form-control"
                    id="projectUrl"
                    v-model="newTicket.urlToProject"
                    required
                />
              </div>
              <button type="submit" class="btn btn-primary w-100">Создать тикет</button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import apiClient from '@/utils/apiClient';
import TicketItem from '@/components/ticket/TicketItem.vue';
import { mapState } from 'vuex';
import { useToast } from 'vue-toastification';

export default {
  components: {
    TicketItem,
  },
  data() {
    return {
      tickets: [],
      newTicket: {
        descrption: '',
        urlToProject: '',
        createdByUser: null,
        status: 2, // Новый тикет по умолчанию открытый
      },
      searchTerm: '',
      selectedStatus: '',
      selectedSort: 'date-asc',
      filteredTickets: [],
    };
  },
  computed: {
    ...mapState(['userId']),
  },
  methods: {
    // Загрузка списка тикетов
    async fetchTickets() {
      try {
        const response = await apiClient.get('/tickets/list');
        this.tickets = response.data.map(ticket => ({
          ...ticket,
          status: Number(ticket.status), // Приводим статус к числу
        }));
        console.log('Загруженные тикеты:', this.tickets); // Логирование для отладки
        this.filterTickets();
      } catch (error) {
        console.error('Ошибка при загрузке тикетов:', error);
        useToast().error('Ошибка при загрузке тикетов');
      }
    },
    // Фильтрация тикетов
    filterTickets() {
      this.filteredTickets = this.tickets.filter(ticket => {
        const description = ticket.descrption || '';
        const searchTerm = this.searchTerm || '';
        const matchesSearchTerm = description.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesStatus = this.selectedStatus
            ? Number(ticket.status) === Number(this.selectedStatus)
            : true;
        const isNotTaken = ticket.acceptedByUser === null; // Только тикеты, не взятые на выполнение

        // Логирование для отладки фильтрации
        console.log('Фильтрация тикета:', {
          ticketId: ticket.ticketId,
          status: ticket.status,
          selectedStatus: this.selectedStatus,
          acceptedByUser: ticket.acceptedByUser,
          matchesStatus,
          matchesSearchTerm,
          isNotTaken,
        });

        return matchesSearchTerm && matchesStatus && isNotTaken;
      });
      this.sortTickets();
    },
    // Сортировка тикетов
    sortTickets() {
      if (this.selectedSort === 'date-asc') {
        this.filteredTickets.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
      } else if (this.selectedSort === 'date-desc') {
        this.filteredTickets.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      } else if (this.selectedSort === 'status-asc') {
        this.filteredTickets.sort((a, b) => Number(a.status) - Number(b.status));
      } else if (this.selectedSort === 'status-desc') {
        this.filteredTickets.sort((a, b) => Number(b.status) - Number(a.status));
      }
    },
    // Создание нового тикета
    async createTicket() {
      try {
        this.newTicket.createdByUser = this.userId;
        await apiClient.post('/tickets/create', this.newTicket);
        this.newTicket.descrption = '';
        this.newTicket.urlToProject = '';
        const modal = bootstrap.Modal.getInstance(document.getElementById('createTicketModal'));
        modal.hide();
        await this.fetchTickets();
        useToast().success('Тикет успешно создан');
      } catch (error) {
        console.error('Ошибка при создании тикета:', error);
        useToast().error('Не удалось создать тикет. Попробуйте позже.');
      }
    },
    // Открытие модального окна для создания тикета
    openCreateTicketModal() {
      const modal = new bootstrap.Modal(document.getElementById('createTicketModal'));
      modal.show();
    },
    // Переход к деталям тикета
    viewTicketDetails(ticketId) {
      this.$router.push({ name: 'ticketDetails', params: { id: ticketId } });
    },
  },
  created() {
    this.fetchTickets();
  },
};
</script>

<style scoped>
.container {
  margin-top: 40px;
  max-width: 1200px;
}

h2 {
  font-size: 2rem;
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
  font-weight: 500;
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

.btn-success {
  background-color: #28a745;
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
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

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.row > * {
  animation: fadeIn 0.3s ease-out;
}
</style>