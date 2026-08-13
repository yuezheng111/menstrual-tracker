const { API_BASE } = require('./constants')

const request = (method, url, data) => {
  const app = getApp()
  return new Promise((resolve, reject) => {
    wx.request({
      url: API_BASE + url,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': app.globalData.token ? 'Bearer ' + app.globalData.token : ''
      },
      success(res) {
        if (res.data.code === 0) {
          resolve(res.data.data)
        } else if (res.data.code === 401) {
          wx.removeStorageSync('token')
          wx.removeStorageSync('role')
          wx.redirectTo({ url: '/pages/login/login' })
          reject(new Error(res.data.message))
        } else {
          wx.showToast({ title: res.data.message || '请求失败', icon: 'none' })
          reject(new Error(res.data.message))
        }
      },
      fail(err) { reject(err) }
    })
  })
}

module.exports = {
  get: (url, data) => request('GET', url, data),
  post: (url, data) => request('POST', url, data),
  put: (url, data) => request('PUT', url, data),
  del: (url) => request('DELETE', url)
}
