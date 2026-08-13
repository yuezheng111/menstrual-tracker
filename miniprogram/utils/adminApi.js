const { API_BASE } = require('./constants')

const request = (method, url, data) => {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('admin_token')
    wx.request({
      url: API_BASE + url,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? 'Bearer ' + token : ''
      },
      success(res) {
        if (res.data.code === 0) {
          resolve(res.data.data)
        } else if (res.data.code === 401 || res.data.code === 403) {
          wx.removeStorageSync('admin_token')
          wx.removeStorageSync('admin_user')
          wx.showToast({ title: '登录已失效，请重新登录', icon: 'none' })
          setTimeout(() => {
            wx.reLaunch({ url: '/pages/admin/login/login' })
          }, 800)
          reject(new Error(res.data.message))
        } else {
          wx.showToast({ title: res.data.message || '请求失败', icon: 'none' })
          reject(new Error(res.data.message))
        }
      },
      fail(err) {
        wx.showToast({ title: '网络错误，请检查后端服务', icon: 'none' })
        reject(err)
      }
    })
  })
}

module.exports = {
  get: (url, data) => request('GET', url, data),
  post: (url, data) => request('POST', url, data),
  put: (url, data) => request('PUT', url, data),
  del: (url) => request('DELETE', url)
}
