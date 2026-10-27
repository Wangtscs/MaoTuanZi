import * as echarts from 'echarts'
import { nextTick, onBeforeUnmount, onMounted, watch, type Ref } from 'vue'
export function useChart(elRef: Ref<HTMLElement | null>, option: Ref<any>) {
  let chart: echarts.ECharts | null = null
  let container: HTMLElement | null = null
  let observer: ResizeObserver | null = null
  let themeObserver: MutationObserver | null = null
  let disposed = false
  function release() { observer?.disconnect(); observer = null; chart?.dispose(); chart = null; container = null }
  async function render() {
    await nextTick()
    if (disposed) return
    const el = elRef.value
    if (container !== el) release()
    if (!el) return
    if (!observer) {
      container = el
      observer = new ResizeObserver(() => { if (chart) chart.resize(); else void render() })
      observer.observe(el)
    }
    if (el.clientWidth === 0 || el.clientHeight === 0) return
    if (!chart) chart = echarts.init(el, undefined, { renderer: 'canvas' })
    const color = getComputedStyle(el).getPropertyValue('--ops-muted').trim() || '#64748b'
    chart.setOption({ backgroundColor: 'transparent', textStyle: { color }, grid: { left: 48, right: 24, top: 36, bottom: 32 }, ...option.value }, true)
    chart.resize()
  }
  watch([elRef, option], () => { void render() }, { deep: true, flush: 'post' })
  onMounted(() => {
    void render()
    themeObserver = new MutationObserver(() => { void render() })
    themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['class', 'data-theme', 'style'] })
  })
  onBeforeUnmount(() => { disposed = true; release(); themeObserver?.disconnect() })
  return { resize: () => chart?.resize() }
}
