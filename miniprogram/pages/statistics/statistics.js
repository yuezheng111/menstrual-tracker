const api = require('../../utils/api')
const share = require('../../utils/share')

Page({
  data: {
    loading: false,
    hasData: false,
    overview: null
  },

  onShow() {
    share.enableShareMenu()
    this.loadData()
  },

  // 分享功能
  onShareAppMessage() {
    return share.appMessage('墨鱼小日记')
  },

  // 分享到朋友圈
  onShareTimeline() {
    return share.timeline('墨鱼小日记')
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const overview = await api.get('/statistics/overview')
      this.setData({
        hasData: overview.totalRecords > 0,
        overview
      })
    } catch (e) {
      this.setData({ hasData: false })
    }
    this.setData({ loading: false })
  }
})
