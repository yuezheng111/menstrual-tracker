const api = require('../../utils/api')

Page({
  data: {
    loading: false,
    hasData: false,
    overview: null
  },

  onShow() {
    this.loadData()
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
