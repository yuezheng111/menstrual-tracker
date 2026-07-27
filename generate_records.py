import os
def w(p,c):
    with open(p,"w",encoding="utf-8") as f:
        f.write(c)
    print("Written:",os.path.basename(p))
base=r"D:\111网安学习\menstrual-tracker\miniprogram\pages\record"
# Generate clean record.wxml
wxml='''<view class="page-record">
  <view class="page-header">
    <text class="page-title">\U0001f4dd \u8bb0\u5f55\u7ecf\u671f</text>
    <text class="page-desc" wx:if="{{!isEditing}}">\u8bb0\u5f55\u4f60\u7684\u8eab\u4f53\u72b6\u6001\uff0c\u83b7\u53d6\u66f4\u51c6\u786e\u7684\u9884\u6d4b</text>
  </view>
  <scroll-view class="form-scroll" scroll-y enhanced scroll-with-animation>
    <glass-card customClass="form-section">
      <view class="section-title">\U0001f4c5 \u65e5\u671f</view>
      <view class="date-row">
        <picker mode="date" value="{{form.startDate}}" bindchange="onStartDateChange">
          <view class="date-box"><text class="date-lbl">\u5f00\u59cb</text><text class="date-val {{form.startDate ? "" : "empty"}}">{{form.startDate || "\u9009\u62e9"}}</text></view>
        </picker>
        <text class="date-sep">~</text>
        <picker mode="date" value="{{form.endDate}}" bindchange="onEndDateChange">
          <view class="date-box"><text class="date-lbl">\u7ed3\u675f</text><text class="date-val {{form.endDate ? "" : "empty"}}">{{form.endDate || "\u9009\u62e9"}}</text></view>
        </picker>
      </view>
    </glass-card>
    <glass-card customClass="form-section">
      <view class="section-title">\U0001f4a7 \u7ecf\u91cf</view>
      <view class="chip-row">
        <view wx:for="{{flowOptions}}" wx:key="value" class="chip {{form.flow === item.value ? "active" : ""}}" bindtap="selectFlow" data-value="{{item.value}}"><text>{{item.emoji}} {{item.label}}</text></view>
      </view>
    </glass-card>
    <glass-card customClass="form-section">
      <view class="section-title">\U0001f623 \u75db\u7ecf\u7a0b\u5ea6</view>
      <view class="chip-row">
        <view wx:for="{{painOptions}}" wx:key="value" class="chip {{form.painLevel === item.value ? "active" : ""}}" bindtap="selectPain" data-value="{{item.value}}"><text>{{item.emoji}} {{item.label}}</text></view>
      </view>
    </glass-card>
    <glass-card customClass="form-section">
      <view class="section-title">\U0001f912 \u75c7\u72b6</view>
      <view class="chip-row">
        <view wx:for="{{symptomOptions}}" wx:for-index="idx" wx:for-item="item" wx:key="*this" class="chip-sm {{symptomActive[idx] ? "active" : ""}}" bindtap="toggleSymptom" data-value="{{item}}">{{item}}</view>
      </view>
    </glass-card>
    <glass-card customClass="form-section">
      <view class="section-title">\U0001f60a \u60c5\u7eea</view>
      <view class="chip-row">
        <view wx:for="{{moodOptions}}" wx:for-index="idx" wx:for-item="item" wx:key="*this" class="chip-sm {{moodActive[idx] ? "active" : ""}}" bindtap="toggleMood" data-value="{{item}}">{{item}}</view>
      </view>
    </glass-card>
    <glass-card customClass="form-section">
      <view class="section-title">\U0001f4cb \u5176\u4ed6</view>
      <view class="switch-row"><text>\u8840\u5757</text><switch checked="{{form.clots}}" bindchange="onClotsChange" color="#ec407a" /></view>
      <textarea class="input-glass" placeholder="\u5907\u6ce8\uff08\u53ef\u8bb0\u5f55\u8eab\u4f53\u611f\u53d7\u3001\u7528\u836f\u7b49\uff09" value="{{form.notes}}" bindinput="onNotesInput" style="margin-top:14px;height:80px;resize:none" />
    </glass-card>
    <view style="margin:20px 0 40px">
      <cute-btn text="{{isEditing ? "\u4fdd\u5b58\u4fee\u6539" : "\u4fdd\u5b58\u8bb0\u5f55"}}" block="{{true}}" loading="{{saving}}" bindtap="saveRecord" />
    </view>
  </scroll-view>
  <view wx:if="{{isEditing}}" style="margin-top:8px;text-align:center">
    <text style="font-size:14px;color:#e53935;padding:8px 20px;display:inline-block" bindtap="confirmDelete">\U0001f5d1\ufe0f \u5220\u9664\u8fd9\u6761\u8bb0\u5f55</text>
  </view>
</view>'''
w(base+r"\record.wxml",wxml)
print("record.wxml written")
