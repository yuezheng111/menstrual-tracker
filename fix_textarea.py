import os
p = r"D:\111网安学习\menstrual-tracker\miniprogram\pages\record\record.wxml"
c = open(p, "r", encoding="utf-8").read()
d = chr(34)
old = 'placeholder=' + d + '\u6fb3\u56e8\u655e\uff31\u7ab9\u8bb0\u5f55\u8eab\ue0a1\u7f51\u6e5b\u5f39\u300a\u7528\u837f\ue21c\u7433\u300b' + d
new = 'placeholder=' + d + '\u5907\u6ce8\uff08\u53ef\u8bb0\u5f55\u8eab\u4f53\u611f\u53d7\u3001\u7528\u836f\u7b49\uff09' + d + ' value=' + d + '{{form.notes}}' + d + ' bindinput=' + d + 'onNotesInput' + d + ' style=' + d + 'margin-top:14px;height:80px;resize:none' + d
c = c.replace(old, new)
open(p, "w", encoding="utf-8").write(c)
print("Fixed")
