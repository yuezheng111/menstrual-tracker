const adminApi = require('../../../utils/adminApi')

Page({
  data: {
    typeFilter: '',
    list: [],
    loading: false
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

  onTypeChange(e) {
    const type = e.currentTarget.dataset.type
    this.setData({ typeFilter: this.data.typeFilter === type ? '' : type })
    this.load()
  },

  async load() {
    this.setData({ loading: true })
    try {
      const data = await adminApi.get('/admin/tags', {
        type: this.data.typeFilter || undefined
      })
      this.setData({ list: data || [] })
    } catch (e) {
      console.error('load tags failed', e)
    }
    this.setData({ loading: false })
  },

  handleDelete(e) {
    const row = e.currentTarget.dataset.row
    wx.showModal({
      title: '删除标签',
      content: `确定删除标签「${row.name}」吗？`,
      confirmColor: '#ec407a',
      success: async (res) => {
        if (res.confirm) {
          try {
            await adminApi.del(`/admin/tags/${row.id}`)
            wx.showToast({ title: '已删除', icon: 'success' })
            this.load()
          } catch (err) {
            console.error('delete tag failed', err)
          }
        }
      }
    })
  }
})
