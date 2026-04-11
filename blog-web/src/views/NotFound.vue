<template>
  <div class="not-found">
    <div class="content">
      <h1>404</h1>
      <p>页面不存在</p>
      <button class="back-btn" @click="$router.push('/')">返回首页</button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);

  .content {
    text-align: center;

    h1 {
      font-size: 120px;
      font-weight: var(--font-bold);
      background: var(--gradient-primary);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin-bottom: 20px;
    }

    p {
      font-size: 24px;
      color: var(--text-secondary);
      margin-bottom: 30px;
    }
  }

  .back-btn {
    padding: var(--space-3) var(--space-8);
    font-size: var(--text-base);
    font-weight: var(--font-medium);
    color: white;
    background: var(--gradient-primary);
    border: none;
    border-radius: var(--radius-lg);
    cursor: pointer;
    transition: all var(--transition-fast);

    &:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-lg);
    }

    &:active {
      transform: translateY(0);
    }
  }
}
</style>
