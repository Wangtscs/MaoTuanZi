import { defineStore } from 'pinia'
import { ref } from 'vue'
function saved(key:string){try{return localStorage.getItem(key)}catch{return null}}
function persist(key:string,value:string){try{localStorage.setItem(key,value)}catch{/* Preferences remain usable without storage. */}}
export const useAppStore=defineStore('app',()=>{
 const collapsed=ref(saved('ops.sidebar.collapsed')==='true')
 const compact=ref(saved('ops.density')==='compact')
 const theme=ref<'dark'|'light'>(saved('ops.theme')==='light'?'light':'dark')
 function setTheme(value:'dark'|'light'){theme.value=value;document.documentElement.classList.toggle('dark',value==='dark');document.documentElement.dataset.theme=value;persist('ops.theme',value)}
 function toggleCollapsed(){collapsed.value=!collapsed.value;persist('ops.sidebar.collapsed',String(collapsed.value))}
 function setCompact(value:boolean){compact.value=value;document.documentElement.dataset.density=value?'compact':'comfortable';persist('ops.density',value?'compact':'comfortable')}
 setTheme(theme.value);setCompact(compact.value)
 return {collapsed,theme,compact,setTheme,toggleCollapsed,setCompact}
})
