FROM nginx:1.27-alpine

COPY deploy/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY admin-frontend/dist /usr/share/nginx/html/admin

# Serve the mini program landing page at /
RUN echo '<!DOCTYPE html><html lang="zh-CN"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>墨鱼小日记</title></head><body style="font-family:sans-serif;display:flex;align-items:center;justify-content:center;min-height:100vh;margin:0;background:#fdf2f8;color:#4a154b;text-align:center;padding:20px"><div><div style="font-size:64px">🐙</div><h1>墨鱼小日记</h1><p>请在微信中搜索小程序「墨鱼小日记」使用</p></div></body></html>' > /usr/share/nginx/html/index.html
