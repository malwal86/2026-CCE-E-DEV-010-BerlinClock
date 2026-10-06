import { LampRow } from './LampRow'
import type { BerlinClockRows } from './types'
import './BerlinClock.css'

interface Props {
  clock: BerlinClockRows
  /** The time the clock shows, as HH:mm:ss; it names the clock for assistive technology. */
  time: string
  size?: 'large' | 'small'
}

export function BerlinClock({ clock, time, size = 'large' }: Props) {
  return (
    <div className={`berlin-clock berlin-clock--${size}`} role="img" aria-label={`Berlin Clock showing ${time}`}>
      <span className="berlin-clock__seconds" data-testid="seconds-lamp" data-lamp={clock.seconds} />
      <LampRow lamps={clock.fiveHours} testId="five-hours-row" />
      <LampRow lamps={clock.singleHours} testId="single-hours-row" />
      <LampRow lamps={clock.fiveMinutes} testId="five-minutes-row" />
      <LampRow lamps={clock.singleMinutes} testId="single-minutes-row" />
    </div>
  )
}
