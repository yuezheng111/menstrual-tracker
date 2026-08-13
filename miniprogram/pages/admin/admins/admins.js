const adminApi = require('../../../utils/adminApi')

Page({
  data: {
    list: [],
    loading: false,
    currentAdmin: '',
    grantVisible: false,
    grantUsername: '',
    granting: false
  },

  onShow() {
    this.checkAuth()
    const info = wx.getStorageSync('admin_user') || {}
    this.setData({ currentAdmin: info.username || '' })
    this.load()
  },

  checkAuth() {
    if (!wx.getStorageSync('admin_token')) {
      wx.reLaunch({ url: '/pages/admin/login/login' })
    }
  },

  async load() {
    this.setData({ loading: true })
    try {
      const data = await adminApi.get('/admin/admins')
      this.setData({ list: data || [] })
    } catch (e) {
      console.error('load admins failed', e)
    }
    this.setData({ loading: false })
  },

  openGrant() {
    this.setData({ grantVisible: true, grantUsername: '' })
  },

  closeGrant() {
    this.setData({ grantVisible: false })
  },

  onGrantInput(e) {
    this.setData({ grantUsername: e.detail.value })
  },

  async confirmGrant() {
    const name = (this.data.grantUsername || '').trim()
    if (!name) {
      wx.showToast({ title: '请输入用户名', icon: 'none' })
      return
    }
    this.setData({ granting: true })
    try {
      const data = await adminApi.get('/admin/users', { page: 0, size: 100, keyword: name })
      const user = (data.content || []).find(u => u.username === name)
      if (!user) {
        wx.showToast({ title: '未找到该用户', icon: 'none' })
        return
      }
      await adminApi.put(`/admin/admins/${user.id}/grant`)
      wx.showToast({ title: '已授予管理员角色', icon: 'success' })
      this.setData({ grantVisible: false })
      this.load()
    } catch (e) {
      console.error('grant admin failed', e)
    }
    this.setData({ granting: false })
  },

  handleRevoke(e) {
    const row = e.currentTarget.dataset.row
    wx.showModal({
      title: '撤销管理员',
      content: `确定撤销「${row.username}」的管理员权限吗？`,
      confirmColor: '#ec407a',
      success: async (res) => {
        if (res.confirm) {
          try {
            await adminApi.put(`/admin/admins/${row.id}/revoke`)
            wx.showToast({ title: '已撤销', icon: 'success' })
            this.load()
          } catch (err) {
            console.error('revoke admin failed', err)
          }
        }
      }
    })
  }
})
