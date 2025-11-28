class TaskManagementWebSocket {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectInterval = 3000;
    }

    connect(token) {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);

        this.stompClient.connect(
            { 'Authorization': `Bearer ${token}` },
            (frame) => {
                console.log('WebSocket connected:', frame);
                this.connected = true;
                this.reconnectAttempts = 0;
                this.subscribeToChannels();
            },
            (error) => {
                console.error('WebSocket connection error:', error);
                this.connected = false;
                this.handleReconnect(token);
            }
        );
    }

    subscribeToChannels() {
        // Подписка на уведомления пользователя
        this.stompClient.subscribe('/user/queue/notifications', (message) => {
            const notification = JSON.parse(message.body);
            this.handleNotification(notification);
        });

        // Подписка на события workspace
        const workspaceId = this.getCurrentWorkspaceId();
        if (workspaceId) {
            this.subscribeToWorkspace(workspaceId);
        }
    }

    subscribeToWorkspace(workspaceId) {
        this.stompClient.subscribe(`/topic/workspace-${workspaceId}`, (message) => {
            const update = JSON.parse(message.body);
            this.handleWorkspaceUpdate(update);
        });

        this.stompClient.subscribe(`/topic/workspace-${workspaceId}/tasks`, (message) => {
            const taskUpdate = JSON.parse(message.body);
            this.handleTaskUpdate(taskUpdate);
        });

        this.stompClient.subscribe(`/topic/workspace-${workspaceId}/online-users`, (message) => {
            const onlineUsers = JSON.parse(message.body);
            this.handleOnlineUsersUpdate(onlineUsers);
        });
    }

    subscribeToTask(taskId) {
        this.stompClient.subscribe(`/topic/task-${taskId}`, (message) => {
            const taskUpdate = JSON.parse(message.body);
            this.handleTaskDetailUpdate(taskUpdate);
        });

        this.stompClient.subscribe(`/topic/task-${taskId}/typing`, (message) => {
            const typing = JSON.parse(message.body);
            this.handleTypingIndicator(typing);
        });
    }

    sendTypingIndicator(taskId, isTyping) {
        if (this.connected) {
            this.stompClient.send(
                `/app/workspaces/${this.getCurrentWorkspaceId()}/tasks/${taskId}/typing`,
                {},
                JSON.stringify({ typing: isTyping, taskId: taskId })
            );
        }
    }

    requestOnlineUsers(workspaceId) {
        if (this.connected) {
            this.stompClient.send(
                `/app/workspaces/${workspaceId}/online-users`,
                {},
                JSON.stringify({})
            );
        }
    }

    handleNotification(notification) {
        console.log('New notification:', notification);

        // Показываем уведомление в UI
        this.showNotification(notification);

        // Обновляем счетчик непрочитанных уведомлений
        this.updateUnreadCount();
    }

    handleWorkspaceUpdate(update) {
        console.log('Workspace update:', update);

        // Обновляем список задач или другие элементы UI
        if (update.updateType === 'TASK_CREATED') {
            this.addTaskToList(update.data);
        } else if (update.updateType === 'TASK_UPDATED') {
            this.updateTaskInList(update.data);
        } else if (update.updateType === 'TASK_DELETED') {
            this.removeTaskFromList(update.data.taskId);
        }
    }

    handleTaskUpdate(update) {
        console.log('Task update:', update);

        // Если открыта детальная страница задачи, обновляем её
        if (this.isTaskDetailOpen(update.taskId)) {
            this.refreshTaskDetail(update.taskId);
        }
    }

    handleOnlineUsersUpdate(onlineUsers) {
        console.log('Online users update:', onlineUsers);
        this.updateOnlineUsersList(onlineUsers);
    }

    handleTypingIndicator(typing) {
        console.log('Typing indicator:', typing);
        this.showTypingIndicator(typing);
    }

    handleReconnect(token) {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

            setTimeout(() => {
                this.connect(token);
            }, this.reconnectInterval);
        } else {
            console.error('Max reconnection attempts reached');
        }
    }

    disconnect() {
        if (this.stompClient && this.connected) {
            this.stompClient.disconnect();
            this.connected = false;
            console.log('WebSocket disconnected');
        }
    }

    // Вспомогательные методы
    getCurrentWorkspaceId() {
        // Получаем ID текущего workspace из URL или состояния приложения
        return window.currentWorkspaceId;
    }

    isTaskDetailOpen(taskId) {
        // Проверяем, открыта ли детальная страница задачи
        return window.currentTaskId === taskId;
    }

    // Методы для работы с UI
    showNotification(notification) {
        // Реализация показа уведомления в UI
        if ('Notification' in window && Notification.permission === 'granted') {
            new Notification(notification.title, {
                body: notification.message,
                icon: '/favicon.ico'
            });
        }

        // Или показать уведомление в самом приложении
        this.showInAppNotification(notification);
    }

    updateUnreadCount() {
        // Обновление счетчика непрочитанных уведомлений
        const countElement = document.getElementById('unread-notifications-count');
        if (countElement) {
            const currentCount = parseInt(countElement.textContent) || 0;
            countElement.textContent = currentCount + 1;
            countElement.style.display = 'block';
        }
    }

    showInAppNotification(notification) {
        // Показ уведомления в интерфейсе приложения
        const notificationContainer = document.getElementById('notifications-container');
        if (notificationContainer) {
            const notificationElement = this.createNotificationElement(notification);
            notificationContainer.appendChild(notificationElement);

            // Автоматическое скрытие через 5 секунд
            setTimeout(() => {
                notificationElement.remove();
            }, 5000);
        }
    }

    createNotificationElement(notification) {
        const div = document.createElement('div');
        div.className = 'notification alert alert-info';
        div.innerHTML = `
            <strong>${notification.title}</strong>
            <p>${notification.message}</p>
            <small>${new Date().toLocaleTimeString()}</small>
        `;
        return div;
    }
}

// Глобальный экземпляр WebSocket клиента
window.taskWebSocket = new TaskManagementWebSocket();

// Запрос разрешения на уведомления
if ('Notification' in window) {
    Notification.requestPermission();
}
