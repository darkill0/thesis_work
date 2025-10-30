<template>
  <div class="sidebar-container">
    <nav id="sidebar" :class="{ 'collapsed': isCollapsed }">
      <!-- Header -->
      <div class="sidebar-header">
        <button class="toggle-btn" @click="toggleSidebar" aria-label="Toggle sidebar">
          <span class="material-icons">{{ isCollapsed ? 'chevron_right' : 'menu' }}</span>
        </button>
      </div>

      <!-- Menu -->
      <div class="sidebar-menu">
        <ul class="menu-list">
          <li v-if="userRole !== 'admin'" class="menu-item" :class="{ 'active': $route.path === '/user/dashboard' }">
            <router-link to="/user/dashboard">
              <span class="material-icons">home</span>
              <span class="menu-text">Главная</span>
            </router-link>
          </li>
          <li v-if="userRole !== 'admin'" class="menu-item" :class="{ 'active': $route.path === '/projects' }">
            <router-link to="/projects">
              <span class="material-icons">folder</span>
              <span class="menu-text">Проекты</span>
            </router-link>
          </li>
          <li v-if="userRole === 'manager'" class="menu-item" :class="{ 'active': $route.path === '/projects/manager' }">
            <router-link to="/projects/manager">
              <span class="material-icons">work_history</span>
              <span class="menu-text">Мои проекты</span>
            </router-link>
          </li>
          <li v-if="userRole === 'admin'" class="menu-item" :class="{ 'active': $route.path === '/admin/users' }">
            <router-link to="/admin/users">
              <span class="material-icons">group</span>
              <span class="menu-text">Пользователи</span>
            </router-link>
          </li>
          <li v-if="userRole === 'admin'" class="menu-item" :class="{ 'active': $route.path === '/admin/projects' }">
            <router-link to="/admin/projects">
              <span class="material-icons">dashboard</span>
              <span class="menu-text">Проекты</span>
            </router-link>
          </li>
          <li v-if="userRole === 'admin'" class="menu-item" :class="{ 'active': $route.path === '/admin/tasks' }">
            <router-link to="/admin/tasks">
              <span class="material-icons">task</span>
              <span class="menu-text">Задания</span>
            </router-link>
          </li>
          <li v-if="userRole === 'admin'" class="menu-item" :class="{ 'active': $route.path === '/admin/tickets' }">
            <router-link to="/admin/tickets">
              <span class="material-icons">confirmation_number</span>
              <span class="menu-text">Тикеты</span>
            </router-link>
          </li>
          <li v-if="userRole === 'manager'" class="menu-item" :class="{ 'active': $route.path === '/task/manager' }">
            <router-link to="/task/manager">
              <span class="material-icons">flag</span>
              <span class="menu-text">Задания</span>
            </router-link>
          </li>
          <li v-if="userRole === 'user'" class="menu-item" :class="{ 'active': $route.path === '/users/projects' }">
            <router-link to="/users/projects">
              <span class="material-icons">star</span>
              <span class="menu-text">Мои проекты</span>
            </router-link>
          </li>
          <li v-if="userRole === 'user'" class="menu-item" :class="{ 'active': $route.path === '/users/tasks' }">
            <router-link to="/users/tasks">
              <span class="material-icons">check_circle</span>
              <span class="menu-text">Задания</span>
            </router-link>
          </li>
          <li v-if="userRole !== 'admin'" class="menu-item" :class="{ 'active': $route.path === '/tickets' }">
            <router-link to="/tickets">
              <span class="material-icons">confirmation_number</span>
              <span class="menu-text">Тикеты</span>
            </router-link>
          </li>
          <li v-if="userRole !== 'admin'" class="menu-item" :class="{ 'active': $route.path === '/tickets/my' }">
            <router-link to="/tickets/my">
              <span class="material-icons">task_alt</span>
              <span class="menu-text">Мои тикеты</span>
            </router-link>
          </li>
          <li v-if="userRole !== 'admin'" class="menu-item" :class="{ 'active': $route.path === '/tickets/accepted' }">
            <router-link to="/tickets/accepted">
              <span class="material-icons">fact_check</span>
              <span class="menu-text">Взятые тикеты</span>
            </router-link>
          </li>
          <li class="menu-item" :class="{ 'active': $route.path === `/todo/${userId}` }">
            <router-link :to="`/todo/${userId}`">
              <span class="material-icons">list</span>
              <span class="menu-text">Список дел</span>
            </router-link>
          </li>
          <li class="menu-item" :class="{ 'active': $route.path === '/profile' }">
            <router-link to="/profile">
              <span class="material-icons">person</span>
              <span class="menu-text">Профиль</span>
            </router-link>
          </li>
          <li class="menu-item" :class="{ 'active': $route.path === '/logout' }">
            <router-link to="/logout">
              <span class="material-icons">exit_to_app</span>
              <span class="menu-text">Выход</span>
            </router-link>
          </li>
        </ul>
      </div>
    </nav>

    <main id="content" :class="{ 'expanded': isCollapsed }">
      <router-view></router-view>
    </main>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';

export default {
  name: 'SidebarLayout',
  data() {
    return {
      isCollapsed: false,
    };
  },
  methods: {
    toggleSidebar() {
      this.isCollapsed = !this.isCollapsed;
    },
  },
  computed: {
    ...mapGetters(['getUserRole', 'getUserId', 'getUserData']),
    userRole() {
      return this.getUserRole;
    },
    userId() {
      return this.getUserId;
    },
    userData() {
      return this.getUserData;
    },
  },
  mounted() {
    console.log('SidebarLayout mounted. userRole:', this.userRole, 'userId:', this.userId, 'userData:', this.userData);
  },
};
</script>

<style scoped>
.sidebar-container {
  display: grid;
  grid-template-columns: minmax(0, 280px) 1fr;
  height: 100vh;
  transition: all 0.3s ease;
}

#sidebar {
  display: grid;
  grid-template-rows: auto 1fr;
  background: linear-gradient(135deg, #2b2d42 0%, #1d3557 100%);
  color: #ffffff;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

#sidebar.collapsed {
  width: 60px;
}

.sidebar-header {
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
  gap: 15px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 15px;
}

.avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-size: cover;
  background-position: center;
  border: 2px solid #ffffff;
  transition: all 0.3s ease;
}

#sidebar.collapsed .avatar {
  width: 40px;
  height: 40px;
}

.user-name {
  font-size: 1.1rem;
  font-weight: 500;
  margin: 0;
  transition: opacity 0.2s ease;
}

#sidebar.collapsed .user-name {
  opacity: 0;
  position: absolute;
}

.toggle-btn {
  background: none;
  border: none;
  color: #ffffff;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.toggle-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.sidebar-menu {
  padding: 20px 0;
  overflow-y: auto;
}

.menu-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.menu-item {
  margin: 5px 0;
}

.menu-item a {
  display: grid;
  grid-template-columns: 40px 1fr;
  align-items: center;
  padding: 15px 20px;
  color: #ffffff;
  text-decoration: none;
  transition: all 0.3s ease;
}

.menu-item a:hover {
  background: rgba(255, 255, 255, 0.1);
  padding-left: 25px;
}

.menu-item.active a {
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
}

.material-icons {
  font-size: 24px;
  justify-self: center;
}

.menu-text {
  font-size: 1rem;
  transition: opacity 0.2s ease;
}

#sidebar.collapsed .menu-text {
  opacity: 0;
  position: absolute;
}

#content {
  padding: 30px;
  background: #f5f7fa;
  transition: all 0.3s ease;
  overflow-y: auto;
}

#content.expanded {
  grid-column: 1 / -1;
}

@keyframes slideIn {
  from {
    transform: translateX(-100%);
  }
  to {
    transform: translateX(0);
  }
}

#sidebar {
  animation: slideIn 0.3s ease forwards;
}

.sidebar-menu::-webkit-scrollbar {
  width: 6px;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: transparent;
}
</style>