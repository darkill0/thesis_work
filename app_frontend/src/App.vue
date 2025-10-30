<template>
  <div id="app">
    <Navbar />
    <router-view />
    <Footer />
    <!-- Контейнер для уведомлений -->
    <div class="notifications">
      <transition-group name="fade">
        <div
            v-for="notification in notifications"
            :key="notification.id"
            class="notification-popup"
        >
          <p>{{ notification.message }}</p>
          <div class="progress-bar">
            <div :style="{ width: notification.progress + '%' }"></div>
          </div>
        </div>
      </transition-group>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue';
import { useStore } from 'vuex';
import Navbar from '@/components/site/Navbar.vue';
import Footer from '@/components/site/Footer.vue';
import WebSocketService from '@/utils/WebSocketService';

export default {
  components: {
    Navbar,
    Footer,
  },
  setup() {
    const store = useStore();
    const notifications = ref([]);
    const webSocketService = ref(null);

    const addNotification = (notification) => {
      if (notification.to === store.state.userId) {
        const newNotification = {
          ...notification,
          id: notification.id || Date.now(),
          message: formatMessage(notification.message),
          progress: 100,
        };
        notifications.value.push(newNotification);

        const interval = setInterval(() => {
          newNotification.progress -= 5;
          if (newNotification.progress <= 0) {
            notifications.value = notifications.value.filter(n => n.id !== newNotification.id);
            clearInterval(interval);
          }
        }, 250);
      }
    };

    const formatMessage = (rawMessage) => {
      if (rawMessage.includes('TaskForm')) {
        try {
          const taskData = rawMessage.match(/TaskForm\((.+)\)/)[1];
          const fields = taskData.split(', ').reduce((acc, field) => {
            const [key, value] = field.split('=');
            acc[key] = value;
            return acc;
          }, {});
          return `Задача "${fields.titleTask}" обновлена. Описание: ${fields.descriptionTask}`;
        } catch (e) {
          console.error('Ошибка при форматировании уведомления:', e);
          return rawMessage;
        }
      }
      return rawMessage;
    };

    onMounted(() => {
      if (store.state.userId) {
        webSocketService.value = new WebSocketService(store.state.userId, addNotification);
      } else {
        console.warn('userId не определён');
      }
    });

    onUnmounted(() => {
      if (webSocketService.value) {
        webSocketService.value.disconnect();
      }
    });

    return {
      notifications,
    };
  },
};
</script>

<style scoped>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

nav {
  padding: 30px;
}

nav a {
  font-weight: bold;
  color: #2c3e50;
}

nav a.router-link-exact-active {
  color: #42b983;
}

.notifications {
  position: fixed;
  top: 20px;
  right: 20px;
  width: 300px;
  z-index: 1000;
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

.notification-popup p {
  margin: 0;
  font-size: 14px;
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
  transition: width 0.25s linear;
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