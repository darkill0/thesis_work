import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import SidebarLayout from '@/components/site/Sidebar.vue';
import ProjectList from '@/components/project/ProjectList.vue';
import ProjectDetails from '@/components/project/ProjectDetails.vue';
import TaskDetails from '@/components/task/TaskDetails.vue';
import Logout from '@/components/site/Logout.vue';
import ProjectsManager from '@/components/project/ProjectsManager.vue';
import TaskManager from '@/components/task/TaskManager.vue';
import TicketPage from '@/components/ticket/TicketPage.vue';
import TicketDetails from '@/components/ticket/TicketDetails.vue';
import MyTickets from '@/components/ticket/MyTickets.vue';
import AcceptedTickets from '@/components/ticket/AcceptedTickets.vue';
import UsersProject from '@/components/project/UsersProject.vue';
import UsersTask from '@/components/task/UsersTask.vue';
import AdminUserPage from '@/components/admin/AdminUserPage.vue';
import AdminProjects from '@/components/admin/AdminProjects.vue';
import AdminTasks from '@/components/admin/AdminTasks.vue';
import AdminTickets from '@/components/admin/AdminTickets.vue';
import Todo from '@/views/Todo.vue';
import Profile from '@/components/site/Profile.vue';
import UserDashboard from '@/components/dashboards/UserDashboard.vue';
import store from '@/store';

const routes = [
  {
    path: '/',
    name: 'home',
    component: Profile,
    meta: { requiresAuth: false },
  },
  {
    path: '/app',
    component: SidebarLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '/user/dashboard',
        name: 'dashboardUser',
        component: UserDashboard,
        meta: { requiresAuth: true },
      },
      {
        path: '/logout',
        name: 'logout',
        component: Logout,
        meta: { requiresAuth: true },
      },
      {
        path: '/projects',
        name: 'projects',
        component: ProjectList,
        meta: { requiresAuth: true },
      },
      {
        path: '/projects/:id',
        name: 'projectDetails',
        component: ProjectDetails,
        meta: { requiresAuth: true },
      },
      {
        path: '/projects/manager',
        name: 'projectManager',
        component: ProjectsManager,
        meta: { requiresAuth: true, roles: ['manager'] },
      },
      {
        path: '/task/manager',
        name: 'taskManager',
        component: TaskManager,
        meta: { requiresAuth: true, roles: ['manager'] },
      },
      {
        path: '/tasks/:id',
        name: 'TaskDetails',
        component: TaskDetails,
        meta: { requiresAuth: true },
      },
      {
        path: '/tickets',
        name: 'tickets',
        component: TicketPage,
        meta: { requiresAuth: true },
      },
      {
        path: '/tickets/:id',
        name: 'ticketDetails',
        component: TicketDetails,
        meta: { requiresAuth: true },
      },
      {
        path: '/tickets/my',
        name: 'ticketsMy',
        component: MyTickets,
        meta: { requiresAuth: true },
      },
      {
        path: '/tickets/accepted',
        name: 'ticketsAccepted',
        component: AcceptedTickets,
        meta: { requiresAuth: true },
      },
      {
        path: '/users/projects',
        name: 'usersProject',
        component: UsersProject,
        meta: { requiresAuth: true, roles: ['user'] },
      },
      {
        path: '/users/tasks',
        name: 'usersTasks',
        component: UsersTask,
        meta: { requiresAuth: true, roles: ['user'] },
      },
      {
        path: '/admin/users',
        name: 'adminUsers',
        component: AdminUserPage,
        meta: { requiresAuth: true, roles: ['admin'] },
      },
      {
        path: '/admin/projects',
        name: 'adminProjects',
        component: AdminProjects,
        meta: { requiresAuth: true, roles: ['admin'] },
      },
      {
        path: '/admin/tasks',
        name: 'adminTasks',
        component: AdminTasks,
        meta: { requiresAuth: true, roles: ['admin'] },
      },
      {
        path: '/admin/tickets',
        name: 'adminTickets',
        component: AdminTickets,
        meta: { requiresAuth: true, roles: ['admin'] },
      },
      {
        path: '/profile',
        name: 'profile',
        component: Profile,
        meta: { requiresAuth: true },
      },
      {
        path: '/todo/:id',
        name: 'todo',
        component: Todo,
        meta: { requiresAuth: true },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    return savedPosition || { top: 0 };
  },
});

router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const isAuthenticated = store.getters.isAuthenticated;
  const userRole = store.getters.getUserRole;

  console.log('Router beforeEach:', { to: to.path, isAuthenticated, userRole });

  if (requiresAuth && !isAuthenticated) {
    next('/');
  } else if (to.path === '/' && isAuthenticated) {
    next('/user/dashboard');
  } else if (to.meta.roles && !to.meta.roles.includes(userRole)) {
    next('/user/dashboard');
  } else {
    next();
  }
});

export default router;