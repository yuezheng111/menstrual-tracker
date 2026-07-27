import os
p = r"D:\111网安学习\menstrual-tracker\miniprogram\pages\index\index.js"
c = open(p, "r", encoding="utf-8").read()
d = chr(34)
old = "  goToRecord() {"
new = "  goToRecords() {\n    wx.navigateTo({ url: " + d + "/pages/records/records" + d + " })\n  }\n\n  goToRecord() {"
c = c.replace(old, new)
open(p, "w", encoding="utf-8").write(c)

# Fix 3b: Add button to index.wxml
p2 = r"D:\111网安学习\menstrual-tracker\miniprogram\pages\index\index.wxml"
c2 = open(p2, "r", encoding="utf-8").read()
old2 = "<cute-btn text=\U0001f4dd \u8bb0\u5f55\u7ecf\u671f block={{true}} bindtap=goToRecord />"
new2 = old2 + "\n      <cute-btn text=\U0001f4cb \u5386\u53f2\u8bb0\u5f55 block={{true}} bindtap=goToRecords />"
c2 = c2.replace('<cute-btn text="' + chr(55357) + chr(56333) + ' ' + chr(35760) + chr(24405) + chr(32463) + chr(26399) + '" block="{{true}}" bindtap="goToRecord" />', 
               '<cute-btn text="' + chr(55357) + chr(56333) + ' ' + chr(35760) + chr(24405) + chr(32463) + chr(26399) + '" block="{{true}}" bindtap="goToRecord" />\n      <cute-btn text="' + chr(55357) + chr(56331) + ' ' + chr(21382) + chr(21490) + chr(35760) + chr(24405) + '" block="{{true}}" bindtap="goToRecords" />')
open(p2, "w", encoding="utf-8").write(c2)

# Fix 4: Cycle day calculation
p3 = r"D:\111网安学习\menstrual-tracker\backend\src\main\java\com\menstrualtracker\record\service\MenstrualRecordService.java"
c3 = open(p3, "r", encoding="utf-8").read()
old3 = """        LocalDate prevStart = recordRepository.findByUserIdAndDeletedFalseOrderByStartDateDesc(userId)
                .stream().findFirst().map(MenstrualRecord::getStartDate).orElse(null);
        int cycleDay = 1;
        if (prevStart != null) cycleDay = (int) ChronoUnit.DAYS.between(prevStart, request.getStartDate()) + 1;"""
new3 = """        java.util.List<MenstrualRecord> allRecords = recordRepository.findByUserIdAndDeletedFalseOrderByStartDateDesc(userId);
        LocalDate prevStart = allRecords.stream()
                .map(MenstrualRecord::getStartDate)
                .filter(d -> d.isBefore(request.getStartDate()))
                .findFirst().orElse(null);
        int cycleDay = 1;
        if (prevStart != null) cycleDay = (int) ChronoUnit.DAYS.between(prevStart, request.getStartDate()) + 1;"""
c3 = c3.replace(old3, new3)
open(p3, "w", encoding="utf-8").write(c3)

print("Fix 3 + 4 done")
