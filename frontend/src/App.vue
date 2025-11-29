<template>
  <div id="app" class="min-h-screen bg-gray-50">
    <Navbar />
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import Navbar from '@/components/layout/Navbar.vue'
</script>