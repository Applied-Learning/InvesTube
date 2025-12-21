// Utility functions to format dates in Korea Standard Time (KST / Asia/Seoul)
function parseToDate(input) {
  if (!input && input !== 0) return null
  // number (ms since epoch)
  if (typeof input === 'number') return new Date(input)
  // numeric string
  if (/^\d+$/.test(String(input))) return new Date(Number(input))

  const s = String(input).trim()

  // If no timezone info (no Z, no +/- offset), assume the backend sent KST time
  // Add +09:00 to explicitly mark it as Korea time (backend uses serverTimezone=Asia/Seoul)
  // This handles: "YYYY-MM-DD HH:mm:ss" or "YYYY-MM-DDTHH:mm:ss" or "YYYY-MM-DDTHH:mm:ss.SSS"
  if (/^\d{4}-\d{2}-\d{2}[T ]\d{2}:\d{2}:\d{2}(\.\d+)?$/.test(s)) {
    // Replace space with T if needed, and add +09:00 to treat as KST
    return new Date(s.replace(' ', 'T') + '+09:00')
  }

  // If the string contains timezone info (Z or +hh:mm/-hh:mm), parse as is
  if (/[zZ]|[+-]\d{2}:?\d{2}$/.test(s)) {
    return new Date(s)
  }

  // Fallback: let Date try to parse (may be treated as local)
  return new Date(s)
}

export function formatKST(dateInput) {
  const date = parseToDate(dateInput)
  if (!date || isNaN(date.getTime())) return ''
  return new Intl.DateTimeFormat('ko-KR', {
    timeZone: 'Asia/Seoul',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

export function formatKSTDate(dateInput) {
  const date = parseToDate(dateInput)
  if (!date || isNaN(date.getTime())) return ''
  return new Intl.DateTimeFormat('ko-KR', {
    timeZone: 'Asia/Seoul',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }).format(date)
}

export function timeAgoKST(dateInput) {
  const date = parseToDate(dateInput)
  if (!date || isNaN(date.getTime())) return ''
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '방금 전'
  if (minutes < 60) return `${minutes}분 전`
  if (hours < 24) return `${hours}시간 전`
  if (days < 7) return `${days}일 전`

  return formatKSTDate(date)
}

export default { formatKST, formatKSTDate, timeAgoKST }
