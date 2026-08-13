const adminApi = require('../../../utils/adminApi')

Page({
  data: {
    username: '',
    password: '',
    loading: false
  },

  onShow() {
    const token = wx.getStorageSync('admin_token')
    if (token) {
      wx.reLaunch({ url: '/pages/admin/index/index' })
    }
  },

  onInput(e) {
    this.setData({ [e.currentTarget.dataset.field]: e.detail.value })
  },

  async handleLogin() {
    if (!this.data.username || !this.data.password) {
      wx.showToast({ title: '请输入用户名和密码', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    try {
      const result = await adminApi.post('/admin/login', {
        username: this.data.username,
        password: this.data.password
      })
      wx.setStorageSync('admin_token', result.token)
      wx.setStorageSync('admin_user', { username: result.username, role: result.role })
      wx.showToast({ title: '登录成功', icon: 'success' })
      if (result.mustChangePassword) {
        wx.reLaunch({ url: '/pages/admin/change-password/change-password' })
      } else {
        wx.reLaunch({ url: '/pages/admin/index/index' })
      }
    } catch (e) {
      // toast already handled in adminApi
    }
    this.setData({ loading: false })
  }
})
