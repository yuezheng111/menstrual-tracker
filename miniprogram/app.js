const { API_BASE } = require('./config')

App({
  globalData: {
    token: null,
    userInfo: null,
    apiBaseUrl: API_BASE
  },
  onLaunch() {
    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.token = token
    }
  }
})
