const api = require('../../utils/api')
const share = require('../../utils/share')

Page({
  data: {
    loading: false,
    agreed: false
  },

  onShow() {
    share.enableShareMenu()
    const token = wx.getStorageSync('token')
    if (token) {
      getApp().globalData.token = token
      wx.reLaunch({ url: '/pages/index/index' })
    }
  },

  // 分享功能
  onShareAppMessage() {
    return share.appMessage('墨鱼小日记')
  },

  // 分享到朋友圈
  onShareTimeline() {
    return share.timeline('墨鱼小日记')
  },

  openAgreement() {
    wx.navigateTo({ url: '/pages/agreement/agreement' })
  },

  openPrivacy() {
    wx.navigateTo({ url: '/pages/privacy/privacy' })
  },

  onAgreeChange(e) {
    const values = e.detail.value || []
    this.setData({ agreed: values.indexOf('agree') > -1 })
  },

  handleWxLogin() {
    if (!this.data.agreed) {
      wx.showToast({ title: '请先勾选用户协议和隐私政策', icon: 'none' })
      return
    }
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
