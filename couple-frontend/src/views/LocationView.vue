<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMe } from '@/api/user'
import { getCoupleStatus } from '@/api/couple'
import Globe from 'globe.gl'

const store = useUserStore()

interface LocationPoint {
  lat: number
  lng: number
  color: string
  label: string
  radius: number
}

interface LabelElement {
  lat: number
  lng: number
  element: HTMLElement
}

const containerRef = ref<HTMLElement>()
const loading = ref(true)
const bound = ref(false)

const myInfo = ref({
  nickname: '',
  city: '',
  ip: '',
  lat: null as number | null,
  lng: null as number | null,
})

const partnerInfo = ref({
  nickname: '',
  city: '',
  ip: '',
  lat: null as number | null,
  lng: null as number | null,
})

const hasMyLocation = computed(() => myInfo.value.lat != null && myInfo.value.lng != null)
const hasPartnerLocation = computed(() => partnerInfo.value.lat != null && partnerInfo.value.lng != null)
const hasBothLocations = computed(() => hasMyLocation.value && hasPartnerLocation.value)

const distanceKm = computed(() => {
  if (!hasBothLocations.value) return null
  const lat1 = myInfo.value.lat!
  const lng1 = myInfo.value.lng!
  const lat2 = partnerInfo.value.lat!
  const lng2 = partnerInfo.value.lng!
  const R = 6371
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLng = (lng2 - lng1) * Math.PI / 180
  const a = Math.sin(dLat / 2) ** 2
    + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * Math.sin(dLng / 2) ** 2
  return Math.round(R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)))
})

let globeInstance: any = null
let worldBorders: any[] = []

function createLabelEl(text: string, color: string): HTMLElement {
  const el = document.createElement('div')
  el.className = 'globe-label'
  el.innerHTML = `<span class="label-dot" style="background:${color}"></span>${text}`
  return el
}

function buildPoints(): LocationPoint[] {
  const points: LocationPoint[] = []
  if (hasMyLocation.value) {
    points.push({
      lat: myInfo.value.lat!,
      lng: myInfo.value.lng!,
      color: '#F2877A',
      label: `💕 ${myInfo.value.nickname || '我'} · ${myInfo.value.city}`,
      radius: 0.6,
    })
  }
  if (hasPartnerLocation.value) {
    points.push({
      lat: partnerInfo.value.lat!,
      lng: partnerInfo.value.lng!,
      color: '#7EB8F4',
      label: `💗 ${partnerInfo.value.nickname || 'TA'} · ${partnerInfo.value.city}`,
      radius: 0.6,
    })
  }
  return points
}

function buildLabels(): LabelElement[] {
  const labels: LabelElement[] = []
  if (hasMyLocation.value) {
    labels.push({
      lat: myInfo.value.lat!,
      lng: myInfo.value.lng!,
      element: createLabelEl(myInfo.value.city || '我', '#F2877A'),
    })
  }
  if (hasPartnerLocation.value) {
    labels.push({
      lat: partnerInfo.value.lat!,
      lng: partnerInfo.value.lng!,
      element: createLabelEl(partnerInfo.value.city || 'TA', '#7EB8F4'),
    })
  }
  return labels
}

function calcMidpoint(): { lat: number; lng: number } {
  if (hasBothLocations.value) {
    return {
      lat: (myInfo.value.lat! + partnerInfo.value.lat!) / 2,
      lng: (myInfo.value.lng! + partnerInfo.value.lng!) / 2,
    }
  }
  if (hasMyLocation.value) {
    return { lat: myInfo.value.lat!, lng: myInfo.value.lng! }
  }
  return { lat: 30, lng: 110 }
}

function calcAltitude(): number {
  if (hasBothLocations.value) {
    const dist = distanceKm.value || 1000
    if (dist < 500) return 1.2
    if (dist < 2000) return 1.8
    if (dist < 5000) return 2.5
    return 3.0
  }
  return 2.0
}

async function loadBorders() {
  try {
    const res = await fetch(
      'https://raw.githubusercontent.com/nvkelso/natural-earth-vector/master/geojson/ne_110m_admin_0_countries.geojson'
    )
    const json = await res.json()
    worldBorders = (json.features || []).filter(
      (f: any) =>
        f.geometry &&
        (f.geometry.type === 'Polygon' || f.geometry.type === 'MultiPolygon')
    )
  } catch (e) {
    console.warn('Failed to load borders:', e)
  }
}

function buildPolygons(): any[] {
  return worldBorders
}

function initGlobe(polygons: any[]) {
  if (!containerRef.value) return
  const points = buildPoints()
  const labels = buildLabels()
  const mid = calcMidpoint()
  const alt = calcAltitude()

  globeInstance = new Globe(containerRef.value)
    .globeImageUrl('https://unpkg.com/three-globe/example/img/earth-blue-marble.jpg')
    .bumpImageUrl('https://unpkg.com/three-globe/example/img/earth-topology.png')
    .backgroundColor('rgba(0,0,0,0)')
    .atmosphereColor('rgba(180,200,255,0.2)')
    .atmosphereAltitude(0.15)
    // Country borders — stroke only, no fill, for performance
    .polygonsData(polygons)
    .polygonGeoJsonGeometry('geometry')
    .polygonCapColor(() => 'rgba(0,0,0,0)')
    .polygonSideColor(() => 'rgba(0,0,0,0)')
    .polygonStrokeColor(() => 'rgba(255,255,255,0.22)')
    .polygonAltitude(0.001)
    .polygonCapCurvatureResolution(8)
    .polygonsTransitionDuration(0)
    .polygonLabel((d: any) => {
      const p = d.properties || {}
      return p.name || p.NAME || p.Name || ''
    })
    // User markers
    .pointsData(points)
    .pointLat((d: any) => d.lat)
    .pointLng((d: any) => d.lng)
    .pointColor((d: any) => d.color)
    .pointRadius((d: any) => d.radius)
    .pointLabel((d: any) => d.label)
    .pointAltitude(0.015)
    .pointResolution(32)
    // City labels on surface
    .htmlElementsData(labels)
    .htmlLat((d: any) => d.lat)
    .htmlLng((d: any) => d.lng)
    .htmlElement((d: any) => d.element)
    .htmlAltitude(0.08)

  if (hasBothLocations.value) {
    globeInstance
      .arcsData([{
        startLat: myInfo.value.lat!,
        startLng: myInfo.value.lng!,
        endLat: partnerInfo.value.lat!,
        endLng: partnerInfo.value.lng!,
      }])
      .arcStartLat((d: any) => d.startLat)
      .arcStartLng((d: any) => d.startLng)
      .arcEndLat((d: any) => d.endLat)
      .arcEndLng((d: any) => d.endLng)
      .arcColor(() => '#F2B987')
      .arcStroke(() => 1.6)
      .arcDashLength(() => 0.6)
      .arcDashGap(() => 0.12)
      .arcDashAnimateTime(() => 3500)
      .arcAltitude(0.15)
      .arcLabel(() => `${distanceKm.value} km`)
  }

  globeInstance.controls().autoRotate = true
  globeInstance.controls().autoRotateSpeed = 0.5
  globeInstance.controls().enableZoom = true
  globeInstance.controls().minDistance = 150
  globeInstance.controls().maxDistance = 800

  globeInstance.pointOfView({ lat: mid.lat, lng: mid.lng, altitude: alt }, 1500)
}

function updateGlobe() {
  if (!globeInstance) return
  globeInstance.pointsData(buildPoints())
  globeInstance.htmlElementsData(buildLabels())
  globeInstance.pointOfView({
    lat: calcMidpoint().lat,
    lng: calcMidpoint().lng,
    altitude: calcAltitude(),
  }, 1000)
}

async function fetchData() {
  loading.value = true
  try {
    const [me, status] = await Promise.all([getMe(), getCoupleStatus()])
    myInfo.value.nickname = me.nickname || me.username || '我'
    myInfo.value.city = me.city || '未知'
    myInfo.value.ip = me.ip || '未知'
    myInfo.value.lat = me.latitude ?? null
    myInfo.value.lng = me.longitude ?? null

    if (status.bound) {
      bound.value = true
      partnerInfo.value.nickname = status.partnerNickname || 'TA'
      partnerInfo.value.city = status.partnerCity || '未知'
      partnerInfo.value.ip = status.partnerIp || '未知'
      partnerInfo.value.lat = status.partnerLatitude ?? null
      partnerInfo.value.lng = status.partnerLongitude ?? null
    }
  } finally {
    loading.value = false
    await nextTick()

    // Load borders on first init
    if (!globeInstance) {
      if (worldBorders.length === 0) {
        await loadBorders()
      }
      initGlobe(buildPolygons())
    } else {
      updateGlobe()
    }
  }
}

let resizeObserver: ResizeObserver | null = null

onMounted(() => {
  fetchData()
  if (containerRef.value) {
    resizeObserver = new ResizeObserver(() => {
      globeInstance?.resize?.()
    })
    resizeObserver.observe(containerRef.value)
  }
})

onUnmounted(() => {
  resizeObserver?.disconnect()
  if (globeInstance) {
    globeInstance._destructor?.()
    globeInstance = null
  }
})
</script>

<template>
  <div class="location-view fade-in">
    <div class="globe-wrap" :class="{ loading }">
      <div v-if="loading" class="globe-loading">
        <span class="spinner"></span>
        <span>加载中...</span>
      </div>
      <div ref="containerRef" class="globe-container"></div>

      <div class="globe-overlay">
        <h1>📍 我们的位置</h1>
        <div v-if="bound && hasBothLocations" class="distance-badge">
          <span class="dist-dot pink"></span>
          {{ myInfo.city }}
          <span class="dist-arrow">——</span>
          <span class="dist-val">{{ distanceKm }} km</span>
          <span class="dist-arrow">——</span>
          <span class="dist-dot blue"></span>
          {{ partnerInfo.city }}
        </div>
        <p v-else-if="bound">定位数据加载中...</p>
        <p v-else>绑定情侣后，你们的位置将在地球上相连</p>
      </div>
    </div>

    <div class="info-cards">
      <div class="info-card me">
        <div class="card-header">
          <span class="card-dot" style="background:#F2877A"></span>
          <span class="card-name">💕 {{ myInfo.nickname || '我' }}</span>
        </div>
        <div class="card-body">
          <div class="card-row"><span>IP</span><code>{{ myInfo.ip || '未知' }}</code></div>
          <div class="card-row"><span>城市</span><strong>{{ myInfo.city || '未知' }}</strong></div>
          <div class="card-row"><span>坐标</span>
            {{ hasMyLocation ? `${myInfo.lat?.toFixed(4)}, ${myInfo.lng?.toFixed(4)}` : '未知' }}
          </div>
        </div>
      </div>

      <div class="info-card partner" v-if="bound">
        <div class="card-header">
          <span class="card-dot" style="background:#7EB8F4"></span>
          <span class="card-name">💗 {{ partnerInfo.nickname || 'TA' }}</span>
        </div>
        <div class="card-body">
          <div class="card-row"><span>IP</span><code>{{ partnerInfo.ip || '未知' }}</code></div>
          <div class="card-row"><span>城市</span><strong>{{ partnerInfo.city || '未知' }}</strong></div>
          <div class="card-row"><span>坐标</span>
            {{ hasPartnerLocation ? `${partnerInfo.lat?.toFixed(4)}, ${partnerInfo.lng?.toFixed(4)}` : '未知' }}
          </div>
        </div>
      </div>

      <div class="info-card empty" v-else>
        <div class="card-header">
          <span class="card-dot" style="background:#ccc"></span>
          <span class="card-name">💗 等待绑定</span>
        </div>
        <div class="card-body">
          <div class="card-row hint">去「情侣绑定」邀请你的另一半吧</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.location-view {
  padding: 0 0 32px;
}

.globe-wrap {
  position: relative;
  width: 100%;
  height: 520px;
  background: radial-gradient(ellipse at center, #0d1428 0%, #050812 100%);
  overflow: hidden;
  border-bottom: 1px solid rgba(232, 141, 125, 0.1);
}

.globe-wrap.loading .globe-container {
  opacity: 0;
}

.globe-loading {
  position: absolute; inset: 0;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 12px;
  color: rgba(255,255,255,0.45);
  font-size: 14px;
  z-index: 2;
}

.globe-container {
  width: 100%;
  height: 100%;
  transition: opacity 0.6s;
}

.globe-overlay {
  position: absolute;
  top: 0; left: 0; right: 0;
  padding: 24px 28px;
  pointer-events: none;
  z-index: 2;
  background: linear-gradient(to bottom, rgba(5,8,18,0.75) 0%, transparent 100%);
}
.globe-overlay h1 {
  font-size: 22px; font-weight: 800; color: rgba(255,255,255,0.88);
  letter-spacing: 1px; margin: 0 0 8px;
}
.globe-overlay p {
  font-size: 13px; color: rgba(255,255,255,0.45);
  margin: 0;
}

.distance-badge {
  display: flex; align-items: center; gap: 8px;
  font-size: 14px; color: rgba(255,255,255,0.72);
}
.dist-dot {
  width: 10px; height: 10px; border-radius: 50%;
  flex-shrink: 0;
}
.dist-dot.pink { background: #F2877A; box-shadow: 0 0 8px #F2877A; }
.dist-dot.blue { background: #7EB8F4; box-shadow: 0 0 8px #7EB8F4; }
.dist-arrow { color: rgba(255,255,255,0.3); font-size: 12px; }
.dist-val {
  font-weight: 700; color: #F2B987;
  background: rgba(242,185,135,0.12);
  padding: 2px 10px; border-radius: 12px; font-size: 13px;
}

.spinner {
  width: 28px; height: 28px;
  border: 2px solid rgba(255,255,255,0.12);
  border-top-color: rgba(255,255,255,0.55);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

:deep(.globe-label) {
  display: flex; align-items: center; gap: 5px;
  padding: 3px 10px 3px 6px;
  border-radius: 12px;
  background: rgba(0,0,0,0.7);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  pointer-events: none;
  text-shadow: 0 1px 3px rgba(0,0,0,0.5);
  backdrop-filter: blur(4px);
  transform: translate(-50%, -50%);
}
:deep(.label-dot) {
  width: 7px; height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.info-cards {
  max-width: 800px;
  margin: 24px auto 0;
  padding: 0 20px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-card {
  padding: 18px 20px;
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition);
}
.info-card:hover {
  border-color: rgba(232,141,125,0.3);
  box-shadow: var(--shadow-md);
}

.card-header {
  display: flex; align-items: center; gap: 10px;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--color-border);
}
.card-dot {
  width: 10px; height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.card-name {
  font-size: 16px; font-weight: 700; color: var(--color-text);
}

.card-body { display: flex; flex-direction: column; gap: 6px; }
.card-row {
  display: flex; align-items: center; gap: 8px;
  font-size: 13px; color: var(--color-text-soft);
}
.card-row span:first-child {
  color: var(--color-text-muted);
  font-size: 11px;
  width: 36px;
  flex-shrink: 0;
}
.card-row code {
  font-size: 12px; color: var(--color-primary);
  background: rgba(232,141,125,0.08);
  padding: 1px 8px; border-radius: 4px;
  font-family: 'SF Mono', 'Consolas', monospace;
}
.card-row strong {
  color: var(--color-text);
  font-weight: 600;
}
.card-row.hint {
  color: var(--color-text-muted);
  font-style: italic;
}

.info-card.empty {
  opacity: 0.55;
}

@media (max-width: 768px) {
  .globe-wrap { height: 340px; }
  .globe-overlay { padding: 14px 16px; }
  .globe-overlay h1 { font-size: 18px; }
  .info-cards { grid-template-columns: 1fr; padding: 0 12px; margin-top: 16px; }
}
</style>
