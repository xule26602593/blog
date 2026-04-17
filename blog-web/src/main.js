import { createApp } from 'vue'
import { createPinia } from 'pinia'

// Vant 4
import Vant from 'vant'
import 'vant/lib/index.css'

// Highlight.js for code blocks
import 'highlight.js/styles/github-dark.css'

// v-viewer for image preview
import VueViewer from 'v-viewer'
import 'viewerjs/dist/viewer.css'

// App
import App from './App.vue'
import router from './router'

// Styles (Apple design system)
import './styles/index.scss'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Vant)
app.use(VueViewer)

app.mount('#app')