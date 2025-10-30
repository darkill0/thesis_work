import { createStore } from 'vuex';

export default createStore({
  state: {
    userLogin: null,
    userRole: null,
    userData: null,
    userId: null,
  },
  getters: {
    getUserLogin(state) {
      return state.userLogin;
    },
    getUserRole(state) {
      return state.userRole;
    },
    getUserData(state) {
      return state.userData;
    },
    getUserId(state) {
      return state.userId;
    },
    isAuthenticated(state) {
      return !!state.userId; // Считаем пользователя авторизованным, если есть userId
    },
  },
  mutations: {
    setUserLogin(state, login) {
      state.userLogin = login;
    },
    setUserRole(state, role) {
      state.userRole = role;
    },
    setUserData(state, data) {
      state.userData = data;
    },
    setUserId(state, id) {
      state.userId = id;
    },
  },
  actions: {
    login({ commit }, { login, role, userData, userId }) {
      commit('setUserLogin', login);
      commit('setUserRole', role);
      commit('setUserData', userData);
      commit('setUserId', userId);
    },
    logout({ commit }) {
      commit('setUserLogin', null);
      commit('setUserRole', null);
      commit('setUserData', null);
      commit('setUserId', null);
    },
  },
  modules: {},
});