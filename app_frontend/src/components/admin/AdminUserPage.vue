<template>
  <div class="container">
    <h1>Администрирование пользователей</h1>

    <!-- Кнопка создания пользователя -->
    <button class="btn btn-primary" @click="openCreateUserModal">Создать пользователя</button>

    <!-- Таблица пользователей -->
    <h2>Пользователи</h2>
    <table class="table">
      <thead>
      <tr>
        <th>ID</th>
        <th>Логин</th>
        <th>Роль</th>
        <th>Действия</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="user in users" :key="user.userId">
        <td>{{ user.userId }}</td>
        <td>{{ user.login }}</td>
        <td>
          <select v-model="user.roleId" @change="updateUserRole(user)">
            <option v-for="role in roles" :key="role.roleId" :value="role.roleId">{{ role.role }}</option>
          </select>
        </td>
        <td>
          <button class="btn btn-warning" @click="openEditUserModal(user)">Редактировать</button>
          <button class="btn btn-danger" @click="deleteUser(user.userId)">Удалить</button>
        </td>
      </tr>
      </tbody>
    </table>

    <!-- Таблица проектов -->
    <h2>Проекты</h2>
    <table class="table">
      <thead>
      <tr>
        <th>Проект</th>
        <th>Статус</th>
        <th>Завершение</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="project in projects" :key="project.id">
        <td>{{ project.title }}</td>
        <td>{{ project.status }}</td>
        <td>
          <div class="progress">
            <div class="progress-bar" :style="{ width: project.completionPercentage + '%' }">{{ project.completionPercentage }}%</div>
          </div>
        </td>
      </tr>
      </tbody>
    </table>

    <!-- Таблица открытых сессий -->
    <h2>Пользователи онлайн</h2>
    <table class="table">
      <thead>
      <tr>
        <th>Id</th>
        <th>Login</th>
        <th>Имя</th>
        <th>Фамилия</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="session in activeSessions" :key="session.userId">
        <td>{{ session.id }}</td>
        <td>{{ session.username }}</td>
        <td>{{ session.firstName }}</td>
        <td>{{session.lastName }}</td>
      </tr>
      </tbody>
    </table>

    <!-- Модальное окно для создания пользователя -->
    <div class="modal fade" id="createUserModal" tabindex="-1" role="dialog" aria-labelledby="createUserModalLabel" aria-hidden="true">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="createUserModalLabel">Создать пользователя</h5>
            <button type="button" class="close" @click="closeCreateUserModal" aria-label="Close">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <form @submit.prevent="createUser">
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label">Логин</label>
                <input type="text" class="form-control" v-model="newUser.login" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Пароль</label>
                <input type="password" class="form-control" v-model="newUser.passwordUser" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Роль</label>
                <select class="form-select" v-model="newUser.roleId" required>
                  <option v-for="role in roles" :key="role.roleId" :value="role.roleId">{{ role.role }}</option>
                </select>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeCreateUserModal">Закрыть</button>
              <button type="submit" class="btn btn-primary">Создать</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Модальное окно для редактирования пользователя -->
    <div class="modal fade" id="editUserModal" tabindex="-1" role="dialog" aria-labelledby="editUserModalLabel" aria-hidden="true">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="editUserModalLabel">Редактировать пользователя</h5>
            <button type="button" class="close" @click="closeEditUserModal" aria-label="Close">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <form @submit.prevent="updateUser">
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label">Логин</label>
                <input type="text" class="form-control" v-model="currentUser.login" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Роль</label>
                <select class="form-select" v-model="currentUser.roleId" required>
                  <option v-for="role in roles" :key="role.roleId" :value="role.roleId">{{ role.role }}</option>
                </select>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeEditUserModal">Закрыть</button>
              <button type="submit" class="btn btn-warning">Сохранить</button>
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
      users: [],
      projects: [],
      activeSessions: [],
      roles: [],
      newUser: { login: '', passwordUser: '', roleId: null },
      currentUser: {}
    };
  },
  methods: {
    openCreateUserModal() {
      this.createUserModalInstance =   new window.bootstrap.Modal(document.getElementById('createUserModal'));
      this.createUserModalInstance.show();
    },
    closeCreateUserModal() {
      if (this.createUserModalInstance) this.createUserModalInstance.hide();
      this.resetNewUser();
    },
    openEditUserModal(user) {
      this.currentUser = { ...user };
      this.editUserModalInstance =  new window.bootstrap.Modal(document.getElementById('editUserModal'));
      this.editUserModalInstance.show();
    },
    closeEditUserModal() {
      if (this.editUserModalInstance) this.editUserModalInstance.hide();
      this.resetCurrentUser();
    },
    fetchUsers() {
      apiClient.get('/users/list')
          .then(response => {
            this.users = response.data;
          })
          .catch(error => {
            console.error("Ошибка при получении пользователей:", error);
          });
    },
    fetchProjects() {
      apiClient.get('/project/list')
          .then(response => {
            this.projects = response.data;
          })
          .catch(error => {
            console.error("Ошибка при получении проектов:", error);
          });
    },
    fetchActiveSessions() {
      apiClient.get('/users/keycloak/online-users')
          .then(response => {
            this.activeSessions = response.data;
            console.log(this.activeSessions);
          })
          .catch(error => {
            console.error("Ошибка при получении открытых сессий:", error);
          });
    },
    fetchRoles() {
      apiClient.get('/role/list')
          .then(response => {
            this.roles = response.data;
            console.log(this.roles);
          })
          .catch(error => {
            console.error("Ошибка при получении ролей:", error);
          });
    },

    createUser() {
      apiClient.post('/users/create', this.newUser)
          .then(() => {
            this.fetchUsers();
            $('#createUserModal').modal('hide'); // Закрыть модальное окно
          })
          .catch(error => {
            console.error("Ошибка при создании пользователя:", error);
          });
    },

    updateUser() {
      apiClient.put(`/users/update/${this.currentUser.userId}`, this.currentUser)
          .then(() => {
            this.fetchUsers();
            $('#editUserModal').modal('hide'); // Закрыть модальное окно
          })
          .catch(error => {
            console.error("Ошибка при обновлении пользователя:", error);
          });
    },
    deleteUser(userId) {
      apiClient.delete(`/users/delete/${userId}`)
          .then(() => {
            this.fetchUsers();
          })
          .catch(error => {
            console.error("Ошибка при удалении пользователя:", error);
          });
    },
    updateUserRole(user) {
      apiClient.put(`/users/${user.userId}/updateRole`, { roleId: user.roleId })
          .catch(error => {
            console.error("Ошибка при обновлении роли пользователя:", error);
          });
    },
    resetNewUser() {
      this.newUser = { login: '', passwordUser: '', roleId: null };
    },
    resetCurrentUser() {
      this.currentUser = {};
    }
  },
  created() {
    this.fetchUsers();
    this.fetchProjects();
    this.fetchActiveSessions();
    this.fetchRoles();
  }
};
</script>

<style scoped>
.progress {
  height: 20px;
}
</style>
