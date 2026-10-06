import type { BerlinClockReading } from './types'

/** Reads a time as a Berlin Clock; read-only, so the live clock never fills the history. */
export async function fetchBerlinClock(time: string): Promise<BerlinClockReading> {
  const url = new URL('/api/berlin-clock', window.location.origin)
  url.searchParams.set('time', time)
  const response = await fetch(url)
  if (!response.ok) {
    throw new Error(`GET ${url.pathname} answered ${response.status}`)
  }
  return response.json()
}
