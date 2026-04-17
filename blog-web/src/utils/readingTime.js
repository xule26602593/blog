/**
 * Calculate reading time for text content
 * @param {string} text - Plain text or HTML content
 * @returns {{ words: number, minutes: number }} Reading statistics
 */
export function calculateReadingTime(text) {
  // Handle edge case: no text provided
  if (!text) {
    return { words: 0, minutes: 1 }
  }

  // Remove HTML tags to get plain text
  const plainText = text.replace(/<[^>]*>/g, '')

  // Count Chinese characters
  const chineseMatches = plainText.match(/[\u4e00-\u9fa5]/g)
  const chineseCount = chineseMatches ? chineseMatches.length : 0

  // Count English words
  const englishMatches = plainText.match(/[a-zA-Z]+/g)
  const englishCount = englishMatches ? englishMatches.length : 0

  // Total words
  const totalWords = chineseCount + englishCount

  // Reading time (minimum 1 minute, rounded up)
  const minutes = Math.max(1, Math.ceil(totalWords / 300))

  return { words: totalWords, minutes }
}
