<template>
  <div class="container">
    <h1>Администрирование тикетов</h1>

    <!-- Поиск по тикетам -->
    <div class="mb-3">
      <input type="text" v-model="searchQuery" class="form-control" placeholder="Поиск тикета" @input="fetchTickets" />
    </div>

    <!-- Кнопки сортировки -->
    <div class="d-flex justify-content-end mb-3">
      <button class="btn btn-outline-secondary me-2" @click="sortTickets('id')">Сортировать по ID</button>
      <button class="btn btn-outline-secondary me-2" @click="sortTickets('title')">Сортировать по названию</button>
      <button class="btn btn-outline-secondary" @click="sortTickets('status')">Сортировать по статусу</button>
    </div>

    <!-- Таблица тикетов -->
    <table class="table table-striped">
      <thead>
      <tr>
        <th>ID</th>
        <th>Название</th>
        <th>Статус</th>
        <th>Дата создания</th>
        <th>Действия</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="ticket in filteredTickets" :key="ticket.id">
        <td>{{ ticket.ticketId }}</td>
        <td>{{ ticket.descrption }}</td>
        <td>{{ ticket.status }}</td>
        <td>{{ ticket.createdAt }}</td>
        <td>
          <button class="btn btn-warning btn-sm me-2" @click="openEditTicketModal(ticket)">Редактировать</button>
          <button class="btn btn-danger btn-sm" @click="deleteTicket(ticket.id)">Удалить</button>
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

    <!-- Модальное окно редактирования тикета -->
    <div class="modal fade" id="editTicketModal" tabindex="-1" role="dialog" aria-labelledby="editTicketModalLabel" aria-hidden="true">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="editTicketModalLabel">Редактировать тикет</h5>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close" @click="resetCurrentTicket">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <form @submit.prevent="updateTicket">
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label">Название</label>
                <input type="text" class="form-control" v-model="currentTicket.descrption" required />
              </div>
              <div class="mb-3">
                <label class="form-label">Статус</label>
                <input type="text" class="form-control" v-model="currentTicket.status" required />
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-dismiss="modal" @click="resetCurrentTicket">Закрыть</button>
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
      tickets: [],
      searchQuery: "",
      sortKey: "",
      currentPage: 1,
      perPage: 5,
      totalPages: 0,
      currentTicket: {}
    };
  },
  computed: {
    filteredTickets() {
      let filtered = this.tickets;
      if (this.searchQuery) {
        filtered = filtered.filter(ticket => ticket.title.toLowerCase().includes(this.searchQuery.toLowerCase()));
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
    fetchTickets() {
      apiClient.get("/tickets/list")
          .then(response => {
            this.tickets = response.data;
          })
          .catch(error => {
            console.error("Ошибка при получении списка тикетов:", error);
          });
    },
    sortTickets(key) {
      this.sortKey = key;
      this.currentPage = 1;
    },
    changePage(page) {
      if (page > 0 && page <= this.totalPages) {
        this.currentPage = page;
      }
    },
    openEditTicketModal(ticket) {
      this.currentTicket = { ...ticket };
      const modalElement = document.getElementById("editTicketModal");
      this.editTicketModalInstance = new window.bootstrap.Modal(modalElement);
      this.editTicketModalInstance.show();
    },
    updateTicket() {
      apiClient.put(`/tickets/update/${this.currentTicket.id}`, this.currentTicket)
          .then(() => {
            this.fetchTickets();
            this.editTicketModalInstance.hide();
            this.resetCurrentTicket();
          })
          .catch(error => {
            console.error("Ошибка при обновлении тикета:", error);
          });
    },
    deleteTicket(id) {
      apiClient.delete(`/tickets/delete/${id}`)
          .then(() => {
            this.fetchTickets();
          })
          .catch(error => {
            console.error("Ошибка при удалении тикета:", error);
          });
    },
    resetCurrentTicket() {
      this.currentTicket = {};
      this.editTicketModalInstance.hide();
    }
  },
  created() {
    this.fetchTickets();
  }
};
</script>

<style scoped>
.progress {
  height: 20px;
}
</style>
