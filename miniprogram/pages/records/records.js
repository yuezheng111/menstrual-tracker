const api = require('../../utils/api')
const share = require('../../utils/share')

const FLOW_MAP = { LIGHT: '小', MEDIUM: '中', HEAVY: '大' }
const PAIN_MAP = { NONE: '无', MILD: '轻微', MODERATE: '中等', SEVERE: '严重' }

Page({
  data: {
    records: [],
    loading: false,
    total: 0
  },

  onShow() {
    share.enableShareMenu()
    this.loadRecords()
  },

  onPullDownRefresh() {
    this.loadRecords()
  },

  // 分享功能
  onShareAppMessage() {
    return share.appMessage('墨鱼小日记')
  },

  // 分享到朋友圈
  onShareTimeline() {
    return share.timeline('墨鱼小日记')
  },

  async loadRecords() {
    this.setData({ loading: true })
    try {
      const res = await api.get('/records', { page: 1, size: 50 })
      const records = (res.content || []).map(r => ({
        ...r,
        flowLabel: FLOW_MAP[r.flow] || r.flow || '-',
        painLabel: PAIN_MAP[r.painLevel] || r.painLevel || '-',
        flow_lower: (r.flow || '').toLowerCase(),
        pain_lower: (r.painLevel || '').toLowerCase()
      }))
      this.setData({ records, total: res.totalElements || records.length })
    } catch (e) {
      console.error('Failed to load records', e)
    }
    this.setData({ loading: false })
    wx.stopPullDownRefresh()
  },

  editRecord(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/record/record?id=' + id })
  },

  confirmDelete(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '确认删除',
      content: '删除后无法恢复，确定删除这条记录吗？',
      confirmText: '删除',
      confirmColor: '#ec407a',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) this.deleteRecord(id)
      }
    })
  },

  async deleteRecord(id) {
    try {
      await api.del('/records/' + id)
      wx.showToast({ title: '已删除', icon: 'success' })
      this.refreshHome()
      this.loadRecords()
    } catch (e) {
      wx.showToast({ title: '删除失败', icon: 'none' })
    }
  },

  refreshHome() {
    const pages = getCurrentPages()
    for (const pg of pages) {
      if (pg.route === 'pages/index/index' && typeof pg.loadData === 'function') {
        pg.loadData()
      }
    }
  },

  goToRecord() {
    wx.navigateTo({ url: '/pages/record/record' })
  }
})
