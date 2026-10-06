import { useEffect, useState } from 'react'
import { fetchBerlinClock } from './api'
import type { BerlinClockReading } from './types'

const pad = (n: number) => String(n).padStart(2, '0')

/** The browser's local time as HH:mm:ss: the server's time zone never matters. */
const localTime = (now: Date) => `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`

/**
 * The browser's current time as a Berlin Clock, asked of the backend on every second. An answer that arrives
 * after a newer one is ignored, so the clock never moves backwards.
 */
export function useLiveBerlinClock(): BerlinClockReading | undefined {
  const [reading, setReading] = useState<BerlinClockReading>()

  useEffect(() => {
    let timer: ReturnType<typeof setTimeout>
    let stopped = false
    let asked = 0
    let shown = 0

    async function tick() {
      const now = new Date()
      // Wait for the next whole second, so the clock changes when the second does.
      timer = setTimeout(tick, 1000 - now.getMilliseconds())
      const request = ++asked
      try {
        const answer = await fetchBerlinClock(localTime(now))
        if (stopped || request < shown) return
        shown = request
        setReading(answer)
      } catch {
        // Keep the last state shown; the next tick tries again.
      }
    }

    tick()
    return () => {
      stopped = true
      clearTimeout(timer)
    }
  }, [])

  return reading
}
