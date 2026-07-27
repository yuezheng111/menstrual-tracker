import os
base = r"D:\111网安学习\menstrual-tracker"

def rp(path):
    return base + path

# Fix 1: Header padding
p = rp("\miniprogram\pages\index\index.wxml")
c = open(p, "r", encoding="utf-8").read()
old = "<view class=\"page-index\">"
new = "<view class=\"page-index\" style=\"padding-top:{{statusBarHeight+10}}px\">"
c = c.replace(old, new)
open(p, "w", encoding="utf-8").write(c)
print("Fix 1: Header padding OK")

# Fix 1b: statusBarHeight in index.js
p = rp("\miniprogram\pages\index\index.js")
c = open(p, "r", encoding="utf-8").read()
c = c.replace(
    "  onShow() {\n    this.setData({ username:",
    "  onShow() {\n    this.initStatusBar()\n    this.setData({ username:"
)
insert = '  initStatusBar() {\n    try {\n      const info = wx.getSystemInfoSync()\n      this.setData({ statusBarHeight: info.statusBarHeight })\n    } catch (e) {\n      this.setData({ statusBarHeight: 20 })\n    }\n  }\n\n'
c = c.replace("  setGreeting() {", insert + "  setGreeting() {")
open(p, "w", encoding="utf-8").write(c)
print("Fix 1b: statusBarHeight load OK")

# Fix 2: Symptom null safety
p = rp("\miniprogram\pages\record\record.wxml")
c = open(p, "r", encoding="utf-8").read()
c = c.replace("form.symptoms.indexOf(item) > -1", "(form.symptoms||[]).indexOf(item) > -1")
c = c.replace("form.moodTags.indexOf(item) > -1", "(form.moodTags||[]).indexOf(item) > -1")
open(p, "w", encoding="utf-8").write(c)
print("Fix 2: Symptom null safety OK")

# Fix 3: History records link
p = rp("\miniprogram\pages\index\index.wxml")
c = open(p, "r", encoding="utf-8").read()
old_btn = '<cute-btn text="\U0001f4dd \u8bb0\u5f55\u7ecf\u671f" block="{{true}}" bindtap="goToRecord" />'
new_btn = '<view style="margin-bottom:10px">' + old_btn + '</view>\n      <cute-btn text="\U0001f4cb \u5386\u53f2\u8bb0\u5f55" block="{{true}}" bindtap="goToRecords" />'
c = c.replace(old_btn, new_btn)
open(p, "w", encoding="utf-8").write(c)

p = rp("\miniprogram\pages\index\index.js")
c = open(p, "r", encoding="utf-8").read()
c = c.replace(
    "  goToRecord() {\n    wx.navigateTo({ url:",
    '  goToRecords() {\n    wx.navigateTo({ url: "/pages/records/records" })\n  }\n\n  goToRecord() {\n    wx.navigateTo({ url:'
)
open(p, "w", encoding="utf-8").write(c)
print("Fix 3: History records link OK")

# Fix 4: Cycle day calc
p = rp("\backend\src\main\java\com\menstrualtracker\record\service\MenstrualRecordService.java")
c = open(p, "r", encoding="utf-8").read()
old_calc = '''        LocalDate prevStart = recordRepository.findByUserIdAndDeletedFalseOrderByStartDateDesc(userId)
                .stream().findFirst().map(MenstrualRecord::getStartDate).orElse(null);
        int cycleDay = 1;
        if (prevStart != null) cycleDay = (int) ChronoUnit.DAYS.between(prevStart, request.getStartDate()) + 1;'''
new_calc = '''        java.util.List<MenstrualRecord> allRecords = recordRepository.findByUserIdAndDeletedFalseOrderByStartDateDesc(userId);
        LocalDate prevStart = allRecords.stream()
                .map(MenstrualRecord::getStartDate)
                .filter(d -> d.isBefore(request.getStartDate()))
                .findFirst().orElse(null);
        int cycleDay = 1;
        if (prevStart != null) cycleDay = (int) ChronoUnit.DAYS.between(prevStart, request.getStartDate()) + 1;'''
c = c.replace(old_calc, new_calc)
open(p, "w", encoding="utf-8").write(c)
print("Fix 4: Cycle day calc OK")

# Fix 5: Button visibility
p = rp("\miniprogram\pages\record\record.wxss")
c = open(p, "r", encoding="utf-8").read()
c = c.replace(
    ".page-record {\n  padding: 0 16px 0;\n}",
    ".page-record {\n  padding: 0 16px;\n  display: flex;\n  flex-direction: column;\n  height: 100vh;\n}"
)
c = c.replace(
    ".page-header {\n  padding: 20px 0 16px;\n}",
    ".page-header {\n  padding: 20px 0 16px;\n  flex-shrink: 0;\n}"
)
c = c.replace(
    ".form-scroll {\n  height: calc(100vh - 100px);\n}",
    ".form-scroll {\n  flex: 1;\n  overflow-y: auto;\n  padding-bottom: 40px;\n}"
)
open(p, "w", encoding="utf-8").write(c)
print("Fix 5: Button visibility OK\n\n=== All 5 fixes applied! ===")
