/**
 * 小程序环境配置
 * 切换方式：修改下方 ENV 的值即可。
 * - 'dev'  -> 本地后端（微信开发者工具本地联调）
 * - 'prod' -> 服务器后端（159.75.222.241）
 */
const ENV = 'dev'

const CONFIG = {
  dev: {
    API_BASE: 'http://localhost:8080/api'
  },
  prod: {
    // 正式域名；如需临时切回 IP，改成 https://42.194.244.111/api
    API_BASE: 'https://www.moyoo.asia/api'
  }
}

module.exports = {
  ENV: ENV,
  API_BASE: CONFIG[ENV].API_BASE
}
