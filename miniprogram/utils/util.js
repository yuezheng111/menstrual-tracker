function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return y + '-' + m + '-' + d
}

function getToday() { return formatDate(new Date()) }

function daysBetween(d1, d2) {
  const a = new Date(d1); const b = new Date(d2)
  return Math.round((b - a) / (1000 * 60 * 60 * 24))
}

function addDays(dateStr, days) {
  const d = new Date(dateStr)
  d.setDate(d.getDate() + days)
  return formatDate(d)
}

function getMonthDays(year, month) {
  return new Date(year, month, 0).getDate()
}

function getFirstDayOfMonth(year, month) {
  return new Date(year, month - 1, 1).getDay()
}

module.exports = { formatDate, getToday, daysBetween, addDays, getMonthDays, getFirstDayOfMonth }
