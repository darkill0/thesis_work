import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import Keycloak from 'keycloak-js'
import { jwtDecode } from 'jwt-decode'
import apiClient from "@/utils/apiClient"
import axios from "axios"

// Конфигурация Keycloak
const keycloak = new Keycloak({
    url: 'http://localhost:18080/',
    realm: 'sandbox',
    clientId: 'front-end',
})

// Функция для получения ролей пользователя из токена
const getUserRoles = (token) => {
    if (token) {
        const decoded = jwtDecode(token)
        return decoded.resource_access["front-end"]["roles"][0].split("_")
    }
    return []
}

// Функция для получения данных пользователя
const fetchUserData = (login) => {
    return apiClient.get(`users/findByLogin/${login}`)
        .then(response => response.data)
        .catch(error => {
            console.error('Не удалось получить данные пользователя:', error)
            return null
        })
}

// Функция для ручного обновления токена
const refreshToken = async () => {
    try {
        const response = await axios.post(`${keycloak.authServerUrl}/realms/${keycloak.realm}/protocol/openid-connect/token`, new URLSearchParams({
            client_id: keycloak.clientId,
            grant_type: 'refresh_token',
            refresh_token: keycloak.refreshToken,
        }), {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
        })

        // Обновляем токены в Keycloak
        keycloak.token = response.data.access_token
        keycloak.refreshToken = response.data.refresh_token

        localStorage.setItem('token', response.data.access_token)
        localStorage.setItem('refreshToken', response.data.refresh_token)

        console.log('Токен обновлен:', response.data.access_token)
    } catch (error) {
        console.error('Не удалось обновить токен:', error)
    }
}

// Функция инициализации Keycloak и Vue
const initKeycloak = () => {
    return new Promise((resolve, reject) => {
        keycloak.init({
            onLoad: 'login-required',
        }).then(authenticated => {
            if (authenticated) {
                localStorage.clear()
                const token = keycloak.token
                console.log('Извлеченный токен:', token)

                if (token && token.split('.').length === 3) {
                    localStorage.setItem('token', token)
                    const decoded = jwtDecode(token)
                    const login = decoded.preferred_username
                    localStorage.setItem('userLogin', login)
                    localStorage.setItem('userRole', getUserRoles(token)[1].toLowerCase())

                    store.commit("setUserLogin", login)
                    store.commit("setUserRole", getUserRoles(token)[1].toLowerCase())

                    fetchUserData(login).then(userData => {
                        if (userData) {
                            localStorage.setItem('userData', JSON.stringify(userData))
                            localStorage.setItem('userId', userData.userId)
                            store.commit('setUserData', userData)
                            store.commit('setUserId', userData.userId)
                        }
                    })

                    // Обновление токена каждые 4 минуты
                    setInterval(() => {
                        refreshToken() // Вызов функции обновления токена
                    }, 240000) // 4 минуты

                } else {
                    console.error('Недействительный токен:', token)
                }
            }
            resolve(authenticated)
        }).catch(error => {
            console.error('Ошибка инициализации Keycloak:', error)
            reject(error)
        })
    })
}

// Интерсептор Axios для обработки 401 ошибки
apiClient.interceptors.response.use(
    response => response,
    async error => {
        if (error.response.status === 401) {
            console.error('Ошибка 401, требуется аутентификация')
        }
        return Promise.reject(error)
    }
)

// Инициализация приложения Vue
initKeycloak().then(() => {
    createApp(App).use(store).use(router).mount('#app')
}).catch(error => {
    console.error('Инициализация Keycloak не удалась:', error)
})

// Логирование информации о токене
const logTokenInfo = (token) => {
    if (token && token.split('.').length === 3) {
        const decoded = jwtDecode(token)
        console.log('Время истечения токена:', new Date(decoded.exp * 1000))
    } else {
        console.error('Недействительный токен:', token)
    }
}

export default keycloak
