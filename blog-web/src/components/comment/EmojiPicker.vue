<!-- EmojiPicker.vue -->
<template>
  <div class="emoji-picker">
    <div class="emoji-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.name"
        :class="['tab-btn', { active: activeTab === tab.name }]"
        @click="activeTab = tab.name"
      >
        {{ tab.icon }}
      </button>
    </div>
    <div class="emoji-grid">
      <button
        v-for="emoji in currentEmojis"
        :key="emoji"
        class="emoji-btn"
        @click="$emit('select', emoji)"
      >
        {{ emoji }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

defineEmits(['select', 'close'])

const tabs = [
  { name: 'smileys', icon: '😀' },
  { name: 'gestures', icon: '👍' },
  { name: 'hearts', icon: '❤️' },
  { name: 'objects', icon: '🎉' }
]

const emojis = {
  smileys: [
    '😀', '😂', '🤣', '😊', '😇', '🙂', '😉', '😍',
    '🥰', '😘', '😋', '🤔', '😏', '😒', '😌', '😴',
    '😷', '🤗', '🤩', '😎', '🥳', '😢', '😭', '😤'
  ],
  gestures: [
    '👍', '👎', '👏', '🙌', '🤝', '✌️', '🤞', '👌',
    '👈', '👉', '👆', '👇', '☝️', '✋', '🤚', '🖐️',
    '🖖', '👋', '🤙', '💪', '🦾', '🙏', '✍️', '🤳'
  ],
  hearts: [
    '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍',
    '💔', '❣️', '💕', '💖', '💗', '💓', '💞', '💘',
    '💝', '💟', '♥️', '❤️‍🔥', '❤️‍🩹', '💕', '💗', '💖'
  ],
  objects: [
    '🎉', '🎊', '🎁', '🎈', '🎂', '🎄', '🎃', '🎄',
    '🔥', '⭐', '✨', '💫', '🌟', '💥', '💢', '💯',
    '📚', '📝', '💡', '📌', '📎', '✏️', '🖊️', '📖'
  ]
}

const activeTab = ref('smileys')

const currentEmojis = computed(() => emojis[activeTab.value] || [])
</script>

<style scoped>
.emoji-picker {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  padding: 8px;
  max-width: 280px;
}

.emoji-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
  border-bottom: 1px solid #eee;
  padding-bottom: 8px;
}

.tab-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-btn.active {
  background: #f0f0f0;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
}

.emoji-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.emoji-btn:hover {
  background: #f0f0f0;
}
</style>
