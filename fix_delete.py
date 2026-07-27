import os

# Add delete button to record.wxml
p = r"D:\111网安学习\menstrual-tracker\miniprogram\pages\record\record.wxml"
c = open(p, "r", encoding="utf-8").read()
old = '</scroll-view>\n</view>'
new = '    <view wx:if="{{isEditing}}" style="margin-top:8px;text-align:center">\n      <text style="font-size:14px;color:#e53935;padding:8px 20px;display:inline-block" bindtap="confirmDelete">\U0001f5d1\ufe0f \u5220\u9664\u8fd9\u6761\u8bb0\u5f55</text>\n    </view>\n  </scroll-view>\n</view>'
c = c.replace(old, new)
open(p, "w", encoding="utf-8").write(c)
print("record.wxml delete button added")

# Add confirmDelete and deleteRecord to record.js
p = r"D:\111网安学习\menstrual-tracker\miniprogram\pages\record\record.js"
c = open(p, "r", encoding="utf-8").read()
# Add methods before loadData
old_method = "  onLoad(options) {"
new_method = '''  confirmDelete() {
    wx.showModal({
      title: "\u786e\u8ba4\u5220\u9664",
      content: "\u786e\u5b9a\u5220\u9664\u8fd9\u6761\u8bb0\u5f55\u5417\uff1f",
      confirmText: "\u5220\u9664",
      confirmColor: "#ec407a",
      success: (res) => {
        if (res.confirm) this.deleteRecord()
      }
    })
  },

  async deleteRecord() {
    try {
      const api = require("../../utils/api")
      await api.del("/records/" + this.data.recordId)
      wx.showToast({ title: "\u5df2\u5220\u9664", icon: "success" })
      wx.navigateBack()
    } catch (e) {
      wx.showToast({ title: "\u5220\u9664\u5931\u8d25", icon: "none" })
    }
  },

  onLoad(options) {'''
c = c.replace(old_method, new_method)
open(p, "w", encoding="utf-8").write(c)
print("record.js delete methods added")
