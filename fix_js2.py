import os
p = r"D:\111网安学习\menstrual-tracker\miniprogram\pages\record\record.js"
c = open(p, "r", encoding="utf-8").read()

# Add symptomActive/moodActive to data
c = c.replace("moodTags: [],", "moodTags: [],\n    symptomActive: [],\n    moodActive: [],")

# Add updateActive method before saveRecord
old_save = "  saveRecord()"
new_method = '''  updateActive() {
    const s = this.data.form.symptoms || []
    const m = this.data.form.moodTags || []
    const symptomActive = this.data.symptomOptions.map(x => s.indexOf(x) > -1)
    const moodActive = this.data.moodOptions.map(x => m.indexOf(x) > -1)
    this.setData({ symptomActive, moodActive })
  },

  saveRecord()'''
c = c.replace(old_save, new_method)

# Call updateActive after toggleSymptom
c = c.replace(
  "this.setData({ 'form.symptoms': arr })",
  "this.setData({ 'form.symptoms': arr }); this.updateActive()"
)
# Call updateActive after toggleMood
c = c.replace(
  "this.setData({ 'form.moodTags': arr })",
  "this.setData({ 'form.moodTags': arr }); this.updateActive()"
)
# Call updateActive after loadRecord
c = c.replace(
  "this.setData({ saving: false })\n  },\n\n  onStartDateChange",
  "      this.updateActive()\n    this.setData({ saving: false })\n  },\n\n  onStartDateChange"
)

open(p, "w", encoding="utf-8").write(c)
print("JS fixed")
