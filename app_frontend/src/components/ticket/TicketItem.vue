<template>
  <div class="col-md-12 mb-4">
    <div class="card shadow-sm border-0 h-100">
      <div class="card-body d-flex align-items-center">
        <slot name="checkbox" />
        <div class="flex-grow-1">
          <h5 class="card-title text-primary">{{ ticket.descrption }}</h5>
          <p class="card-text text-muted">
            <strong>Статус:</strong> {{ ticket.status === 2 ? 'Открытый' : ticket.status === 3 ? 'Закрытый' : 'В процессе' }}
          </p>
          <p class="card-text">
            <strong>Проект:</strong>
            <a :href="ticket.urlToProject" target="_blank" class="text-primary text-truncate d-block">
              {{ ticket.urlToProject }}
            </a>
          </p>
          <div class="d-flex justify-content-end gap-2">
            <button v-if="canTakeTicket" @click="takeTicket" class="btn btn-primary btn-sm">
              <i class="bi bi-check-circle me-2"></i>Взять на выполнение
            </button>
            <button @click="$emit('viewDetails')" class="btn btn-outline-secondary btn-sm">
              <i class="bi bi-eye me-2"></i>Подробнее
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue';
import { useStore } from 'vuex';
import { useToast } from 'vue-toastification';
import apiClient from '@/utils/apiClient';

export default {
  props: {
    ticket: {
      type: Object,
      required: true,
    },
  },
  emits: ['viewDetails', 'taskUpdated'],
  setup(props, { emit }) {
    const store = useStore();
    const toast = useToast();

    // ID текущего пользователя из Vuex
    const userId = computed(() => store.state.userId);

    // Условие для отображения кнопки "Взять на выполнение"
    const canTakeTicket = computed(() => {
      const showButton =
          userId.value !== props.ticket.createdByUser && // Не создатель тикета
          !props.ticket.acceptedByUser; // Тикет не взят другим пользователем

      // Логирование для отладки
      console.log('canTakeTicket:', {
        userId: userId.value,
        createdByUser: props.ticket.createdByUser,
        acceptedByUser: props.ticket.acceptedByUser,
        status: props.ticket.status,
        ticketId: props.ticket.ticketId,
        showButton,
      });

      return showButton;
    });

    // Взятие тикета в обработку
    const takeTicket = async () => {
      if (!props.ticket.ticketId) {
        console.error('Ошибка: ticket.ticketId не определён', props.ticket);
        toast.error('Ошибка: ID тикета не найден');
        return;
      }

      try {
        const response = await apiClient.post(`/tickets/take/${props.ticket.ticketId}`, {
          userId: userId.value,
        });
        toast.success(response.data.message || 'Тикет успешно взят');
        emit('taskUpdated');
      } catch (error) {
        console.error('Ошибка при взятии тикета:', error);
        toast.error('Ошибка при взятии тикета: ' + (error.response?.data?.message || error.message));
      }
    };

    return {
      userId,
      canTakeTicket,
      takeTicket,
    };
  },
};
</script>

<style scoped>
.card {
  border-radius: 10px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.card-title {
  font-size: 1.25rem;
  color: #1a3c6d;
}

.btn-outline-secondary,
.btn-primary {
  border-radius: 8px;
  transition: background-color 0.2s ease, transform 0.2s ease;
}

.btn-primary:hover {
  background-color: #0056b3;
  transform: scale(1.05);
}

.btn-outline-secondary:hover {
  background-color: #e9ecef;
  transform: scale(1.05);
}

.text-truncate {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
}

.gap-2 {
  gap: 0.5rem;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.card {
  animation: fadeIn 0.3s ease-out;
}
</style>