/**
 * 默认头像处理工具
 */

// 默认头像 SVG（用户图标）
const DEFAULT_AVATAR_SVG = `data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"%3E%3Ccircle cx="50" cy="50" r="50" fill="%23e5e7eb"/%3E%3Ccircle cx="50" cy="38" r="18" fill="%239ca3af"/%3E%3Cellipse cx="50" cy="75" rx="28" ry="20" fill="%239ca3af"/%3E%3C/svg%3E`

const COLORS = [
  '#f87171', '#fb923c', '#fbbf24', '#a3e635',
  '#4ade80', '#2dd4bf', '#22d3ee', '#38bdf8',
  '#60a5fa', '#818cf8', '#a78bfa', '#c084fc',
  '#e879f9', '#f472b6'
]

function generateColorAvatar(text, seed) {
  const initial = (text?.charAt(0) || 'U').toUpperCase()
  const colorIndex = seed ? String(seed).charCodeAt(0) % COLORS.length : 0
  const bgColor = COLORS[colorIndex]

  return `data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"%3E%3Ccircle cx="50" cy="50" r="50" fill="${encodeURIComponent(bgColor)}"/%3E%3Ctext x="50" y="50" text-anchor="middle" dominant-baseline="central" font-size="48" font-family="system-ui, sans-serif" font-weight="500" fill="white"%3E${encodeURIComponent(initial)}%3C/text%3E%3C/svg%3E`
}

/**
 * 获取用户头像，如果为空则返回默认头像
 * @param {string|null|undefined} avatar - 用户头像URL
 * @returns {string} 头像URL
 */
export function getAvatar(avatar) {
  return avatar || DEFAULT_AVATAR_SVG
}

/**
 * 根据昵称生成首字母头像
 * @param {string} nickname - 用户昵称
 * @returns {string} SVG data URI
 */
export function getInitialAvatar(nickname) {
  return generateColorAvatar(nickname, nickname)
}

/**
 * 综合头像获取函数
 * 优先使用用户头像，其次根据昵称生成首字母头像，最后根据用户ID生成
 * @param {object} options - 选项
 * @param {string} options.avatar - 用户头像URL
 * @param {string} options.nickname - 用户昵称
 * @param {string|number} options.userId - 用户ID（可选）
 * @returns {string} 头像URL
 */
export function getUserAvatar({ avatar, nickname, userId }) {
  if (avatar) return avatar
  if (nickname) return getInitialAvatar(nickname)
  if (userId) return generateColorAvatar(String(userId), userId)
  return DEFAULT_AVATAR_SVG
}

export default {
  getAvatar,
  getInitialAvatar,
  getUserAvatar
}
