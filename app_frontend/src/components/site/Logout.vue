<template>
  <div class="logout-container">
    <div class="card" style="width: 18rem;">
      <div class="card-body">
        <h5 class="card-title">Выход</h5>
        <p class="card-text">Вы уверены что хотите выйти?</p>
        <div class="button-container">
          <button @click="logout" class="logout-button" title="Выйти из аккаунта">
            <span class="logout-icon">🔒</span>
            <span class="logout-text">Выйти</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import keycloak from "@/main";

export default {
  name: "Logout",
  methods: {
    logout() {
      keycloak.logout({
        redirectUri: window.location.origin,
      }).then(() => {
        localStorage.clear();
        this.$store.commit("setUserLogin", null);
        this.$store.commit("setUserRole", null);
        this.$store.commit("setUserData", null);
        this.$store.commit("setUserId", null);

        this.$router.push({ name: "Home" });
      }).catch(error => {
        console.error("Ошибка при выходе из Keycloak:", error);
      });
    },
  },
};
</script>

<style scoped>
.logout-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 50vh; /* Высота контейнера равна высоте окна */
}

.button-container {
  display: flex;
  justify-content: center; /* Центрирование кнопки */
}

.logout-button {
  background-color: #dc3545;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: background-color 0.3s ease;
}

.logout-button:hover {
  background-color: #c82333;
}

.logout-icon {
  margin-right: 8px;
  font-size: 18px;
}

.logout-text {
  font-size: 16px;
}

.logout-button:focus {
  outline: none;
  box-shadow: 0 0 4px #fff;
}
</style>
