const api = require('../../utils/api')

Page({
  data: {
    userInfo: {
      username: '',
      nickname: '',
      role: 'USER'
    }
  },

  onShow() {
    const role = wx.getStorageSync('role') || 'USER'
    if (role !== 'ADMIN') {
      wx.reLaunch({ url: '/pages/index/index' })
      return
    }
    this.loadProfile()
  },

  async loadProfile() {
    try {
      const data = await api.get('/auth/profile')
      wx.setStorageSync('nickname', data.nickname || data.username || '用户')
      this.setData({ userInfo: { ...this.data.userInfo, ...data } })
    } catch (e) {
      console.error('Failed to load profile', e)
    }
  },

  editNickname() {
    const current = this.data.userInfo.nickname && this.data.userInfo.nickname !== '微信用户'
      ? this.data.userInfo.nickname
      : ''
    wx.showModal({
      title: '修改昵称',
      editable: true,
      content: current,
      placeholderText: '请输入昵称',
      success: async (res) => {
        if (!res.confirm) return
        const name = (res.content || '').trim()
        if (!name) {
          wx.showToast({ title: '昵称不能为空', icon: 'none' })
          return
        }
        if (name.length > 30) {
          wx.showToast({ title: '昵称最多30个字符', icon: 'none' })
          return
        }
        try {
          const updated = await api.put('/auth/profile', { nickname: name })
          wx.setStorageSync('nickname', updated.nickname || name)
          this.setData({ 'userInfo.nickname': updated.nickname || name })
          wx.showToast({ title: '已更新', icon: 'success' })
        } catch (e) {
          wx.showToast({ title: '修改失败', icon: 'none' })
        }
      }
    })
  },

  goToAdmin() {
    const token = wx.getStorageSync('admin_token')
    if (token) {
      wx.navigateTo({ url: '/pages/admin/index/index' })
    } else {
      wx.navigateTo({ url: '/pages/admin/login/login' })
    }
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
          wx.removeStorageSync('nickname')
          wx.removeStorageSync('role')
          wx.redirectTo({ url: '/pages/login/login' })
        }
      }
    })
  }
})
