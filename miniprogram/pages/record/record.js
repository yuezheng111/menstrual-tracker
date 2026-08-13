const api = require('../../utils/api')

Page({
  data: {
    isEditing: false,
    recordId: null,
    saving: false,
    symptomActive: [],
    moodActive: [],
    form: {
      startDate: '',
      endDate: '',
      flow: '',
      painLevel: '',
      symptoms: [],
      moodTags: [],
      clots: false,
      notes: ''
    },
    flowOptions: [
      { value: 'LIGHT', label: '少', emoji: '\u{1F4A7}' },
      { value: 'MEDIUM', label: '中', emoji: '\u{1F4A6}' },
      { value: 'HEAVY', label: '多', emoji: '\u{1F30A}' }
    ],
    painOptions: [
      { value: 'NONE', label: '无', emoji: '\u{1F60A}' },
      { value: 'MILD', label: '轻微', emoji: '\u{1F642}' },
      { value: 'MODERATE', label: '中等', emoji: '\u{1F623}' },
      { value: 'SEVERE', label: '严重', emoji: '\u{1F62B}' }
    ],
    symptomOptions: ['头痛','疲劳','腹胀','腹痛','背痛','恶心','头晕','长痘','乳房胀痛','失眠'],
    moodOptions: ['开心','难过','焦虑','易怒','平静','精力充沛','情绪化','疲惫','专注','压力大']
  },

  confirmDelete() {
    wx.showModal({
      title: "确认删除",
      content: "确定删除这条记录吗？",
      confirmText: "删除",
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
      wx.showToast({ title: "已删除", icon: "success" })
      this.refreshHome()
      wx.navigateBack()
    } catch (e) {
      wx.showToast({ title: "删除失败", icon: "none" })
    }
  },


  refreshHome() {
    const pages = getCurrentPages()
    for (const pg of pages) {
      if (pg.route === 'pages/index/index' && typeof pg.loadData === 'function') {
        pg.loadData()
      }
    }
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ isEditing: true, recordId: options.id })
      this.loadRecord(options.id)
    } else {
      const today = new Date()
      const y = today.getFullYear()
      const m = String(today.getMonth() + 1).padStart(2, '0')
      const d = String(today.getDate()).padStart(2, '0')
      this.setData({ 'form.startDate': y + '-' + m + '-' + d })
    }
  },

  async loadRecord(id) {
    try {
      const r = await api.get('/records/' + id)
      this.setData({
        'form.startDate': r.startDate || '',
        'form.endDate': r.endDate || '',
        'form.flow': r.flow || '',
        'form.painLevel': r.painLevel || '',
        'form.symptoms': r.symptoms || [],
        'form.moodTags': r.moodTags || [],
        'form.clots': r.clots || false,
        'form.notes': r.notes || ''
      })
      this.updateActive()
    } catch (e) {
      wx.showToast({ title: '加载记录失败', icon: 'none' })
      wx.navigateBack()
    }
  },

  onStartDateChange(e) {
    const start = e.detail.value
    const patch = { 'form.startDate': start }
    if (this.data.form.endDate && this.data.form.endDate < start) {
      patch['form.endDate'] = start
    }
    this.setData(patch)
  },
  onEndDateChange(e) {
    const end = e.detail.value
    const patch = { 'form.endDate': end }
    if (this.data.form.startDate && end < this.data.form.startDate) {
      patch['form.endDate'] = this.data.form.startDate
    }
    this.setData(patch)
  },

  selectFlow(e) {
    this.setData({ 'form.flow': e.currentTarget.dataset.value })
  },
  selectPain(e) {
    this.setData({ 'form.painLevel': e.currentTarget.dataset.value })
  },

  toggleSymptom(e) {
    const v = e.currentTarget.dataset.value
    const arr = [...this.data.form.symptoms]
    const i = arr.indexOf(v)
    i > -1 ? arr.splice(i, 1) : arr.push(v)
    this.setData({ 'form.symptoms': arr }); this.updateActive()
  },

  toggleMood(e) {
    const v = e.currentTarget.dataset.value
    const arr = [...this.data.form.moodTags]
    const i = arr.indexOf(v)
    i > -1 ? arr.splice(i, 1) : arr.push(v)
    this.setData({ 'form.moodTags': arr }); this.updateActive()
  },

  onClotsChange(e) {
    this.setData({ 'form.clots': e.detail.value })
  },
  onNotesInput(e) {
    this.setData({ 'form.notes': e.detail.value })
  },

  updateActive() {
    const s = this.data.form.symptoms || [];
    const m = this.data.form.moodTags || [];
    const symptomActive = this.data.symptomOptions.map(x => s.indexOf(x) > -1);
    const moodActive = this.data.moodOptions.map(x => m.indexOf(x) > -1);
    this.setData({ symptomActive, moodActive })
  },

  async saveRecord() {
    if (!this.data.form.startDate) {
      wx.showToast({ title: '请选择开始日期', icon: 'none' })
      return
    }
    if (this.data.form.endDate && this.data.form.endDate < this.data.form.startDate) {
      wx.showToast({ title: '结束日期不能早于开始日期', icon: 'none' })
      return
    }
    if (this.data.saving) return
    this.setData({ saving: true })
    try {
      const payload = { ...this.data.form }
      if (!payload.endDate) payload.endDate = payload.startDate
      if (this.data.isEditing) {
        await api.put('/records/' + this.data.recordId, payload)
        wx.showToast({ title: '记录已更新', icon: 'success' })
      } else {
        await api.post('/records', payload)
        wx.showToast({ title: '记录已保存', icon: 'success' })
      }
      this.refreshHome()
      wx.navigateBack()
    } catch (e) {
      wx.showToast({ title: '保存失败，请重试', icon: 'none' })
    }
    this.setData({ saving: false })
  }
})
