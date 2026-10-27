<template>
 <header class="page-heading"><div><div v-if="eyebrow" class="eyebrow muted">{{ eyebrow }}</div><h1>{{ title }}</h1><p v-if="description">{{ description }}</p></div><div v-if="$slots.default" class="heading-actions"><slot /></div></header>
</template>
<script setup lang="ts">
defineProps<{ title: string; eyebrow?: string; description?: string }>()
</script>
<style scoped>
.page-heading{display:flex;align-items:center;justify-content:space-between;gap:18px;margin-bottom:25px}.page-heading h1{font-size:27px;line-height:1.3;letter-spacing:-.8px;margin:8px 0 7px;font-weight:700}.page-heading p{margin:0;color:var(--ops-sub);font-size:12px;line-height:1.8}.heading-actions{display:flex;align-items:center;gap:9px;flex-wrap:wrap;justify-content:flex-end}@media(max-width:760px){.page-heading{align-items:flex-start;flex-direction:column;gap:16px}.page-heading h1{font-size:23px}.heading-actions{justify-content:flex-start}}
</style>
