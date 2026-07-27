const api = require('../../utils/api')

Page({
  data: {
    username: '',
    greeting: '你好',
    hasData: false,
    totalRecords: 0,
    avgCycle: '-',
    avgPeriod: '-',
    cycleDay: 0,
    cycleProgress: 0,
    statusText: '加载中...',
    statusEmoji: '🌸',
    nextPeriodDays: null,
    prediction: {}
  },

  onShow() {
    this.initStatusBar()
    this.setData({ username: wx.getStorageSync('username') || '用户' })
    this.setGreeting()
    this.loadData()
  },

  initStatusBar() {
    try {
      const info = wx.getSystemInfoSync()
      this.setData({ statusBarHeight: info.statusBarHeight })
    } catch (e) {
      this.setData({ statusBarHeight: 20 })
    }
  },

  setGreeting() {
    const h = new Date().getHours()
    let g = '你好'
    if (h < 6) g = '夜深了'
    else if (h < 9) g = '早上好'
    else if (h < 12) g = '上午好'
    else if (h < 14) g = '中午好'
    else if (h < 18) g = '下午好'
    else g = '晚上好'
    this.setData({ greeting: g })
  },

  async loadData() {
    try {
      const o = await api.get('/statistics/overview')
      if (o.totalRecords > 0) {
        const today = new Date()
        today.setHours(0, 0, 0, 0)

        // 计算周期天数：从最近一次记录的开始日期到今天
        let cycleDay = 0
        if (o.lastPeriodStart) {
          const lastStart = new Date(o.lastPeriodStart)
          lastStart.setHours(0, 0, 0, 0)
          cycleDay = Math.round((today - lastStart) / (1000 * 60 * 60 * 24)) + 1
          if (cycleDay < 1) cycleDay = 1
        }

        // 判断是否正在经期中：今天是否在最近记录的日期范围内
        let inPeriod = false
        if (o.lastPeriodStart && o.lastPeriodEnd) {
          const lastEnd = new Date(o.lastPeriodEnd)
          lastEnd.setHours(0, 0, 0, 0)
          const lastStart = new Date(o.lastPeriodStart)
          lastStart.setHours(0, 0, 0, 0)
          inPeriod = today >= lastStart && today <= lastEnd
        }

        // 距离下次经期天数
        let nextDays = null
        if (o.nextPredictedStart) {
          const next = new Date(o.nextPredictedStart)
          next.setHours(0, 0, 0, 0)
          nextDays = Math.round((next - today) / (1000 * 60 * 60 * 24))
        }

        // 状态判断
        let statusText = '安全期 🌿'
        let statusEmoji = '🌿'
        if (inPeriod) {
          statusText = '经期中 💕'
          statusEmoji = '💕'
        } else if (nextDays !== null && nextDays <= 5 && nextDays >= 0) {
          statusText = '即将到来 ⏰'
          statusEmoji = '⏰'
        } else if (o.ovulationStart && nextDays !== null && nextDays > 10 && nextDays < 18) {
          statusText = '排卵期 🌸'
          statusEmoji = '🌸'
        }

        let progress = 0
        if (o.avgCycleLength && o.avgCycleLength > 0) {
          progress = Math.round((cycleDay / o.avgCycleLength) * 100)
          progress = Math.max(0, Math.min(100, progress))
        }

        this.setData({
          hasData: true,
          totalRecords: o.totalRecords || 0,
          avgCycle: o.avgCycleLength ? o.avgCycleLength.toFixed(1) + '天' : '-',
          avgPeriod: o.avgPeriodLength ? o.avgPeriodLength.toFixed(1) + '天' : '-',
          cycleDay,
          cycleProgress: progress,
          statusText,
          statusEmoji,
          nextPeriodDays: nextDays,
          prediction: {
            nextPeriodStart: o.nextPredictedStart || '-',
            nextPeriodEnd: o.nextPredictedEnd || '-',
            ovulationStart: o.ovulationStart || '-',
            ovulationEnd: o.ovulationEnd || '-',
            fertileWindowStart: o.fertileWindowStart || '-',
            fertileWindowEnd: o.fertileWindowEnd || '-',
            safePeriodStart: o.safePeriodStart || '-',
            safePeriodEnd: o.safePeriodEnd || '-'
          }
        })
      }
    } catch (e) {
      console.error('Failed to load data', e)
    }
  },

  goToRecords() {
    wx.navigateTo({ url: "/pages/records/records" })
  },

  goToRecord() {
    wx.navigateTo({ url: '/pages/record/record' })
  },

  goToStatistics() {
    wx.reLaunch({ url: '/pages/statistics/statistics' })
  }
})
