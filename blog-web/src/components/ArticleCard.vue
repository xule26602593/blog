<template>
  <article class="article-card" @click="$router.push('/article/' + article.id)">
    <div class="card-inner">
      <div v-if="article.coverImage" class="card-cover">
        <img :src="article.coverImage" :alt="article.title" loading="lazy" />
      </div>
      <div class="card-body">
        <h3 class="card-title">{{ article.title }}</h3>
        <p class="card-summary">{{ article.summary }}</p>
        <div class="card-meta">
          <span v-if="article.categoryName" class="meta-category">{{ article.categoryName }}</span>
          <span class="meta-date">{{ formatDate(article.publishTime) }}</span>
          <span class="meta-views">{{ article.viewCount }} 阅读</span>
          <span class="meta-likes">{{ article.likeCount }} 赞</span>
        </div>
        <div v-if="article.tags?.length" class="card-tags">
          <span
            v-for="tag in article.tags.slice(0, 3)"
            :key="tag.id"
            class="tag"
          >
            {{ tag.name }}
          </span>
        </div>
      </div>
    </div>
  </article>
</template>

<script setup>
import dayjs from 'dayjs'

const props = defineProps({
  article: {
    type: Object,
    required: true
  }
})

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD')
</script>

<style lang="scss" scoped>
.article-card {
  margin-bottom: var(--space-4);
  cursor: pointer;
}

.card-inner {
  display: flex;
  gap: var(--space-5);
  padding: var(--space-5);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  transition: all var(--transition-base);

  &:hover {
    border-color: var(--text-muted);
    box-shadow: var(--shadow-md);

    .card-title {
      color: var(--text-secondary);
    }

    .card-cover img {
      transform: scale(1.02);
    }
  }
}

.card-cover {
  width: 180px;
  height: 120px;
  flex-shrink: 0;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--bg-secondary);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform var(--transition-slow);
  }
}

.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.card-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  margin-bottom: var(--space-2);
  transition: color var(--transition-fast);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-summary {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  margin-bottom: var(--space-3);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-xs);
  color: var(--text-muted);
  margin-bottom: var(--space-2);
}

.meta-category {
  padding: var(--space-1) var(--space-2);
  background: var(--bg-secondary);
  border-radius: var(--radius-sm);
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-top: auto;
}

.tag {
  padding: 2px var(--space-2);
  font-size: var(--text-xs);
  background: var(--bg-secondary);
  border-radius: var(--radius-full);
  color: var(--text-tertiary);
}

// 响应式
@media (max-width: 768px) {
  .card-inner {
    flex-direction: column;
    gap: var(--space-4);
  }

  .card-cover {
    width: 100%;
    height: 160px;
  }

  .card-title {
    font-size: var(--text-base);
  }
}

@media (max-width: 480px) {
  .card-inner {
    padding: var(--space-4);
  }

  .card-cover {
    height: 140px;
  }

  .card-meta {
    gap: var(--space-1);
  }
}
</style>
