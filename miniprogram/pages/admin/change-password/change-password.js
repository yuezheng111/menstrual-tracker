const adminApi = require('../../../utils/adminApi')

Page({
  data: {
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
    loading: false
  },

  onInput(e) {
    this.setData({ [e.currentTarget.dataset.field]: e.detail.value })
  },

  async handleChange() {
    const { oldPassword, newPassword, confirmPassword } = this.data
    if (!oldPassword || !newPassword || !confirmPassword) {
      wx.showToast({ title: '请填写完整', icon: 'none' })
      return
    }
    if (newPassword.length < 8) {
      wx.showToast({ title: '新密码至少8位', icon: 'none' })
      return
    }
    if (newPassword !== confirmPassword) {
      wx.showToast({ title: '两次输入不一致', icon: 'none' })
      return
    }
    if (newPassword === oldPassword) {
      wx.showToast({ title: '新密码不能与原密码相同', icon: 'none' })
      return
    }

    this.setData({ loading: true })
    try {
      const result = await adminApi.put('/admin/password/change', {
        oldPassword,
        newPassword
      })
      wx.setStorageSync('admin_token', result.token)
      wx.setStorageSync('admin_user', { username: result.username, role: result.role })
      wx.showToast({ title: '修改成功', icon: 'success' })
      wx.reLaunch({ url: '/pages/admin/index/index' })
    } catch (e) {
      // toast already handled in adminApi
    }
    this.setData({ loading: false })
  }
})
