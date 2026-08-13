const { API_BASE } = require('../../../utils/constants')

Page({
  data: {
    exporting: ''
  },

  onShow() {
    if (!wx.getStorageSync('admin_token')) {
      wx.reLaunch({ url: '/pages/admin/login/login' })
    }
  },

  handleExport(e) {
    const type = e.currentTarget.dataset.type
    const filename = type === 'users' ? 'users.csv' : 'records.csv'
    this.setData({ exporting: type })
    const token = wx.getStorageSync('admin_token')
    wx.downloadFile({
      url: API_BASE + '/admin/export/' + type,
      header: { 'Authorization': 'Bearer ' + token },
      success: (res) => {
        if (res.statusCode === 200) {
          const fs = wx.getFileSystemManager()
          const savedPath = `${wx.env.USER_DATA_PATH}/${filename}`
          fs.saveFile({
            tempFilePath: res.tempFilePath,
            filePath: savedPath,
            success: () => {
              wx.showModal({
                title: '导出成功',
                content: `文件已保存到小程序本地：${savedPath}
可通过微信文件管理查看，也可在网页管理端导出更完整的数据。`,
                showCancel: false
              })
            },
            fail: (err) => {
              wx.showToast({ title: '保存失败', icon: 'none' })
              console.error('save file failed', err)
            }
          })
        } else {
          wx.showToast({ title: '导出失败（' + res.statusCode + '）', icon: 'none' })
        }
      },
      fail: (err) => {
        wx.showToast({ title: '下载失败，请检查网络', icon: 'none' })
        console.error('download failed', err)
      },
      complete: () => {
        this.setData({ exporting: '' })
      }
    })
  }
})
