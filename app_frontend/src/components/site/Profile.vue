<template>
  <div class="container mt-5">
    <h2 class="mb-4 text-primary fw-bold">Профиль пользователя</h2>
    <div v-if="user" class="row">
      <!-- Основная информация и аватар -->
      <div class="col-md-4">
        <div class="card shadow-sm border-0 mb-4">
          <div class="card-body text-center">
            <img
                :src="avatarDataUrl || '/default-avatar.png'"
                alt="Аватар"
                class="rounded-circle mb-3"
                style="width: 150px; height: 150px; object-fit: cover;"
            />
            <h4 class="text-primary">{{ keycloakUser?.username || user.login }}</h4>
            <p class="text-muted">{{ user.email || 'Email не указан' }}</p>
            <p class="text-muted">Роль: {{ roleName || 'Не определена' }}</p>
            <button
                class="btn btn-primary w-100 mb-2"
                data-bs-toggle="modal"
                data-bs-target="#uploadAvatarModal"
            >
              <i class="bi bi-camera me-2"></i>Загрузить аватар
            </button>
            <button
                class="btn btn-outline-secondary w-100"
                data-bs-toggle="modal"
                data-bs-target="#editProfileModal"
            >
              <i class="bi bi-pencil me-2"></i>Редактировать профиль
            </button>
          </div>
        </div>
      </div>
      <!-- Детали профиля и активность -->
      <div class="col-md-8">
        <div class="card shadow-sm border-0 mb-4">
          <div class="card-header bg-primary text-white">
            <h5 class="mb-0">Информация о пользователе</h5>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-md-6">
                <p><strong>Логин:</strong> {{ keycloakUser?.username || user.login }}</p>
                <p><strong>Email:</strong> {{ user.email || 'Не указан' }}</p>
                <p><strong>Дата регистрации:</strong> {{ formatDate(user.createdAt) }}</p>
                <p><strong>Последнее обновление:</strong> {{ formatDate(user.updatedAt) }}</p>
              </div>
              <div class="col-md-6">
                <p><strong>Роль:</strong> {{ roleName || 'Не определена' }}</p>
                <p><strong>Проекты:</strong> {{ user.projects?.length || 0 }}</p>
                <p><strong>Созданные задачи:</strong> {{ user.tasksCreatedByUser?.length || 0 }}</p>
                <p><strong>Взятые тикеты:</strong> {{ user.ticketsCreatedByUser?.length || 0 }}</p>
              </div>
            </div>
          </div>
        </div>
        <!-- Активность -->
        <div class="card shadow-sm border-0">
          <div class="card-header bg-primary text-white">
            <h5 class="mb-0">Активность</h5>
          </div>
          <div class="card-body">
            <ul class="nav nav-tabs mb-3">
              <li class="nav-item">
                <a
                    class="nav-link"
                    :class="{ active: activeTab === 'projects' }"
                    @click="activeTab = 'projects'"
                    href="#"
                >Проекты</a>
              </li>
              <li class="nav-item">
                <a
                    class="nav-link"
                    :class="{ active: activeTab === 'tasks' }"
                    @click="activeTab = 'tasks'"
                    href="#"
                >Задачи</a>
              </li>
              <li class="nav-item">
                <a
                    class="nav-link"
                    :class="{ active: activeTab === 'tickets' }"
                    @click="activeTab = 'tickets'"
                    href="#"
                >Тикеты</a>
              </li>
            </ul>
            <div v-if="activeTab === 'projects'" class="activity-list">
              <div v-if="user.projects?.length" class="list-group">
                <router-link
                    v-for="project in user.projects"
                    :key="project.projectId"
                    :to="`/projects/${project.projectId}`"
                    class="list-group-item list-group-item-action"
                >
                  {{ project.titleProject }}
                </router-link>
              </div>
              <p v-else class="text-muted">Проекты отсутствуют.</p>
            </div>
            <div v-if="activeTab === 'tasks'" class="activity-list">
              <div v-if="user.tasksCreatedByUser?.length" class="list-group">
                <router-link
                    v-for="task in user.tasksCreatedByUser"
                    :key="task.taskId"
                    :to="`/tasks/${task.taskId}`"
                    class="list-group-item list-group-item-action"
                >
                  {{ task.titleTask }} ({{ task.status ? 'Завершено' : 'В процессе' }})
                </router-link>
              </div>
              <p v-else class="text-muted">Задачи отсутствуют.</p>
            </div>
            <div v-if="activeTab === 'tickets'" class="activity-list">
              <div v-if="user.ticketsCreatedByUser?.length" class="list-group">
                <router-link
                    v-for="ticket in user.ticketsCreatedByUser"
                    :key="ticket.ticketId"
                    :to="`/tickets/${ticket.ticketId}`"
                    class="list-group-item list-group-item-action"
                >
                  {{ ticket.descrption }} ({{ ticket.status === 'open' ? 'Открытый' : ticket.status === 'closed' ? 'Закрытый' : 'В процессе' }})
                </router-link>
              </div>
              <p v-else class="text-muted">Тикеты отсутствуют.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="text-center">
      <p class="text-muted">Загрузка данных...</p>
    </div>

    <!-- Модальное окно для загрузки аватара -->
    <div
        class="modal fade"
        id="uploadAvatarModal"
        tabindex="-1"
        aria-labelledby="uploadAvatarModalLabel"
        aria-hidden="true"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-primary text-white">
            <h5 class="modal-title" id="uploadAvatarModalLabel">Загрузить аватар</h5>
            <button
                type="button"
                class="btn-close btn-close-white"
                data-bs-dismiss="modal"
                aria-label="Закрыть"
            ></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="uploadAvatar">
              <div class="mb-3">
                <label for="avatarInput" class="form-label">Выберите изображение</label>
                <input
                    type="file"
                    class="form-control"
                    id="avatarInput"
                    accept="image/*"
                    @change="onAvatarSelected"
                    required
                />
              </div>
              <button type="submit" class="btn btn-primary w-100">Загрузить</button>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- Модальное окно для редактирования профиля -->
    <div
        class="modal fade"
        id="editProfileModal"
        tabindex="-1"
        aria-labelledby="editProfileModalLabel"
        aria-hidden="true"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-primary text-white">
            <h5 class="modal-title" id="editProfileModalLabel">Редактировать профиль</h5>
            <button
                type="button"
                class="btn-close btn-close-white"
                data-bs-dismiss="modal"
                aria-label="Закрыть"
            ></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="updateProfile">
              <div class="mb-3">
                <label for="usernameInput" class="form-label">Имя пользователя</label>
                <input
                    type="text"
                    class="form-control"
                    id="usernameInput"
                    v-model="editedProfile.username"
                    required
                />
              </div>
              <div class="mb-3">
                <label for="emailInput" class="form-label">Email</label>
                <input
                    type="email"
                    class="form-control"
                    id="emailInput"
                    v-model="editedProfile.email"
                    required
                />
              </div>
              <button type="submit" class="btn btn-primary w-100">Сохранить</button>
            </form>
            <hr />
            <form @submit.prevent="updatePassword">
              <div class="mb-3">
                <label for="currentPasswordInput" class="form-label">Текущий пароль</label>
                <input
                    type="password"
                    class="form-control"
                    id="currentPasswordInput"
                    v-model="passwordForm.currentPassword"
                    required
                />
              </div>
              <div class="mb-3">
                <label for="newPasswordInput" class="form-label">Новый пароль</label>
                <input
                    type="password"
                    class="form-control"
                    id="newPasswordInput"
                    v-model="passwordForm.newPassword"
                    required
                />
              </div>
              <div class="mb-3">
                <label for="confirmPasswordInput" class="form-label">Подтвердите пароль</label>
                <input
                    type="password"
                    class="form-control"
                    id="confirmPasswordInput"
                    v-model="passwordForm.confirmPassword"
                    required
                />
              </div>
              <button type="submit" class="btn btn-primary w-100">Обновить пароль</button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useStore } from 'vuex';
import apiClient from '@/utils/apiClient';
import { useRouter } from 'vue-router';

// Данные
const store = useStore();
const router = useRouter();
const user = ref(null);
const keycloakUser = ref(null);
const roleName = ref('');
const activeTab = ref('projects');
const editedProfile = ref({
  username: '',
  email: '',
});
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
});
const avatarFile = ref(null);
const avatarDataUrl = ref(null);

// Вычисляемые свойства
const userId = computed(() => store.state.userId);
const roleId = computed(() => user.value?.roleId);

// Методы
const fetchUserData = async () => {
  try {
    // Получение данных из базы данных
    const response = await apiClient.get(`/users/get/${userId.value}`);
    user.value = response.data;
    console.log('User data:', user.value);

    // Загрузка аватара
    if (user.value?.url) {
      await fetchAvatar(user.value.url);
    }

    // Получение данных из Keycloak
    const token = localStorage.getItem('token');
    if (!token) {
      console.warn('Токен отсутствует в localStorage');
      alert('Пожалуйста, войдите в систему заново.');
      router.push('/login');
      return;
    }

    const keycloakResponse = await apiClient.get('/users/keycloak', {
      headers: { Authorization: `Bearer ${token}` },
    });

    // Поиск пользователя в Keycloak
    keycloakUser.value = keycloakResponse.data.find(u => u.username === user.value.login);
    if (!keycloakUser.value) {
      console.warn(`Пользователь с логином ${user.value.login} не найден в Keycloak`);
      keycloakUser.value = { username: user.value.login }; // Fallback на login из базы
    }

    // Получение названия роли
    if (roleId.value) {
      const roleResponse = await apiClient.get(`/role/get/${roleId.value}`);
      roleName.value = roleResponse.data.nameRole || 'Не определена';
    } else {
      console.warn('roleId не определен');
      roleName.value = 'Не определена';
    }

    // Инициализация формы редактирования
    editedProfile.value.username = keycloakUser.value.username || user.value.login;
    editedProfile.value.email = user.value.email || '';
  } catch (error) {
    console.error('Ошибка при загрузке данных пользователя:', error);
    alert('Не удалось загрузить данные профиля. Попробуйте позже.');
  }
};

const fetchAvatar = async (url) => {
  try {
    const response = await apiClient.get(url, { responseType: 'blob' });
    const reader = new FileReader();
    reader.onloadend = () => {
      avatarDataUrl.value = reader.result;
    };
    reader.readAsDataURL(response.data);
  } catch (error) {
    console.error('Ошибка при загрузке аватара:', error);
    avatarDataUrl.value = '/default-avatar.png';
  }
};

const formatDate = (date) => {
  return date ? new Date(date).toLocaleDateString('ru-RU', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }) : 'Не указана';
};

const onAvatarSelected = (event) => {
  avatarFile.value = event.target.files[0];
};

const uploadAvatar = async () => {
  if (!avatarFile.value) {
    alert('Выберите файл для загрузки.');
    return;
  }
  const formData = new FormData();
  formData.append('avatar', avatarFile.value);
  try {
    const response = await apiClient.post(`/users/${userId.value}/uploadAvatar`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    console.log('Upload response:', response.data);
    user.value.url = response.data.avatarUrl;
    await fetchAvatar(user.value.url); // Загружаем новый аватар
    const modal = bootstrap.Modal.getInstance(document.getElementById('uploadAvatarModal'));
    modal.hide();
    document.querySelectorAll('.modal-backdrop').forEach((backdrop) => backdrop.remove());
    alert('Аватар успешно загружен.');
  } catch (error) {
    console.error('Ошибка при загрузке аватара:', error);
    alert('Не удалось загрузить аватар. Попробуйте позже.');
  }
};

const updateProfile = async () => {
  try {
    // Обновление имени пользователя
    await apiClient.post(`/users/${userId.value}/updateUsername`, {
      username: editedProfile.value.username,
    });
    // Обновление email
    await apiClient.post(`/users/${userId.value}/updateEmail`, {
      email: editedProfile.value.email,
    });
    const modal = bootstrap.Modal.getInstance(document.getElementById('editProfileModal'));
    modal.hide();
    document.querySelectorAll('.modal-backdrop').forEach((backdrop) => backdrop.remove());
    await fetchUserData();
    alert('Профиль успешно обновлен.');
  } catch (error) {
    console.error('Ошибка при обновлении профиля:', error);
    alert('Не удалось обновить профиль. Попробуйте позже.');
  }
};

const updatePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    alert('Новый пароль и подтверждение пароля не совпадают.');
    return;
  }
  try {
    const response = await apiClient.post(`/users/${userId.value}/updatePassword`, passwordForm.value);
    if (response.data.includes('успешно')) {
      const modal = bootstrap.Modal.getInstance(document.getElementById('editProfileModal'));
      modal.hide();
      document.querySelectorAll('.modal-backdrop').forEach((backdrop) => backdrop.remove());
      passwordForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' };
      alert('Пароль успешно обновлен.');
    } else {
      alert(response.data);
    }
  } catch (error) {
    console.error('Ошибка при обновлении пароля:', error);
    alert('Не удалось обновить пароль. Попробуйте позже.');
  }
};

// Загрузка данных при монтировании
onMounted(() => {
  if (userId.value) {
    fetchUserData();
  } else {
    console.warn('userId не определен');
    router.push('/login');
  }
});
</script>

<style scoped>
.container {
  margin-top: 40px;
  max-width: 1200px;
}

h2, h4, h5 {
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

.btn-outline-secondary {
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
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

.form-control {
  border-radius: 8px;
  border: 1px solid #ced4da;
  transition: border-color 0.2s ease;
}

.form-control:focus {
  border-color: #1a3c6d;
  box-shadow: 0 0 5px rgba(26, 60, 109, 0.3);
}

.nav-tabs .nav-link {
  color: #1a3c6d;
  border-radius: 8px 8px 0 0;
}

.nav-tabs .nav-link.active {
  background-color: #1a3c6d;
  color: white;
  border-color: #1a3c6d;
}

.list-group-item {
  border-radius: 8px;
  margin-bottom: 8px;
  transition: background-color 0.2s ease;
}

.list-group-item:hover {
  background-color: #f8f9fa;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.card, .list-group-item {
  animation: fadeIn 0.3s ease-out;
}
</style>