const DEFAULT_TITLE = '墨鱼小日记'
const SHARE_IMAGE = '/images/share.png'

function enableShareMenu() {
  if (!wx.showShareMenu) return
  try {
    wx.showShareMenu({
      menus: ['shareAppMessage', 'shareTimeline']
    })
  } catch (e) {
    console.warn('showShareMenu failed', e)
  }
}

function appMessage(title, path) {
  return {
    title: title || DEFAULT_TITLE,
    path: path || '/pages/index/index',
    imageUrl: SHARE_IMAGE
  }
}

function timeline(title, query) {
  const result = {
    title: title || DEFAULT_TITLE,
    imageUrl: SHARE_IMAGE
  }
  if (query) {
    result.query = query
  }
  return result
}

module.exports = {
  enableShareMenu,
  appMessage,
  timeline
}
