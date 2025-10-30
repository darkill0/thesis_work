<template>
  <div class="container mt-5">
    <h2 class="mb-4 text-primary fw-bold">Мои Тикеты</h2>
    <div class="row">
      <!-- Фильтры -->
      <div class="col-md-3">
        <div class="card shadow-sm border-0 mb-4">
          <div class="card-header bg-primary text-white">
            <h5 class="mb-0">Фильтры</h5>
          </div>
          <div class="card-body">
            <input
                type="text"
                class="form-control mb-3"
                v-model="searchTerm"
                placeholder="Поиск по описанию"
                @input="filterTickets"
            />
            <button class="btn btn-primary mb-2 w-100" @click="sortTickets('date')">
              Сортировать по дате
            </button>
            <button class="btn btn-primary w-100" @click="sortTickets('status')">
              Сортировать по статусу
            </button>
          </div>
        </div>
      </div>
      <!-- Список тикетов -->
      <div class="col-md-6">
        <div class="row">
          <TicketItem
              v-for="ticket in filteredTickets"
              :key="ticket.ticketId"
              :ticket="ticket"
              :isMyTickets="true"
              class="col-12 mb-3"
              @viewDetails="viewTicketDetails(ticket.ticketId)"
          />
          <p v-if="!filteredTickets.length" class="text-muted text-center mt-4">
            Нет тикетов, соответствующих фильтрам.
          </p>
        </div>
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
      tickets: [],
      filteredTickets: [],
      searchTerm: '',
      notifications: [],
      filteredNotifications: [],
      notificationSearch: '',
      webSocketService: null,
    };
  },
  computed: {
    ...mapGetters(['getUserId']),
  },
  async mounted() {
    if (this.getUserId) {
      await this.fetchMyTickets();
      await this.fetchNotifications();
      this.webSocketService = new WebSocketService(this.getUserId, this.addNotification);
    } else {
      console.warn('userId не определён');
      alert('Пользователь не аутентифицирован.');
    }
  },
  methods: {
    async fetchMyTickets() {
      try {
        const response = await apiClient.get(`/tickets/my/${this.getUserId}`);
        this.tickets = response.data || [];
        this.filteredTickets = this.tickets;
        console.log('Мои тикеты:', this.tickets);
      } catch (error) {
        console.error('Ошибка при получении тикетов:', error);
        alert('Не удалось загрузить тикеты. Попробуйте позже.');
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
        console.log('Уведомления:', this.notifications);
      } catch (error) {
        console.error('Ошибка при получении уведомлений:', error);
        alert('Не удалось загрузить уведомления. Попробуйте позже.');
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
      if (this.searchTerm.trim() === '') {
        this.filteredTickets = this.tickets;
      } else {
        this.filteredTickets = this.tickets.filter((ticket) =>
            ticket.descrption &&
            ticket.descrption.toLowerCase().includes(this.searchTerm.toLowerCase())
        );
      }
    },
    sortTickets(criteria) {
      if (criteria === 'date') {
        this.filteredTickets.sort(
            (a, b) => new Date(b.createdDate) - new Date(a.createdDate)
        );
      } else if (criteria === 'status') {
        this.filteredTickets.sort((a, b) => a.status - b.status);
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
</style>