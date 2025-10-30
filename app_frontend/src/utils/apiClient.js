import axios from 'axios'

// Создаем экземпляр Axios с базовой конфигурацией
const apiClient = axios.create({
    baseURL: 'http://localhost:8081/api', // Укажите базовый URL вашего API
    timeout: 10000, // Максимальное время ожидания запроса в миллисекундах
    headers: {
        'Content-Type': 'application/json',
    },
})



// Добавляем интерсептор для автоматической вставки токена в заголовок Authorization
apiClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// Интерсептор для обработки ошибок, например, при истечении срока действия токена
apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (error.response && error.response.status === 401) {
            console.error('Ошибка 401: Необходима повторная аутентификация.')
            // Здесь можно добавить логику для обновления токена
        }
        return Promise.reject(error)
    }
)

export default apiClient
