const adminApi = require('../../../utils/adminApi')

Page({
  data: {
    keyword: '',
    list: [],
    page: 1,
    size: 10,
    totalPages: 0,
    total: 0,
    loading: false,
    resetVisible: false,
    resetTarget: null,
    newPassword: ''
  },

  onShow() {
    this.checkAuth()
    this.load()
  },

  checkAuth() {
    if (!wx.getStorageSync('admin_token')) {
      wx.reLaunch({ url: '/pages/admin/login/login' })
    }
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onSearch() {
    this.setData({ page: 1 })
    this.load()
  },

  onClearSearch() {
    this.setData({ keyword: '', page: 1 })
    this.load()
  },

  async load() {
    this.setData({ loading: true })
    try {
      const data = await adminApi.get('/admin/users', {
        page: this.data.page - 1,
        size: this.data.size,
        keyword: this.data.keyword || undefined
      })
      this.setData({
        list: data.content || [],
        total: data.totalElements || 0,
        totalPages: data.totalPages || 0
      })
    } catch (e) {
      console.error('load users failed', e)
    }
    this.setData({ loading: false })
  },

  prevPage() {
    if (this.data.page > 1) {
      this.setData({ page: this.data.page - 1 })
      this.load()
    }
  },

  nextPage() {
    if (this.data.page < this.data.totalPages) {
      this.setData({ page: this.data.page + 1 })
      this.load()
    }
  },

  toggleStatus(e) {
    const row = e.currentTarget.dataset.row
    const action = row.enabled ? '禁用' : '启用'
    wx.showModal({
      title: '提示',
      content: `确定要${action}用户「${row.username}」吗？`,
      confirmColor: '#ec407a',
      success: async (res) => {
        if (res.confirm) {
          try {
            await adminApi.put(`/admin/users/${row.id}/status`, { enabled: !row.enabled })
            wx.showToast({ title: `${action}成功`, icon: 'success' })
            this.load()
          } catch (e) {
            console.error('toggle status failed', e)
          }
        }
      }
    })
  },

  openReset(e) {
    const row = e.currentTarget.dataset.row
    this.setData({ resetVisible: true, resetTarget: row, newPassword: '' })
  },

  onPasswordInput(e) {
    this.setData({ newPassword: e.detail.value })
  },

  async confirmReset() {
    const pw = this.data.newPassword
    if (!pw || pw.length < 8) {
      wx.showToast({ title: '新密码至少 8 位', icon: 'none' })
      return
    }
    try {
      await adminApi.put(`/admin/users/${this.data.resetTarget.id}/password`, { newPassword: pw })
      wx.showToast({ title: '密码已重置', icon: 'success' })
      this.setData({ resetVisible: false })
    } catch (e) {
      console.error('reset password failed', e)
    }
  },

  closeReset() {
    this.setData({ resetVisible: false })
  }
})
