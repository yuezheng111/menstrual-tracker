const api = require('../../utils/api')

Page({
  data: {
    loading: false
  },

  onShow() {
    const token = wx.getStorageSync('token')
    if (token) {
      getApp().globalData.token = token
      wx.reLaunch({ url: '/pages/index/index' })
    }
  },

  openAgreement() {
    wx.navigateTo({ url: '/pages/agreement/agreement' })
  },

  openPrivacy() {
    wx.navigateTo({ url: '/pages/privacy/privacy' })
  },

  handleWxLogin() {
    this.setData({ loading: true })
    wx.login({
      success: async (res) => {
        if (res.code) {
          try {
            const result = await api.post('/auth/wx-login', { code: res.code })
            wx.setStorageSync('token', result.token)
            wx.setStorageSync('nickname', result.user.nickname || result.user.username || '用户')
            wx.setStorageSync('role', result.user.role || 'USER')
            getApp().globalData.token = result.token
            wx.showToast({ title: '登录成功', icon: 'success' })
            wx.reLaunch({ url: '/pages/index/index' })
          } catch (e) {
            wx.showToast({ title: '登录失败，请重试', icon: 'none' })
          }
        } else {
          wx.showToast({ title: '获取登录信息失败', icon: 'none' })
        }
        this.setData({ loading: false })
      },
      fail: () => {
        wx.showToast({ title: '微信登录失败', icon: 'none' })
        this.setData({ loading: false })
      }
    })
  }
})
