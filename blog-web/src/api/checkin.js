import request from '@/utils/request'

export function checkin() {
  return request.post('/api/portal/checkin')
}

export function getCheckinStatus() {
  return request.get('/api/portal/checkin/status')
}

export function getCheckinCalendar(month) {
  return request.get('/api/portal/checkin/calendar', { params: { month } })
}
