const api = require('../../utils/api')

Page({
  data: {
    saving: false,
    userInfo: {
      username: '',
      phone: '',
      email: '',
      birthDate: '',
      menarcheAge: null,
      avgCycleDays: 28,
      avgPeriodDays: 5
    }
  },

  onShow() {
    this.loadProfile()
  },

  async loadProfile() {
    try {
      const data = await api.get('/auth/profile')
      this.setData({ userInfo: { ...this.data.userInfo, ...data } })
    } catch (e) {
      console.error('Failed to load profile', e)
    }
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field
    const value = e.detail.value
    this.setData({ ['userInfo.' + field]: value })
  },

  onBirthDateChange(e) {
    this.setData({ 'userInfo.birthDate': e.detail.value })
  },

  async saveProfile() {
    this.setData({ saving: true })
    try {
      await api.put('/auth/profile', this.data.userInfo)
      wx.showToast({ title: '设置已保存', icon: 'success' })
    } catch (e) {
      wx.showToast({ title: '保存失败', icon: 'none' })
    }
    this.setData({ saving: false })
  },

  logout() {
    wx.showModal({
      title: '退出登录',
      content: '确定退出登录吗？',
      confirmText: '退出',
      confirmColor: '#ec407a',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('token')
          wx.removeStorageSync('username')
          wx.redirectTo({ url: '/pages/login/login' })
        }
      }
    })
  }
})
