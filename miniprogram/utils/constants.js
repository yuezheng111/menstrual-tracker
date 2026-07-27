/** App Constants */
const API_BASE = 'http://localhost:8080/api'

const FLOW_OPTIONS = [
  { value: 'LIGHT', label: '少', emoji: '💌' },
  { value: 'MEDIUM', label: '中', emoji: '🌻' },
  { value: 'HEAVY', label: '多', emoji: '🌺' }
]

const PAIN_OPTIONS = [
  { value: 'NONE', label: '无', emoji: '😊' },
  { value: 'MILD', label: '轻微', emoji: '😕' },
  { value: 'MODERATE', label: '中等', emoji: '😟' },
  { value: 'SEVERE', label: '严重', emoji: '😢' }
]

const SYMPTOM_OPTIONS = [
  '头痛', '笮劳', '腹胀', '腹痛', '背痛',
  '恶心', '头晕', '长痘', '乳房胀痛', '失眠'
]

const MOOD_OPTIONS = [
  '开心', '难过', '焦虑', '易怒', '平静',
  '精力充沛', '情绪化', '笮惫', '专注', '压力大'
]

module.exports = {
  API_BASE, FLOW_OPTIONS, PAIN_OPTIONS, SYMPTOM_OPTIONS, MOOD_OPTIONS
}
