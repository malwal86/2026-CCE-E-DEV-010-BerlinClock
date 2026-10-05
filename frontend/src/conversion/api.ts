import type { Conversion } from './types'

// Resolved against the page origin so the same code works behind the Vite proxy, nginx and in tests.
const url = (path: string) => new URL(path, window.location.origin)

export async function convertTime(time: string): Promise<Conversion> {
  const response = await fetch(url('/api/conversions'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ time }),
  })
  return response.json()
}

export async function fetchRecentConversions(): Promise<Conversion[]> {
  const response = await fetch(url('/api/conversions'))
  return response.json()
}
