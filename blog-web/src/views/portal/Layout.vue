<template>
  <div class="portal-layout">
    <!-- Header -->
    <header class="header">
      <div class="header-inner">
        <router-link to="/" class="brand">
          <span class="brand-mark">随</span>
          <span class="brand-text">随笔</span>
        </router-link>

        <!-- Desktop Nav -->
        <nav class="nav nav-desktop">
          <router-link
            to="/"
            class="nav-link"
            :class="{ active: route.path === '/' }"
          >
            <svg
              class="nav-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M2.25 12l8.954-8.955c.44-.439 1.152-.439 1.591 0L21.75 12M4.5 9.75v10.125c0 .621.504 1.125 1.125 1.125H9.75v-4.875c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125V21h4.125c.621 0 1.125-.504 1.125-1.125V9.75M8.25 21h8.25"
              />
            </svg>
            <span>首页</span>
          </router-link>
          <router-link
            to="/archives"
            class="nav-link"
            :class="{ active: route.path === '/archives' }"
          >
            <svg
              class="nav-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M20.25 7.5l-.625 10.632a2.25 2.25 0 01-2.247 2.118H6.622a2.25 2.25 0 01-2.247-2.118L3.75 7.5M10 11.25h4M3.375 7.5h17.25c.621 0 1.125-.504 1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125H3.375c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125z"
              />
            </svg>
            <span>归档</span>
          </router-link>
        </nav>

        <div class="header-actions">
          <!-- Desktop Search -->
          <div
            class="search-wrapper search-desktop"
            :class="{ focused: searchFocused }"
          >
            <svg
              class="search-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z"
              />
            </svg>
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索文章..."
              class="search-input"
              @focus="searchFocused = true"
              @blur="searchFocused = false"
              @keyup.enter="handleSearch"
            />
          </div>

          <!-- Mobile Search Toggle -->
          <button
            class="icon-btn search-toggle"
            @click="toggleMobileSearch"
            title="搜索"
          >
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z"
              />
            </svg>
          </button>

          <button
            class="icon-btn theme-toggle"
            @click="toggleTheme"
            :title="themeStore.isDark ? '切换到亮色模式' : '切换到深色模式'"
          >
            <svg
              v-if="themeStore.isDark"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M12 3v2.25m6.364.386l-1.591 1.591M21 12h-2.25m-.386 6.364l-1.591-1.591M12 18.75V21m-4.773-4.227l-1.591 1.591M5.25 12H3m4.227-4.773L5.636 5.636M15.75 12a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0z"
              />
            </svg>
            <svg
              v-else
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M21.752 15.002A9.718 9.718 0 0118 15.75c-5.385 0-9.75-4.365-9.75-9.75 0-1.33.266-2.597.748-3.752A9.753 9.753 0 003 11.25C3 16.635 7.365 21 12.75 21a9.753 9.753 0 009.002-5.998z"
              />
            </svg>
          </button>

          <template v-if="userStore.isLoggedIn">
            <div class="user-menu">
              <!-- PC端：下拉菜单 -->
              <button class="user-trigger user-trigger-desktop">
                <span class="user-avatar">{{
                  userStore.userInfo?.nickname?.charAt(0) || "U"
                }}</span>
                <span class="user-name">{{
                  userStore.userInfo?.nickname || "用户"
                }}</span>
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.5"
                  class="chevron"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M19.5 8.25l-7.5 7.5-7.5-7.5"
                  />
                </svg>
              </button>
              <!-- 移动端：直接跳转 -->
              <router-link to="/user" class="user-trigger user-trigger-mobile">
                <span class="user-avatar">{{
                  userStore.userInfo?.nickname?.charAt(0) || "U"
                }}</span>
                <span class="user-name">{{
                  userStore.userInfo?.nickname || "用户"
                }}</span>
              </router-link>
              <div class="user-dropdown">
                <div class="dropdown-header">
                  <span class="dropdown-avatar">{{
                    userStore.userInfo?.nickname?.charAt(0) || "U"
                  }}</span>
                  <div class="dropdown-user-info">
                    <span class="dropdown-nickname">{{
                      userStore.userInfo?.nickname || "用户"
                    }}</span>
                    <span class="dropdown-email">{{
                      userStore.userInfo?.email || "未设置邮箱"
                    }}</span>
                  </div>
                </div>
                <div class="dropdown-divider"></div>
                <router-link to="/user" class="dropdown-item">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.5"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z"
                    />
                  </svg>
                  个人中心
                </router-link>
                <router-link
                  v-if="userStore.isAdmin"
                  to="/admin"
                  class="dropdown-item"
                >
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.5"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      d="M9.594 3.94c.09-.542.56-.94 1.11-.94h2.593c.55 0 1.02.398 1.11.94l.213 1.281c.063.374.313.686.645.87.074.04.147.083.22.127.324.196.72.257 1.075.124l1.217-.456a1.125 1.125 0 011.37.49l1.296 2.247a1.125 1.125 0 01-.26 1.431l-1.003.827c-.293.24-.438.613-.431.992a6.759 6.759 0 010 .255c-.007.378.138.75.43.99l1.005.828c.424.35.534.954.26 1.43l-1.298 2.247a1.125 1.125 0 01-1.369.491l-1.217-.456c-.355-.133-.75-.072-1.076.124a6.57 6.57 0 01-.22.128c-.331.183-.581.495-.644.869l-.213 1.28c-.09.543-.56.941-1.11.941h-2.594c-.55 0-1.02-.398-1.11-.94l-.213-1.281c-.062-.374-.312-.686-.644-.87a6.52 6.52 0 01-.22-.127c-.325-.196-.72-.257-1.076-.124l-1.217.456a1.125 1.125 0 01-1.369-.49l-1.297-2.247a1.125 1.125 0 01.26-1.431l1.004-.827c.292-.24.437-.613.43-.992a6.932 6.932 0 010-.255c.007-.378-.138-.75-.43-.99l-1.004-.828a1.125 1.125 0 01-.26-1.43l1.297-2.247a1.125 1.125 0 011.37-.491l1.216.456c.356.133.751.072 1.076-.124.072-.044.146-.087.22-.128.332-.183.582-.495.644-.869l.214-1.281z"
                    />
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                    />
                  </svg>
                  管理后台
                </router-link>
                <button class="dropdown-item logout" @click="handleLogout">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.5"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15m3 0l3-3m0 0l-3-3m3 3H9"
                    />
                  </svg>
                  退出登录
                </button>
              </div>
            </div>
          </template>
          <template v-else>
            <router-link to="/login" class="btn-login"> 登录 </router-link>
          </template>

          <!-- Mobile Menu Toggle -->
          <button
            class="icon-btn menu-toggle"
            @click="toggleMobileMenu"
            title="菜单"
          >
            <svg
              v-if="!mobileMenuOpen"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5"
              />
            </svg>
            <svg
              v-else
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>
      </div>

      <!-- Mobile Search Bar -->
      <div class="mobile-search-bar" :class="{ open: mobileSearchOpen }">
        <div class="mobile-search-inner">
          <svg
            class="search-icon"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z"
            />
          </svg>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索文章..."
            class="mobile-search-input"
            @keyup.enter="handleMobileSearch"
            ref="mobileSearchInput"
          />
          <button class="mobile-search-close" @click="closeMobileSearch">
            取消
          </button>
        </div>
      </div>
    </header>

    <!-- Mobile Menu Overlay -->
    <Transition name="fade">
      <div
        v-if="mobileMenuOpen"
        class="mobile-overlay"
        @click="closeMobileMenu"
      ></div>
    </Transition>

    <!-- Mobile Menu Drawer -->
    <Transition name="slide-right">
      <div v-if="mobileMenuOpen" class="mobile-menu">
        <div class="mobile-menu-header">
          <span class="mobile-menu-title">导航</span>
          <button class="mobile-menu-close" @click="closeMobileMenu">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>
        <nav class="mobile-nav">
          <router-link
            to="/"
            class="mobile-nav-item"
            :class="{ active: route.path === '/' }"
            @click="closeMobileMenu"
          >
            <svg
              class="mobile-nav-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M2.25 12l8.954-8.955c.44-.439 1.152-.439 1.591 0L21.75 12M4.5 9.75v10.125c0 .621.504 1.125 1.125 1.125H9.75v-4.875c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125V21h4.125c.621 0 1.125-.504 1.125-1.125V9.75M8.25 21h8.25"
              />
            </svg>
            <span>首页</span>
          </router-link>
          <router-link
            to="/archives"
            class="mobile-nav-item"
            :class="{ active: route.path === '/archives' }"
            @click="closeMobileMenu"
          >
            <svg
              class="mobile-nav-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M20.25 7.5l-.625 10.632a2.25 2.25 0 01-2.247 2.118H6.622a2.25 2.25 0 01-2.247-2.118L3.75 7.5M10 11.25h4M3.375 7.5h17.25c.621 0 1.125-.504 1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125H3.375c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125z"
              />
            </svg>
            <span>归档</span>
          </router-link>
          <div class="mobile-nav-divider"></div>
          <template v-if="userStore.isLoggedIn">
            <router-link
              to="/user"
              class="mobile-nav-item"
              @click="closeMobileMenu"
            >
              <svg
                class="mobile-nav-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z"
                />
              </svg>
              <span>个人中心</span>
            </router-link>
            <router-link
              v-if="userStore.isAdmin"
              to="/admin"
              class="mobile-nav-item"
              @click="closeMobileMenu"
            >
              <svg
                class="mobile-nav-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M9.594 3.94c.09-.542.56-.94 1.11-.94h2.593c.55 0 1.02.398 1.11.94l.213 1.281c.063.374.313.686.645.87.074.04.147.083.22.127.324.196.72.257 1.075.124l1.217-.456a1.125 1.125 0 011.37.49l1.296 2.247a1.125 1.125 0 01-.26 1.431l-1.003.827c-.293.24-.438.613-.431.992a6.759 6.759 0 010 .255c-.007.378.138.75.43.99l1.005.828c.424.35.534.954.26 1.43l-1.298 2.247a1.125 1.125 0 01-1.369.491l-1.217-.456c-.355-.133-.75-.072-1.076.124a6.57 6.57 0 01-.22.128c-.331.183-.581.495-.644.869l-.213 1.28c-.09.543-.56.941-1.11.941h-2.594c-.55 0-1.02-.398-1.11-.94l-.213-1.281c-.062-.374-.312-.686-.644-.87a6.52 6.52 0 01-.22-.127c-.325-.196-.72-.257-1.076-.124l-1.217.456a1.125 1.125 0 01-1.369-.49l-1.297-2.247a1.125 1.125 0 01.26-1.431l1.004-.827c.292-.24.437-.613.43-.992a6.932 6.932 0 010-.255c.007-.378-.138-.75-.43-.99l-1.004-.828a1.125 1.125 0 01-.26-1.43l1.297-2.247a1.125 1.125 0 011.37-.491l1.216.456c.356.133.751.072 1.076-.124.072-.044.146-.087.22-.128.332-.183.582-.495.644-.869l.214-1.281z"
                />
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                />
              </svg>
              <span>管理后台</span>
            </router-link>
            <div class="mobile-nav-divider"></div>
            <button class="mobile-nav-item logout" @click="handleLogout">
              <svg
                class="mobile-nav-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15m3 0l3-3m0 0l-3-3m3 3H9"
                />
              </svg>
              <span>退出登录</span>
            </button>
          </template>
          <template v-else>
            <router-link
              to="/login"
              class="mobile-nav-item highlight"
              @click="closeMobileMenu"
            >
              <svg
                class="mobile-nav-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15m3 0l3-3m0 0l-3-3m3 3H9"
                />
              </svg>
              <span>登录</span>
            </router-link>
          </template>
          <div class="mobile-nav-divider"></div>
          <button class="mobile-nav-item" @click="toggleTheme">
            <svg
              class="mobile-nav-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path
                v-if="themeStore.isDark"
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M12 3v2.25m6.364.386l-1.591 1.591M21 12h-2.25m-.386 6.364l-1.591-1.591M12 18.75V21m-4.773-4.227l-1.591 1.591M5.25 12H3m4.227-4.773L5.636 5.636M15.75 12a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0z"
              />
              <path
                v-else
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M21.752 15.002A9.718 9.718 0 0118 15.75c-5.385 0-9.75-4.365-9.75-9.75 0-1.33.266-2.597.748-3.752A9.753 9.753 0 003 11.25C3 16.635 7.365 21 12.75 21a9.753 9.753 0 009.002-5.998z"
              />
            </svg>
            <span>{{ themeStore.isDark ? '切换亮色模式' : '切换深色模式' }}</span>
          </button>
        </nav>
      </div>
    </Transition>

    <!-- Main -->
    <main class="main">
      <router-view />
    </main>

    <!-- Footer -->
    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <router-link to="/" class="footer-logo">
            <span class="logo-mark">随</span>
            <span class="logo-text">随笔</span>
          </router-link>
          <p class="footer-desc">记录想法，分享思考</p>
        </div>
        <div class="footer-links">
          <div class="link-group">
            <h5 class="link-title">导航</h5>
            <router-link to="/" class="link-item">首页</router-link>
            <router-link to="/archives" class="link-item">归档</router-link>
          </div>
          <div class="link-group">
            <h5 class="link-title">关于</h5>
            <span class="link-item">Powered by Vue 3</span>
            <span class="link-item">Built with ❤️</span>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <p class="copyright">© {{ currentYear }} 随笔. All rights reserved.</p>
      </div>
    </footer>

    <!-- Mobile Bottom Navigation -->
    <nav class="mobile-bottom-nav">
      <router-link
        to="/"
        class="mobile-bottom-item"
        :class="{ active: route.path === '/' }"
      >
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1.5"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M2.25 12l8.954-8.955c.44-.439 1.152-.439 1.591 0L21.75 12M4.5 9.75v10.125c0 .621.504 1.125 1.125 1.125H9.75v-4.875c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125V21h4.125c.621 0 1.125-.504 1.125-1.125V9.75M8.25 21h8.25"
          />
        </svg>
        <span>首页</span>
      </router-link>
      <router-link
        to="/archives"
        class="mobile-bottom-item"
        :class="{ active: route.path === '/archives' }"
      >
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1.5"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M20.25 7.5l-.625 10.632a2.25 2.25 0 01-2.247 2.118H6.622a2.25 2.25 0 01-2.247-2.118L3.75 7.5M10 11.25h4M3.375 7.5h17.25c.621 0 1.125-.504 1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125H3.375c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125z"
          />
        </svg>
        <span>归档</span>
      </router-link>
      <button class="mobile-bottom-item" @click="toggleMobileSearch">
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1.5"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z"
          />
        </svg>
        <span>搜索</span>
      </button>
      <router-link
        v-if="userStore.isLoggedIn"
        to="/user"
        class="mobile-bottom-item"
        :class="{ active: route.path === '/user' }"
      >
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1.5"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z"
          />
        </svg>
        <span>我的</span>
      </router-link>
      <router-link
        v-else
        to="/login"
        class="mobile-bottom-item"
        :class="{ active: route.path === '/login' }"
      >
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1.5"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15m3 0l3-3m0 0l-3-3m3 3H9"
          />
        </svg>
        <span>登录</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { useThemeStore } from "@/stores/theme";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const themeStore = useThemeStore();

const searchKeyword = ref("");
const searchFocused = ref(false);
const mobileMenuOpen = ref(false);
const mobileSearchOpen = ref(false);
const mobileSearchInput = ref(null);

const currentYear = computed(() => new Date().getFullYear());

watch(
  () => route.query.keyword,
  (value) => {
    searchKeyword.value = typeof value === "string" ? value : "";
  },
  { immediate: true }
);

// Close mobile menu on route change
watch(
  () => route.path,
  () => {
    mobileMenuOpen.value = false;
    mobileSearchOpen.value = false;
  }
);

const toggleTheme = () => {
  themeStore.toggleTheme();
};

const handleSearch = () => {
  const keyword = searchKeyword.value.trim();
  if (keyword) {
    router.push({ path: "/search", query: { keyword } });
  }
};

const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value;
  if (mobileMenuOpen.value) {
    document.body.style.overflow = "hidden";
  } else {
    document.body.style.overflow = "";
  }
};

const closeMobileMenu = () => {
  mobileMenuOpen.value = false;
  document.body.style.overflow = "";
};

const toggleMobileSearch = () => {
  mobileSearchOpen.value = !mobileSearchOpen.value;
  if (mobileSearchOpen.value) {
    nextTick(() => {
      mobileSearchInput.value?.focus();
    });
  }
};

const closeMobileSearch = () => {
  mobileSearchOpen.value = false;
  searchKeyword.value = "";
};

const handleMobileSearch = () => {
  const keyword = searchKeyword.value.trim();
  if (keyword) {
    router.push({ path: "/search", query: { keyword } });
    closeMobileSearch();
  }
};

const handleLogout = () => {
  userStore.logout();
  closeMobileMenu();
  router.push("/");
};
</script>

<style lang="scss" scoped>
.portal-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
}

// ========================================
// Header - 毛玻璃悬浮导航
// ========================================
.header {
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
  background: var(--bg-blur);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid var(--border-color);
  transition: all var(--transition-base);

  // 滚动时增强效果
  &.scrolled {
    box-shadow: var(--shadow-md);
  }
}

.header-inner {
  display: flex;
  align-items: center;
  gap: var(--space-8);
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-4) var(--space-6);
}

.brand {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  text-decoration: none;
}

.brand-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  font-family: var(--font-sans);
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: white;
  background: linear-gradient(
    135deg,
    var(--color-primary) 0%,
    var(--color-accent) 100%
  );
  border-radius: var(--radius-md);
  box-shadow: 0 2px 8px rgba(180, 83, 9, 0.3);
  transition: all var(--transition-fast);
  position: relative;
  overflow: hidden;

  // 微光效果
  &::before {
    content: "";
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(
      90deg,
      transparent,
      rgba(255, 255, 255, 0.3),
      transparent
    );
    transition: left 0.5s ease;
  }

  &:hover::before {
    left: 100%;
  }

  &:hover {
    transform: scale(1.05);
    box-shadow: 0 4px 12px rgba(180, 83, 9, 0.4);
  }
}

.brand-text {
  font-family: var(--font-serif);
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.nav {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.nav-link {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  border-radius: var(--radius-lg);
  text-decoration: none;
  transition: all var(--transition-fast);
  position: relative;

  .nav-icon {
    width: 18px;
    height: 18px;
    opacity: 0.7;
    transition: all var(--transition-fast);
  }

  // 下划线动画
  &::after {
    content: "";
    position: absolute;
    bottom: 6px;
    left: var(--space-4);
    right: var(--space-4);
    height: 2px;
    background: var(--gradient-primary);
    border-radius: var(--radius-full);
    transform: scaleX(0);
    transform-origin: center;
    transition: transform var(--transition-fast);
  }

  &:hover {
    color: var(--color-primary);
    background: var(--bg-hover);

    .nav-icon {
      opacity: 1;
      transform: translateY(-1px);
    }

    &::after {
      transform: scaleX(0.5);
    }
  }

  &.active {
    color: var(--color-primary);
    background: var(--color-primary-light);

    .nav-icon {
      opacity: 1;
    }

    &::after {
      transform: scaleX(1);
    }
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-left: auto;
}

.search-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: var(--space-3);
  width: 16px;
  height: 16px;
  color: var(--text-muted);
  pointer-events: none;
  transition: color var(--transition-fast);
}

.search-input {
  width: 200px;
  height: 40px;
  padding: 0 var(--space-4) 0 var(--space-10);
  font-size: var(--text-sm);
  color: var(--text-primary);
  background: var(--bg-secondary);
  border: 1px solid transparent;
  border-radius: var(--radius-full);
  outline: none;
  transition: all var(--transition-fast);

  &::placeholder {
    color: var(--text-muted);
  }

  &:focus {
    width: 260px;
    background: var(--bg-card);
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(180, 83, 9, 0.1);
  }
}

.search-wrapper.focused .search-icon {
  color: var(--color-primary);
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: transparent;
  border: none;
  border-radius: var(--radius-lg);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);

  svg {
    width: 20px;
    height: 20px;
  }

  &:hover {
    color: var(--color-primary);
    background: var(--bg-hover);
  }
}

.user-menu {
  position: relative;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-3) var(--space-1) var(--space-1);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);
  text-decoration: none;

  &:hover {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(180, 83, 9, 0.08);
  }
}

.user-trigger-mobile {
  padding: var(--space-1);
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: white;
  background: var(--color-primary);
  border-radius: var(--radius-full);
  box-shadow: 0 0 0 2px transparent;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.user-name {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-primary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chevron {
  width: 14px;
  height: 14px;
  color: var(--text-muted);
  transition: transform var(--transition-fast);
}

// 透明连接区域，消除按钮和下拉菜单之间的间隙
.user-menu::before {
  content: "";
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  height: var(--space-2);
  opacity: 0;
}

.user-dropdown {
  position: absolute;
  top: calc(100% + var(--space-2));
  right: 0;
  min-width: 220px;
  padding: var(--space-2);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  opacity: 0;
  visibility: hidden;
  transform: translateY(-8px);
  transition: all var(--transition-fast);

  .user-menu:hover &,
  .user-menu:focus-within & {
    opacity: 1;
    visibility: visible;
    transform: translateY(0);
  }
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  margin-bottom: var(--space-2);
}

.dropdown-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: white;
  background: var(--color-primary);
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.dropdown-user-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.dropdown-nickname {
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.dropdown-email {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.dropdown-divider {
  height: 1px;
  background: var(--border-color);
  margin: var(--space-2) 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  width: 100%;
  padding: var(--space-3);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  background: none;
  border: none;
  border-radius: var(--radius-lg);
  text-align: left;
  cursor: pointer;
  text-decoration: none;
  transition: all var(--transition-fast);

  svg {
    width: 18px;
    height: 18px;
    opacity: 0.7;
  }

  &:hover {
    color: var(--color-primary);
    background: var(--bg-hover);

    svg {
      opacity: 1;
    }
  }

  &.logout {
    color: var(--color-error);
  }
}

.btn-login {
  display: inline-flex;
  align-items: center;
  padding: var(--space-2) var(--space-5);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: white;
  background: var(--color-primary);
  border-radius: var(--radius-full);
  text-decoration: none;
  transition: all var(--transition-fast);

  &:hover {
    background: var(--color-accent);
    box-shadow: var(--shadow-md);
  }
}

// ========================================
// Main
// ========================================
.main {
  flex: 1;
  padding: var(--space-12) var(--space-6);
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
}

// ========================================
// Footer
// ========================================
.footer {
  margin-top: auto;
  padding: var(--space-12) var(--space-6) var(--space-6);
  border-top: 1px solid var(--border-color);
  background: var(--bg-secondary);
}

.footer-inner {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto var(--space-8);
}

.footer-brand {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  text-decoration: none;
}

.logo-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  font-family: var(--font-serif);
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: white;
  background: var(--color-primary);
  border-radius: var(--radius-md);
}

.logo-text {
  font-family: var(--font-serif);
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.footer-desc {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.footer-links {
  display: flex;
  gap: var(--space-16);
}

.link-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.link-title {
  font-family: var(--font-serif);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.link-item {
  font-size: var(--text-sm);
  color: var(--text-muted);
  text-decoration: none;
  transition: color var(--transition-fast);

  &:hover {
    color: var(--color-primary);
  }
}

.footer-bottom {
  padding-top: var(--space-6);
  border-top: 1px solid var(--border-color);
  text-align: center;
}

.copyright {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

// ========================================
// 响应式
// ========================================
.user-trigger-mobile {
  display: none;
}

@media (max-width: 768px) {
  .header-inner {
    gap: var(--space-4);
    padding: var(--space-3) var(--space-4);
  }

  .brand-text {
    display: none;
  }

  .nav-desktop {
    display: none;
  }

  .search-desktop {
    display: none;
  }

  .user-name {
    display: none;
  }

  .user-menu {
    position: static;
  }

  .user-trigger-desktop {
    display: none;
  }

  .user-trigger-mobile {
    display: flex;
  }

  .user-dropdown {
    display: none;
  }

  .theme-toggle {
    display: none;
  }

  .menu-toggle {
    display: flex;
  }

  .main {
    padding: var(--space-4);
    padding-bottom: calc(var(--space-4) + 70px);
  }

  .footer {
    padding-bottom: calc(var(--space-6) + 70px);
  }

  .footer-inner {
    flex-direction: column;
    gap: var(--space-8);
    text-align: center;
    align-items: center;
  }

  .footer-brand {
    align-items: center;
  }

  .footer-links {
    gap: var(--space-10);
  }
}

@media (max-width: 480px) {
  .search-wrapper {
    display: none;
  }

  .user-trigger .user-name {
    display: none;
  }
}

// ========================================
// Mobile Search Bar
// ========================================
.mobile-search-bar {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--bg-overlay);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--border-color);
  padding: var(--space-3) var(--space-4);
  transform: translateY(-100%);
  opacity: 0;
  visibility: hidden;
  transition: all var(--transition-base);

  &.open {
    transform: translateY(0);
    opacity: 1;
    visibility: visible;
  }
}

.mobile-search-inner {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  padding: 0 var(--space-4);

  .search-icon {
    position: static;
    width: 18px;
    height: 18px;
    color: var(--text-muted);
    flex-shrink: 0;
    margin-right: var(--space-1);
  }
}

.mobile-search-input {
  flex: 1;
  height: 44px;
  background: none;
  border: none;
  font-size: var(--text-base);
  color: var(--text-primary);
  outline: none;

  &::placeholder {
    color: var(--text-muted);
  }
}

.mobile-search-close {
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  background: none;
  border: none;
  cursor: pointer;
  transition: color var(--transition-fast);

  &:hover {
    color: var(--color-primary);
  }
}

// ========================================
// Mobile Menu
// ========================================
.mobile-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: var(--z-modal-backdrop);
}

.mobile-menu {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: 300px;
  max-width: 85vw;
  background: var(--bg-card);
  z-index: var(--z-modal);
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-2xl);
}

.mobile-menu-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--border-color);
}

.mobile-menu-title {
  font-family: var(--font-serif);
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.mobile-menu-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: none;
  border: none;
  border-radius: var(--radius-lg);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);

  svg {
    width: 20px;
    height: 20px;
  }

  &:hover {
    background: var(--bg-hover);
    color: var(--text-primary);
  }
}

.mobile-nav {
  flex: 1;
  padding: var(--space-4);
  overflow-y: auto;
}

.mobile-nav-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  width: 100%;
  padding: var(--space-4);
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  background: none;
  border: none;
  border-radius: var(--radius-lg);
  text-decoration: none;
  cursor: pointer;
  transition: all var(--transition-fast);

  .mobile-nav-icon {
    width: 22px;
    height: 22px;
    flex-shrink: 0;
    opacity: 0.7;
    transition: opacity var(--transition-fast);
  }

  &:hover {
    color: var(--text-primary);
    background: var(--bg-hover);

    .mobile-nav-icon {
      opacity: 1;
    }
  }

  &.active {
    color: var(--color-primary);
    background: rgba(180, 83, 9, 0.08);

    .mobile-nav-icon {
      opacity: 1;
    }
  }

  &.highlight {
    color: var(--color-primary);
    background: rgba(180, 83, 9, 0.08);
  }

  &.logout {
    color: var(--color-error);
  }
}

.mobile-nav-divider {
  height: 1px;
  background: var(--border-color);
  margin: var(--space-3) 0;
}

// ========================================
// Mobile Bottom Navigation
// ========================================
.mobile-bottom-nav {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--bg-overlay);
  backdrop-filter: blur(16px);
  border-top: 1px solid var(--border-color);
  padding-bottom: env(safe-area-inset-bottom);
  z-index: var(--z-fixed);
}

.mobile-bottom-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
  flex: 1;
  padding: var(--space-2) 0;
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: var(--text-muted);
  background: none;
  border: none;
  text-decoration: none;
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;

  svg {
    width: 22px;
    height: 22px;
    transition: all var(--transition-fast);
  }

  // 底部指示器动画
  &::before {
    content: "";
    position: absolute;
    top: 0;
    left: 50%;
    transform: translateX(-50%) scaleX(0);
    width: 32px;
    height: 3px;
    background: var(--gradient-primary);
    border-radius: var(--radius-full);
    transition: transform var(--transition-fast);
  }

  &:hover {
    color: var(--text-secondary);

    svg {
      transform: translateY(-2px);
    }
  }

  &.active {
    color: var(--color-primary);

    svg {
      transform: scale(1.15);
      filter: drop-shadow(0 2px 4px rgba(180, 83, 9, 0.3));
    }

    &::before {
      transform: translateX(-50%) scaleX(1);
    }
  }
}

// ========================================
// Transitions
// ========================================
.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--transition-base);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform var(--transition-base) var(--ease-out);
}

.slide-right-enter-from,
.slide-right-leave-to {
  transform: translateX(100%);
}

// ========================================
// Desktop only elements
// ========================================
.search-toggle,
.menu-toggle {
  display: none;
}

@media (max-width: 768px) {
  .search-toggle,
  .menu-toggle {
    display: flex;
  }

  .mobile-bottom-nav {
    display: flex;
  }

  .footer {
    display: none;
  }

  .main {
    min-height: calc(100vh - 60px - 70px);
  }
}
</style>
