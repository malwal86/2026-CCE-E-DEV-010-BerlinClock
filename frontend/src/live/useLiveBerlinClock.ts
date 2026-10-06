import { useEffect, useState } from 'react'
import { fetchBerlinClock } from './api'
import type { BerlinClockReading } from './types'

const pad = (n: number) => String(n).padStart(2, '0')

/** The browser's local time as HH:mm:ss: the server's time zone never matters. */
const localTime = (now: Date) => `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`

export interface LiveBerlinClock {
  /** The latest time the backend read, if any yet. */
  reading?: BerlinClockReading
  /** The last request failed: `reading` is out of date until a later one succeeds. */
  unavailable: boolean
}

/**
 * The browser's current time as a Berlin Clock, asked of the backend on every second. An answer (or a failure) that
 * arrives after a newer one is ignored, so the clock never moves backwards. While the backend fails, it keeps asking.
 */
export function useLiveBerlinClock(): LiveBerlinClock {
  const [reading, setReading] = useState<BerlinClockReading>()
  const [unavailable, setUnavailable] = useState(false)

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
      const isLatest = () => !stopped && request >= shown
      try {
        const answer = await fetchBerlinClock(localTime(now))
        if (!isLatest()) return
        shown = request
        setReading(answer)
        setUnavailable(false)
      } catch {
        if (!isLatest()) return
        // Keep the last state shown; the next tick tries again.
        shown = request
        setUnavailable(true)
      }
    }

    tick()
    return () => {
      stopped = true
      clearTimeout(timer)
    }
  }, [])

  return { reading, unavailable }
}
