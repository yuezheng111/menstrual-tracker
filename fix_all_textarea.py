import os
p = r"D:\111网安学习\menstrual-tracker\miniprogram\pages\record\record.wxml"
c = open(p, "r", encoding="utf-8").read()

# Fix all textarea lines
# Remove corrupted value attribute patterns
d = chr(34)
lb = chr(123)
rb = chr(125)

# 1. Replace the WRONG pattern (missing value=) 
#    placeholder="\u5907\u6ce8..."{{\u0066orm.notes}}"
wrong1 = ' placeholder=' + d + '\u5907\u6ce8\uff08\u53ef\u8bb0\u5f55\u8eab\u4f53\u611f\u53d7\u3001\u7528\u836f\u7b49\uff09' + d + lb + lb + 'form.notes' + rb + rb + d
# 2. Replace with correct pattern  
#    placeholder="\u5907\u6ce8..." value="{{form.notes}}"
correct = ' placeholder=' + d + '\u5907\u6ce8\uff08\u53ef\u8bb0\u5f55\u8eab\u4f53\u611f\u53d7\u3001\u7528\u836f\u7b49\uff09' + d + ' value=' + d + lb + lb + 'form.notes' + rb + rb + d

c = c.replace(wrong1, correct)

# Also fix the style attribute to be on same line
# Move style inline after bindinput
c = c.replace(' bindinput="onNotesInput"\n        style="margin-top:14px;height:80px;resize:none" />', ' bindinput="onNotesInput" style="margin-top:14px;height:80px;resize:none" />')

# Also fix the textarea at line 101 (which has extra wx:if)
# Find and remove the corrupted duplicate
old101 = 'textarea class=' + d + 'input-glass' + d + ' placeholder=' + d + '\u5907\u6ce8\uff08\u53ef\u8bb0\u5f55\u8eab\u4f53\u611f\u53d7\u3001\u7528\u836f\u7b49\uff09' + d + ' value=' + d + lb + lb + 'form.notes' + rb + rb + d + d + ' wx:if=' + d + lb + '!isEditing' + rb + d + '>'
# This is a corrupted line, remove the extra " and wx:if
c = c.replace(old101, 'textarea class=' + d + 'input-glass' + d + ' placeholder=' + d + '\u5907\u6ce8\uff08\u53ef\u8bb0\u5f55\u8eab\u4f53\u611f\u53d7\u3001\u7528\u836f\u7b49\uff09' + d + ' value=' + d + lb + lb + 'form.notes' + rb + rb + d + ' bindinput=' + d + 'onNotesInput' + d)

open(p, "w", encoding="utf-8").write(c)
print("All textarea fixed")
