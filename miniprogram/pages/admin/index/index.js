const adminApi = require('../../../utils/adminApi')

Page({
  data: {
    loading: true,
    stats: {
      totalUsers: 0,
      newUsersThisMonth: 0,
      totalRecords: 0,
      todayRecords: 0,
      activeUsers7d: 0
    },
    topSymptoms: [],
    maxSymptomCount: 0,
    adminName: ''
  },

  onShow() {
    this.checkAuth()
    this.setData({ adminName: (wx.getStorageSync('admin_user') || {}).username || '管理员' })
    this.loadOverview()
  },

  onPullDownRefresh() {
    this.loadOverview().finally(() => wx.stopPullDownRefresh())
  },

  checkAuth() {
    if (!wx.getStorageSync('admin_token')) {
      wx.reLaunch({ url: '/pages/admin/login/login' })
    }
  },

  async loadOverview() {
    this.setData({ loading: true })
    try {
      const data = await adminApi.get('/admin/stats/overview')
      const symptoms = data.topSymptoms || []
      this.setData({
        stats: {
          totalUsers: data.totalUsers || 0,
          newUsersThisMonth: data.newUsersThisMonth || 0,
          totalRecords: data.totalRecords || 0,
          todayRecords: data.todayRecords || 0,
          activeUsers7d: data.activeUsers7d || 0
        },
        topSymptoms: symptoms,
        maxSymptomCount: symptoms.reduce((m, s) => Math.max(m, s.count || 0), 0)
      })
    } catch (e) {
      console.error('load overview failed', e)
    }
    this.setData({ loading: false })
  },

  goTo(e) {
    const url = e.currentTarget.dataset.url
    wx.navigateTo({ url })
  },

  logout() {
    wx.showModal({
      title: '退出管理后台',
      content: '确定退出当前管理员账号吗？',
      confirmText: '退出',
      confirmColor: '#ec407a',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('admin_token')
          wx.removeStorageSync('admin_user')
          wx.reLaunch({ url: '/pages/admin/login/login' })
        }
      }
    })
  }
})
