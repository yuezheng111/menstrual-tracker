import os
p = r"D:\111网安学习\menstrual-tracker\miniprogram\pages\record\record.wxml"
c = open(p, "r", encoding="utf-8").read()
c = c.replace('wx:for="{{symptomOptions}}" wx:key="*this"', 'wx:for="{{symptomOptions}}" wx:for-index="idx" wx:for-item="item" wx:key="*this"')
c = c.replace('wx:for="{{moodOptions}}" wx:key="*this"', 'wx:for="{{moodOptions}}" wx:for-index="idx" wx:for-item="item" wx:key="*this"')
c = c.replace("(form.symptoms||[]).indexOf(item) > -1", "symptomActive[idx]")
c = c.replace("(form.moodTags||[]).indexOf(item) > -1", "moodActive[idx]")
open(p, "w", encoding="utf-8").write(c)
print("WXML fixed")
