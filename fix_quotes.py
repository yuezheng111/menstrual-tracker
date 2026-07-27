import os
p = r"D:\111网安学习\menstrual-tracker\miniprogram\pages\record\record.wxml"
c = open(p, "r", encoding="utf-8").read()
d = chr(34)
s = chr(39)
# Fix: replace "xxxxx" with 'xxxxx' inside {{ }} when inside WXML attributes
c = c.replace(d + "\u4fdd\u5b58\u4fee\u6539" + d, s + "\u4fdd\u5b58\u4fee\u6539" + s)
c = c.replace(d + "\u4fdd\u5b58\u8bb0\u5f55" + d, s + "\u4fdd\u5b58\u8bb0\u5f55" + s)
c = c.replace(d + "\u9009\u62e9" + d, s + "\u9009\u62e9" + s)
open(p, "w", encoding="utf-8").write(c)
print("Fixed")
