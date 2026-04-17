import { ref, onMounted, onUnmounted, nextTick } from 'vue'

/**
 * Table of Contents composable for article navigation
 * @param {import('vue').Ref<HTMLElement|null>} containerRef - Vue ref to the article container DOM element
 * @returns {Object} { headings, activeId, extractHeadings, scrollToHeading }
 */
export function useToc(containerRef) {
  // Array of heading objects: { id, text, level, top }
  const headings = ref([])

  // Currently highlighted heading id
  const activeId = ref('')

  /**
   * Extract all h1-h6 headings from the container
   * Assigns ids to headings that don't have one
   */
  function extractHeadings() {
    if (!containerRef.value) {
      headings.value = []
      return
    }

    const headingElements = containerRef.value.querySelectorAll('h1, h2, h3, h4, h5, h6')

    const extractedHeadings = []

    headingElements.forEach((el, index) => {
      // Assign id if missing
      if (!el.id) {
        el.id = `heading-${index}`
      }

      const level = parseInt(el.tagName.charAt(1))
      const text = el.textContent?.trim() || ''

      extractedHeadings.push({
        id: el.id,
        text,
        level,
        top: el.offsetTop
      })
    })

    headings.value = extractedHeadings
  }

  /**
   * Smooth scroll to a heading with offset for fixed header
   * @param {string} id - The heading id to scroll to
   */
  function scrollToHeading(id) {
    if (!containerRef.value) return

    const element = document.getElementById(id)
    if (!element) return

    const headerOffset = 80 // Fixed header height
    const elementPosition = element.getBoundingClientRect().top
    const offsetPosition = elementPosition + window.pageYOffset - headerOffset

    window.scrollTo({
      top: offsetPosition,
      behavior: 'smooth'
    })
  }

  /**
   * Update activeId based on scroll position
   */
  function handleScroll() {
    if (!containerRef.value || headings.value.length === 0) return

    const scrollPosition = window.pageYOffset
    const offset = 100 // Offset for detecting active heading

    // Find the heading that is currently in view
    let currentActiveId = ''

    for (const heading of headings.value) {
      const element = document.getElementById(heading.id)
      if (!element) continue

      const top = element.offsetTop - offset
      if (scrollPosition >= top) {
        currentActiveId = heading.id
      }
    }

    activeId.value = currentActiveId
  }

  onMounted(() => {
    // Extract headings after DOM is ready
    nextTick(() => {
      extractHeadings()
    })

    // Add scroll listener
    window.addEventListener('scroll', handleScroll)
  })

  onUnmounted(() => {
    // Remove scroll listener
    window.removeEventListener('scroll', handleScroll)
  })

  return {
    headings,
    activeId,
    extractHeadings,
    scrollToHeading
  }
}
