import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

class WebSocketService {
    constructor(userId, onNotification) {
        this.userId = userId;
        this.onNotification = onNotification;
        this.stompClient = null;
        this.connect();
    }

    connect() {
        const socket = new SockJS('https://localhost:8085/ws');
        this.stompClient = Stomp.over(socket);

        this.stompClient.connect({}, (frame) => {
            console.log('WebSocket подключен:', frame);
            this.subscribeToMessages();
        }, (error) => {
            console.error('Ошибка WebSocket:', error);
            setTimeout(() => this.connect(), 2000);
        });
    }

    subscribeToMessages() {
        this.stompClient.subscribe('/topic/messages', (message) => {
            const notification = JSON.parse(message.body);
            if (notification.to === this.userId) {
                this.onNotification(notification);
            }
        });
    }
}

export default WebSocketService;
