// firebase-messaging-sw.js
// Service Worker для push-уведомлений MAX
// Расположить в корневой директории веб-приложения

importScripts('https://www.gstatic.com/firebasejs/13.2.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/13.2.0/firebase-messaging-compat.js');

firebase.initializeApp({
  apiKey: 'AIzaSyAz2dkis9NgvZDFCq9CLqPbDL61Cp7XLAE',
  authDomain: 'chess-cc-messanger.firebaseapp.com',
  databaseURL: 'https://chess-cc-messanger-default-rtdb.firebaseio.com',
  projectId: 'chess-cc-messanger',
  storageBucket: 'chess-cc-messanger.firebasestorage.app',
  messagingSenderId: '681157216826',
  appId: '1:681157216826:web:6ec88f4ac9b02219850303',
  measurementId: 'G-DK84QH1JYN',
});

const messaging = firebase.messaging();

messaging.onBackgroundMessage(function(payload) {
  console.log('[FCM] Background message received:', payload);
  
  const notificationData = payload.data || {};
  const title = notificationData.sender || 'MAX';
  
  const notificationOptions = {
    body: notificationData.message || 'Новое сообщение',
    icon: '/assets/icons/logo-192.png',
    badge: '/assets/icons/logo-192.png',
    tag: notificationData.messageId || payload.messageId || 'default-tag',
    renotify: true,
    requireInteraction: notificationData.type === 'call_request',
  };
  
  // Для входящих звонков — показываем кнопки действий
  if (notificationData.type === 'call_request') {
    notificationOptions.actions = [
      {
        action: 'accept_call',
        title: '✅ Принять звонок',
        icon: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNiIgaGVpZ2h0PSIxNiIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSIjMDBhMDAwIj48cGF0aCBkPSJNNy40MiAxNGMyLjE0IDAgNC4yNS0uOTQgNS44MS0yLjUzYTEgMSAwIDAgMCAxIDEuNDEgMS40MWwtMS44OCAxLjh4LS4wNmMtLjc2Ljc2LTEuODggMS4yNC0zLjA2IDEuMjRzLTMuMTctLjQ4LTQuMTQtMS40NEwyLjUgMTMuNDlhMSAxIDAgMCAxIDAtMS40MWwyLjgxLTIuODFhMSAxIDAgMCAxIDEuNDEgMGwuNy43em0tMS40MS0zLjIxbDIuODEgMi44MWEuNS41IDAgMCAwIC43MSAwYy45Ny0uOTcgMS40NC0yLjE0IDEuNDQtMy4xNnMtLjQ3LTIuMTktMS40NC0zLjE2YS41LjUgMCAwIDAtLjcxIDBsLTIuODEgMi44MWMtLjM5LjM5LS4zOSAxLjAyIDAgMS40MXoiIGZpbGw9IndoaXRlIi8+PC9zdmc+',
      },
      {
        action: 'decline_call',
        title: '❌ Отклонить',
        icon: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNiIgaGVpZ2h0PSIxNiIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSIjZmZmZmZmIj48cGF0aCBkPSJNNy40MiAxNGMyLjE0IDAgNC4yNS0uOTQgNS44MS0yLjUzYTEgMSAwIDAgMCAxIDEuNDEgMS40MWwtMS44OCAxLjh4LS4wNmMtLjc2Ljc2LTEuODggMS4yNC0zLjA2IDEuMjRzLTMuMTctLjQ4LTQuMTQtMS40NEwyLjUgMTMuNDlhMSAxIDAgMCAxIDAtMS40MWwyLjgxLTIuODFhMSAxIDAgMCAxIDEuNDEgMGwuNy43em0tMS40MS0zLjIxbDIuODEgMi44MWEuNS41IDAgMCAwIC43MSAwYy45Ny0uOTcgMS40NC0yLjE0IDEuNDQtMy4xNnMtLjQ3LTIuMTktMS40NC0zLjE2YS41LjUgMCAwIDAtLjcxIDBsLTIuODEgMi44MWMtLjM5LjM5LS4zOSAxLjAyIDAgMS40MXoiIGZpbGw9IndoaXRlIi8+PC9zdmc+',
      },
    ];
  }

  self.registration.showNotification(title, notificationOptions);
});

// Обработка клика по уведомлению
self.addEventListener('notificationclick', function(event) {
  event.notification.close();
  
  const action = event.action;
  const callId = event.notification.data?.callId;
  
  if (action === 'accept_call' && callId) {
    // Переход к активному звонку
    clients.openWindow('/calls/' + callId);
  } else if (action === 'decline_call' && callId) {
    // Отклонение звонка через API
    fetch('/api/calls/decline', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ callId }),
    });
    // Закрыть приложение
    clients.matchAll({ type: 'window' }).then(clients => {
      clients.forEach(client => client.close());
    });
  } else {
    // Обычное уведомление — перейти в чат
    const messageId = event.notification.tag;
    if (messageId) {
      clients.openWindow('/chat/' + messageId);
    } else {
      clients.openWindow('/');
    }
  }
});

// Обработка закрытия уведомления
self.addEventListener('notificationclose', function(event) {
  console.log('[FCM] Notification closed:', event.notification.tag);
});

// Событие сообщения при открытии приложения
self.addEventListener('pushsubscriptionchange', function(event) {
  const newSub = event.newSubscription;
  const oldSub = event.oldSubscription;
  
  if (newSub) {
    newSub.serialize().then(subscription => {
      // Отправить токен подписки на сервер
      fetch('/api/fcm/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ subscription }),
      });
    });
  }
});
