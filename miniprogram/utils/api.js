const { API_BASE } = require('./constants')

let isRefreshing = false
let redirecting = false
let refreshSubscribers = []

function subscribeTokenRefresh() {
  return new Promise((resolve, reject) => {
    refreshSubscribers.push({ resolve, reject })
  })
}

function onRefreshed(token) {
  const subscribers = refreshSubscribers
  refreshSubscribers = []
  subscribers.forEach(item => item.resolve(token))
}

function onRefreshFailed(error) {
  const subscribers = refreshSubscribers
  refreshSubscribers = []
  subscribers.forEach(item => item.reject(error))
}

function wxRequest(config) {
  return new Promise((resolve, reject) => {
    wx.request({
      ...config,
      success: resolve,
      fail: reject
    })
  })
}

function isAuthFailure(res) {
  const code = res.data && res.data.code
  return res.statusCode === 401 || res.statusCode === 403 || code === 401 || code === 403
}

function handleBusinessError(res) {
  wx.showToast({ title: (res.data && res.data.message) || '请求失败', icon: 'none' })
  throw new Error(res.data && res.data.message)
}

async function requestWithAuth(requestConfig, retried) {
  const res = await wxRequest(requestConfig)

  if (res.data && res.data.code === 0) {
    return res.data.data
  }

  if (!isAuthFailure(res)) {
    handleBusinessError(res)
  }

  if (res.statusCode === 401 && !retried) {
    return refreshAndRetry(requestConfig)
  }

  clearAuthAndRedirect()
  throw new Error((res.data && res.data.message) || '登录已失效，请重新登录')
}

function refreshAndRetry(requestConfig) {
  if (isRefreshing) {
    return subscribeTokenRefresh()
      .then(token => requestWithAuth({
        ...requestConfig,
        header: { ...requestConfig.header, Authorization: 'Bearer ' + token }
      }, true))
      .catch(error => {
        clearAuthAndRedirect()
        throw error
      })
  }

  isRefreshing = true
  return refreshToken()
    .then(token => {
      isRefreshing = false
      onRefreshed(token)
      return requestWithAuth({
        ...requestConfig,
        header: { ...requestConfig.header, Authorization: 'Bearer ' + token }
      }, true)
    })
    .catch(error => {
      isRefreshing = false
      onRefreshFailed(error)
      clearAuthAndRedirect()
      throw error
    })
}

const request = (method, url, data) => {
  const app = getApp()
  const requestConfig = {
    url: API_BASE + url,
    method,
    data,
    header: {
      'Content-Type': 'application/json',
      'Authorization': app.globalData.token ? 'Bearer ' + app.globalData.token : ''
    }
  }
  return requestWithAuth(requestConfig, false)
}

function refreshToken() {
  return new Promise((resolve, reject) => {
    wx.login({
      success: async (res) => {
        if (!res.code) {
          reject(new Error('获取登录信息失败'))
          return
        }
        try {
          const app = getApp()
          const oldToken = app.globalData.token
          const response = await wxRequest({
            url: API_BASE + '/auth/refresh-token',
            method: 'POST',
            data: { code: res.code },
            header: {
              'Content-Type': 'application/json',
              'Authorization': oldToken ? 'Bearer ' + oldToken : ''
            }
          })

          if (response.data && response.data.code === 0 && response.data.data.token) {
            const newToken = response.data.data.token
            wx.setStorageSync('token', newToken)
            app.globalData.token = newToken
            resolve(newToken)
          } else {
            reject(new Error((response.data && response.data.message) || '刷新token失败'))
          }
        } catch (e) {
          reject(e)
        }
      },
      fail: () => {
        reject(new Error('微信登录失败'))
      }
    })
  })
}

function clearAuthAndRedirect() {
  wx.removeStorageSync('token')
  wx.removeStorageSync('role')
  wx.removeStorageSync('nickname')
  const app = getApp()
  if (app && app.globalData) {
    app.globalData.token = null
    app.globalData.userInfo = null
  }
  if (redirecting) return
  redirecting = true
  wx.redirectTo({
    url: '/pages/login/login',
    complete: () => {
      redirecting = false
    }
  })
}

module.exports = {
  get: (url, data) => request('GET', url, data),
  post: (url, data) => request('POST', url, data),
  put: (url, data) => request('PUT', url, data),
  del: (url) => request('DELETE', url)
}
