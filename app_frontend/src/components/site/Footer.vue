<template>
  <footer class="py-4 bg-light">
    <div class="container">
      <div class="row">
        <!-- Контакты -->
        <div class="col-md-4 mb-3">
          <h5>Контакты</h5>
          <ul class="list-unstyled">
            <li class="mb-2">
              <i class="bi bi-envelope me-2"></i>
              <a href="mailto:support@weborder.ru" class="text-muted">support@weborder.ru</a>
            </li>
            <li class="mb-2">
              <i class="bi bi-telephone me-2"></i>
              <a href="tel:+78001234567" class="text-muted">+7 (800) 123-45-67</a>
            </li>
            <li class="mb-2">
              <i class="bi bi-geo-alt me-2"></i>
              <span class="text-muted">Москва, ул. Примерная, д. 10</span>
            </li>
          </ul>
        </div>

        <!-- Документация -->
        <div class="col-md-4 mb-3">
          <h5>Документация</h5>
          <ul class="list-unstyled">
            <li class="mb-2">
              <a href="/docs/user-guide" class="text-muted">Руководство пользователя</a>
            </li>
            <li class="mb-2">
              <a href="/docs/api" class="text-muted">API документация</a>
            </li>
            <li class="mb-2">
              <a href="/docs/terms" class="text-muted">Условия использования</a>
            </li>
            <li class="mb-2">
              <a href="/docs/privacy" class="text-muted">Политика конфиденциальности</a>
            </li>
          </ul>
        </div>

        <!-- Техническая поддержка -->
        <div class="col-md-4 mb-3">
          <h5>Техническая поддержка</h5>
          <ul class="list-unstyled">
            <li class="mb-2">
              <a href="/support/faq" class="text-muted">Часто задаваемые вопросы</a>
            </li>
            <li class="mb-2">
              <a href="/support/tickets" class="text-muted">Создать заявку</a>
            </li>
            <li class="mb-2">
              <a href="/support/status" class="text-muted">Статус системы</a>
            </li>
            <li class="mb-2">
              <a href="/support/chat" class="text-muted">Онлайн-чат</a>
            </li>
          </ul>
        </div>
      </div>

      <!-- Нижняя часть футера -->
      <div class="d-flex flex-column flex-sm-row justify-content-between py-3 mt-3 border-top">
        <p class="text-muted">© 2025 WebOrder, Все права защищены.</p>
        <ul class="list-unstyled d-flex">
          <li class="ms-3">
            <a class="text-muted" href="https://t.me/weborder" target="_blank">
              <i class="bi bi-telegram"></i>
            </a>
          </li>
          <li class="ms-3">
            <a class="text-muted" href="https://vk.com/weborder" target="_blank">
              <i class="bi bi-vk"></i>
            </a>
          </li>
          <li class="ms-3">
            <a class="text-muted" href="https://www.youtube.com/weborder" target="_blank">
              <i class="bi bi-youtube"></i>
            </a>
          </li>
        </ul>
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'AppFooter',
};
</script>

<style scoped>
footer {
  background-color: #f8f9fa;
  color: #6c757d;
}

h5 {
  color: #343a40;
  margin-bottom: 1rem;
}

.list-unstyled a {
  text-decoration: none;
  transition: color 0.2s;
}

.list-unstyled a:hover {
  color: #007bff;
}

.bi {
  font-size: 1.2rem;
  vertical-align: middle;
}

.border-top {
  border-color: #dee2e6 !important;
}

@media (max-width: 576px) {
  .d-flex {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .list-unstyled {
    text-align: center;
  }
}
</style>