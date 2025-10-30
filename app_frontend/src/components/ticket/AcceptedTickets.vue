<template>
  <div class="container mt-5">
    <h2 class="mb-4 text-primary fw-bold">Взятые Тикеты</h2>
    <div class="row">
      <!-- Фильтры и сортировка -->
      <div class="col-md-3">
        <div class="card shadow-sm border-0 mb-4">
          <div class="card-header bg-primary text-white">
            <h5 class="mb-0">Фильтры и Сортировка</h5>
          </div>
          <div class="card-body">
            <input
                type="text"
                class="form-control mb-3"
                v-model="searchTerm"
                placeholder="Поиск по описанию"
                @input="filterTickets"
            />
            <select
                class="form-select mb-3"
                v-model="statusFilter"
                @change="filterTickets"
            >
              <option value="">Все статусы</option>
              <option value="0">Открытый</option>
              <option value="1">В процессе</option>
              <option value="2">Закрытый</option>
            </select>
            <button class="btn btn-primary mb-2 w-100" @click="sortTickets('date')">
              Сортировать по дате
            </button>
            <button class="btn btn-primary mb-2 w-100" @click="sortTickets('status')">
              Сортировать по статусу
            </button>
            <button class="btn btn-primary mb-2 w-100" @click="sortTickets('description')">
              Сортировать по описанию
            </button>
            <button class="btn btn-success mb-2 w-100" @click="exportToCSV">
              Экспорт в CSV
            </button>
            <button
                class="btn btn-warning w-100"
                @click="completeSelectedTickets"
                :disabled="selectedTickets.length === 0"
            >
              Завершить выбранные
            </button>
          </div>
        </div>
      </div>
      <!-- Список тикетов -->
      <div class="col-md-6">
        <div class="mb-3">
          <label class="form-check-label">
            <input
                type="checkbox"
                class="form-check-input"
                @change="toggleSelectAll"
                v-model="selectAll"
            >
            Выбрать все
          </label>
        </div>
        <div class="ticket-list">
          <TicketItem
              v-for="ticket in paginatedTickets"
              :key="ticket.ticketId"
              :ticket="ticket"
              :isAcceptedTickets="true"
              @viewDetails="viewTicketDetails(ticket.ticketId)"
              @dblclick="viewTicketDetails(ticket.ticketId)"
              class="mb-3"
          >
            <template #checkbox>
              <input
                  type="checkbox"
                  class="form-check-input"
                  :value="ticket.ticketId"
                  v-model="selectedTickets"
                  @change="updateSelectAll"
              />
            </template>
            <template #actions>
              <button
                  v-if="ticket.status === 0 && ticket.acceptedByUser === null"
                  class="btn btn-primary btn-sm ms-2"
                  @click="takeTicket(ticket.ticketId)"
              >
                Взять в обработку
              </button>
            </template>
          </TicketItem>
          <p v-if="!paginatedTickets.length" class="text-muted text-center mt-4">
            Нет тикетов, соответствующих фильтрам.
          </p>
        </div>
        <!-- Пагинация -->
        <nav v-if="filteredTickets.length > itemsPerPage">
          <ul class="pagination justify-content-center">
            <li class="page-item" :class="{ disabled: currentPage === 1 }">
              <button class="page-link" @click="currentPage--">
                Предыдущая
              </button>
            </li>
            <li
                v-for="page in totalPages"
                :key="page"
                class="page-item"
                :class="{ active: currentPage === page }"
            >
              <button class="page-link" @click="currentPage = page">
                {{ page }}
              </button>
            </li>
            <li
                class="page-item"
                :class="{ disabled: currentPage === totalPages }"
            >
              <button class="page-link" @click="currentPage++">
                Следующая
              </button>
            </li>
          </ul>
        </nav>
      </div>
      <!-- Уведомления -->
      <div class="col-md-3">
        <div class="notifications">
          <h4 class="text-primary fw-bold">Уведомления</h4>
          <input
              type="text"
              class="form-control mb-3"
              v-model="notificationSearch"
              placeholder="Поиск уведомлений"
              @input="filterNotifications"
          />
          <transition-group name="fade">
            <div
                v-for="notification in filteredNotifications"
                :key="notification.id"
                class="notification-popup"
            >
              <p>{{ notification.message }}</p>
              <div class="progress-bar">
                <div :style="{ width: notification.progress + '%' }"></div>
              </div>
            </div>
          </transition-group>
          <p v-if="!filteredNotifications.length" class="text-muted mt-3">
            Нет уведомлений.
          </p>
        </div>
      </div>
    </div>

    <!-- Модальное окно для сообщений -->
    <div
        class="modal fade"
        id="messageModal"
        tabindex="-1"
        aria-labelledby="messageModalLabel"
        aria-hidden="true"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header" :class="modalIsError ? 'bg-danger text-white' : 'bg-primary text-white'">
            <h5 class="modal-title" id="messageModalLabel">{{ modalTitle }}</h5>
            <button
                type="button"
                class="btn-close btn-close-white"
                data-bs-dismiss="modal"
                aria-label="Закрыть"
            ></button>
          </div>
          <div class="modal-body">
            <p>{{ modalMessage }}</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Закрыть</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import apiClient from '@/utils/apiClient';
import TicketItem from '@/components/ticket/TicketItem.vue';
import { mapGetters } from 'vuex';
import WebSocketService from '@/utils/WebSocketService';

export default {
  components: {
    TicketItem,
  },
  data() {
    return {
      acceptedTickets: [],
      filteredTickets: [],
      searchTerm: '',
      statusFilter: '',
      notifications: [],
      filteredNotifications: [],
      notificationSearch: '',
      webSocketService: null,
      currentPage: 1,
      itemsPerPage: 5,
      selectAll: false,
      selectedTickets: [],
      modalTitle: '',
      modalMessage: '',
      modalIsError: false,
    };
  },
  computed: {
    ...mapGetters(['getUserId']),
    totalPages() {
      return Math.ceil(this.filteredTickets.length / this.itemsPerPage);
    },
    paginatedTickets() {
      const start = (this.currentPage - 1) * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.filteredTickets.slice(start, end);
    },
  },
  async mounted() {
    if (this.getUserId) {
      await this.fetchAcceptedTickets();
      await this.fetchNotifications();
      this.webSocketService = new WebSocketService(this.getUserId, this.addNotification);
    } else {
      console.warn('userId не определён');
      this.showModal('Ошибка', 'Пользователь не аутентифицирован.', true);
    }
  },
  methods: {
    async fetchAcceptedTickets() {
      try {
        const response = await apiClient.get(`/tickets/my/accepted/${this.getUserId}`);
        // Фильтруем тикеты, исключая созданные текущим пользователем
        this.acceptedTickets = (response.data || []).filter(
            (ticket) => ticket.createdByUser !== this.getUserId
        );
        this.filteredTickets = this.acceptedTickets;
        console.log('Взятые тикеты:', this.acceptedTickets);
      } catch (error) {
        console.error('Ошибка при получении взятых тикетов:', error);
        this.showModal('Ошибка', 'Не удалось загрузить тикеты. Попробуйте позже.', true);
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
        if (!response.ok) throw new Error(`Ошибка HTTP: ${response.status}`);
        const data = await response.json();
        this.notifications = data.map((notification) => ({
          id: notification.id,
          message: this.formatNotification(notification.message),
          progress: 100,
        }));
        this.filteredNotifications = this.notifications;
      } catch (error) {
        console.error('Ошибка при получении уведомлений:', error);
        this.showModal('Ошибка', 'Не удалось загрузить уведомления. Попробуйте позже.', true);
      }
    },
    addNotification(notification) {
      const id = notification.id || Date.now();
      const formattedMessage = this.formatNotification(notification.message);
      const newNotification = { id, message: formattedMessage, progress: 100 };
      this.notifications.push(newNotification);
      this.filterNotifications();
      const interval = setInterval(() => {
        newNotification.progress -= 5;
        if (newNotification.progress <= 0) {
          this.notifications = this.notifications.filter((n) => n.id !== id);
          this.filterNotifications();
          clearInterval(interval);
        }
      }, 300);
    },
    formatNotification(rawMessage) {
      if (rawMessage.includes('TicketForm')) {
        try {
          const ticketData = rawMessage.match(/TicketForm\((.+)\)/)[1];
          const fields = ticketData.split(', ').reduce((acc, field) => {
            const [key, value] = field.split('=');
            acc[key] = value;
            return acc;
          }, {});
          return `Тикет "${fields.descrption}" обновлён. Статус: ${
              fields.status === '0' ? 'Открытый' : fields.status === '1' ? 'В процессе' : 'Закрытый'
          }`;
        } catch (e) {
          console.error('Ошибка при форматировании уведомления:', e);
          return rawMessage;
        }
      }
      return rawMessage;
    },
    filterNotifications() {
      if (this.notificationSearch.trim() === '') {
        this.filteredNotifications = this.notifications;
      } else {
        this.filteredNotifications = this.notifications.filter((notification) =>
            notification.message.toLowerCase().includes(this.notificationSearch.toLowerCase())
        );
      }
    },
    viewTicketDetails(ticketId) {
      this.$router.push({ name: 'ticketDetails', params: { id: ticketId } });
    },
    filterTickets() {
      this.filteredTickets = this.acceptedTickets.filter((ticket) => {
        const matchesSearch =
            ticket.descrption &&
            ticket.descrption.toLowerCase().includes(this.searchTerm.toLowerCase());
        const matchesStatus = this.statusFilter
            ? ticket.status === parseInt(this.statusFilter)
            : true;
        return matchesSearch && matchesStatus;
      });
      this.currentPage = 1;
    },
    sortTickets(criteria) {
      if (criteria === 'date') {
        this.filteredTickets.sort(
            (a, b) => new Date(b.createdDate) - new Date(a.createdDate)
        );
      } else if (criteria === 'status') {
        this.filteredTickets.sort((a, b) => a.status - b.status);
      } else if (criteria === 'description') {
        this.filteredTickets.sort((a, b) =>
            (a.descrption || '').localeCompare(b.descrption || '')
        );
      }
      this.currentPage = 1;
    },
    exportToCSV() {
      const headers = ['ID,Описание,Статус,Ссылка на проект,Дата создания'];
      const rows = this.filteredTickets.map((ticket) =>
          [
            ticket.ticketId,
            `"${(ticket.descrption || '').replace(/"/g, '""')}"`,
            ticket.status,
            ticket.urlToProject || '',
            ticket.createdDate,
          ].join(',')
      );
      const csvContent = headers.concat(rows).join('\n');
      const bom = '\uFEFF';
      const blob = new Blob([bom + csvContent], { type: 'text/csv;charset=utf-8;' });
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = 'accepted_tickets.csv';
      link.click();
    },
    async takeTicket(ticketId) {
      try {
        await apiClient.post(`/tickets/take/${ticketId}`, { userId: this.getUserId });
        await this.fetchAcceptedTickets();
        this.showModal('Успех', 'Тикет успешно взят в обработку.', false);
      } catch (error) {
        console.error('Ошибка при взятии тикета:', error);
        this.showModal('Ошибка', 'Не удалось взять тикет в обработку.', true);
      }
    },
    async completeSelectedTickets() {
      if (this.selectedTickets.length === 0) return;
      this.showModal(
          'Подтверждение',
          `Завершить ${this.selectedTickets.length} выбранных тикетов?`,
          false,
          async () => {
            try {
              for (const ticketId of this.selectedTickets) {
                const ticket = this.acceptedTickets.find((t) => t.ticketId === ticketId);
                await apiClient.post(`/tickets/update/${ticketId}`, {
                  status: 2,
                  descrption: ticket.descrption,
                  urlToProject: ticket.urlToProject || null,
                });
              }
              this.selectedTickets = [];
              this.selectAll = false;
              await this.fetchAcceptedTickets();
              this.showModal('Успех', 'Выбранные тикеты успешно завершены.', false);
            } catch (error) {
              console.error('Ошибка при завершении тикетов:', error);
              this.showModal('Ошибка', 'Не удалось завершить тикеты. Попробуйте позже.', true);
            }
          }
      );
    },
    toggleSelectAll() {
      if (this.selectAll) {
        this.selectedTickets = this.paginatedTickets.map((ticket) => ticket.ticketId);
      } else {
        this.selectedTickets = [];
      }
    },
    updateSelectAll() {
      this.selectAll =
          this.selectedTickets.length === this.paginatedTickets.length &&
          this.paginatedTickets.length > 0;
    },
    showModal(title, message, isError, confirmCallback = null) {
      this.modalTitle = title;
      this.modalMessage = message;
      this.modalIsError = isError;
      const modal = new bootstrap.Modal(document.getElementById('messageModal'));
      modal.show();
      if (confirmCallback) {
        const confirmButton = document.createElement('button');
        confirmButton.type = 'button';
        confirmButton.className = 'btn btn-primary';
        confirmButton.innerText = 'Подтвердить';
        confirmButton.onclick = async () => {
          await confirmCallback();
          modal.hide();
        };
        const modalFooter = document.querySelector('#messageModal .modal-footer');
        modalFooter.insertBefore(confirmButton, modalFooter.firstChild);
      }
    },
  },
};
</script>

<style scoped>
.container {
  margin-top: 40px;
  max-width: 1200px;
}

h2,
h4 {
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

.btn-warning {
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
}

.notifications {
  position: sticky;
  top: 20px;
  width: 100%;
  padding: 15px;
  background-color: #e9ecef;
  border-radius: 10px;
}

.notification-popup {
  background: #17a2b8;
  color: white;
  padding: 15px;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  margin-bottom: 10px;
  animation: fadeIn 0.3s ease-out;
}

.progress-bar {
  height: 5px;
  background: rgba(255, 255, 255, 0.5);
  margin-top: 10px;
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar div {
  height: 100%;
  background: white;
  transition: width 0.3s linear;
}

.pagination .page-link {
  border-radius: 8px;
  margin: 0 5px;
  color: #1a3c6d;
}

.pagination .page-item.active .page-link {
  background-color: #1a3c6d;
  border-color: #1a3c6d;
  color: white;
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

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-enter,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
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
</style>