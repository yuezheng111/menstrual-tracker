App({
  globalData: {
    token: null,
    userInfo: null,
    apiBaseUrl: 'http://localhost:8080/api'
  },
  onLaunch() {
    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.token = token
    }
  }
})
